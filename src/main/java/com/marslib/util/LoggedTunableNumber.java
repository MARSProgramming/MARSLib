package com.marslib.util;

import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.DoubleSubscriber;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInLayouts;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A tunable number that can be modified at runtime via NetworkTables for live PID tuning.
 *
 * <p>When the robot is connected to an FMS (real competition), the value is locked to the default
 * for safety and determinism. In practice/simulation mode, values can be freely adjusted from the
 * SmartDashboard or AdvantageScope.
 *
 * <p>Students: Use {@code hasChanged(int id)} to detect when a value has been modified. Each
 * consumer (identified by a unique int ID) independently tracks whether it has seen the latest
 * value.
 */
public class LoggedTunableNumber implements Sendable {
  private static final String tableKey = "TunableNumbers";
  private static final List<LoggedTunableNumber> registeredTunables = new ArrayList<>();
  private static boolean dashboardBuilt = false;

  private final String key;
  private boolean hasDefault = false;
  private double defaultValue;
  private DoubleSubscriber subscriber;
  private DoublePublisher publisher;

  /** Per-consumer change tracking: maps consumer ID → last seen value */
  private final Map<Integer, Double> lastHasChangedValues = new HashMap<>();

  public LoggedTunableNumber(String dashboardKey) {
    this.key = dashboardKey;
    synchronized (registeredTunables) {
      registeredTunables.add(this);
    }
  }

  public LoggedTunableNumber(String dashboardKey, double defaultValue) {
    this(dashboardKey);
    initDefault(defaultValue);
  }

  public final void initDefault(double defaultValue) {
    if (!hasDefault) {
      hasDefault = true;
      this.defaultValue = defaultValue;
      var topic = NetworkTableInstance.getDefault().getTable(tableKey).getDoubleTopic(key);
      publisher = topic.publish();
      publisher.set(defaultValue);
      subscriber = topic.subscribe(defaultValue);
    }
  }

  public double get() {
    if (!hasDefault) {
      return 0.0;
    }
    if (DriverStation.isFMSAttached()) {
      return defaultValue;
    }
    return subscriber != null ? subscriber.get() : defaultValue;
  }

  /**
   * Returns true if the value has changed since the last time this specific consumer checked.
   *
   * @param id A unique identifier for the consumer (e.g., {@code this.hashCode()} or {@code
   *     motor.getDeviceID()}).
   * @return Whether the current value differs from the last value seen by this consumer.
   */
  public boolean hasChanged(int id) {
    if (DriverStation.isFMSAttached()) {
      return false;
    }
    double currentValue = get();
    Double lastValue = lastHasChangedValues.get(id);
    if (lastValue == null || currentValue != lastValue) {
      lastHasChangedValues.put(id, currentValue);
      return true;
    }
    return false;
  }

  @Override
  public void initSendable(SendableBuilder builder) {
    builder.setSmartDashboardType("Number");
    builder.addDoubleProperty(
        "Value",
        this::get,
        (val) -> {
          if (!DriverStation.isFMSAttached()) {
            if (!hasDefault) {
              initDefault(val);
            } else if (publisher != null) {
              publisher.set(val);
            }
          }
        });
  }

  /**
   * Generates a structural Command that pulls the live value of all registered tunables and dumps
   * them as a formatted JSON document on the RoboRIO deployment storage.
   */
  public static Command getDumpCommand() {
    return Commands.runOnce(
        () -> {
          StringBuilder sb = new StringBuilder("{\n");
          synchronized (registeredTunables) {
            for (int i = 0; i < registeredTunables.size(); i++) {
              LoggedTunableNumber p = registeredTunables.get(i);
              sb.append("  \"").append(p.key).append("\": ").append(p.get());
              if (i < registeredTunables.size() - 1) {
                sb.append(",");
              }
              sb.append("\n");
            }
          }
          sb.append("}\n");

          try {
            Path path = Paths.get("/home/lvuser/tunables_dump.json");
            Files.writeString(path, sb.toString());
            DriverStation.reportWarning("Successfully dumped tunables to " + path, false);
          } catch (Exception e) {
            DriverStation.reportError(
                "Failed to dump tunables: " + e.getMessage(), e.getStackTrace());
          }
        });
  }

  /**
   * Automates the visual construction of the native WPILib Tuning dashboard. Dynamically iterates
   * over every registered tunable globally, parses its subsystem path prefix, and buckets it into
   * beautifully sorted List layouts seamlessly for 0-friction calibration logging.
   */
  public static void buildTuningDashboard() {
    if (dashboardBuilt) {
      return;
    }
    dashboardBuilt = true;
    ShuffleboardTab tuningTab = Shuffleboard.getTab("Tuning");

    // Utilities
    tuningTab.add("Dump Live Tunables", getDumpCommand()).withSize(2, 1).withPosition(0, 0);
    tuningTab
        .add("Emergency USB Offload", com.marslib.util.LogUploader.getUsbOffloadCommand())
        .withSize(2, 1)
        .withPosition(2, 0);

    // Group variables into logically sorted List panels
    Map<String, List<LoggedTunableNumber>> sortedGroups = new TreeMap<>();
    synchronized (registeredTunables) {
      for (LoggedTunableNumber tunable : registeredTunables) {
        String[] paths = tunable.key.split("/");
        // Handle basic variables vs nested components gracefully
        String groupName = paths.length > 1 ? paths[0] : "Global Adjustments";
        sortedGroups.computeIfAbsent(groupName, k -> new ArrayList<>()).add(tunable);
      }
    }

    int currentX = 0;
    int currentY = 1;
    final int columnLimit = 8;

    for (Map.Entry<String, List<LoggedTunableNumber>> entry : sortedGroups.entrySet()) {
      ShuffleboardLayout layout =
          tuningTab
              .getLayout(entry.getKey(), BuiltInLayouts.kList)
              .withSize(2, Math.min(5, entry.getValue().size() + 1))
              .withPosition(currentX, currentY);

      for (LoggedTunableNumber tunable : entry.getValue()) {
        String[] paths = tunable.key.split("/");
        layout.add(paths[paths.length - 1], tunable);
      }

      currentX += 2;
      if (currentX >= columnLimit) {
        currentX = 0;
        currentY += 5; // Step below the previous row blocks
      }
    }
  }

  /** Clears all registered tunables. Only used for unit testing. */
  public static void clear() {
    synchronized (registeredTunables) {
      registeredTunables.clear();
    }
    dashboardBuilt = false;
  }

  /**
   * Releases native NetworkTables handles. Should be called when a tunable is no longer needed,
   * especially in unit tests to prevent resource leaks and native crashes.
   */
  public void close() {
    if (publisher != null) {
      publisher.close();
      publisher = null;
    }
    if (subscriber != null) {
      subscriber.close();
      subscriber = null;
    }
    synchronized (registeredTunables) {
      registeredTunables.remove(this);
    }
  }
}
