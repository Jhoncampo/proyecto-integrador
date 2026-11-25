package datos;

import modelo.Pedido;

import java.util.List;

public interface RepositorioPedidos {
    void guardar(Pedido pedido);

    List<Pedido> listar();
}
