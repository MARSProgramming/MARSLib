package gg.questnav;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QuestNavTest {

  private QuestNav questNav;
  private NetworkTable table;

  @BeforeEach
  public void setUp() {
    questNav = new QuestNav();
    table = NetworkTableInstance.getDefault().getTable("questnav");
  }

  @Test
  public void testConnectionStatus() {
    table.getIntegerTopic("connected").publish().set(0);
    NetworkTableInstance.getDefault().flush();
    assertFalse(questNav.isConnected());

    table.getIntegerTopic("connected").publish().set(1);
    NetworkTableInstance.getDefault().flush();
    assertTrue(questNav.isConnected());
  }

  @Test
  public void testPoseParsing() {
    double[] pose = new double[] {1.0, 2.0, 3.0, 0.1, 0.2, 0.3};
    table.getDoubleArrayTopic("pose").publish().set(pose);
    NetworkTableInstance.getDefault().flush();

    var result = questNav.getPose3d();
    assertEquals(1.0, result.getX(), 0.001);
    assertEquals(2.0, result.getY(), 0.001);
    assertEquals(3.0, result.getZ(), 0.001);
    assertEquals(0.1, result.getRotation().getX(), 0.001);
  }

  @Test
  public void testLatency() {
    table.getDoubleTopic("latency").publish().set(50.0); // 50ms
    NetworkTableInstance.getDefault().flush();

    assertEquals(0.05, questNav.getLatencySeconds(), 0.001);
  }
}
