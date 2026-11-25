package pruebas;

import datos.RepositorioArchivo;
import logica.ServicioDescuentos;
import modelo.ItemPedido;
import modelo.Pedido;
import modelo.Producto;
import modelo.TipoCliente;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Pruebas sin dependencias: java -cp out pruebas.Pruebas */
public class Pruebas {
    private static final ServicioDescuentos SERVICIO = ServicioDescuentos.porDefecto();
    private static final Producto TINTO = new Producto("Tinto", 2_500);
    private static final Producto CAPUCHINO = new Producto("Capuchino", 6_000);
    private static final Producto SANDWICH = new Producto("Sándwich", 9_000);
    private static int hechas = 0;

    public static void main(String[] args) throws Exception {
        Pedido p = aplicar(TipoCliente.ESTUDIANTE, new ItemPedido(CAPUCHINO, 2));
        esperar("Estudiante 10%", p.descuento());
        esperar(1_200, p.montoDescuento());
        esperar(10_800, p.total());

        p = aplicar(TipoCliente.EMPLEADO, new ItemPedido(SANDWICH, 6)); // 54.000: empleado (20%) gana a 5+ unidades (8%)
        esperar("Empleado 20%", p.descuento());
        esperar(10_800, p.montoDescuento());

        p = aplicar(TipoCliente.GENERAL, new ItemPedido(TINTO, 5)); // 12.500
        esperar("5 o más unidades 8%", p.descuento());
        esperar(1_000, p.montoDescuento());

        p = aplicar(TipoCliente.GENERAL, new ItemPedido(SANDWICH, 4)); // 36.000, solo 4 unidades
        esperar("Compra desde $30.000 5%", p.descuento());
        esperar(1_800, p.montoDescuento());

        p = aplicar(TipoCliente.GENERAL, new ItemPedido(TINTO, 1));
        esperar("Sin descuento", p.descuento());
        esperar(2_500, p.total());

        try {
            Pedido.nuevo("Ana", TipoCliente.GENERAL, List.of());
            throw new AssertionError("Un pedido vacío debía fallar");
        } catch (IllegalArgumentException esperado) {
            hechas++;
        }

        Path dir = Files.createTempDirectory("cafeteria");
        RepositorioArchivo repo = new RepositorioArchivo(dir.resolve("pedidos.dat"));
        esperar(0, repo.listar().size());
        Pedido a = aplicar(TipoCliente.ESTUDIANTE, new ItemPedido(CAPUCHINO, 2));
        Pedido b = aplicar(TipoCliente.GENERAL, new ItemPedido(TINTO, 5));
        repo.guardar(a);
        repo.guardar(b);
        List<Pedido> leidos = new RepositorioArchivo(dir.resolve("pedidos.dat")).listar(); // instancia nueva: viene del disco
        esperar(2, leidos.size());
        if (!leidos.equals(List.of(a, b))) throw new AssertionError("Lo leído no coincide con lo guardado");
        hechas++;

        System.out.println("OK: " + hechas + " verificaciones superadas");
    }

    private static Pedido aplicar(TipoCliente tipo, ItemPedido... items) {
        return SERVICIO.aplicar(Pedido.nuevo("Cliente", tipo, List.of(items)));
    }

    private static void esperar(Object esperado, Object real) {
        if (!esperado.toString().equals(real.toString()) && !numerosIguales(esperado, real))
            throw new AssertionError("Esperado " + esperado + " pero fue " + real);
        hechas++;
    }

    private static boolean numerosIguales(Object a, Object b) {
        return a instanceof Number x && b instanceof Number y && Math.abs(x.doubleValue() - y.doubleValue()) < 0.001;
    }
}
