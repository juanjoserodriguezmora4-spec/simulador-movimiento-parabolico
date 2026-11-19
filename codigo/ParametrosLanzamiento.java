/**
 * Datos que el usuario ingresa antes de lanzar el proyectil.
 *
 * Todas las magnitudes se guardan en unidades del Sistema Internacional:
 *   velocidadInicial   -> m/s
 *   anguloGrados       -> grados (se convierte a radianes dentro del motor)
 *   alturaInicial      -> m
 *   gravedad           -> m/s^2
 *   masa               -> kg
 *   diametro           -> m
 *   coeficienteArrastre-> adimensional
 *   densidadAire       -> kg/m^3
 */
public class ParametrosLanzamiento {

    private final double velocidadInicial;
    private final double anguloGrados;
    private final double alturaInicial;
    private final double gravedad;

    private final boolean considerarAire;
    private final double masa;
    private final double diametro;
    private final double coeficienteArrastre;
    private final double densidadAire;

    public ParametrosLanzamiento(double velocidadInicial, double anguloGrados, double alturaInicial,
                                 double gravedad, boolean considerarAire, double masa, double diametro,
                                 double coeficienteArrastre, double densidadAire) {
        this.velocidadInicial = velocidadInicial;
        this.anguloGrados = anguloGrados;
        this.alturaInicial = alturaInicial;
        this.gravedad = gravedad;
        this.considerarAire = considerarAire;
        this.masa = masa;
        this.diametro = diametro;
        this.coeficienteArrastre = coeficienteArrastre;
        this.densidadAire = densidadAire;
    }

    public double getVelocidadInicial() {
        return velocidadInicial;
    }

    public double getAnguloGrados() {
        return anguloGrados;
    }

    public double getAnguloRadianes() {
        return Math.toRadians(anguloGrados);
    }

    public double getAlturaInicial() {
        return alturaInicial;
    }

    public double getGravedad() {
        return gravedad;
    }

    public boolean isConsiderarAire() {
        return considerarAire;
    }

    public double getMasa() {
        return masa;
    }

    public double getDiametro() {
        return diametro;
    }

    public double getCoeficienteArrastre() {
        return coeficienteArrastre;
    }

    public double getDensidadAire() {
        return densidadAire;
    }

    /** Area transversal del proyectil, suponiendolo esferico: A = pi * d^2 / 4 */
    public double getAreaTransversal() {
        return Math.PI * diametro * diametro / 4.0;
    }

    /**
     * Constante de arrastre agrupada: k = (rho * Cd * A) / (2 * m)
     * Con ella la aceleracion por rozamiento queda a = -k * |v| * v.
     */
    public double getConstanteArrastre() {
        if (!considerarAire || masa <= 0) {
            return 0.0;
        }
        return (densidadAire * coeficienteArrastre * getAreaTransversal()) / (2.0 * masa);
    }

    /** Texto corto para identificar el lanzamiento en las comparaciones. */
    public String descripcionCorta() {
        return "v0 = " + Formato.num(velocidadInicial, 1) + " m/s | "
                + Formato.num(anguloGrados, 1) + " grados | y0 = "
                + Formato.num(alturaInicial, 1) + " m"
                + (considerarAire ? " | con aire" : "");
    }
}
