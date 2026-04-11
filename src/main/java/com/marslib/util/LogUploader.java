package com.marslib.util;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class to automatically upload WPILog files to a GitHub repository's Releases. It avoids
 * disrupting robot loop timings by running on a separate daemon thread.
 */
public final class LogUploader {
  private static final Logger LOGGER = Logger.getLogger(LogUploader.class.getName());

  // Configuration
  private static final String GITHUB_OWNER = "MARSProgramming";
  private static final String GITHUB_REPO = "MARSLib-Logs";
  private static final int COOLDOWN_SECONDS = 10;

  // Paths
  private static final Path PAT_FILE = Path.of("/home/lvuser/deploy/github_pat.txt");
  // Only valid on roboRIO - local machines might use standard current dir path
  private static final Path PAT_FILE_LOCAL = Path.of("src/main/deploy/github_pat.txt");
  private static final Path UPLOAD_MANIFEST = Path.of(".uploaded_logs");

  private static final Path[] LOG_DIRS = {
    Path.of("/U/logs"), // USB drive on roboRIO
    Path.of("/home/lvuser/logs"), // Internal roboRIO fallback
    Path.of("logs") // Simulation default
  };

  private static final ExecutorService executor =
      Executors.newSingleThreadExecutor(
          r -> {
            Thread t = new Thread(r, "LogUploader");
            t.setDaemon(true);
            return t;
          });

  private static final HttpClient HTTP_CLIENT =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  private static final AtomicBoolean isUploading = new AtomicBoolean(false);
  private static long lastTriggerTime = 0;
  private static final int MAX_MANIFEST_ENTRIES = 50;

  private LogUploader() {}

  /**
   * Attempts to scan for and upload log files if the cooldown has elapsed and conditions are met.
   * This should be called from disabledPeriodic().
   */
  public static void tryUploadLogsAsync() {
    // 1. Enforce conditions
    if (DriverStation.isFMSAttached()) {
      return; // Never upload during competition
    }

    // Safety check - we only upload when the system explicitly requests it, which usually
    // occurs in disabled. But we also ensure we don't spam requests.
    long now = (long) edu.wpi.first.wpilibj.Timer.getFPGATimestamp();
    if (now - lastTriggerTime < COOLDOWN_SECONDS) {
      return;
    }
    lastTriggerTime = now;

    if (!isUploading.compareAndSet(false, true)) {
      return; // Already uploading
    }

    executor.execute(
        () -> {
          try {
            doUploadLogs();
          } catch (Exception e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
              LOGGER.log(
                  Level.WARNING, "Error during log upload async process: " + e.getMessage(), e);
            }
            DriverStation.reportWarning("Log Upload Failed: " + e.getMessage(), false);
          } finally {
            isUploading.set(false);
          }
        });
  }

  private static void doUploadLogs() throws Exception {
    String pat = readPatToken();
    if (pat == null) {
      return; // Silent fail if PAT is not configured
    }

    Set<String> uploadedSet = readUploadedManifest();

    for (Path dir : LOG_DIRS) {
      if (!Files.exists(dir) || !Files.isDirectory(dir)) {
        continue;
      }

      try (Stream<Path> files = Files.list(dir)) {
        var wpilogs =
            files
                .filter(p -> p.getFileName().toString().endsWith(".wpilog"))
                .collect(Collectors.toList());

        for (Path logFile : wpilogs) {
          String originalName = logFile.getFileName().toString();

          if (!uploadedSet.contains(originalName)) {
            // Apply naming convention for uploads
            String prefix = RobotBase.isReal() ? "REAL_" : "SIM_";
            String uploadName = prefix + originalName;

            boolean success = uploadAssetToGitHub(pat, logFile, uploadName);
            if (success) {
              uploadedSet.add(originalName);
              saveUploadedManifest(uploadedSet);
              DriverStation.reportWarning("Successfully uploaded log: " + uploadName, false);

              try {
                // Task 2: Physically delete log file from RoboRIO flash after successful cloud
                // backup
                // CRITICAL SAFETY: Only erase logs originating from the internal volume. If the
                // driver
                // has a massive USB thumb drive plugged in, mathematically it won't fill up, and
                // they
                // might want the physical copies off to hand over to a stat-tracking laptop.
                if (!logFile.toString().replace('\\', '/').startsWith("/U")) {
                  Files.deleteIfExists(logFile);
                  DriverStation.reportWarning(
                      "Purged uploaded log from RIO storage: " + originalName, false);
                }
              } catch (IOException e) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                  LOGGER.log(
                      Level.WARNING,
                      "Failed to purge log file after successful upload: " + logFile,
                      e);
                }
              }
            }
          }
        }
      } catch (IOException e) {
        if (LOGGER.isLoggable(Level.WARNING)) {
          LOGGER.log(Level.WARNING, "Failed to list directory: " + dir, e);
        }
      }
    }
  }

  private static String readPatToken() {
    try {
      if (Files.exists(PAT_FILE)) {
        return Files.readString(PAT_FILE).trim();
      } else if (Files.exists(PAT_FILE_LOCAL)) {
        return Files.readString(PAT_FILE_LOCAL).trim();
      }
    } catch (IOException e) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.log(Level.WARNING, "Failed to read PAT file", e);
      }
    }
    return null;
  }

  private static Set<String> readUploadedManifest() {
    Set<String> set = new HashSet<>();
    try {
      if (Files.exists(UPLOAD_MANIFEST)) {
        set.addAll(Files.readAllLines(UPLOAD_MANIFEST));
      }
    } catch (IOException e) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.log(Level.WARNING, "Failed to read upload manifest", e);
      }
    }
    return set;
  }

  private static void saveUploadedManifest(Set<String> uploadedSet) {
    try {
      String content =
          uploadedSet.stream()
              .limit(MAX_MANIFEST_ENTRIES)
              .collect(Collectors.joining(System.lineSeparator()));
      if (!content.isEmpty()) {
        content += System.lineSeparator();
      }
      Files.writeString(
          UPLOAD_MANIFEST,
          content,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.log(Level.WARNING, "Failed to write to upload manifest", e);
      }
    }
  }

  private static boolean uploadAssetToGitHub(String pat, Path logFile, String assetName)
      throws Exception {
    String tag = "logs-" + LocalDate.now(ZoneId.systemDefault()).toString();

    // 1. Get or Create Release
    Integer releaseId = getReleaseByTag(pat, tag);
    if (releaseId == null) {
      releaseId = createRelease(pat, tag, "Log dump for " + tag);
      if (releaseId == null) {
        throw new IOException("Failed to find or create GitHub release.");
      }
    }

    // 2. Upload the file
    // https://uploads.github.com/repos/OWNER/REPO/releases/RELEASE_ID/assets?name=assetName
    String uploadUrl =
        String.format(
            "https://uploads.github.com/repos/%s/%s/releases/%s/assets?name=%s",
            GITHUB_OWNER, GITHUB_REPO, releaseId, assetName);

    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(uploadUrl))
              .timeout(Duration.ofMinutes(15))
              .header("Authorization", "Bearer " + pat)
              .header("Content-Type", "application/octet-stream")
              .header("Accept", "application/vnd.github.v3+json")
              .POST(HttpRequest.BodyPublishers.ofFile(logFile)) // Uses standard file publishing
              .build();

      HttpResponse<String> response =
          HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 201) {
        return true;
      } else {
        // If 422, it could be validation failed (e.g. file already exists).
        if (response.statusCode() == 422 && response.body().contains("already_exists")) {
          return true; // We'll count this as a success to avoid repeatedly failing on it.
        }
        if (LOGGER.isLoggable(Level.WARNING)) {
          LOGGER.log(
              Level.WARNING,
              "Asset upload failed: " + response.statusCode() + " " + response.body());
        }
        return false;
      }
    } catch (IOException e) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.log(Level.WARNING, "Asset upload encountered IO exception", e);
      }
      return false;
    }
  }

  private static Integer getReleaseByTag(String pat, String tag) throws Exception {
    String url =
        String.format(
            "https://api.github.com/repos/%s/%s/releases/tags/%s", GITHUB_OWNER, GITHUB_REPO, tag);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .header("Authorization", "Bearer " + pat)
            .header("Accept", "application/vnd.github.v3+json")
            .GET()
            .build();

    HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() == 200) {
      return extractIdFromJson(response.body());
    }
    return null;
  }

  private static Integer createRelease(String pat, String tag, String title) throws Exception {
    String url =
        String.format("https://api.github.com/repos/%s/%s/releases", GITHUB_OWNER, GITHUB_REPO);

    String jsonBody =
        String.format(
            "{\"tag_name\":\"%s\",\"name\":\"%s\",\"body\":\"Robot telemetry logs.\",\"draft\":false,\"prerelease\":false}",
            tag, title);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .header("Authorization", "Bearer " + pat)
            .header("Accept", "application/vnd.github.v3+json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

    HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() == 201) {
      return extractIdFromJson(response.body());
    }
    if (LOGGER.isLoggable(Level.WARNING)) {
      LOGGER.log(
          Level.WARNING,
          "Failed to create release: " + response.statusCode() + " - " + response.body());
    }
    return null;
  }

  private static Integer extractIdFromJson(String json) {
    // Simple manual parsing to avoid bringing in Gson directly here,
    // though WPILib contains Jackson/Gson. Looking for: "id": 1234567,
    String searchKey = "\"id\":";
    int index = json.indexOf(searchKey);
    if (index != -1) {
      int start = index + searchKey.length();
      int end = json.indexOf(",", start);
      if (end == -1) {
        // ID could be the last element or followed by a '}'
        int endBrace = json.indexOf("}", start);
        if (endBrace != -1) {
          end = endBrace;
        } else {
          return null; // parse failure
        }
      }
      try {
        return Integer.parseInt(json.substring(start, end).trim());
      } catch (NumberFormatException e) {
        if (LOGGER.isLoggable(Level.WARNING)) {
          LOGGER.log(Level.WARNING, "Could not parse ID from release json", e);
        }
      }
    }
    return null;
  }

  /**
   * Generates a structural Command to physically copy any FRC internal match logs directly onto the
   * tethered USB stick, subsequently purging the RIO to guarantee high storage overhead. Useful for
   * offline competition events where Wi-Fi upload drops entirely.
   */
  public static Command getUsbOffloadCommand() {
    return Commands.runOnce(
            () -> {
              Path internalDir = Path.of("/home/lvuser/logs");
              Path usbDir = Path.of("/U/logs");

              if (!Files.exists(usbDir) || !Files.isDirectory(usbDir)) {
                DriverStation.reportError(
                    "USB Offload Failed: No valid flash drive found on /U/logs", false);
                return;
              }

              if (Files.exists(internalDir)) {
                try (Stream<Path> files = Files.list(internalDir)) {
                  files
                      .filter(p -> p.getFileName().toString().endsWith(".wpilog"))
                      .forEach(
                          p -> {
                            try {
                              Path destination = usbDir.resolve(p.getFileName());
                              Files.copy(
                                  p,
                                  destination,
                                  java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                              Files.deleteIfExists(p);
                              DriverStation.reportWarning(
                                  "Successfully offloaded " + p.getFileName() + " to USB.", false);
                            } catch (IOException ex) {
                              DriverStation.reportError(
                                  "Failed to offload log: " + p.getFileName(), false);
                            }
                          });
                } catch (IOException e) {
                  DriverStation.reportError("Failed to scan internal logs for USB offload.", false);
                }
              } else {
                DriverStation.reportWarning("No internal RoboRIO logs found to offload.", false);
              }
            })
        .ignoringDisable(true); // Extremely important to let Drive Team run this while Disabled
  }
}
