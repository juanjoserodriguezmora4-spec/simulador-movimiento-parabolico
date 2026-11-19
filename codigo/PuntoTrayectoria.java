/**
 * Estado del proyectil en un instante: posicion y velocidad.
 * Es la unidad basica que forma la trayectoria completa.
 */
public class PuntoTrayectoria {

    private final double tiempo;  // s
    private final double x;       // m
    private final double y;       // m
    private final double vx;      // m/s
    private final double vy;      // m/s

    public PuntoTrayectoria(double tiempo, double x, double y, double vx, double vy) {
        this.tiempo = tiempo;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
    }

    public double getTiempo() {
        return tiempo;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    /** Rapidez instantanea: |v| = raiz(vx^2 + vy^2) */
    public double getRapidez() {
        return Math.sqrt(vx * vx + vy * vy);
    }

    /** Angulo de la velocidad respecto a la horizontal, en grados. */
    public double getAnguloVelocidad() {
        return Math.toDegrees(Math.atan2(vy, vx));
    }
}
