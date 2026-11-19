import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utilidades para mostrar y leer numeros.
 *
 * Se usa el formato colombiano: coma como separador decimal.
 * La lectura acepta tanto coma como punto para que el usuario no se equivoque.
 */
public final class Formato {

    private static final DecimalFormatSymbols SIMBOLOS = crearSimbolos();

    private Formato() {
        // Clase de utilidades: no se instancia.
    }

    private static DecimalFormatSymbols crearSimbolos() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.forLanguageTag("es-CO"));
        simbolos.setDecimalSeparator(',');
        simbolos.setGroupingSeparator('.');
        return simbolos;
    }

    /** Devuelve el numero como texto con la cantidad de decimales indicada. */
    public static String num(double valor, int decimales) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            return "--";
        }
        StringBuilder patron = new StringBuilder("#,##0");
        if (decimales > 0) {
            patron.append('.');
            for (int i = 0; i < decimales; i++) {
                patron.append('0');
            }
        }
        DecimalFormat formato = new DecimalFormat(patron.toString(), SIMBOLOS);
        String salida = formato.format(valor);
        // Evita que aparezca "-0,00" cuando el valor es practicamente cero.
        if (salida.startsWith("-") && Math.abs(valor) < 5e-5) {
            salida = salida.substring(1);
        }
        return salida;
    }

    /**
     * Numero sin ceros sobrantes: 1 -> "1", 2,5 -> "2,5", 3,6039 -> "3,604".
     * Se usa para los tiempos de la tabla, que casi siempre son redondos.
     */
    public static String numCorto(double valor, int maxDecimales) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            return "--";
        }
        StringBuilder patron = new StringBuilder("#,##0");
        if (maxDecimales > 0) {
            patron.append('.');
            for (int i = 0; i < maxDecimales; i++) {
                patron.append('#');
            }
        }
        String salida = new DecimalFormat(patron.toString(), SIMBOLOS).format(valor);
        return salida.equals("-0") ? "0" : salida;
    }

    /** Igual que num(), pero agregando la unidad fisica al final. */
    public static String conUnidad(double valor, int decimales, String unidad) {
        return num(valor, decimales) + " " + unidad;
    }

    /**
     * Convierte el texto escrito por el usuario en un numero.
     * Acepta "12.5" y "12,5". Devuelve null si el texto no es un numero valido.
     */
    public static Double leerNumero(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.trim().replace(" ", "");
        if (limpio.isEmpty()) {
            return null;
        }
        // Si aparecen los dos separadores, el punto se toma como separador de miles.
        if (limpio.indexOf(',') >= 0 && limpio.indexOf('.') >= 0) {
            limpio = limpio.replace(".", "");
        }
        limpio = limpio.replace(',', '.');
        if (!limpio.matches("[+-]?(\\d+(\\.\\d*)?|\\.\\d+)")) {
            return null;
        }
        try {
            double valor = Double.parseDouble(limpio);
            return Double.isFinite(valor) ? Double.valueOf(valor) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
