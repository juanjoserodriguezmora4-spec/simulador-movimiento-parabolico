import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * SIMULADOR DE MOVIMIENTO PARABOLICO
 * Proyecto final de Fisica - Ingenieria de Software
 *
 * Punto de entrada del programa. Solo se encarga de preparar la apariencia
 * y de abrir la ventana principal dentro del hilo grafico de Swing.
 *
 * Para compilar y ejecutar desde la terminal:
 *     javac *.java
 *     java Main
 */
public class Main {

    public static void main(String[] args) {
        // Apariencia propia del sistema operativo, para que se vea nativo.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Si falla se usa la apariencia por defecto de Java: no es un error grave.
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                VentanaSimulador ventana = new VentanaSimulador();
                ventana.setExtendedState(JFrame.MAXIMIZED_BOTH);
                ventana.setLocationRelativeTo(null);
                ventana.setVisible(true);
            }
        });
    }
}
