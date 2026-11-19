import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana principal del simulador.
 *
 * Organiza la interfaz en tres zonas, como pide el enunciado:
 *  - panel de entrada (izquierda),
 *  - zona de simulacion y graficas (centro, en pestanas),
 *  - panel de resultados (abajo a la izquierda).
 */
public class VentanaSimulador extends JFrame {

    private static final long serialVersionUID = 1L;

    // ---- Panel de entrada ----
    private final JComboBox<Planeta> comboPlaneta = new JComboBox<Planeta>(Planeta.values());
    private final JTextField campoV0 = new JTextField("25");
    private final JTextField campoAngulo = new JTextField("45");
    private final JTextField campoAltura = new JTextField("0");
    private final JTextField campoGravedad = new JTextField("9,81");
    private final JSlider sliderAngulo = new JSlider(-90, 90, 45);

    private final JCheckBox chkAire = new JCheckBox("Considerar resistencia del aire");
    private final JTextField campoMasa = new JTextField("0,145");
    private final JTextField campoDiametro = new JTextField("0,074");
    private final JTextField campoCd = new JTextField("0,47");
    private final JTextField campoDensidad = new JTextField("1,225");

    private final JCheckBox chkBlanco = new JCheckBox("Colocar un blanco a cierta distancia");
    private final JTextField campoBlanco = new JTextField("60");

    // ---- Botones ----
    private final JButton btnSimular = new JButton("Simular");
    private final JButton btnPausa = new JButton("Pausar");
    private final JButton btnReiniciar = new JButton("Reiniciar");
    private final JButton btnGuardar = new JButton("Guardar para comparar");
    private final JButton btnBorrar = new JButton("Borrar comparaciones");
    private final JButton btnCsv = new JButton("Exportar CSV");
    private final JSlider sliderVelocidad = new JSlider(25, 200, 100);

    // ---- Salidas ----
    private final DefaultTableModel modeloResultados;
    private final PanelTablaDatos panelTabla = new PanelTablaDatos();
    private final PanelEscenario panelEscenario = new PanelEscenario();
    private final PanelGrafica graficaYX = new PanelGrafica("distancia x (m)", "altura y (m)");
    private final PanelGrafica graficaXT = new PanelGrafica("tiempo t (s)", "distancia x (m)");
    private final PanelGrafica graficaYT = new PanelGrafica("tiempo t (s)", "altura y (m)");
    private final JLabel barraEstado = new JLabel(" Ingresa las condiciones iniciales y presiona Simular.");

    // ---- Estado de la simulacion ----
    private final Timer temporizador;
    private ResultadoSimulacion resultadoIdeal;
    private ResultadoSimulacion resultadoAire;
    private ResultadoSimulacion resultadoMostrado;
    private final List<ResultadoSimulacion> comparaciones = new ArrayList<ResultadoSimulacion>();
    private double tiempoActual = 0.0;
    private boolean sincronizandoAngulo = false;

    public VentanaSimulador() {
        setTitle("Simulador de movimiento parabolico - Fisica, Ingenieria de Software");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));

        modeloResultados = new DefaultTableModel(
                new Object[] {"Magnitud", "Modelo ideal", "Con aire", "Diferencia"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        temporizador = new Timer(20, e -> avanzarAnimacion());

        construirInterfaz();
        conectarEventos();

        // Se ejecuta una primera simulacion para que la ventana no arranque vacia.
        simular();
    }

    // ==================================================================
    // CONSTRUCCION DE LA INTERFAZ
    // ==================================================================

    private void construirInterfaz() {
        JPanel raiz = new JPanel(new BorderLayout(12, 12));
        raiz.setBackground(Paleta.FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(12, 12, 10, 12));

        raiz.add(crearEncabezado(), BorderLayout.NORTH);
        raiz.add(crearColumnaIzquierda(), BorderLayout.WEST);
        raiz.add(crearZonaCentral(), BorderLayout.CENTER);

        barraEstado.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 4));
        barraEstado.setForeground(Paleta.TINTA_BAJA);
        raiz.add(barraEstado, BorderLayout.SOUTH);

        setContentPane(raiz);
    }

    private JComponent crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titulo = new JLabel("Simulador de movimiento parabolico");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 21));
        titulo.setForeground(Paleta.TINTA);

        JLabel subtitulo = new JLabel("Cambia las condiciones iniciales y observa como cambia la trayectoria.");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitulo.setForeground(Paleta.TINTA_BAJA);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(subtitulo);

        panel.add(textos, BorderLayout.WEST);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 0));
        return panel;
    }

    private JComponent crearColumnaIzquierda() {
        JPanel columna = new JPanel();
        columna.setOpaque(false);
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));

        columna.add(crearPanelEntrada());

        JScrollPane scroll = new JScrollPane(columna);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(360, 100));
        scroll.getViewport().setBackground(Paleta.FONDO);
        return scroll;
    }

    /** Panel de entrada: condiciones iniciales, opciones y botones. */
    private JComponent crearPanelEntrada() {
        JPanel panel = crearTarjeta("Panel de entrada");
        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 0, 3, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int fila = 0;

        fila = agregarFila(cuerpo, gbc, fila, "Cuerpo celeste", comboPlaneta);
        fila = agregarFila(cuerpo, gbc, fila, "Velocidad inicial v0 (m/s)", campoV0);
        fila = agregarFila(cuerpo, gbc, fila, "Angulo de lanzamiento (grados)", campoAngulo);

        sliderAngulo.setOpaque(false);
        sliderAngulo.setMajorTickSpacing(45);
        sliderAngulo.setPaintTicks(true);
        sliderAngulo.setPaintLabels(true);
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        cuerpo.add(sliderAngulo, gbc);
        gbc.gridwidth = 1;

        fila = agregarFila(cuerpo, gbc, fila, "Altura inicial y0 (m)", campoAltura);
        fila = agregarFila(cuerpo, gbc, fila, "Gravedad g (m/s^2)", campoGravedad);

        // --- Resistencia del aire ---
        chkAire.setOpaque(false);
        chkAire.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 0, 2, 0);
        cuerpo.add(chkAire, gbc);
        gbc.insets = new Insets(3, 0, 3, 0);
        gbc.gridwidth = 1;

        fila = agregarFila(cuerpo, gbc, fila, "Masa (kg)", campoMasa);
        fila = agregarFila(cuerpo, gbc, fila, "Diametro (m)", campoDiametro);
        fila = agregarFila(cuerpo, gbc, fila, "Coeficiente Cd", campoCd);
        fila = agregarFila(cuerpo, gbc, fila, "Densidad del aire (kg/m^3)", campoDensidad);

        // --- Blanco ---
        chkBlanco.setOpaque(false);
        chkBlanco.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 0, 2, 0);
        cuerpo.add(chkBlanco, gbc);
        gbc.insets = new Insets(3, 0, 3, 0);
        gbc.gridwidth = 1;

        fila = agregarFila(cuerpo, gbc, fila, "Distancia del blanco (m)", campoBlanco);

        // --- Velocidad de la animacion ---
        sliderVelocidad.setOpaque(false);
        gbc.gridx = 0;
        gbc.gridy = fila++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 0, 0, 0);
        cuerpo.add(etiqueta("Velocidad de la animacion"), gbc);
        gbc.insets = new Insets(0, 0, 4, 0);
        gbc.gridy = fila++;
        cuerpo.add(sliderVelocidad, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(3, 0, 3, 0);

        panel.add(cuerpo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(crearPanelBotones());
        return panel;
    }

    private JComponent crearPanelBotones() {
        JPanel botones = new JPanel(new GridBagLayout());
        botones.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 0, 3, 4);
        gbc.weightx = 1;

        estiloBotonPrincipal(btnSimular);
        estiloBoton(btnPausa);
        estiloBoton(btnReiniciar);
        estiloBoton(btnGuardar);
        estiloBoton(btnBorrar);
        estiloBoton(btnCsv);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        botones.add(btnSimular, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        botones.add(btnPausa, gbc);
        gbc.gridx = 1;
        botones.add(btnReiniciar, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        botones.add(btnGuardar, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        botones.add(btnBorrar, gbc);
        gbc.gridx = 1;
        botones.add(btnCsv, gbc);

        return botones;
    }

    /** Panel de resultados: la tabla con las magnitudes calculadas. */
    private JComponent crearPanelResultados() {
        JPanel panel = crearTarjeta("Panel de resultados");

        JTable tabla = new JTable(modeloResultados);
        tabla.setRowHeight(22);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabla.setGridColor(Paleta.LINEA_FINA);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        tabla.getColumnModel().getColumn(0).setPreferredWidth(150);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(330, 190));
        scroll.setBorder(BorderFactory.createLineBorder(Paleta.LINEA_FINA));
        panel.add(scroll);
        return panel;
    }

    /** Zona central: escenario, graficas y tabla de datos en pestanas. */
    private JComponent crearZonaCentral() {
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(new Font("SansSerif", Font.PLAIN, 12));

        pestanas.addTab("Escenario", envolver(panelEscenario));
        pestanas.addTab("Altura vs distancia (y-x)", envolver(graficaYX));
        pestanas.addTab("Distancia vs tiempo (x-t)", envolver(graficaXT));
        pestanas.addTab("Altura vs tiempo (y-t)", envolver(graficaYT));

        pestanas.addTab("Tabla de datos", panelTabla);

        // El panel de resultados va debajo para que se vea siempre,
        // sin importar que pestana este abierta.
        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(pestanas, BorderLayout.CENTER);
        centro.add(crearPanelResultados(), BorderLayout.SOUTH);
        return centro;
    }

    // ---------- Ayudas de construccion ----------

    private JPanel crearTarjeta(String titulo) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Paleta.PANEL);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Paleta.LINEA),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel etiquetaTitulo = new JLabel(titulo);
        etiquetaTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        etiquetaTitulo.setForeground(Paleta.TINTA);
        etiquetaTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.add(etiquetaTitulo);
        tarjeta.add(Box.createVerticalStrut(10));
        return tarjeta;
    }

    private JComponent envolver(JComponent componente) {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(Paleta.PANEL);
        contenedor.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        contenedor.add(componente, BorderLayout.CENTER);
        return contenedor;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(Paleta.TINTA_BAJA);
        return l;
    }

    private int agregarFila(JPanel panel, GridBagConstraints gbc, int fila,
                            String texto, JComponent campo) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 1;
        panel.add(etiqueta(texto), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        campo.setPreferredSize(new Dimension(126, 26));
        if (campo instanceof JTextField) {
            ((JTextField) campo).setHorizontalAlignment(JTextField.RIGHT);
            campo.setFont(new Font("Monospaced", Font.PLAIN, 13));
        }
        panel.add(campo, gbc);
        return fila + 1;
    }

    private void estiloBoton(JButton boton) {
        boton.setFocusPainted(false);
        boton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        boton.setBackground(Paleta.PANEL);
        boton.setForeground(Paleta.TINTA);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Paleta.TINTA),
                BorderFactory.createEmptyBorder(7, 8, 7, 8)));
    }

    private void estiloBotonPrincipal(JButton boton) {
        estiloBoton(boton);
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setBackground(Paleta.TINTA);
        boton.setForeground(Color.WHITE);
        boton.setOpaque(true);
    }

    // ==================================================================
    // EVENTOS
    // ==================================================================

    private void conectarEventos() {
        btnSimular.addActionListener(e -> simular());
        btnPausa.addActionListener(e -> alternarPausa());
        btnReiniciar.addActionListener(e -> reiniciar());
        btnGuardar.addActionListener(e -> guardarComparacion());
        btnBorrar.addActionListener(e -> borrarComparaciones());
        btnCsv.addActionListener(e -> exportarCsv());

        comboPlaneta.addActionListener(e -> aplicarPlaneta());
        chkAire.addActionListener(e -> actualizarHabilitados());
        chkBlanco.addActionListener(e -> actualizarHabilitados());

        // El deslizador y la casilla del angulo se mantienen sincronizados.
        sliderAngulo.addChangeListener(e -> {
            if (sincronizandoAngulo) {
                return;
            }
            sincronizandoAngulo = true;
            campoAngulo.setText(String.valueOf(sliderAngulo.getValue()));
            sincronizandoAngulo = false;
        });
        campoAngulo.addActionListener(e -> simular());
        campoV0.addActionListener(e -> simular());
        campoAltura.addActionListener(e -> simular());
        campoGravedad.addActionListener(e -> simular());

        actualizarHabilitados();
    }

    private void aplicarPlaneta() {
        Planeta planeta = (Planeta) comboPlaneta.getSelectedItem();
        if (planeta == null || planeta == Planeta.PERSONALIZADO) {
            return;
        }
        campoGravedad.setText(Formato.num(planeta.getGravedad(), 2));
        campoDensidad.setText(Formato.num(planeta.getDensidadAire(), 3));
    }

    private void actualizarHabilitados() {
        boolean aire = chkAire.isSelected();
        campoMasa.setEnabled(aire);
        campoDiametro.setEnabled(aire);
        campoCd.setEnabled(aire);
        campoDensidad.setEnabled(aire);
        campoBlanco.setEnabled(chkBlanco.isSelected());
    }

    // ==================================================================
    // LECTURA Y VALIDACION DE DATOS
    // ==================================================================

    /**
     * Lee los campos, valida rango por rango y devuelve los parametros.
     * Si algo esta mal, marca el campo, avisa en la barra de estado y devuelve null.
     */
    private ParametrosLanzamiento leerParametros() {
        limpiarMarcas();

        Double v0 = Formato.leerNumero(campoV0.getText());
        Double angulo = Formato.leerNumero(campoAngulo.getText());
        Double altura = Formato.leerNumero(campoAltura.getText());
        Double gravedad = Formato.leerNumero(campoGravedad.getText());

        if (v0 == null || v0 <= 0) {
            return error(campoV0, "La velocidad inicial debe ser un numero mayor que cero.");
        }
        if (angulo == null || angulo < -90 || angulo > 90) {
            return error(campoAngulo, "El angulo debe estar entre -90 y 90 grados.");
        }
        if (altura == null || altura < 0) {
            return error(campoAltura, "La altura inicial no puede ser negativa.");
        }
        if (gravedad == null || gravedad <= 0) {
            return error(campoGravedad, "La gravedad debe ser un numero mayor que cero.");
        }
        if (angulo <= 0 && altura == 0) {
            return error(campoAngulo, "Con angulo menor o igual a cero y altura cero no hay vuelo. "
                    + "Sube la altura inicial o el angulo.");
        }

        double masa = 0.145;
        double diametro = 0.074;
        double cd = 0.47;
        double densidad = 1.225;

        if (chkAire.isSelected()) {
            Double lMasa = Formato.leerNumero(campoMasa.getText());
            Double lDiametro = Formato.leerNumero(campoDiametro.getText());
            Double lCd = Formato.leerNumero(campoCd.getText());
            Double lDensidad = Formato.leerNumero(campoDensidad.getText());

            if (lMasa == null || lMasa <= 0) {
                return error(campoMasa, "La masa debe ser mayor que cero.");
            }
            if (lDiametro == null || lDiametro <= 0) {
                return error(campoDiametro, "El diametro debe ser mayor que cero.");
            }
            if (lCd == null || lCd < 0) {
                return error(campoCd, "El coeficiente de arrastre no puede ser negativo.");
            }
            if (lDensidad == null || lDensidad < 0) {
                return error(campoDensidad, "La densidad del aire no puede ser negativa.");
            }
            masa = lMasa;
            diametro = lDiametro;
            cd = lCd;
            densidad = lDensidad;
        }

        if (chkBlanco.isSelected()) {
            Double blanco = Formato.leerNumero(campoBlanco.getText());
            if (blanco == null || blanco <= 0) {
                return error(campoBlanco, "La distancia del blanco debe ser mayor que cero.");
            }
        }

        // El deslizador se pone de acuerdo con el valor escrito.
        sincronizandoAngulo = true;
        sliderAngulo.setValue((int) Math.round(angulo));
        sincronizandoAngulo = false;

        return new ParametrosLanzamiento(v0, angulo, altura, gravedad,
                chkAire.isSelected(), masa, diametro, cd, densidad);
    }

    private ParametrosLanzamiento error(JTextField campo, String mensaje) {
        campo.setBackground(new Color(0xFDECE4));
        barraEstado.setForeground(Paleta.ALERTA);
        barraEstado.setText(" " + mensaje);
        campo.requestFocusInWindow();
        return null;
    }

    private void limpiarMarcas() {
        JTextField[] campos = {campoV0, campoAngulo, campoAltura, campoGravedad,
            campoMasa, campoDiametro, campoCd, campoDensidad, campoBlanco};
        for (JTextField campo : campos) {
            campo.setBackground(Color.WHITE);
        }
        barraEstado.setForeground(Paleta.TINTA_BAJA);
    }

    // ==================================================================
    // SIMULACION
    // ==================================================================

    private void simular() {
        ParametrosLanzamiento p = leerParametros();
        if (p == null) {
            return;
        }

        // El modelo ideal se calcula siempre para poder comparar.
        resultadoIdeal = MotorFisico.simularIdeal(p, "Modelo ideal");
        resultadoAire = p.isConsiderarAire()
                ? MotorFisico.simularConAire(p, "Con resistencia del aire") : null;
        resultadoMostrado = (resultadoAire != null) ? resultadoAire : resultadoIdeal;

        double tolerancia = Math.max(0.5, resultadoMostrado.getAlcanceHorizontal() * 0.02);
        double distanciaBlanco = 0;
        if (chkBlanco.isSelected()) {
            Double valor = Formato.leerNumero(campoBlanco.getText());
            distanciaBlanco = (valor == null) ? 0 : valor;
        }

        panelEscenario.setBlanco(chkBlanco.isSelected(), distanciaBlanco, tolerancia);
        panelEscenario.setDatos(resultadoMostrado,
                (resultadoAire != null) ? resultadoIdeal : null, comparaciones);

        actualizarTablaResultados(distanciaBlanco, tolerancia);
        actualizarGraficas();
        panelTabla.setResultado(resultadoMostrado);

        tiempoActual = 0;
        panelEscenario.setTiempo(0);
        btnPausa.setText("Pausar");
        temporizador.start();

        barraEstado.setForeground(Paleta.TINTA_BAJA);
        barraEstado.setText(" Simulacion en curso. " + p.descripcionCorta());
    }

    private void avanzarAnimacion() {
        if (resultadoMostrado == null) {
            temporizador.stop();
            return;
        }
        double factor = sliderVelocidad.getValue() / 100.0;
        tiempoActual += 0.020 * factor;

        double total = resultadoMostrado.getTiempoVuelo();
        if (tiempoActual >= total) {
            tiempoActual = total;
            temporizador.stop();
            btnPausa.setText("Repetir");
            barraEstado.setForeground(Paleta.TINTA_BAJA);
            barraEstado.setText(" Vuelo terminado. Revisa las pestanas de graficas y la tabla de datos.");
        }
        panelEscenario.setTiempo(tiempoActual);
    }

    private void alternarPausa() {
        if (resultadoMostrado == null) {
            return;
        }
        if (temporizador.isRunning()) {
            temporizador.stop();
            btnPausa.setText("Reanudar");
        } else {
            if (tiempoActual >= resultadoMostrado.getTiempoVuelo()) {
                tiempoActual = 0;
            }
            temporizador.start();
            btnPausa.setText("Pausar");
        }
    }

    /** Vuelve la ventana al estado inicial. */
    private void reiniciar() {
        temporizador.stop();
        comboPlaneta.setSelectedItem(Planeta.TIERRA);
        campoV0.setText("25");
        campoAngulo.setText("45");
        campoAltura.setText("0");
        campoGravedad.setText("9,81");
        campoMasa.setText("0,145");
        campoDiametro.setText("0,074");
        campoCd.setText("0,47");
        campoDensidad.setText("1,225");
        campoBlanco.setText("60");
        chkAire.setSelected(false);
        chkBlanco.setSelected(false);
        sliderVelocidad.setValue(100);
        comparaciones.clear();
        limpiarMarcas();
        actualizarHabilitados();

        modeloResultados.setRowCount(0);
        panelTabla.limpiar();
        graficaYX.limpiar();
        graficaXT.limpiar();
        graficaYT.limpiar();
        panelEscenario.limpiar();

        resultadoIdeal = null;
        resultadoAire = null;
        resultadoMostrado = null;
        tiempoActual = 0;

        barraEstado.setForeground(Paleta.TINTA_BAJA);
        barraEstado.setText(" Valores restablecidos. Presiona Simular cuando quieras.");
    }

    private void guardarComparacion() {
        if (resultadoMostrado == null) {
            barraEstado.setForeground(Paleta.ALERTA);
            barraEstado.setText(" Primero ejecuta una simulacion.");
            return;
        }
        if (comparaciones.size() >= 6) {
            barraEstado.setForeground(Paleta.ALERTA);
            barraEstado.setText(" Ya hay 6 lanzamientos guardados. Borra algunos para seguir comparando.");
            return;
        }
        comparaciones.add(resultadoMostrado);
        panelEscenario.setDatos(resultadoMostrado,
                (resultadoAire != null) ? resultadoIdeal : null, comparaciones);
        actualizarGraficas();
        barraEstado.setForeground(Paleta.EXITO);
        barraEstado.setText(" Lanzamiento guardado (" + comparaciones.size()
                + "). Cambia los datos y simula otra vez para comparar.");
    }

    private void borrarComparaciones() {
        comparaciones.clear();
        if (resultadoMostrado != null) {
            panelEscenario.setDatos(resultadoMostrado,
                    (resultadoAire != null) ? resultadoIdeal : null, comparaciones);
        }
        actualizarGraficas();
        barraEstado.setForeground(Paleta.TINTA_BAJA);
        barraEstado.setText(" Comparaciones borradas.");
    }

    private void exportarCsv() {
        if (resultadoMostrado == null) {
            barraEstado.setForeground(Paleta.ALERTA);
            barraEstado.setText(" Primero ejecuta una simulacion.");
            return;
        }
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File("trayectoria.csv"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            ExportadorCSV.guardar(selector.getSelectedFile(), resultadoMostrado);
            barraEstado.setForeground(Paleta.EXITO);
            barraEstado.setText(" Datos guardados en " + selector.getSelectedFile().getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el archivo:\n" + ex.getMessage(),
                    "Error al exportar", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================================================================
    // ACTUALIZACION DE LAS SALIDAS
    // ==================================================================

    private void actualizarTablaResultados(double distanciaBlanco, double tolerancia) {
        modeloResultados.setRowCount(0);
        ResultadoSimulacion ideal = resultadoIdeal;
        ResultadoSimulacion aire = resultadoAire;

        agregarResultado("Velocidad inicial en x", ideal.getV0x(),
                (aire == null) ? null : aire.getV0x(), 2, "m/s");
        agregarResultado("Velocidad inicial en y", ideal.getV0y(),
                (aire == null) ? null : aire.getV0y(), 2, "m/s");
        agregarResultado("Tiempo de subida", ideal.getTiempoAlturaMaxima(),
                (aire == null) ? null : aire.getTiempoAlturaMaxima(), 3, "s");
        agregarResultado("Tiempo de vuelo", ideal.getTiempoVuelo(),
                (aire == null) ? null : aire.getTiempoVuelo(), 3, "s");
        agregarResultado("Altura maxima", ideal.getAlturaMaxima(),
                (aire == null) ? null : aire.getAlturaMaxima(), 3, "m");
        agregarResultado("Alcance horizontal", ideal.getAlcanceHorizontal(),
                (aire == null) ? null : aire.getAlcanceHorizontal(), 3, "m");
        agregarResultado("Velocidad final", ideal.getVelocidadFinal(),
                (aire == null) ? null : aire.getVelocidadFinal(), 2, "m/s");
        agregarResultado("Angulo de impacto", ideal.getAnguloFinal(),
                (aire == null) ? null : aire.getAnguloFinal(), 2, "grados");

        if (chkAire.isSelected()) {
            modeloResultados.addRow(new Object[] {"Area transversal", "--",
                Formato.conUnidad(resultadoMostrado.getParametros().getAreaTransversal(), 5, "m^2"), ""});
        }

        if (chkBlanco.isSelected()) {
            double alcance = resultadoMostrado.getAlcanceHorizontal();
            double diferencia = alcance - distanciaBlanco;
            boolean acierto = Math.abs(diferencia) <= tolerancia;
            String detalle = (diferencia >= 0 ? "se paso " : "quedo corto ")
                    + Formato.conUnidad(Math.abs(diferencia), 2, "m");
            modeloResultados.addRow(new Object[] {"Blanco",
                Formato.conUnidad(distanciaBlanco, 2, "m"),
                acierto ? "ACERTO" : "FALLO", detalle});
        }
    }

    private void agregarResultado(String nombre, double valorIdeal, Double valorAire,
                                  int decimales, String unidad) {
        String columnaAire = "--";
        String columnaDiferencia = "";
        if (valorAire != null) {
            columnaAire = Formato.conUnidad(valorAire, decimales, unidad);
            if (Math.abs(valorIdeal) > 1e-9) {
                double porcentaje = (valorAire - valorIdeal) / Math.abs(valorIdeal) * 100.0;
                columnaDiferencia = Formato.num(porcentaje, 1) + " %";
            }
        }
        modeloResultados.addRow(new Object[] {nombre,
            Formato.conUnidad(valorIdeal, decimales, unidad), columnaAire, columnaDiferencia});
    }

    private void actualizarGraficas() {
        if (resultadoMostrado == null) {
            return;
        }
        List<PanelGrafica.Serie> serieYX = new ArrayList<PanelGrafica.Serie>();
        List<PanelGrafica.Serie> serieXT = new ArrayList<PanelGrafica.Serie>();
        List<PanelGrafica.Serie> serieYT = new ArrayList<PanelGrafica.Serie>();

        for (int i = 0; i < comparaciones.size(); i++) {
            ResultadoSimulacion r = comparaciones.get(i);
            Color color = Paleta.colorComparacion(i);
            String nombre = "Guardado " + (i + 1);
            serieYX.add(new PanelGrafica.Serie(nombre, color, r.arregloX(), r.arregloY(), true));
            serieXT.add(new PanelGrafica.Serie(nombre, color, r.arregloTiempos(), r.arregloX(), true));
            serieYT.add(new PanelGrafica.Serie(nombre, color, r.arregloTiempos(), r.arregloY(), true));
        }

        if (resultadoAire != null) {
            serieYX.add(new PanelGrafica.Serie("Ideal (sin aire)", Paleta.TRAYECTORIA,
                    resultadoIdeal.arregloX(), resultadoIdeal.arregloY(), true));
            serieXT.add(new PanelGrafica.Serie("Ideal (sin aire)", Paleta.TRAYECTORIA,
                    resultadoIdeal.arregloTiempos(), resultadoIdeal.arregloX(), true));
            serieYT.add(new PanelGrafica.Serie("Ideal (sin aire)", Paleta.TRAYECTORIA,
                    resultadoIdeal.arregloTiempos(), resultadoIdeal.arregloY(), true));
        }

        Color colorActual = (resultadoAire != null) ? Paleta.CON_AIRE : Paleta.TRAYECTORIA;
        String nombreActual = (resultadoAire != null) ? "Con resistencia del aire" : "Lanzamiento actual";
        serieYX.add(new PanelGrafica.Serie(nombreActual, colorActual,
                resultadoMostrado.arregloX(), resultadoMostrado.arregloY(), false));
        serieXT.add(new PanelGrafica.Serie(nombreActual, colorActual,
                resultadoMostrado.arregloTiempos(), resultadoMostrado.arregloX(), false));
        serieYT.add(new PanelGrafica.Serie(nombreActual, colorActual,
                resultadoMostrado.arregloTiempos(), resultadoMostrado.arregloY(), false));

        graficaYX.setSeries(serieYX);
        graficaXT.setSeries(serieXT);
        graficaYT.setSeries(serieYT);
    }
}
