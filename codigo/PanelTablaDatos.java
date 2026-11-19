import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Pestana "Tabla de datos".
 *
 * Muestra la posicion y la velocidad del proyectil en instantes exactos
 * (por ejemplo 0, 1, 2, 3 s) segun el intervalo que elija el usuario.
 * Ademas agrega siempre dos filas especiales, aunque no caigan en un
 * tiempo redondo: el instante de altura maxima y el instante del impacto.
 */
public class PanelTablaDatos extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Intervalos de tiempo que puede elegir el usuario (s). */
    private static final double[] INTERVALOS = {0.1, 0.25, 0.5, 1.0, 2.0, 5.0};
    private static final int INTERVALO_POR_DEFECTO = 3;   // 1 s

    /** Tipo de cada fila, para pintarla de un color distinto. */
    private enum TipoFila { NORMAL, LANZAMIENTO, ALTURA_MAXIMA, IMPACTO }

    private static final Color FILA_PAR       = Color.WHITE;
    private static final Color FILA_IMPAR     = new Color(0xF5F8FB);
    private static final Color FONDO_MAXIMA   = new Color(0xEFEAF8);
    private static final Color FONDO_IMPACTO  = new Color(0xFBEDEB);
    private static final Color FONDO_SELECCION = new Color(0xD6E4F0);

    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JComboBox<String> comboIntervalo;
    private final JLabel resumen = new JLabel(" ");
    private final List<TipoFila> tipos = new ArrayList<TipoFila>();

    private transient ResultadoSimulacion resultado;

    public PanelTablaDatos() {
        super(new BorderLayout(0, 10));
        setBackground(Paleta.PANEL);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        String[] opciones = new String[INTERVALOS.length];
        for (int i = 0; i < INTERVALOS.length; i++) {
            opciones[i] = "cada " + Formato.numCorto(INTERVALOS[i], 2) + " s";
        }
        comboIntervalo = new JComboBox<String>(opciones);
        comboIntervalo.setSelectedIndex(INTERVALO_POR_DEFECTO);
        comboIntervalo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        comboIntervalo.addActionListener(e -> llenarTabla());

        modelo = new DefaultTableModel(new Object[] {
            "t (s)", "x (m)", "y (m)", "vx (m/s)", "vy (m/s)", "|v| (m/s)", "Momento"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        configurarTabla();

        add(crearBarraSuperior(), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setColumnHeaderView(tabla.getTableHeader());
        scroll.setBorder(BorderFactory.createLineBorder(Paleta.LINEA));
        scroll.getViewport().setBackground(Paleta.PANEL);
        add(scroll, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------------
    // Construccion
    // ------------------------------------------------------------------

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izquierda.setOpaque(false);
        JLabel etiqueta = new JLabel("Mostrar una fila");
        etiqueta.setFont(new Font("SansSerif", Font.PLAIN, 12));
        etiqueta.setForeground(Paleta.TINTA);
        izquierda.add(etiqueta);
        izquierda.add(comboIntervalo);

        resumen.setFont(new Font("SansSerif", Font.PLAIN, 12));
        resumen.setForeground(Paleta.TINTA_BAJA);
        izquierda.add(resumen);

        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        leyenda.setOpaque(false);
        leyenda.add(muestraLeyenda(FONDO_MAXIMA, Paleta.ACENTO, "Altura maxima"));
        leyenda.add(muestraLeyenda(FONDO_IMPACTO, Paleta.TRAYECTORIA, "Impacto con el suelo"));

        barra.add(izquierda, BorderLayout.WEST);
        barra.add(leyenda, BorderLayout.EAST);
        return barra;
    }

    private JLabel muestraLeyenda(Color fondo, Color borde, String texto) {
        JLabel muestra = new JLabel(texto);
        muestra.setOpaque(true);
        muestra.setBackground(fondo);
        muestra.setForeground(borde);
        muestra.setFont(new Font("SansSerif", Font.BOLD, 11));
        muestra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, borde),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        return muestra;
    }

    private void configurarTabla() {
        tabla.setRowHeight(28);
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(Paleta.LINEA_FINA);
        tabla.setIntercellSpacing(new Dimension(0, 1));
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(Paleta.PANEL);
        tabla.setSelectionBackground(FONDO_SELECCION);
        tabla.setSelectionForeground(Paleta.TINTA);
        tabla.getTableHeader().setReorderingAllowed(false);

        // Encabezado oscuro con texto blanco. Se pinta con un renderer propio
        // porque la apariencia de Windows ignora el color de fondo del encabezado.
        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setPreferredSize(new Dimension(100, 34));
        encabezado.setDefaultRenderer(new RendererEncabezado());

        RendererCelda renderer = new RendererCelda();
        for (int i = 0; i < modelo.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        tabla.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(130);
    }

    // ------------------------------------------------------------------
    // Datos
    // ------------------------------------------------------------------

    /** Recibe el lanzamiento que se acaba de simular y vuelve a llenar la tabla. */
    public void setResultado(ResultadoSimulacion nuevo) {
        this.resultado = nuevo;
        llenarTabla();
    }

    public void limpiar() {
        resultado = null;
        modelo.setRowCount(0);
        tipos.clear();
        resumen.setText(" ");
    }

    private double intervaloElegido() {
        int indice = comboIntervalo.getSelectedIndex();
        return INTERVALOS[(indice < 0) ? INTERVALO_POR_DEFECTO : indice];
    }

    /**
     * Arma la lista de instantes: 0, h, 2h, 3h... hasta antes del impacto,
     * mas el instante de altura maxima y el del impacto en su lugar.
     */
    private void llenarTabla() {
        modelo.setRowCount(0);
        tipos.clear();
        if (resultado == null) {
            resumen.setText(" ");
            return;
        }

        double h = intervaloElegido();
        double tVuelo = resultado.getTiempoVuelo();
        double tMaxima = resultado.getTiempoAlturaMaxima();
        boolean hayMaxima = tMaxima > 1e-9;
        boolean maximaAgregada = !hayMaxima;
        final double tolerancia = 1e-6;

        for (int k = 0; ; k++) {
            // Se redondea para evitar valores como 0,30000000000000004
            double t = Math.round(k * h * 1e6) / 1e6;
            if (t >= tVuelo - tolerancia) {
                break;
            }
            // Si la altura maxima cae entre la fila anterior y esta, va antes.
            if (!maximaAgregada && tMaxima < t - tolerancia) {
                agregarFila(tMaxima, TipoFila.ALTURA_MAXIMA);
                maximaAgregada = true;
            }
            if (!maximaAgregada && Math.abs(tMaxima - t) <= tolerancia) {
                agregarFila(t, TipoFila.ALTURA_MAXIMA);
                maximaAgregada = true;
                continue;
            }
            agregarFila(t, (k == 0) ? TipoFila.LANZAMIENTO : TipoFila.NORMAL);
        }
        if (!maximaAgregada && tMaxima < tVuelo - tolerancia) {
            agregarFila(tMaxima, TipoFila.ALTURA_MAXIMA);
        }
        agregarFila(tVuelo, TipoFila.IMPACTO);

        resumen.setText("   " + modelo.getRowCount() + " filas  |  tiempo de vuelo "
                + Formato.numCorto(tVuelo, 3) + " s");
    }

    private void agregarFila(double t, TipoFila tipo) {
        PuntoTrayectoria p = MotorFisico.estadoEnTiempo(resultado, t);
        String momento;
        switch (tipo) {
            case LANZAMIENTO:   momento = "Lanzamiento";   break;
            case ALTURA_MAXIMA: momento = "Altura maxima"; break;
            case IMPACTO:       momento = "Impacto";       break;
            default:            momento = "";              break;
        }
        modelo.addRow(new Object[] {
            Formato.numCorto(p.getTiempo(), 3),
            Formato.num(p.getX(), 3),
            Formato.num(p.getY(), 3),
            Formato.num(p.getVx(), 3),
            Formato.num(p.getVy(), 3),
            Formato.num(p.getRapidez(), 3),
            momento
        });
        tipos.add(tipo);
    }

    // ------------------------------------------------------------------
    // Pintado de celdas
    // ------------------------------------------------------------------

    /** Encabezado: fondo azul oscuro, texto blanco centrado. */
    private static class RendererEncabezado extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        RendererEncabezado() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(t, valor, false, false, fila, columna);
            setBackground(Paleta.TINTA);
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            // Mismo alineado y margen que las celdas, para que titulo y numeros coincidan.
            boolean esTexto = (columna == 6);
            setHorizontalAlignment(esTexto ? SwingConstants.LEFT : SwingConstants.RIGHT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0x2C3B55)),
                    esTexto ? BorderFactory.createEmptyBorder(0, 18, 0, 10)
                            : BorderFactory.createEmptyBorder(0, 10, 0, 16)));
            return this;
        }
    }

    /** Celdas: filas alternadas, numeros alineados a la derecha, filas especiales con color. */
    private class RendererCelda extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        private final Font fuenteNumeros = new Font("Monospaced", Font.PLAIN, 13);
        private final Font fuenteNumerosNegrita = new Font("Monospaced", Font.BOLD, 13);
        private final Font fuenteTexto = new Font("SansSerif", Font.BOLD, 12);

        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(t, valor, seleccionada, false, fila, columna);

            TipoFila tipo = (fila < tipos.size()) ? tipos.get(fila) : TipoFila.NORMAL;
            boolean esTexto = (columna == 6);

            // Fondo
            Color fondo = (fila % 2 == 0) ? FILA_PAR : FILA_IMPAR;
            Color tinta = Paleta.TINTA;
            if (tipo == TipoFila.ALTURA_MAXIMA) {
                fondo = FONDO_MAXIMA;
                tinta = Paleta.ACENTO;
            } else if (tipo == TipoFila.IMPACTO) {
                fondo = FONDO_IMPACTO;
                tinta = Paleta.TRAYECTORIA;
            }
            setBackground(seleccionada ? FONDO_SELECCION : fondo);
            setForeground(tinta);

            // Letra y alineacion
            boolean destacada = (tipo != TipoFila.NORMAL);
            if (esTexto) {
                setFont(fuenteTexto);
                setHorizontalAlignment(SwingConstants.LEFT);
            } else {
                setFont((destacada || columna == 0) ? fuenteNumerosNegrita : fuenteNumeros);
                setHorizontalAlignment(SwingConstants.RIGHT);
            }

            // Barra de color a la izquierda en las filas especiales
            if (columna == 0 && (tipo == TipoFila.ALTURA_MAXIMA || tipo == TipoFila.IMPACTO)) {
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, tinta),
                        BorderFactory.createEmptyBorder(0, 10, 0, 16)));
            } else {
                setBorder(esTexto
                        ? BorderFactory.createEmptyBorder(0, 18, 0, 10)
                        : BorderFactory.createEmptyBorder(0, 10, 0, 16));
            }
            return this;
        }
    }
}
