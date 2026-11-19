import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 * Panel de graficas cartesianas.
 * Se usa tres veces con los mismos datos vistos de distinta forma:
 * altura contra distancia, distancia contra tiempo y altura contra tiempo.
 */
public class PanelGrafica extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Una curva dentro de la grafica. */
    public static class Serie {
        private final String nombre;
        private final Color color;
        private final double[] xs;
        private final double[] ys;
        private final boolean punteada;

        public Serie(String nombre, Color color, double[] xs, double[] ys, boolean punteada) {
            this.nombre = nombre;
            this.color = color;
            this.xs = xs;
            this.ys = ys;
            this.punteada = punteada;
        }
    }

    private final String tituloX;
    private final String tituloY;
    private final List<Serie> series = new ArrayList<Serie>();

    private static final int MARGEN_IZQ = 74;
    private static final int MARGEN_DER = 22;
    private static final int MARGEN_SUP = 26;
    private static final int MARGEN_INF = 54;

    public PanelGrafica(String tituloX, String tituloY) {
        this.tituloX = tituloX;
        this.tituloY = tituloY;
        setBackground(Paleta.PANEL);
        setPreferredSize(new Dimension(520, 360));
    }

    public void setSeries(List<Serie> nuevas) {
        series.clear();
        if (nuevas != null) {
            series.addAll(nuevas);
        }
        repaint();
    }

    public void limpiar() {
        series.clear();
        repaint();
    }

    /** Elige un paso de cuadricula redondo: 1, 2, 5, 10, 20, 50... */
    static double pasoBonito(double rango) {
        return pasoBonito(rango, 6);
    }

    /** Igual que el anterior, indicando en cuantas divisiones se quiere partir el rango. */
    static double pasoBonito(double rango, int divisiones) {
        if (rango <= 0 || divisiones <= 0) {
            return 1;
        }
        double bruto = rango / (double) divisiones;
        double exponente = Math.floor(Math.log10(bruto));
        double base = Math.pow(10, exponente);
        double n = bruto / base;
        double multiplicador;
        if (n <= 1) {
            multiplicador = 1;
        } else if (n <= 2) {
            multiplicador = 2;
        } else if (n <= 5) {
            multiplicador = 5;
        } else {
            multiplicador = 10;
        }
        return multiplicador * base;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();
        int areaAncho = ancho - MARGEN_IZQ - MARGEN_DER;
        int areaAlto = alto - MARGEN_SUP - MARGEN_INF;

        if (areaAncho < 40 || areaAlto < 40) {
            g2.dispose();
            return;
        }

        if (series.isEmpty()) {
            g2.setColor(Paleta.TINTA_BAJA);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            String aviso = "Presiona Simular para generar la grafica";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(aviso, (ancho - fm.stringWidth(aviso)) / 2, alto / 2);
            g2.dispose();
            return;
        }

        // Limites de los datos, incluyendo siempre el origen.
        double minX = 0, maxX = 0, minY = 0, maxY = 0;
        for (Serie s : series) {
            for (int i = 0; i < s.xs.length; i++) {
                minX = Math.min(minX, s.xs[i]);
                maxX = Math.max(maxX, s.xs[i]);
                minY = Math.min(minY, s.ys[i]);
                maxY = Math.max(maxY, s.ys[i]);
            }
        }
        if (maxX - minX < 1e-9) {
            maxX = minX + 1;
        }
        if (maxY - minY < 1e-9) {
            maxY = minY + 1;
        }
        maxX += (maxX - minX) * 0.04;
        maxY += (maxY - minY) * 0.08;

        final double fMinX = minX, fMaxX = maxX, fMinY = minY, fMaxY = maxY;
        final int izq = MARGEN_IZQ;
        final int arriba = MARGEN_SUP;
        final int aAncho = areaAncho;
        final int aAlto = areaAlto;

        // Marco del area de dibujo
        g2.setColor(Paleta.PANEL);
        g2.fillRect(izq, arriba, aAncho, aAlto);

        double pasoX = pasoBonito(fMaxX - fMinX);
        double pasoY = pasoBonito(fMaxY - fMinY);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();

        // Cuadricula vertical y numeros del eje x
        g2.setStroke(new BasicStroke(1f));
        for (double v = Math.ceil(fMinX / pasoX) * pasoX; v <= fMaxX + 1e-9; v += pasoX) {
            int px = izq + (int) Math.round((v - fMinX) / (fMaxX - fMinX) * aAncho);
            g2.setColor(Paleta.LINEA_FINA);
            g2.drawLine(px, arriba, px, arriba + aAlto);
            g2.setColor(Paleta.TINTA_BAJA);
            String texto = Formato.num(v, decimalesSegunPaso(pasoX));
            g2.drawString(texto, px - fm.stringWidth(texto) / 2, arriba + aAlto + 16);
        }

        // Cuadricula horizontal y numeros del eje y
        for (double v = Math.ceil(fMinY / pasoY) * pasoY; v <= fMaxY + 1e-9; v += pasoY) {
            int py = arriba + aAlto - (int) Math.round((v - fMinY) / (fMaxY - fMinY) * aAlto);
            g2.setColor(Paleta.LINEA_FINA);
            g2.drawLine(izq, py, izq + aAncho, py);
            g2.setColor(Paleta.TINTA_BAJA);
            String texto = Formato.num(v, decimalesSegunPaso(pasoY));
            g2.drawString(texto, izq - 8 - fm.stringWidth(texto), py + 4);
        }

        // Ejes
        g2.setColor(Paleta.LINEA);
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawLine(izq, arriba, izq, arriba + aAlto);
        g2.drawLine(izq, arriba + aAlto, izq + aAncho, arriba + aAlto);

        // Titulos de los ejes (con unidades)
        g2.setColor(Paleta.TINTA);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fmT = g2.getFontMetrics();
        g2.drawString(tituloX, izq + aAncho / 2 - fmT.stringWidth(tituloX) / 2, alto - 16);

        Graphics2D gr = (Graphics2D) g2.create();
        gr.rotate(-Math.PI / 2, 18, arriba + aAlto / 2.0);
        gr.drawString(tituloY, 18 - fmT.stringWidth(tituloY) / 2, arriba + aAlto / 2.0f + 4);
        gr.dispose();

        // Curvas
        for (Serie s : series) {
            if (s.punteada) {
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        1f, new float[] {6f, 6f}, 0f));
            } else {
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
            g2.setColor(s.color);
            Path2D.Double camino = new Path2D.Double();
            boolean primero = true;
            for (int i = 0; i < s.xs.length; i++) {
                double px = izq + (s.xs[i] - fMinX) / (fMaxX - fMinX) * aAncho;
                double py = arriba + aAlto - (s.ys[i] - fMinY) / (fMaxY - fMinY) * aAlto;
                if (primero) {
                    camino.moveTo(px, py);
                    primero = false;
                } else {
                    camino.lineTo(px, py);
                }
            }
            g2.draw(camino);
        }

        // Leyenda
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        FontMetrics fmL = g2.getFontMetrics();
        int y = arriba + 6;
        for (Serie s : series) {
            int anchoTexto = fmL.stringWidth(s.nombre);
            int x = izq + aAncho - anchoTexto - 32;
            g2.setColor(s.color);
            g2.setStroke(new BasicStroke(2.6f));
            g2.draw(new Line2D.Double(x, y + 6, x + 18, y + 6));
            g2.setColor(Paleta.TINTA);
            g2.drawString(s.nombre, x + 25, y + 10);
            y += 17;
        }

        g2.dispose();
    }

    private static int decimalesSegunPaso(double paso) {
        if (paso >= 10) {
            return 0;
        }
        if (paso >= 1) {
            return 1;
        }
        return 2;
    }
}
