import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

/**
 * Guarda la trayectoria calculada en un archivo CSV.
 *
 * Se usa punto y coma como separador y coma decimal, que es el formato
 * que Excel en espanol abre directamente en columnas.
 */
public final class ExportadorCSV {

    private ExportadorCSV() {
        // Clase de utilidades: no se instancia.
    }

    public static void guardar(File archivo, ResultadoSimulacion resultado) throws IOException {
        Writer salida = new OutputStreamWriter(new FileOutputStream(archivo), "UTF-8");
        PrintWriter escritor = new PrintWriter(salida);
        try {
            ParametrosLanzamiento p = resultado.getParametros();

            escritor.println("Simulador de movimiento parabolico");
            escritor.println("Condiciones iniciales");
            escritor.println("Velocidad inicial (m/s);" + Formato.num(p.getVelocidadInicial(), 3));
            escritor.println("Angulo (grados);" + Formato.num(p.getAnguloGrados(), 3));
            escritor.println("Altura inicial (m);" + Formato.num(p.getAlturaInicial(), 3));
            escritor.println("Gravedad (m/s2);" + Formato.num(p.getGravedad(), 3));
            escritor.println("Resistencia del aire;" + (resultado.isConArrastre() ? "si" : "no"));
            escritor.println();

            escritor.println("Resultados");
            escritor.println("v0x (m/s);" + Formato.num(resultado.getV0x(), 3));
            escritor.println("v0y (m/s);" + Formato.num(resultado.getV0y(), 3));
            escritor.println("Tiempo de vuelo (s);" + Formato.num(resultado.getTiempoVuelo(), 3));
            escritor.println("Altura maxima (m);" + Formato.num(resultado.getAlturaMaxima(), 3));
            escritor.println("Alcance horizontal (m);" + Formato.num(resultado.getAlcanceHorizontal(), 3));
            escritor.println("Velocidad final (m/s);" + Formato.num(resultado.getVelocidadFinal(), 3));
            escritor.println();

            escritor.println("t (s);x (m);y (m);vx (m/s);vy (m/s);|v| (m/s)");
            List<PuntoTrayectoria> puntos = resultado.getPuntos();
            for (PuntoTrayectoria punto : puntos) {
                escritor.println(Formato.num(punto.getTiempo(), 4) + ";"
                        + Formato.num(punto.getX(), 4) + ";"
                        + Formato.num(punto.getY(), 4) + ";"
                        + Formato.num(punto.getVx(), 4) + ";"
                        + Formato.num(punto.getVy(), 4) + ";"
                        + Formato.num(punto.getRapidez(), 4));
            }
        } finally {
            escritor.close();
        }
    }
}
