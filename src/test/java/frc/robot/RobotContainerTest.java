package frc.robot;

import static org.junit.jupiter.api.Assertions.*;

import com.marslib.testing.MARSTestHarness;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.JoystickSim;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.subsystems.MARSSuperstructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RobotContainerTest {

  private RobotContainer robotContainer;

  @BeforeEach
  public void setUp() {
    MARSTestHarness.reset();
    robotContainer = new RobotContainer(false);
  }

  @Test
  public void testRobotInitializesInSimMode() {
    assertNotNull(robotContainer);
    assertNotNull(robotContainer.getSwerveDrive());
  }

  @Test
  public void testIntakeCommandBinding() {
    // 1. Initial State: Stowed
    MARSSuperstructure ss = getSuperstructure();
    assertEquals(MARSSuperstructure.SuperstructureState.STOWED, ss.getCurrentState());

    // 2. Mock Pilot Controller Left Trigger (Axis 2)
    JoystickSim pilotSim = new JoystickSim(0);
    pilotSim.setRawAxis(2, 1.0);
    pilotSim.notifyNewData();
    DriverStationSim.setAllianceStationId(edu.wpi.first.hal.AllianceStationID.Blue1);
    DriverStationSim.notifyNewData();

    // 3. Run scheduler
    runScheduler(10);

    // 4. Verify state change
    assertEquals(MARSSuperstructure.SuperstructureState.INTAKE_RUNNING, ss.getCurrentState());

    // 5. Verify release
    pilotSim.setRawAxis(2, 0.0);
    pilotSim.notifyNewData();
    runScheduler(10);
    assertEquals(MARSSuperstructure.SuperstructureState.STOWED, ss.getCurrentState());
  }

  @Test
  public void testScoreCommandBinding() {
    MARSSuperstructure ss = getSuperstructure();
    JoystickSim pilotSim = new JoystickSim(0);

    // Right Bumper (Button 6) -> SCORE
    pilotSim.setRawButton(6, true);
    pilotSim.notifyNewData();
    DriverStationSim.notifyNewData();

    runScheduler(10);
    assertEquals(MARSSuperstructure.SuperstructureState.SCORE, ss.getCurrentState());

    pilotSim.setRawButton(6, false);
    pilotSim.notifyNewData();
    runScheduler(10);
    assertEquals(MARSSuperstructure.SuperstructureState.STOWED, ss.getCurrentState());
  }

  @Test
  public void testClimbLineupCommandTrigger() {
    JoystickSim pilotSim = new JoystickSim(0);

    // Button X (Button 3) -> Final Climb Lineup
    pilotSim.setRawButton(3, true);
    pilotSim.notifyNewData();
    DriverStationSim.notifyNewData();

    // Run scheduler and verify it doesn't crash
    // We expect it to trigger the sequence
    runScheduler(5);

    // Check if the drivetrain is currently being used by a command
    assertNotNull(
        CommandScheduler.getInstance().requiring(robotContainer.getSwerveDrive()),
        "Drivetrain should be required by the climb lineup command");
  }

  private void runScheduler(int ticks) {
    for (int i = 0; i < ticks; i++) {
      CommandScheduler.getInstance().run();
    }
  }

  private MARSSuperstructure getSuperstructure() {
    // We might need to use reflection to get the private superstructure field
    // if it's not exposed, or just rely on the side effects.
    // In RobotContainer, it's private.
    try {
      java.lang.reflect.Field field = RobotContainer.class.getDeclaredField("superstructure");
      field.setAccessible(true);
      return (MARSSuperstructure) field.get(robotContainer);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
