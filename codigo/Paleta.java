import java.awt.Color;

/** Colores usados en toda la interfaz, reunidos en un solo lugar. */
public final class Paleta {

    public static final Color FONDO         = new Color(0xE7ECF1);
    public static final Color PANEL         = new Color(0xFFFFFF);
    public static final Color TINTA         = new Color(0x16233A);
    public static final Color TINTA_BAJA    = new Color(0x5B6B80);
    public static final Color LINEA         = new Color(0xC7D3E0);
    public static final Color LINEA_FINA    = new Color(0xE1E8EF);

    public static final Color TRAYECTORIA   = new Color(0xC0392B);  // modelo ideal
    public static final Color CON_AIRE      = new Color(0x17607B);  // modelo con resistencia
    public static final Color ACENTO        = new Color(0x6B4FA8);
    public static final Color ALERTA        = new Color(0xA8471C);
    public static final Color EXITO         = new Color(0x1E7A4B);

    public static final Color CIELO_ALTO    = new Color(0xEDF3F8);
    public static final Color CIELO_BAJO    = new Color(0xDCE7F0);
    public static final Color SUELO         = new Color(0x7A8C6B);

    /** Colores para los lanzamientos guardados en la comparacion. */
    public static final Color[] COMPARACION = {
        new Color(0x1E7A4B),
        new Color(0xB07A12),
        new Color(0x6B4FA8),
        new Color(0x0E7490),
        new Color(0xA8471C),
        new Color(0x8A2E5D)
    };

    private Paleta() {
        // Clase de constantes: no se instancia.
    }

    public static Color colorComparacion(int indice) {
        return COMPARACION[Math.abs(indice) % COMPARACION.length];
    }
}
