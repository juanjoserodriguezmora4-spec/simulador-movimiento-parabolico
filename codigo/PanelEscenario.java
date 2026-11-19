import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Representacion visual del fenomeno fisico.
 *
 * Dibuja el escenario completo: suelo, lanzador, trayectoria recorrida,
 * el proyectil en movimiento, su vector velocidad descompuesto en
 * componentes, la altura maxima, el alcance y el blanco opcional.
 */
public class PanelEscenario extends JPanel {

    private static final long serialVersionUID = 1L;

    private ResultadoSimulacion principal;
    private ResultadoSimulacion referenciaIdeal;   // se dibuja punteada cuando hay aire
    private List<ResultadoSimulacion> comparaciones = new ArrayList<ResultadoSimulacion>();

    private double tiempoActual = 0.0;
    private boolean mostrarBlanco = false;
    private double distanciaBlanco = 0.0;
    private double toleranciaBlanco = 1.0;

    // Escala mundo -> pantalla, calculada en cada repintado.
    private double escala = 1.0;
    private double origenX = 0.0;
    private double origenY = 0.0;

    public PanelEscenario() {
        setBackground(Paleta.PANEL);
        setPreferredSize(new Dimension(640, 400));
    }

    public void setDatos(ResultadoSimulacion principal, ResultadoSimulacion referenciaIdeal,
                         List<ResultadoSimulacion> comparaciones) {
        this.principal = principal;
        this.referenciaIdeal = referenciaIdeal;
        this.comparaciones = (comparaciones == null)
                ? new ArrayList<ResultadoSimulacion>() : comparaciones;
        repaint();
    }

    public void setBlanco(boolean mostrar, double distancia, double tolerancia) {
        this.mostrarBlanco = mostrar;
        this.distanciaBlanco = distancia;
        this.toleranciaBlanco = tolerancia;
        repaint();
    }

    public void setTiempo(double t) {
        this.tiempoActual = t;
        repaint();
    }

    public void limpiar() {
        principal = null;
        referenciaIdeal = null;
        comparaciones = new ArrayList<ResultadoSimulacion>();
        tiempoActual = 0;
        repaint();
    }

    // Conversion de coordenadas fisicas (metros) a pixeles de pantalla.
    private double px(double xMundo) {
        return origenX + xMundo * escala;
    }

    private double py(double yMundo) {
        return origenY - yMundo * escala;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();

        // Cielo
        g2.setPaint(new GradientPaint(0, 0, Paleta.CIELO_ALTO, 0, alto, Paleta.CIELO_BAJO));
        g2.fillRect(0, 0, ancho, alto);

        if (principal == null || principal.getPuntos().isEmpty()) {
            g2.setColor(Paleta.TINTA_BAJA);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            String aviso = "Ingresa los datos y presiona Simular";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(aviso, (ancho - fm.stringWidth(aviso)) / 2, alto / 2);
            g2.dispose();
            return;
        }

        // ----- Limites del mundo -----
        double xMax = Math.max(principal.getAlcanceHorizontal(), 1.0);
        double yMax = Math.max(principal.getAlturaMaxima(), 1.0);
        if (referenciaIdeal != null) {
            xMax = Math.max(xMax, referenciaIdeal.getAlcanceHorizontal());
            yMax = Math.max(yMax, referenciaIdeal.getAlturaMaxima());
        }
        for (ResultadoSimulacion r : comparaciones) {
            xMax = Math.max(xMax, r.getAlcanceHorizontal());
            yMax = Math.max(yMax, r.getAlturaMaxima());
        }
        if (mostrarBlanco) {
            xMax = Math.max(xMax, distanciaBlanco + toleranciaBlanco);
        }
        xMax *= 1.10;
        yMax *= 1.18;

        int margenIzq = 56;
        int margenDer = 24;
        int margenSup = 30;
        int alturaSuelo = 46;

        double escalaX = (ancho - margenIzq - margenDer) / xMax;
        double escalaY = (alto - margenSup - alturaSuelo) / yMax;
        // Se usa la misma escala en los dos ejes para que la parabola no se deforme.
        escala = Math.min(escalaX, escalaY);
        origenX = margenIzq;

        // Cuando la trayectoria es mucho mas ancha que alta sobra espacio vertical.
        // Ese sobrante se reparte: una parte engrosa el terreno y el resto queda de cielo.
        double libre = (alto - margenSup - alturaSuelo) - yMax * escala;
        double extraTerreno = Math.max(0, Math.min(libre * 0.5, 90));
        origenY = alto - alturaSuelo - extraTerreno;

        dibujarCuadricula(g2, ancho, xMax, yMax);
        dibujarSuelo(g2, ancho, alto, alturaSuelo);
        dibujarEscalaHorizontal(g2, ancho, xMax, yMax);

        if (mostrarBlanco) {
            dibujarBlanco(g2);
        }

        // Lanzamientos guardados para comparar
        int indice = 0;
        for (ResultadoSimulacion r : comparaciones) {
            dibujarTrayectoriaCompleta(g2, r, Paleta.colorComparacion(indice), true, 1.5f);
            indice++;
        }

        // Trayectoria ideal de referencia cuando la simulacion incluye aire
        if (referenciaIdeal != null) {
            dibujarTrayectoriaCompleta(g2, referenciaIdeal, Paleta.TINTA_BAJA, true, 1.6f);
        }

        dibujarLanzador(g2);
        dibujarMarcadores(g2);
        dibujarRecorrido(g2);
        dibujarProyectil(g2);
        dibujarPanelDatos(g2);

        g2.dispose();
    }

    /**
     * Cuadricula en metros, para que se pueda leer la escala real.
     * Se usa el mismo paso en los dos ejes: como la escala tambien es la misma,
     * las celdas quedan cuadradas y la parabola se ve sin deformar.
     */
    private void dibujarCuadricula(Graphics2D g2, int ancho, double xMax, double yMax) {
        double paso = pasoCuadricula(xMax, yMax);
        int decimales = (paso >= 1) ? 0 : 1;
        g2.setStroke(new BasicStroke(1f));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();

        for (double v = paso; v <= xMax; v += paso) {
            int x = (int) Math.round(px(v));
            g2.setColor(new Color(255, 255, 255, 165));
            g2.drawLine(x, (int) py(yMax), x, (int) origenY);
        }
        for (double v = paso; v <= yMax; v += paso) {
            int y = (int) Math.round(py(v));
            g2.setColor(new Color(255, 255, 255, 165));
            g2.drawLine((int) origenX, y, ancho - 10, y);
            g2.setColor(Paleta.TINTA_BAJA);
            String texto = Formato.num(v, decimales);
            g2.drawString(texto, (int) origenX - 6 - fm.stringWidth(texto), y + 4);
        }
        g2.setColor(Paleta.TINTA_BAJA);
        g2.drawString("altura (m)", 6, (int) py(yMax) - 8);
    }

    /** Un solo paso para los dos ejes, de modo que las celdas queden cuadradas. */
    private double pasoCuadricula(double xMax, double yMax) {
        return PanelGrafica.pasoBonito(Math.max(xMax, yMax), 12);
    }

    /** Numeros del eje horizontal, escritos sobre el terreno para que se lean bien. */
    private void dibujarEscalaHorizontal(Graphics2D g2, int ancho, double xMax, double yMax) {
        double paso = pasoCuadricula(xMax, yMax);
        int decimales = (paso >= 1) ? 0 : 1;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(255, 255, 255, 235));
        for (double v = paso; v <= xMax; v += paso) {
            int x = (int) Math.round(px(v));
            String texto = Formato.num(v, decimales);
            g2.drawString(texto, x - fm.stringWidth(texto) / 2, (int) origenY + 15);
        }
        g2.drawString("distancia (m)", ancho - 90, (int) origenY + 32);
    }

    private void dibujarSuelo(Graphics2D g2, int ancho, int alto, int alturaSuelo) {
        int ySuelo = (int) origenY;
        g2.setColor(Paleta.SUELO);
        g2.fillRect(0, ySuelo, ancho, alto - ySuelo);
        g2.setColor(Paleta.TINTA);
        g2.setStroke(new BasicStroke(1.6f));
        g2.drawLine(0, ySuelo, ancho, ySuelo);

        // Textura sencilla del terreno
        g2.setColor(new Color(255, 255, 255, 55));
        g2.setStroke(new BasicStroke(1f));
        for (int x = 0; x < ancho; x += 14) {
            g2.drawLine(x, ySuelo + 6, x + 8, alto);
        }
    }

    /** Torre o soporte desde el que sale el proyectil, con el canon inclinado. */
    private void dibujarLanzador(Graphics2D g2) {
        double y0 = principal.getParametros().getAlturaInicial();
        double angulo = principal.getParametros().getAnguloRadianes();

        int xBase = (int) Math.round(px(0));
        int yBase = (int) Math.round(py(0));
        int ySalida = (int) Math.round(py(y0));

        if (y0 > 0) {
            int anchoTorre = 14;
            g2.setColor(new Color(0x8A6A4F));
            g2.fillRect(xBase - anchoTorre / 2, ySalida, anchoTorre, yBase - ySalida);
            g2.setColor(Paleta.TINTA);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRect(xBase - anchoTorre / 2, ySalida, anchoTorre, yBase - ySalida);

            // Cota de la altura inicial, escrita al costado de la torre
            g2.setColor(Paleta.TINTA_BAJA);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[] {3f, 3f}, 0f));
            g2.draw(new Line2D.Double(xBase - 30, ySalida, xBase - 10, ySalida));
            g2.draw(new Line2D.Double(xBase - 22, ySalida, xBase - 22, yBase));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString("y0 = " + Formato.num(y0, 1) + " m",
                    xBase + 12, (ySalida + yBase) / 2 + 4);
        }

        // Canon: un rectangulo rotado el angulo de lanzamiento
        AffineTransform original = g2.getTransform();
        g2.translate(xBase, ySalida);
        g2.rotate(-angulo);
        g2.setColor(new Color(0x3E4A5C));
        g2.fillRoundRect(-6, -6, 40, 12, 6, 6);
        g2.setColor(Paleta.TINTA);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(-6, -6, 40, 12, 6, 6);
        g2.setTransform(original);

        g2.setColor(new Color(0x3E4A5C));
        g2.fill(new Ellipse2D.Double(xBase - 9, ySalida - 9, 18, 18));

        // Arco que indica el angulo, con la etiqueta puesta sobre su bisectriz
        g2.setColor(Paleta.ACENTO);
        g2.setStroke(new BasicStroke(1.4f));
        int radio = 34;
        g2.drawArc(xBase - radio, ySalida - radio, radio * 2, radio * 2,
                0, (int) Math.round(Math.toDegrees(angulo)));
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                1f, new float[] {3f, 3f}, 0f));
        g2.draw(new Line2D.Double(xBase, ySalida, xBase + radio + 14, ySalida));

        double bisectriz = angulo / 2.0;
        int etiquetaX = xBase + (int) Math.round((radio + 18) * Math.cos(bisectriz));
        int etiquetaY = ySalida - (int) Math.round((radio + 18) * Math.sin(bisectriz));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString(Formato.num(principal.getParametros().getAnguloGrados(), 1) + " grados",
                etiquetaX, etiquetaY + 4);
    }

    /** Lineas guia de altura maxima y de alcance. */
    private void dibujarMarcadores(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                1f, new float[] {5f, 5f}, 0f));

        double alturaMax = principal.getAlturaMaxima();
        double xAlturaMax = principal.enTiempo(principal.getTiempoAlturaMaxima()).getX();
        g2.setColor(Paleta.ACENTO);
        g2.draw(new Line2D.Double(px(0), py(alturaMax), px(xAlturaMax), py(alturaMax)));
        g2.draw(new Line2D.Double(px(xAlturaMax), py(alturaMax), px(xAlturaMax), origenY));
        textoConFondo(g2, "altura maxima = " + Formato.num(alturaMax, 2) + " m",
                px(xAlturaMax) + 8, py(alturaMax) - 10, Paleta.ACENTO);

        double alcance = principal.getAlcanceHorizontal();
        g2.setColor(Paleta.TRAYECTORIA);
        g2.draw(new Line2D.Double(px(alcance), origenY, px(alcance), origenY - 26));
        String texto = "alcance = " + Formato.num(alcance, 2) + " m";
        FontMetrics fm = g2.getFontMetrics();
        double xTexto = px(alcance) - fm.stringWidth(texto) - 8;
        if (xTexto < 6) {
            // Si el alcance es muy corto la etiqueta no cabe a la izquierda.
            xTexto = px(alcance) + 8;
        }
        textoConFondo(g2, texto, xTexto, origenY - 12, Paleta.TRAYECTORIA);
    }

    /** Escribe un texto sobre un rectangulo claro para que siempre se pueda leer. */
    private void textoConFondo(Graphics2D g2, String texto, double x, double y, Color color) {
        FontMetrics fm = g2.getFontMetrics();
        int ancho = fm.stringWidth(texto);
        int alto = fm.getHeight();
        g2.setColor(new Color(255, 255, 255, 215));
        g2.fillRect((int) x - 3, (int) y - fm.getAscent() - 1, ancho + 6, alto - 1);
        g2.setColor(color);
        g2.drawString(texto, (float) x, (float) y);
    }

    /** Dibuja una trayectoria entera (usada para comparaciones y referencia). */
    private void dibujarTrayectoriaCompleta(Graphics2D g2, ResultadoSimulacion r,
                                            Color color, boolean punteada, float grosor) {
        List<PuntoTrayectoria> puntos = r.getPuntos();
        if (puntos.size() < 2) {
            return;
        }
        Path2D.Double camino = new Path2D.Double();
        camino.moveTo(px(puntos.get(0).getX()), py(puntos.get(0).getY()));
        for (int i = 1; i < puntos.size(); i++) {
            camino.lineTo(px(puntos.get(i).getX()), py(puntos.get(i).getY()));
        }
        if (punteada) {
            g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1f, new float[] {6f, 6f}, 0f));
        } else {
            g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        }
        g2.setColor(color);
        g2.draw(camino);
    }

    /** Parte de la trayectoria que el proyectil ya recorrio. */
    private void dibujarRecorrido(Graphics2D g2) {
        List<PuntoTrayectoria> puntos = principal.getPuntos();
        Path2D.Double camino = new Path2D.Double();
        boolean primero = true;
        for (PuntoTrayectoria p : puntos) {
            if (p.getTiempo() > tiempoActual) {
                break;
            }
            if (primero) {
                camino.moveTo(px(p.getX()), py(p.getY()));
                primero = false;
            } else {
                camino.lineTo(px(p.getX()), py(p.getY()));
            }
        }
        if (primero) {
            return;
        }
        PuntoTrayectoria actual = principal.enTiempo(tiempoActual);
        camino.lineTo(px(actual.getX()), py(actual.getY()));

        g2.setColor(principal.isConArrastre() ? Paleta.CON_AIRE : Paleta.TRAYECTORIA);
        g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(camino);
    }

    /** El objeto y su vector velocidad descompuesto. */
    private void dibujarProyectil(Graphics2D g2) {
        PuntoTrayectoria p = principal.enTiempo(tiempoActual);
        double x = px(p.getX());
        double y = py(p.getY());

        // Vector velocidad: se dibuja con una longitud proporcional a la rapidez.
        double factor = 26.0 / Math.max(1e-6, principal.getParametros().getVelocidadInicial());
        double fx = p.getVx() * factor;
        double fy = -p.getVy() * factor;

        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                1f, new float[] {4f, 4f}, 0f));
        g2.setColor(new Color(0x17607B));
        g2.draw(new Line2D.Double(x, y, x + fx, y));           // componente horizontal
        g2.draw(new Line2D.Double(x + fx, y, x + fx, y + fy)); // componente vertical

        g2.setStroke(new BasicStroke(2.2f));
        g2.setColor(Paleta.ACENTO);
        dibujarFlecha(g2, x, y, x + fx, y + fy);

        // Proyectil
        g2.setColor(new Color(0xD9822B));
        g2.fill(new Ellipse2D.Double(x - 7, y - 7, 14, 14));
        g2.setColor(Paleta.TINTA);
        g2.setStroke(new BasicStroke(1.4f));
        g2.draw(new Ellipse2D.Double(x - 7, y - 7, 14, 14));
    }

    private void dibujarFlecha(Graphics2D g2, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double largo = Math.sqrt(dx * dx + dy * dy);
        if (largo < 2) {
            return;
        }
        double ux = dx / largo;
        double uy = dy / largo;
        double cabeza = Math.min(10, largo * 0.35);
        double bx = x2 - ux * cabeza;
        double by = y2 - uy * cabeza;
        g2.draw(new Line2D.Double(x1, y1, bx, by));
        Path2D.Double punta = new Path2D.Double();
        punta.moveTo(x2, y2);
        punta.lineTo(bx - uy * cabeza * 0.45, by + ux * cabeza * 0.45);
        punta.lineTo(bx + uy * cabeza * 0.45, by - ux * cabeza * 0.45);
        punta.closePath();
        g2.fill(punta);
    }

    /** Blanco al que se puede intentar acertar. */
    private void dibujarBlanco(Graphics2D g2) {
        double xb = px(distanciaBlanco);
        double izquierda = px(distanciaBlanco - toleranciaBlanco);
        double derecha = px(distanciaBlanco + toleranciaBlanco);

        g2.setColor(new Color(30, 122, 75, 60));
        g2.fillRect((int) izquierda, (int) origenY - 8, (int) (derecha - izquierda), 8);

        g2.setColor(Paleta.EXITO);
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new Line2D.Double(xb, origenY, xb, origenY - 40));
        Path2D.Double bandera = new Path2D.Double();
        bandera.moveTo(xb, origenY - 40);
        bandera.lineTo(xb + 22, origenY - 33);
        bandera.lineTo(xb, origenY - 26);
        bandera.closePath();
        g2.fill(bandera);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString("blanco: " + Formato.num(distanciaBlanco, 1) + " m",
                (float) xb - 20, (float) origenY - 46);
    }

    /** Recuadro con los datos instantaneos del proyectil. */
    private void dibujarPanelDatos(Graphics2D g2) {
        PuntoTrayectoria p = principal.enTiempo(tiempoActual);
        String[] lineas = {
            "t  = " + Formato.num(p.getTiempo(), 2) + " s",
            "x  = " + Formato.num(p.getX(), 2) + " m",
            "y  = " + Formato.num(p.getY(), 2) + " m",
            "vx = " + Formato.num(p.getVx(), 2) + " m/s",
            "vy = " + Formato.num(p.getVy(), 2) + " m/s",
            "|v| = " + Formato.num(p.getRapidez(), 2) + " m/s"
        };

        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        int anchoCaja = 0;
        for (String linea : lineas) {
            anchoCaja = Math.max(anchoCaja, fm.stringWidth(linea));
        }
        anchoCaja += 18;
        int altoCaja = lineas.length * 15 + 12;

        int x = getWidth() - anchoCaja - 14;
        int y = 12;

        g2.setColor(new Color(255, 255, 255, 225));
        g2.fillRect(x, y, anchoCaja, altoCaja);
        g2.setColor(Paleta.LINEA);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(x, y, anchoCaja, altoCaja);

        g2.setColor(Paleta.TINTA);
        for (int i = 0; i < lineas.length; i++) {
            g2.drawString(lineas[i], x + 9, y + 20 + i * 15);
        }
    }
}
