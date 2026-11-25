package ui;

import datos.RepositorioPedidos;
import logica.ServicioDescuentos;
import modelo.ItemPedido;
import modelo.Pedido;
import modelo.Producto;
import modelo.TipoCliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VentanaCafeteria extends JFrame {
    private static final List<Producto> MENU = List.of(
            new Producto("Tinto", 2_500), new Producto("Capuchino", 6_000), new Producto("Latte", 6_500),
            new Producto("Croissant", 5_000), new Producto("Sándwich", 9_000), new Producto("Torta", 7_000));
    private static final NumberFormat PESOS = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    static { PESOS.setMaximumFractionDigits(0); }

    private final RepositorioPedidos repo;
    private final ServicioDescuentos descuentos = ServicioDescuentos.porDefecto();
    private final List<ItemPedido> carrito = new ArrayList<>();

    private final JTextField cliente = new JTextField(14);
    private final JComboBox<TipoCliente> tipo = new JComboBox<>(TipoCliente.values());
    private final JComboBox<Producto> producto = new JComboBox<>(MENU.toArray(new Producto[0]));
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final DefaultTableModel itemsModel = modeloTabla("Producto", "Cant.", "Subtotal");
    private final DefaultTableModel historialModel =
            modeloTabla("Fecha", "Cliente", "Tipo", "Unidades", "Subtotal", "Descuento", "Total");
    private final JTable itemsTabla = new JTable(itemsModel);
    private final JTable historialTabla = new JTable(historialModel);
    private final JLabel lblSubtotal = new JLabel();
    private final JLabel lblDescuento = new JLabel();
    private final JLabel lblTotal = new JLabel();

    public VentanaCafeteria(RepositorioPedidos repo) {
        super("Cafetería - Pedidos y descuentos");
        this.repo = repo;
        producto.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                Producto p = (Producto) v;
                return super.getListCellRendererComponent(l, p.nombre() + "  " + PESOS.format(p.precio()), i, s, f);
            }
        });
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 18f));

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Nuevo pedido", panelPedido());
        int[] anchos = {130, 90, 100, 70, 90, 220, 90}; // Fecha, Cliente, Tipo, Unidades, Subtotal, Descuento, Total
        for (int i = 0; i < anchos.length; i++) historialTabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        pestanas.addTab("Historial", new JScrollPane(historialTabla));
        add(pestanas);

        refrescarPedido();
        refrescarHistorial();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(840, 520);
        setLocationRelativeTo(null);
    }

    private JPanel panelPedido() {
        JPanel datos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datos.add(new JLabel("Cliente:"));
        datos.add(cliente);
        datos.add(new JLabel("Tipo:"));
        datos.add(tipo);

        JButton agregar = new JButton("Agregar");
        agregar.addActionListener(e -> {
            carrito.add(new ItemPedido((Producto) producto.getSelectedItem(), (int) cantidad.getValue()));
            refrescarPedido();
        });
        JButton quitar = new JButton("Quitar seleccionado");
        quitar.addActionListener(e -> {
            int fila = itemsTabla.getSelectedRow();
            if (fila >= 0) {
                carrito.remove(fila);
                refrescarPedido();
            }
        });
        JPanel agregarFila = new JPanel(new FlowLayout(FlowLayout.LEFT));
        agregarFila.add(new JLabel("Producto:"));
        agregarFila.add(producto);
        agregarFila.add(new JLabel("Cantidad:"));
        agregarFila.add(cantidad);
        agregarFila.add(agregar);
        agregarFila.add(quitar);

        JPanel norte = new JPanel(new GridLayout(2, 1));
        norte.add(datos);
        norte.add(agregarFila);

        JPanel totales = new JPanel(new GridLayout(3, 2, 4, 4));
        totales.add(new JLabel("Subtotal:"));
        totales.add(lblSubtotal);
        totales.add(new JLabel("Descuento:"));
        totales.add(lblDescuento);
        totales.add(new JLabel("Total a pagar:"));
        totales.add(lblTotal);

        JButton confirmar = new JButton("Confirmar pedido");
        confirmar.addActionListener(e -> confirmar());
        JButton limpiar = new JButton("Limpiar");
        limpiar.addActionListener(e -> {
            carrito.clear();
            refrescarPedido();
        });
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.add(limpiar);
        botones.add(confirmar);

        JPanel sur = new JPanel(new BorderLayout());
        sur.add(totales, BorderLayout.CENTER);
        sur.add(botones, BorderLayout.SOUTH);
        sur.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(norte, BorderLayout.NORTH);
        panel.add(new JScrollPane(itemsTabla), BorderLayout.CENTER);
        panel.add(sur, BorderLayout.SOUTH);
        // el tipo de cliente cambia el descuento: se recalcula al elegirlo
        tipo.addActionListener(e -> refrescarPedido());
        return panel;
    }

    private Pedido pedidoActual() {
        return descuentos.aplicar(Pedido.nuevo(cliente.getText().trim(), (TipoCliente) tipo.getSelectedItem(), carrito));
    }

    private void refrescarPedido() {
        itemsModel.setRowCount(0);
        carrito.forEach(i -> itemsModel.addRow(new Object[]{i.producto().nombre(), i.cantidad(), PESOS.format(i.subtotal())}));
        if (carrito.isEmpty()) {
            lblSubtotal.setText(PESOS.format(0));
            lblDescuento.setText("Sin descuento");
            lblTotal.setText(PESOS.format(0));
            return;
        }
        Pedido p = pedidoActual();
        lblSubtotal.setText(PESOS.format(p.subtotal()));
        lblDescuento.setText(p.descuento() + " (-" + PESOS.format(p.montoDescuento()) + ")");
        lblTotal.setText(PESOS.format(p.total()));
    }

    private void confirmar() {
        if (cliente.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Escribe el nombre del cliente.", "Falta información", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (carrito.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agrega al menos un producto.", "Pedido vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Pedido p = pedidoActual();
            repo.guardar(p);
            carrito.clear();
            cliente.setText("");
            refrescarPedido();
            refrescarHistorial();
            JOptionPane.showMessageDialog(this, "Pedido guardado. Total: " + PESOS.format(p.total()));
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refrescarHistorial() {
        historialModel.setRowCount(0);
        try {
            repo.listar().forEach(p -> historialModel.addRow(new Object[]{
                    FECHA.format(p.fecha()), p.cliente(), p.tipo(), p.unidades(),
                    PESOS.format(p.subtotal()), p.descuento() + " (-" + PESOS.format(p.montoDescuento()) + ")",
                    PESOS.format(p.total())}));
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo leer el historial", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static DefaultTableModel modeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }
}
