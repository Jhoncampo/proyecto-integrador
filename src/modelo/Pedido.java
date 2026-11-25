package modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** Pedido inmutable: aplicar un descuento devuelve una copia nueva. */
public record Pedido(String cliente, TipoCliente tipo, List<ItemPedido> items, LocalDateTime fecha,
                     String descuento, double montoDescuento) implements Serializable {

    public Pedido {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("El pedido debe tener al menos un producto");
        items = List.copyOf(items);
    }

    public static Pedido nuevo(String cliente, TipoCliente tipo, List<ItemPedido> items) {
        return new Pedido(cliente, tipo, items, LocalDateTime.now(), "Sin descuento", 0);
    }

    public Pedido con(String nombreDescuento, double monto) {
        return new Pedido(cliente, tipo, items, fecha, nombreDescuento, monto);
    }

    public double subtotal() { return items.stream().mapToDouble(ItemPedido::subtotal).sum(); }

    public int unidades() { return items.stream().mapToInt(ItemPedido::cantidad).sum(); }

    public double total() { return subtotal() - montoDescuento; }
}
