package logica;

import modelo.Pedido;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/** Regla de descuento: cuándo aplica y cuánto vale, ambas como funciones. */
public record Descuento(String nombre, Predicate<Pedido> aplica, ToDoubleFunction<Pedido> monto) { }
