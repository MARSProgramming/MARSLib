package com.marslib.vision;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.networktables.StructPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VIOSlamIOROS2Test {

  private NetworkTableInstance inst;
  private StructPublisher<Pose3d> publisher;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();
    inst = NetworkTableInstance.getDefault();
    publisher =
        inst.getTable("TestSlam")
            .getStructTopic("poseTopic", Pose3d.struct)
            .publish(PubSubOption.sendAll(true), PubSubOption.keepDuplicates(true));
  }

  @AfterEach
  public void tearDown() {
    publisher.close();
    MARSTestHarness.cleanup();
  }

  @Test
  public void testUpdatesParsedCorrectly() {
    VIOSlamIOROS2 io = new VIOSlamIOROS2("TestSlam", "poseTopic");

    Pose3d pose1 = new Pose3d(new Translation3d(1.0, 2.0, 3.0), new Rotation3d(0, 0, 0));
    Pose3d pose2 = new Pose3d(new Translation3d(4.0, 5.0, 6.0), new Rotation3d(0, 0, 0));

    // Simulate NT4 timestamps in microseconds
    long timestamp1Us = 1_500_000L; // 1.5s
    long timestamp2Us = 2_000_000L; // 2.0s

    publisher.set(pose1, timestamp1Us);
    inst.flush();
    try {
      Thread.sleep(20);
    } catch (InterruptedException ignored) {
    }

    publisher.set(pose2, timestamp2Us);
    inst.flush();

    // Sleep to allow local NT4 events to propagate
    try {
      Thread.sleep(50);
    } catch (InterruptedException ignored) {
    }

    VIOSlamIO.VIOSlamIOInputs inputs = new VIOSlamIO.VIOSlamIOInputs();
    io.updateInputs(inputs);

    // Verify lengths
    assertEquals(2, inputs.estimatedPoses.length);
    assertEquals(2, inputs.timestamps.length);

    // Verify converted timestamps (microseconds to seconds)
    assertArrayEquals(new double[] {1.5, 2.0}, inputs.timestamps, 0.001);

    // Verify Struct Values mapped
    assertEquals(1.0, inputs.estimatedPoses[0].getX());
    assertEquals(5.0, inputs.estimatedPoses[1].getY());
  }

  @Test
  public void testEmptyQueue() {
    VIOSlamIOROS2 io = new VIOSlamIOROS2("TestSlam", "poseTopic");

    VIOSlamIO.VIOSlamIOInputs inputs = new VIOSlamIO.VIOSlamIOInputs();
    inputs.estimatedPoses = new Pose3d[] {new Pose3d()}; // ensure it overwrites
    inputs.timestamps = new double[] {99.0};

    inst.flush();
    try {
      Thread.sleep(50);
    } catch (InterruptedException ignored) {
    }

    io.updateInputs(inputs);

    assertEquals(0, inputs.estimatedPoses.length);
    assertEquals(0, inputs.timestamps.length);
  }
}
