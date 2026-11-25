package logica;

import modelo.Pedido;
import modelo.TipoCliente;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ServicioDescuentos {
    private final List<Descuento> reglas;

    public ServicioDescuentos(List<Descuento> reglas) {
        this.reglas = List.copyOf(reglas);
    }

    public static ServicioDescuentos porDefecto() {
        return new ServicioDescuentos(List.of(
                porcentaje("Estudiante 10%", p -> p.tipo() == TipoCliente.ESTUDIANTE, 0.10),
                porcentaje("Empleado 20%", p -> p.tipo() == TipoCliente.EMPLEADO, 0.20),
                porcentaje("5 o más unidades 8%", p -> p.unidades() >= 5, 0.08),
                porcentaje("Compra desde $30.000 5%", p -> p.subtotal() >= 30_000, 0.05)));
    }

    private static Descuento porcentaje(String nombre, Predicate<Pedido> condicion, double pct) {
        return new Descuento(nombre, condicion, p -> Math.round(p.subtotal() * pct));
    }

    /** Descuentos no acumulables: se elige el de mayor valor entre los que aplican. */
    public Optional<Descuento> mejor(Pedido p) {
        return reglas.stream()
                .filter(d -> d.aplica().test(p))
                .max(Comparator.comparingDouble(d -> d.monto().applyAsDouble(p)));
    }

    public Pedido aplicar(Pedido p) {
        return mejor(p).map(d -> p.con(d.nombre(), d.monto().applyAsDouble(p))).orElse(p);
    }
}
