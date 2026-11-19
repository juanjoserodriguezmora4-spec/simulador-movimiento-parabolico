import java.util.ArrayList;
import java.util.List;

/**
 * MOTOR FISICO DEL SIMULADOR
 * ==========================
 * Aqui se implementan todas las ecuaciones. Ningun resultado se escribe a mano:
 * el programa recibe los datos iniciales y calcula todo a partir de ellos.
 *
 * ---------------------------------------------------------------------------
 * MODELO 1: MOVIMIENTO PARABOLICO IDEAL (sin resistencia del aire)
 * ---------------------------------------------------------------------------
 * Se descompone el movimiento en dos ejes independientes:
 *
 *  (Ec. 1)  v0x = v0 * cos(theta)              componente horizontal de v0
 *  (Ec. 2)  v0y = v0 * sen(theta)              componente vertical de v0
 *
 *  Eje x -> movimiento rectilineo uniforme (a = 0):
 *  (Ec. 3)  x(t)  = v0x * t
 *  (Ec. 4)  vx(t) = v0x                        la velocidad horizontal no cambia
 *
 *  Eje y -> movimiento uniformemente acelerado (a = -g):
 *  (Ec. 5)  y(t)  = y0 + v0y * t - (1/2) * g * t^2
 *  (Ec. 6)  vy(t) = v0y - g * t
 *
 *  Magnitudes derivadas:
 *  (Ec. 7)  Tiempo de subida:   t_sub = v0y / g
 *  (Ec. 8)  Altura maxima:      y_max = y0 + v0y^2 / (2*g)
 *  (Ec. 9)  Tiempo de vuelo (resolviendo y(t) = 0 con la formula cuadratica):
 *           t_vuelo = ( v0y + raiz( v0y^2 + 2*g*y0 ) ) / g
 *  (Ec. 10) Alcance horizontal: R = v0x * t_vuelo
 *  (Ec. 11) Velocidad final:    |v_f| = raiz( vx^2 + vy^2 )
 *           que tambien se puede comprobar con conservacion de energia:
 *           |v_f| = raiz( v0^2 + 2*g*y0 )
 *
 * ---------------------------------------------------------------------------
 * MODELO 2: CON RESISTENCIA DEL AIRE (extension)
 * ---------------------------------------------------------------------------
 * Se usa el arrastre cuadratico, valido para proyectiles de tamano normal:
 *
 *  (Ec. 12) F_d = (1/2) * rho * Cd * A * |v|^2, dirigida en contra de v
 *
 * Dividiendo entre la masa y agrupando k = (rho * Cd * A) / (2*m):
 *
 *  (Ec. 13) dvx/dt = -k * |v| * vx
 *  (Ec. 14) dvy/dt = -g - k * |v| * vy
 *
 * Este sistema no tiene solucion analitica sencilla, asi que se integra
 * numericamente con el metodo de Runge-Kutta de cuarto orden (RK4).
 *
 * SUPUESTOS DEL MODELO
 *  - El proyectil se trata como una particula (no rota, no se deforma).
 *  - g es constante durante todo el vuelo.
 *  - El suelo es plano y horizontal, en y = 0.
 *  - En el modelo ideal no hay aire; en el modelo 2 no hay viento ni efecto Magnus.
 */
public final class MotorFisico {

    /** Paso de integracion para el modelo con aire (s). */
    private static final double PASO_INTEGRACION = 0.0005;

    /** Cada cuantos segundos se guarda un punto de la trayectoria. */
    private static final double INTERVALO_MUESTREO = 0.005;

    /** Cantidad de puntos con los que se dibuja la trayectoria ideal. */
    private static final int MUESTRAS_IDEAL = 600;

    private MotorFisico() {
        // Clase de utilidades: no se instancia.
    }

    // ------------------------------------------------------------------
    // Descomposicion de la velocidad inicial
    // ------------------------------------------------------------------

    /** (Ec. 1) v0x = v0 * cos(theta) */
    public static double componenteHorizontal(double v0, double anguloRadianes) {
        return v0 * Math.cos(anguloRadianes);
    }

    /** (Ec. 2) v0y = v0 * sen(theta) */
    public static double componenteVertical(double v0, double anguloRadianes) {
        return v0 * Math.sin(anguloRadianes);
    }

    // ------------------------------------------------------------------
    // MODELO 1: solucion analitica
    // ------------------------------------------------------------------

    public static ResultadoSimulacion simularIdeal(ParametrosLanzamiento p, String etiqueta) {
        double g = p.getGravedad();
        double y0 = p.getAlturaInicial();
        double v0x = componenteHorizontal(p.getVelocidadInicial(), p.getAnguloRadianes());
        double v0y = componenteVertical(p.getVelocidadInicial(), p.getAnguloRadianes());

        // (Ec. 9) Tiempo de vuelo: raiz positiva de y0 + v0y*t - g*t^2/2 = 0
        double discriminante = v0y * v0y + 2.0 * g * y0;
        double tiempoVuelo = 0.0;
        if (discriminante > 0) {
            tiempoVuelo = (v0y + Math.sqrt(discriminante)) / g;
        }
        if (tiempoVuelo < 0) {
            tiempoVuelo = 0.0;
        }

        // (Ec. 7) y (Ec. 8) Altura maxima. Si el lanzamiento es hacia abajo,
        // el punto mas alto es el de partida.
        double tiempoSubida = v0y / g;
        double alturaMaxima;
        double tiempoAlturaMaxima;
        if (v0y > 0) {
            alturaMaxima = y0 + (v0y * v0y) / (2.0 * g);
            tiempoAlturaMaxima = tiempoSubida;
        } else {
            alturaMaxima = y0;
            tiempoAlturaMaxima = 0.0;
        }

        // (Ec. 10) Alcance horizontal
        double alcance = v0x * tiempoVuelo;

        // (Ec. 6) y (Ec. 11) Velocidad al tocar el suelo
        double vyFinal = v0y - g * tiempoVuelo;
        double velocidadFinal = Math.sqrt(v0x * v0x + vyFinal * vyFinal);
        double anguloFinal = Math.toDegrees(Math.atan2(vyFinal, v0x));

        // Muestreo de la trayectoria usando las ecuaciones 3, 4, 5 y 6.
        List<PuntoTrayectoria> puntos = new ArrayList<PuntoTrayectoria>();
        for (int i = 0; i <= MUESTRAS_IDEAL; i++) {
            double t = tiempoVuelo * i / (double) MUESTRAS_IDEAL;
            double x = v0x * t;                              // (Ec. 3)
            double y = y0 + v0y * t - 0.5 * g * t * t;       // (Ec. 5)
            if (y < 0) {
                y = 0;
            }
            puntos.add(new PuntoTrayectoria(t, x, y, v0x, v0y - g * t));  // (Ec. 4) y (Ec. 6)
        }

        return new ResultadoSimulacion(etiqueta, p, puntos, false, v0x, v0y, tiempoVuelo,
                alturaMaxima, tiempoAlturaMaxima, alcance, velocidadFinal, anguloFinal);
    }

    /**
     * Estado del proyectil en un instante t cualquiera.
     *
     * En el modelo ideal se evaluan directamente las ecuaciones 3, 4, 5 y 6,
     * asi que el valor es exacto. En el modelo con aire no hay formula cerrada
     * y se interpola entre los puntos que calculo Runge-Kutta.
     */
    public static PuntoTrayectoria estadoEnTiempo(ResultadoSimulacion r, double t) {
        if (r.isConArrastre()) {
            return r.enTiempo(t);
        }
        ParametrosLanzamiento p = r.getParametros();
        double g = p.getGravedad();
        double v0x = r.getV0x();
        double v0y = r.getV0y();

        double x = v0x * t;                                          // (Ec. 3)
        double y = p.getAlturaInicial() + v0y * t - 0.5 * g * t * t; // (Ec. 5)
        if (y < 0 && y > -1e-9) {
            y = 0;   // evita mostrar -0,000 por redondeo en el impacto
        }
        double vy = v0y - g * t;                                     // (Ec. 6)
        return new PuntoTrayectoria(t, x, y, v0x, vy);               // (Ec. 4)
    }

    // ------------------------------------------------------------------
    // MODELO 2: integracion numerica con resistencia del aire
    // ------------------------------------------------------------------

    public static ResultadoSimulacion simularConAire(ParametrosLanzamiento p, String etiqueta) {
        double g = p.getGravedad();
        double k = p.getConstanteArrastre();
        double v0x = componenteHorizontal(p.getVelocidadInicial(), p.getAnguloRadianes());
        double v0y = componenteVertical(p.getVelocidadInicial(), p.getAnguloRadianes());

        // Vector de estado: [x, y, vx, vy]
        double[] estado = new double[] {0.0, p.getAlturaInicial(), v0x, v0y};

        List<PuntoTrayectoria> puntos = new ArrayList<PuntoTrayectoria>();
        puntos.add(new PuntoTrayectoria(0, estado[0], estado[1], estado[2], estado[3]));

        double t = 0.0;
        double proximaMuestra = INTERVALO_MUESTREO;
        double alturaMaxima = estado[1];
        double tiempoAlturaMaxima = 0.0;
        double tiempoLimite = 600.0;   // corte de seguridad para no dejar un ciclo infinito

        double[] anterior = estado.clone();
        double tiempoAnterior = 0.0;

        while (t < tiempoLimite) {
            anterior = estado.clone();
            tiempoAnterior = t;

            estado = pasoRungeKutta4(estado, PASO_INTEGRACION, g, k);
            t += PASO_INTEGRACION;

            if (estado[1] > alturaMaxima) {
                alturaMaxima = estado[1];
                tiempoAlturaMaxima = t;
            }

            // El proyectil toco el suelo entre el paso anterior y este:
            // se interpola para encontrar el instante exacto en que y = 0.
            if (estado[1] < 0 && anterior[1] >= 0) {
                double f = anterior[1] / (anterior[1] - estado[1]);
                double tImpacto = tiempoAnterior + f * PASO_INTEGRACION;
                double xImpacto = anterior[0] + f * (estado[0] - anterior[0]);
                double vxImpacto = anterior[2] + f * (estado[2] - anterior[2]);
                double vyImpacto = anterior[3] + f * (estado[3] - anterior[3]);
                puntos.add(new PuntoTrayectoria(tImpacto, xImpacto, 0.0, vxImpacto, vyImpacto));

                double velocidadFinal = Math.sqrt(vxImpacto * vxImpacto + vyImpacto * vyImpacto);
                double anguloFinal = Math.toDegrees(Math.atan2(vyImpacto, vxImpacto));

                return new ResultadoSimulacion(etiqueta, p, puntos, true, v0x, v0y, tImpacto,
                        alturaMaxima, tiempoAlturaMaxima, xImpacto, velocidadFinal, anguloFinal);
            }

            if (t >= proximaMuestra) {
                puntos.add(new PuntoTrayectoria(t, estado[0], estado[1], estado[2], estado[3]));
                proximaMuestra += INTERVALO_MUESTREO;
            }
        }

        // Caso extremo: nunca cayo dentro del tiempo limite.
        double velocidadFinal = Math.sqrt(estado[2] * estado[2] + estado[3] * estado[3]);
        double anguloFinal = Math.toDegrees(Math.atan2(estado[3], estado[2]));
        return new ResultadoSimulacion(etiqueta, p, puntos, true, v0x, v0y, t,
                alturaMaxima, tiempoAlturaMaxima, estado[0], velocidadFinal, anguloFinal);
    }

    /**
     * Derivadas del sistema: dado [x, y, vx, vy] devuelve [vx, vy, ax, ay].
     * Implementa las ecuaciones 13 y 14.
     */
    private static double[] derivadas(double[] s, double g, double k) {
        double vx = s[2];
        double vy = s[3];
        double rapidez = Math.sqrt(vx * vx + vy * vy);
        double ax = -k * rapidez * vx;          // (Ec. 13)
        double ay = -g - k * rapidez * vy;      // (Ec. 14)
        return new double[] {vx, vy, ax, ay};
    }

    /** Un paso del metodo de Runge-Kutta de cuarto orden. */
    private static double[] pasoRungeKutta4(double[] s, double h, double g, double k) {
        double[] k1 = derivadas(s, g, k);
        double[] k2 = derivadas(sumar(s, k1, h / 2.0), g, k);
        double[] k3 = derivadas(sumar(s, k2, h / 2.0), g, k);
        double[] k4 = derivadas(sumar(s, k3, h), g, k);

        double[] resultado = new double[4];
        for (int i = 0; i < 4; i++) {
            resultado[i] = s[i] + (h / 6.0) * (k1[i] + 2 * k2[i] + 2 * k3[i] + k4[i]);
        }
        return resultado;
    }

    /** Devuelve s + factor * derivada, componente a componente. */
    private static double[] sumar(double[] s, double[] derivada, double factor) {
        double[] resultado = new double[4];
        for (int i = 0; i < 4; i++) {
            resultado[i] = s[i] + factor * derivada[i];
        }
        return resultado;
    }
}
