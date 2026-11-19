import java.util.Collections;
import java.util.List;

/**
 * Resultado completo de un lanzamiento: la trayectoria punto por punto
 * y las magnitudes que pide el proyecto (tiempo de vuelo, altura maxima,
 * alcance, velocidades).
 */
public class ResultadoSimulacion {

    private final String etiqueta;
    private final ParametrosLanzamiento parametros;
    private final List<PuntoTrayectoria> puntos;
    private final boolean conArrastre;

    private final double v0x;
    private final double v0y;
    private final double tiempoVuelo;
    private final double alturaMaxima;
    private final double tiempoAlturaMaxima;
    private final double alcanceHorizontal;
    private final double velocidadFinal;
    private final double anguloFinal;

    public ResultadoSimulacion(String etiqueta, ParametrosLanzamiento parametros,
                               List<PuntoTrayectoria> puntos, boolean conArrastre,
                               double v0x, double v0y, double tiempoVuelo, double alturaMaxima,
                               double tiempoAlturaMaxima, double alcanceHorizontal,
                               double velocidadFinal, double anguloFinal) {
        this.etiqueta = etiqueta;
        this.parametros = parametros;
        this.puntos = puntos;
        this.conArrastre = conArrastre;
        this.v0x = v0x;
        this.v0y = v0y;
        this.tiempoVuelo = tiempoVuelo;
        this.alturaMaxima = alturaMaxima;
        this.tiempoAlturaMaxima = tiempoAlturaMaxima;
        this.alcanceHorizontal = alcanceHorizontal;
        this.velocidadFinal = velocidadFinal;
        this.anguloFinal = anguloFinal;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public ParametrosLanzamiento getParametros() {
        return parametros;
    }

    public List<PuntoTrayectoria> getPuntos() {
        return Collections.unmodifiableList(puntos);
    }

    public boolean isConArrastre() {
        return conArrastre;
    }

    public double getV0x() {
        return v0x;
    }

    public double getV0y() {
        return v0y;
    }

    public double getTiempoVuelo() {
        return tiempoVuelo;
    }

    public double getAlturaMaxima() {
        return alturaMaxima;
    }

    public double getTiempoAlturaMaxima() {
        return tiempoAlturaMaxima;
    }

    public double getAlcanceHorizontal() {
        return alcanceHorizontal;
    }

    public double getVelocidadFinal() {
        return velocidadFinal;
    }

    public double getAnguloFinal() {
        return anguloFinal;
    }

    /**
     * Devuelve el estado del proyectil en un instante cualquiera.
     * Como la trayectoria esta guardada en puntos discretos, se interpola
     * linealmente entre los dos puntos mas cercanos.
     */
    public PuntoTrayectoria enTiempo(double t) {
        if (puntos.isEmpty()) {
            return new PuntoTrayectoria(0, 0, 0, 0, 0);
        }
        if (t <= puntos.get(0).getTiempo()) {
            return puntos.get(0);
        }
        PuntoTrayectoria ultimo = puntos.get(puntos.size() - 1);
        if (t >= ultimo.getTiempo()) {
            return ultimo;
        }

        // Busqueda binaria del intervalo que contiene a t.
        int bajo = 0;
        int alto = puntos.size() - 1;
        while (alto - bajo > 1) {
            int medio = (bajo + alto) / 2;
            if (puntos.get(medio).getTiempo() <= t) {
                bajo = medio;
            } else {
                alto = medio;
            }
        }

        PuntoTrayectoria p1 = puntos.get(bajo);
        PuntoTrayectoria p2 = puntos.get(alto);
        double intervalo = p2.getTiempo() - p1.getTiempo();
        double f = (intervalo <= 0) ? 0 : (t - p1.getTiempo()) / intervalo;

        return new PuntoTrayectoria(
                t,
                p1.getX() + f * (p2.getX() - p1.getX()),
                p1.getY() + f * (p2.getY() - p1.getY()),
                p1.getVx() + f * (p2.getVx() - p1.getVx()),
                p1.getVy() + f * (p2.getVy() - p1.getVy()));
    }

    /** Arreglo con los tiempos de todos los puntos (util para las graficas). */
    public double[] arregloTiempos() {
        double[] datos = new double[puntos.size()];
        for (int i = 0; i < puntos.size(); i++) {
            datos[i] = puntos.get(i).getTiempo();
        }
        return datos;
    }

    /** Arreglo con las posiciones horizontales. */
    public double[] arregloX() {
        double[] datos = new double[puntos.size()];
        for (int i = 0; i < puntos.size(); i++) {
            datos[i] = puntos.get(i).getX();
        }
        return datos;
    }

    /** Arreglo con las alturas. */
    public double[] arregloY() {
        double[] datos = new double[puntos.size()];
        for (int i = 0; i < puntos.size(); i++) {
            datos[i] = puntos.get(i).getY();
        }
        return datos;
    }
}
