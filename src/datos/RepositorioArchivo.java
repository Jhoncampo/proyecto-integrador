package datos;

import modelo.Pedido;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

// shortcut: serialización Java en un solo archivo (se reescribe completo y se rompe si cambian las clases),
// migrar a SQLite/JDBC detrás de RepositorioPedidos cuando haya que consultar o crecer.
public class RepositorioArchivo implements RepositorioPedidos {
    private final Path archivo;

    public RepositorioArchivo(Path archivo) {
        this.archivo = archivo;
    }

    @Override
    public void guardar(Pedido pedido) {
        List<Pedido> todos = new ArrayList<>(listar());
        todos.add(pedido);
        Path tmp = archivo.resolveSibling(archivo.getFileName() + ".tmp");
        try (var out = new ObjectOutputStream(Files.newOutputStream(tmp))) {
            out.writeObject(todos);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el pedido", e);
        }
        try {
            Files.move(tmp, archivo, StandardCopyOption.REPLACE_EXISTING); // evita dejar el archivo a medias
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el pedido", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Pedido> listar() {
        if (!Files.exists(archivo)) return List.of();
        try (var in = new ObjectInputStream(Files.newInputStream(archivo))) {
            return (List<Pedido>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("No se pudo leer " + archivo, e);
        }
    }
}
