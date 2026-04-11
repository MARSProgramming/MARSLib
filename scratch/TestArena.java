import edu.wpi.first.math.geometry.Pose3d;
import java.util.List;
import org.ironmaple.simulation.seasonspecific.rebuilt2026.Arena2026Rebuilt;

public class TestArena {
  public static void main(String[] args) {
    Arena2026Rebuilt arena = new Arena2026Rebuilt(true);
    arena.setEfficiencyMode(false);
    arena.resetFieldForAuto();
    List<Pose3d> poses = arena.getGamePiecesPosesByType("Fuel");
    System.out.println("TOTAL BALLS: " + poses.size());
  }
}
