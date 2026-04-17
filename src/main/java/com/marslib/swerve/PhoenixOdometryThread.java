/**
 * MARSLib - FRC Team 2614 "MARS" Software Framework (c) 2024-2026 Mountaineer Area RoboticS (MARS)
 *
 * <p>Developed by MARS 2614 - Mountaineer Area RoboticS. Use of this source code is governed by an
 * MIT-style license that can be found in the LICENSE file.
 */
package com.marslib.swerve;

import com.ctre.phoenix6.BaseStatusSignal;
import com.marslib.util.CANUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * High-frequency CAN bus polling thread for CTRE Phoenix 6 odometry signals.
 *
 * <p>This singleton thread runs at a configurable odometry Hz (typically 250Hz), continuously
 * sampling drive/turn encoder positions and gyro yaw from TalonFX and Pigeon2 devices. Samples are
 * buffered in thread-safe {@link BlockingQueue}s and drained by {@link
 * com.marslib.swerve.SwerveDrive#periodic()} each robot loop iteration.
 *
 * <p><b>Thread Safety:</b> All signal registration and data access is guarded by a {@link
 * ReentrantLock}. The queues use a fixed capacity of 50 to bound memory usage.
 *
 * <p><b>Zero-Allocation Contract:</b> All drain methods ({@link #getSyncData}, {@link
 * #getGyroYawData}) write into pre-allocated fixed-capacity arrays and return a valid sample count.
 * No heap allocations occur on the hot path.
 */
public class PhoenixOdometryThread extends Thread {
  private static volatile PhoenixOdometryThread instance = null;

  /** Maximum number of odometry samples buffered between drains (250Hz / 50Hz = 5 typical). */
  public static final int MAX_SAMPLES = 50;

  /**
   * Returns the singleton instance, creating and starting the thread on first access.
   *
   * @return The global {@link PhoenixOdometryThread} instance.
   */
  public static synchronized PhoenixOdometryThread getInstance() {
    if (instance == null) {
      instance = new PhoenixOdometryThread();
      instance.start();
    }
    return instance;
  }

  /** Stops the current instance and clears the singleton reference. Only used for unit testing. */
  @SuppressWarnings("PMD.NullAssignment")
  public static synchronized void resetInstance() {
    if (instance != null) {
      instance.interrupt();
      instance = null;
    }
  }

  /**
   * Container for a batch of synchronized odometry samples from a single module.
   *
   * <p>Arrays are pre-allocated at fixed capacity ({@link #MAX_SAMPLES}). Use {@link #validCount}
   * to determine how many entries are populated. Do not read beyond {@code validCount - 1}.
   */
  public static class SyncData {
    /** Accumulated drive encoder positions (motor rotations) since the last drain. */
    public final double[] drivePositions = new double[MAX_SAMPLES];

    /** Accumulated turn encoder positions (motor rotations) since the last drain. */
    public final double[] turnPositions = new double[MAX_SAMPLES];

    /** FPGA timestamps corresponding to each position sample. */
    public final double[] timestamps = new double[MAX_SAMPLES];

    /** Number of valid samples in this batch. Only indices {@code [0, validCount)} are valid. */
    public int validCount = 0;
  }

  /**
   * Container for pre-allocated gyro yaw data. Use {@link #validCount} to determine how many
   * entries are populated.
   */
  public static class GyroYawData {
    /** High-frequency yaw position samples (rotations). */
    public final double[] yawPositions = new double[MAX_SAMPLES];

    /** Number of valid samples. Only indices {@code [0, validCount)} are valid. */
    public int validCount = 0;
  }

  private final List<BaseStatusSignal> signals = new ArrayList<>();
  private BaseStatusSignal[] cachedSignalsArray = new BaseStatusSignal[0];
  private final Lock signalsLock = new ReentrantLock();
  private volatile double threadOdometryHz = 250.0;

  // Mapping module index to its data queue
  private final List<BlockingQueue<Double>> drivePositionQueues = new ArrayList<>();
  private final List<BlockingQueue<Double>> turnPositionQueues = new ArrayList<>();
  private final List<BlockingQueue<Double>> timestampQueues = new ArrayList<>();

  // Pre-allocated SyncData per module (max 4 modules)
  private final SyncData[] syncDataCache = new SyncData[4];

  private final BlockingQueue<Double> gyroYawQueue = new ArrayBlockingQueue<>(MAX_SAMPLES);
  private final GyroYawData gyroYawDataCache = new GyroYawData();
  private int gyroSignalIndex = -1;

  public PhoenixOdometryThread() {
    setName("PhoenixOdometryThread");
    setDaemon(true);
    for (int i = 0; i < syncDataCache.length; i++) {
      syncDataCache[i] = new SyncData();
    }
  }

  /**
   * Registers a swerve module's drive and turn position signals for high-frequency sampling.
   *
   * @param drivePosition The drive motor's position {@link BaseStatusSignal}.
   * @param turnPosition The turn motor's position {@link BaseStatusSignal}.
   * @param odometryHz The required update frequency for these signals.
   * @return The module ID used to retrieve synchronized data via {@link #getSyncData(int)}.
   */
  public int registerModule(
      BaseStatusSignal drivePosition, BaseStatusSignal turnPosition, double odometryHz) {
    signalsLock.lock();
    try {
      this.threadOdometryHz = odometryHz;
      int id = drivePositionQueues.size();
      drivePositionQueues.add(new ArrayBlockingQueue<>(MAX_SAMPLES));
      turnPositionQueues.add(new ArrayBlockingQueue<>(MAX_SAMPLES));
      timestampQueues.add(new ArrayBlockingQueue<>(MAX_SAMPLES));

      signals.add(drivePosition);
      signals.add(turnPosition);

      // Configure frequencies
      CANUtil.setUpdateFrequencyWithRetry(threadOdometryHz, drivePosition, turnPosition);

      cachedSignalsArray = signals.toArray(new BaseStatusSignal[0]);
      return id;
    } finally {
      signalsLock.unlock();
    }
  }

  /**
   * Drains all buffered odometry samples for a specific module since the last call.
   *
   * <p><b>Zero-allocation:</b> Returns a pre-allocated {@link SyncData} with fixed-capacity arrays.
   * Check {@link SyncData#validCount} for the number of populated entries. The returned object is
   * reused — do not store references across ticks.
   *
   * @param moduleId The module ID returned by {@link #registerModule}.
   * @return A pre-allocated {@link SyncData} containing synchronized drive/turn positions and
   *     timestamps.
   */
  public SyncData getSyncData(int moduleId) {
    signalsLock.lock();
    try {
      BlockingQueue<Double> dQueue = drivePositionQueues.get(moduleId);
      BlockingQueue<Double> tQueue = turnPositionQueues.get(moduleId);
      BlockingQueue<Double> tsQueue = timestampQueues.get(moduleId);

      SyncData data = syncDataCache[moduleId];
      int size = Math.min(dQueue.size(), MAX_SAMPLES);
      data.validCount = size;

      for (int i = 0; i < size; i++) {
        data.drivePositions[i] = dQueue.poll();
        data.turnPositions[i] = tQueue.poll();
        data.timestamps[i] = tsQueue.poll();
      }

      return data;
    } finally {
      signalsLock.unlock();
    }
  }

  /**
   * Registers the gyro yaw signal for high-frequency sampling alongside module signals.
   *
   * @param yawPos The Pigeon2 yaw position {@link BaseStatusSignal}.
   * @param odometryHz The required update frequency.
   */
  public void registerGyro(BaseStatusSignal yawPos, double odometryHz) {
    signalsLock.lock();
    try {
      this.threadOdometryHz = odometryHz;
      CANUtil.setUpdateFrequencyWithRetry(threadOdometryHz, yawPos);
      signals.add(yawPos);
      gyroSignalIndex = signals.size() - 1;
      cachedSignalsArray = signals.toArray(new BaseStatusSignal[0]);
    } finally {
      signalsLock.unlock();
    }
  }

  /**
   * Drains all buffered high-frequency gyro yaw samples since the last call.
   *
   * <p><b>Zero-allocation:</b> Returns a pre-allocated {@link GyroYawData} with a fixed-capacity
   * array. Check {@link GyroYawData#validCount} for the number of populated entries. The returned
   * object is reused — do not store references across ticks.
   *
   * @return Pre-allocated container with yaw positions (rotations) accumulated since the last
   *     drain.
   */
  public GyroYawData getGyroYawData() {
    signalsLock.lock();
    try {
      int size = Math.min(gyroYawQueue.size(), MAX_SAMPLES);
      gyroYawDataCache.validCount = size;
      for (int i = 0; i < size; i++) {
        gyroYawDataCache.yawPositions[i] = gyroYawQueue.poll();
      }
      return gyroYawDataCache;
    } finally {
      signalsLock.unlock();
    }
  }

  @Override
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      BaseStatusSignal[] currentSignals;
      signalsLock.lock();
      try {
        currentSignals = cachedSignalsArray;
      } finally {
        signalsLock.unlock();
      }

      if (currentSignals.length == 0) {
        try {
          java.util.concurrent.TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
          org.littletonrobotics.junction.Logger.recordOutput(
              "PhoenixOdometryThread/Error", e.toString());
          Thread.currentThread().interrupt();
          break;
        }
        continue;
      }

      // Wait for all signals to update, timeout after 2x the expected period
      BaseStatusSignal.waitForAll(2.0 / threadOdometryHz, currentSignals);

      // Prevent 100% CPU lock in simulation (Sim CTRE waitForAll returns instantly natively)
      if (edu.wpi.first.wpilibj.RobotBase.isSimulation()) {
        try {
          java.util.concurrent.TimeUnit.MILLISECONDS.sleep((long) (1000.0 / threadOdometryHz));
        } catch (InterruptedException e) {
          org.littletonrobotics.junction.Logger.recordOutput(
              "PhoenixOdometryThread/Error", e.toString());
          Thread.currentThread().interrupt();
          break;
        }
      }

      signalsLock.lock();
      try {
        double time = currentSignals[0].getTimestamp().getTime();

        // For each module (2 signals per module)
        for (int i = 0; i < drivePositionQueues.size(); i++) {
          // Drive signals are always added sequentially 0,1 then 2,3 etc.
          double rawDrivePos = currentSignals[i * 2].getValueAsDouble();
          double rawTurnPos = currentSignals[i * 2 + 1].getValueAsDouble();
          // NaN firewall — CAN bus glitches can return NaN from getValueAsDouble()
          double drivePos = Double.isFinite(rawDrivePos) ? rawDrivePos : 0.0;
          double turnPos = Double.isFinite(rawTurnPos) ? rawTurnPos : 0.0;

          if (drivePositionQueues.get(i).remainingCapacity() > 0) {
            drivePositionQueues.get(i).offer(drivePos);
            turnPositionQueues.get(i).offer(turnPos);
            timestampQueues.get(i).offer(time);
          }
        }

        // Process gyro if registered
        if (gyroSignalIndex != -1 && gyroSignalIndex < currentSignals.length) {
          double rawYaw = currentSignals[gyroSignalIndex].getValueAsDouble();
          double yawPos = Double.isFinite(rawYaw) ? rawYaw : 0.0;
          if (gyroYawQueue.remainingCapacity() > 0) {
            gyroYawQueue.offer(yawPos);
          }
        }
      } finally {
        signalsLock.unlock();
      }
    }
  }
}
