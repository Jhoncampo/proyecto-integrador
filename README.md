# Cafetería: gestión de descuentos

Aplicación Java (Swing) que calcula el descuento de cada pedido de la cafetería y guarda el historial. Requiere JDK 17 o superior, sin librerías externas.

## Estructura

| Unidad | Paquete | Contenido |
|---|---|---|
| 2 - Lógica y Versiones | `modelo`, `logica`, `pruebas` | Producto, ItemPedido, Pedido, TipoCliente, reglas de descuento (programación funcional) y pruebas |
| 3 - Interfaz y Persistencia | `datos`, `ui` | Repositorio (interfaz + implementación en archivo) y ventana Swing |

## Compilar y ejecutar

```
javac -d out $(find src -name '*.java')
java -cp out pruebas.Pruebas     # pruebas de lógica y persistencia
java -cp out ui.App              # abre la aplicación (crea pedidos.dat en la carpeta actual)
```

## Reglas de descuento

No se acumulan: se aplica el de mayor valor entre los que corresponden al pedido.

- Estudiante: 10%
- Empleado: 20%
- 5 o más unidades: 8%
- Subtotal desde $30.000: 5%
