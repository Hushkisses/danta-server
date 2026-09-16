package kr.danta.paper.combat.live;

/** Paper-independent short-step movement primitive for DEV-115 live units. */
public final class WaypointStepMover {

    public Step step(
            double x,
            double y,
            double z,
            double goalX,
            double goalY,
            double goalZ,
            double stepDistance
    ) {
        if (stepDistance <= 0.0) throw new IllegalArgumentException("stepDistance must be > 0");

        double dx = goalX - x;
        double dy = goalY - y;
        double dz = goalZ - z;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance == 0.0 || distance <= stepDistance) {
            return new Step(goalX, goalY, goalZ);
        }

        double scale = stepDistance / distance;
        return new Step(
                x + dx * scale,
                y + dy * scale,
                z + dz * scale);
    }

    public record Step(double x, double y, double z) {}
}
