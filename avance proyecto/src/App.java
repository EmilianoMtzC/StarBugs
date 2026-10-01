import HistorialCompras.Stack;
import InventarioRapido.HashNode;
import InventarioRapido.HashTable;
import ManejoClientes.Queue;
import Estructuras.Node;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
    private final Queue clientes = new Queue();
    private final Stack<String> historial = new Stack<>();
    private final HashTable<String, Integer> inventario = new HashTable<>(10);

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane();
        tabs.getTabs().add(crearTabClientes());
        tabs.getTabs().add(crearTabHistorial());
        tabs.getTabs().add(crearTabInventario());

        Scene scene = new Scene(tabs, 600, 400);
        stage.setTitle("Cafetería");
        stage.setScene(scene);
        stage.show();
    }

    private Tab crearTabClientes() {
        TextField nombre = new TextField();
        nombre.setPromptText("Nombre del cliente");
        TextArea resultado = crearAreaResultado();

        Button agregar = new Button("Agregar a la fila");
        agregar.setOnAction(event -> {
            if (nombre.getText().isBlank()) {
                resultado.setText("Escribe el nombre del cliente.");
                return;
            }
            clientes.enqueue(nombre.getText().trim());
            nombre.clear();
            resultado.setText(mostrarClientes());
        });

        Button atender = new Button("Atender cliente");
        atender.setOnAction(event -> {
            String atendido = clientes.dequeue();
            resultado.setText(atendido == null
                    ? "No hay clientes en espera."
                    : "Cliente atendido: " + atendido + "\n\n" + mostrarClientes());
        });

        Button mostrar = new Button("Mostrar fila");
        mostrar.setOnAction(event -> resultado.setText(mostrarClientes()));

        VBox contenido = crearContenido(
                new Label("Fila de clientes"), nombre,
                new HBox(10, agregar, atender, mostrar), resultado);
        return crearTab("Clientes", contenido);
    }

    private Tab crearTabHistorial() {
        TextField producto = new TextField();
        producto.setPromptText("Producto vendido");
        TextArea resultado = crearAreaResultado();

        Button agregar = new Button("Registrar compra");
        agregar.setOnAction(event -> {
            if (producto.getText().isBlank()) {
                resultado.setText("Escribe el nombre del producto.");
                return;
            }
            historial.push(producto.getText().trim());
            producto.clear();
            resultado.setText(mostrarHistorial());
        });

        Button eliminar = new Button("Eliminar última compra");
        eliminar.setOnAction(event -> {
            String eliminado = historial.pop();
            resultado.setText(eliminado == null
                    ? "No hay compras en el historial."
                    : "Compra eliminada: " + eliminado + "\n\n" + mostrarHistorial());
        });

        Button mostrar = new Button("Mostrar historial");
        mostrar.setOnAction(event -> resultado.setText(mostrarHistorial()));

        VBox contenido = crearContenido(
                new Label("Historial de compras"), producto,
                new HBox(10, agregar, eliminar, mostrar), resultado);
        return crearTab("Compras", contenido);
    }

    private Tab crearTabInventario() {
        TextField producto = new TextField();
        producto.setPromptText("Nombre o ID del producto");
        TextField cantidad = new TextField();
        cantidad.setPromptText("Cantidad");
        TextArea resultado = crearAreaResultado();

        Button guardar = new Button("Guardar producto");
        guardar.setOnAction(event -> {
            if (producto.getText().isBlank() || cantidad.getText().isBlank()) {
                resultado.setText("Escribe el producto y la cantidad.");
                return;
            }
            try {
                int valor = Integer.parseInt(cantidad.getText().trim());
                if (valor < 0) {
                    resultado.setText("La cantidad no puede ser negativa.");
                    return;
                }
                inventario.put(producto.getText().trim(), valor);
                producto.clear();
                cantidad.clear();
                resultado.setText(mostrarInventario());
            } catch (NumberFormatException exception) {
                resultado.setText("La cantidad debe ser un número entero.");
            }
        });

        Button buscar = new Button("Buscar producto");
        buscar.setOnAction(event -> {
            if (producto.getText().isBlank()) {
                resultado.setText("Escribe el producto que deseas buscar.");
                return;
            }
            Integer valor = inventario.getV(producto.getText().trim());
            resultado.setText(valor == null
                    ? "Producto no encontrado."
                    : producto.getText().trim() + ": " + valor + " unidades");
        });

        Button mostrar = new Button("Mostrar inventario");
        mostrar.setOnAction(event -> resultado.setText(mostrarInventario()));

        VBox contenido = crearContenido(
                new Label("Inventario"), producto, cantidad,
                new HBox(10, guardar, buscar, mostrar), resultado);
        return crearTab("Inventario", contenido);
    }

    private String mostrarClientes() {
        if (clientes.isEmpty()) return "No hay clientes en espera.";

        StringBuilder texto = new StringBuilder("Clientes en espera:\n");
        Node<String> actual = clientes.front;
        int posicion = 1;
        while (actual != null) {
            texto.append(posicion++).append(". ").append(actual.data).append("\n");
            actual = actual.next;
        }
        return texto.toString();
    }

    private String mostrarHistorial() {
        if (historial.isEnd()) return "No hay compras en el historial.";

        StringBuilder texto = new StringBuilder("Compras más recientes:\n");
        Node<String> actual = historial.top;
        while (actual != null) {
            texto.append("• ").append(actual.data).append("\n");
            actual = actual.next;
        }
        return texto.toString();
    }

    private String mostrarInventario() {
        if (inventario.size == 0) return "El inventario está vacío.";

        StringBuilder texto = new StringBuilder("Productos en inventario:\n");
        for (HashNode<String, Integer> nodo : inventario.bucket) {
            while (nodo != null) {
                texto.append("• ").append(nodo.key).append(": ")
                        .append(nodo.value).append(" unidades\n");
                nodo = nodo.next;
            }
        }
        return texto.toString();
    }

    private TextArea crearAreaResultado() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(10);
        return area;
    }

    private VBox crearContenido(javafx.scene.Node... elementos) {
        VBox contenido = new VBox(12, elementos);
        contenido.setPadding(new Insets(20));
        return contenido;
    }

    private Tab crearTab(String titulo, VBox contenido) {
        Tab tab = new Tab(titulo, contenido);
        tab.setClosable(false);
        return tab;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
