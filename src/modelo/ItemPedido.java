package modelo;

import java.io.Serializable;

public record ItemPedido(Producto producto, int cantidad) implements Serializable {
    public ItemPedido {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
    }

    public double subtotal() { return producto.precio() * cantidad; }
}
