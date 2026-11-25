package ui;

import datos.RepositorioArchivo;

import javax.swing.SwingUtilities;
import java.nio.file.Path;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new VentanaCafeteria(new RepositorioArchivo(Path.of("pedidos.dat"))).setVisible(true));
    }
}
