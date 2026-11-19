/**
 * Cuerpos celestes disponibles en el simulador.
 *
 * Cada uno aporta dos datos que cambian el resultado del lanzamiento:
 *  - la aceleracion de la gravedad g (m/s^2);
 *  - la densidad de la atmosfera rho (kg/m^3), que solo influye cuando se
 *    activa la resistencia del aire.
 *
 * Valores tomados de las fichas planetarias de la NASA.
 */
public enum Planeta {

    TIERRA("Tierra", 9.81, 1.225),
    LUNA("Luna", 1.62, 0.0),
    MARTE("Marte", 3.72, 0.020),
    JUPITER("Jupiter", 24.79, 0.16),
    PERSONALIZADO("Personalizado", 9.81, 1.225);

    private final String nombre;
    private final double gravedad;
    private final double densidadAire;

    Planeta(String nombre, double gravedad, double densidadAire) {
        this.nombre = nombre;
        this.gravedad = gravedad;
        this.densidadAire = densidadAire;
    }

    public String getNombre() {
        return nombre;
    }

    public double getGravedad() {
        return gravedad;
    }

    public double getDensidadAire() {
        return densidadAire;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
