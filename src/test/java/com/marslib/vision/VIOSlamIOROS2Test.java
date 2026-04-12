package com.marslib.vision;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructSubscriber;
import edu.wpi.first.networktables.StructTopic;
import edu.wpi.first.networktables.TimestampedObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class VIOSlamIOROS2Test {

  private MockedStatic<NetworkTableInstance> mockedNt;
  private NetworkTableInstance mockInstance;
  private NetworkTable mockTable;
  private StructTopic<Pose3d> mockTopic;
  private StructSubscriber<Pose3d> mockSubscriber;

  @BeforeEach
  @SuppressWarnings("unchecked")
  public void setUp() {
    mockInstance = mock(NetworkTableInstance.class);
    mockTable = mock(NetworkTable.class);
    mockTopic = (StructTopic<Pose3d>) mock(StructTopic.class);
    mockSubscriber = (StructSubscriber<Pose3d>) mock(StructSubscriber.class);

    when(mockInstance.getTable("TestSlam")).thenReturn(mockTable);
    doReturn(mockTopic).when(mockTable).getStructTopic(eq("poseTopic"), any());
    when(mockTopic.subscribe(any(Pose3d.class))).thenReturn(mockSubscriber);

    mockedNt = mockStatic(NetworkTableInstance.class);
    mockedNt.when(NetworkTableInstance::getDefault).thenReturn(mockInstance);
  }

  @AfterEach
  public void tearDown() {
    if (mockedNt != null) {
      mockedNt.close();
    }
  }

  @Test
  public void testUpdatesParsedCorrectly() {
    VIOSlamIOROS2 io = new VIOSlamIOROS2("TestSlam", "poseTopic");

    // Create mock struct updates from Python Coprocessor
    Pose3d pose1 = new Pose3d(new Translation3d(1.0, 2.0, 3.0), new Rotation3d(0, 0, 0));
    Pose3d pose2 = new Pose3d(new Translation3d(4.0, 5.0, 6.0), new Rotation3d(0, 0, 0));

    // Simulate NT4 timestamps in microseconds
    long timestamp1Us = 1_500_000L; // 1.5s
    long timestamp2Us = 2_000_000L; // 2.0s

    @SuppressWarnings("unchecked")
    TimestampedObject<Pose3d>[] mockQueue =
        new TimestampedObject[] {
          new TimestampedObject<>(timestamp1Us, 1, pose1),
          new TimestampedObject<>(timestamp2Us, 1, pose2)
        };

    when(mockSubscriber.readQueue()).thenReturn(mockQueue);

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

    @SuppressWarnings("unchecked")
    TimestampedObject<Pose3d>[] mockQueue = new TimestampedObject[0];

    when(mockSubscriber.readQueue()).thenReturn(mockQueue);

    VIOSlamIO.VIOSlamIOInputs inputs = new VIOSlamIO.VIOSlamIOInputs();
    inputs.estimatedPoses = new Pose3d[] {new Pose3d()}; // ensure it overwrites
    inputs.timestamps = new double[] {99.0};

    io.updateInputs(inputs);

    assertEquals(0, inputs.estimatedPoses.length);
    assertEquals(0, inputs.timestamps.length);
  }
}
