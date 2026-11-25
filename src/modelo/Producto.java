package modelo;

import java.io.Serializable;

public record Producto(String nombre, double precio) implements Serializable {
    public Producto {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        if (precio <= 0) throw new IllegalArgumentException("El precio debe ser mayor que cero");
    }
}
