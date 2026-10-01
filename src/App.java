import GestionTareas.GestorTareas;
import GestionTareas.Algoritmos.DistribuidorTareas;
import GestionTareas.Algoritmos.EstadisticasTareas;
import GestionTareas.Entidades.Empleado;
import GestionTareas.Entidades.EstadoTarea;
import GestionTareas.Entidades.Tarea;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.ArrayList;

/** Interfaz JavaFX del sistema de gestión de tareas. */
public class App extends Application {
    private final GestorTareas gestor = new GestorTareas();
    private Runnable actualizarResumenPanel = () -> { };
    private final List<ComboBox<String>> selectoresComanda = new ArrayList<>();
    private final List<ComboBox<String>> selectoresBarista = new ArrayList<>();

    /** Construye la ventana principal, la navegación lateral y carga la primera vista. */
    @Override
    public void start(Stage stage) {
        List<Tab> vistas = List.of(
                crearTabAsignacionYEstadisticas(),
                crearTabTareas(),
                crearTabEmpleados(),
                crearTabDependencias(),
                crearTabConsultas());
        List<String> nombres = List.of(
                "Panel de barra", "Comandas y tareas", "Equipo de baristas",
                "Preparación previa", "Consulta rápida");

        BorderPane raiz = new BorderPane();
        raiz.getStyleClass().add("app-cafeteria");
        ScrollPane areaContenido = contenedorDesplazable(vistas.getFirst().getContent());
        areaContenido.getStyleClass().add("area-contenido");

        Label tituloVista = new Label(nombres.getFirst());
        tituloVista.getStyleClass().add("titulo-vista");
        Label subtitulo = new Label("Operación diaria · StarBugs coffeehouse");
        subtitulo.getStyleClass().add("subtitulo-vista");
        VBox titulos = new VBox(2, tituloVista, subtitulo);
        Region separador = new Region();
        HBox.setHgrow(separador, Priority.ALWAYS);
        Label turno = new Label("● Turno activo");
        turno.getStyleClass().add("indicador-turno");
        ToggleButton tema = new ToggleButton("☀ Modo claro");
        tema.getStyleClass().add("boton-tema");
        tema.setTooltip(new Tooltip("Alternar entre modo claro y modo oscuro"));
        tema.setAccessibleText("Activar modo claro");
        HBox encabezado = new HBox(16, titulos, separador, tema, turno);
        encabezado.getStyleClass().add("encabezado-cafeteria");

        Label marca = new Label("StarBugs");
        marca.getStyleClass().add("marca-cafeteria");
        Label descripcion = new Label("Coffeehouse operations");
        descripcion.getStyleClass().add("descripcion-marca");
        VBox navegacion = new VBox(8, marca, descripcion);
        navegacion.getStyleClass().add("barra-lateral");

        List<Button> botones = new ArrayList<>();
        for (int indice = 0; indice < vistas.size(); indice++) {
            Tab vista = vistas.get(indice);
            String nombre = nombres.get(indice);
            Button boton = new Button(nombre);
            boton.setMaxWidth(Double.MAX_VALUE);
            boton.setContentDisplay(ContentDisplay.LEFT);
            boton.getStyleClass().add("boton-navegacion");
            if (indice == 0) boton.getStyleClass().add("activo");
            boton.setOnAction(evento -> {
                areaContenido.setContent(vista.getContent());
                tituloVista.setText(nombre);
                botones.forEach(actual -> actual.getStyleClass().remove("activo"));
                boton.getStyleClass().add("activo");
            });
            botones.add(boton);
            navegacion.getChildren().add(boton);
        }

        Region espacioFinal = new Region();
        VBox.setVgrow(espacioFinal, Priority.ALWAYS);
        Label nota = new Label("StarBugs coffeehouse\nTurnos y equipo");
        nota.getStyleClass().add("nota-lateral");
        navegacion.getChildren().addAll(espacioFinal, nota);

        raiz.setLeft(navegacion);
        raiz.setTop(encabezado);
        raiz.setCenter(areaContenido);

        Scene scene = new Scene(raiz, 1100, 700);
        scene.getStylesheets().add(
                App.class.getResource("/styles/styles.css").toExternalForm()
        );
        String temaClaro = App.class.getResource("/styles/light.css").toExternalForm();
        tema.setOnAction(evento -> {
            if (tema.isSelected()) {
                scene.getStylesheets().add(temaClaro);
            } else {
                scene.getStylesheets().remove(temaClaro);
            }
            tema.setText(tema.isSelected() ? "☾ Modo oscuro" : "☀ Modo claro");
            tema.setAccessibleText(tema.isSelected() ? "Activar modo oscuro" : "Activar modo claro");
        });
        stage.setTitle("Gestor de tareas");
        stage.setScene(scene);
        stage.show();
    }

    /** Crea la pantalla para registrar, actualizar, filtrar y consultar comandas. */
    private Tab crearTabTareas() {
        Label id = new Label("Comanda " + gestor.siguienteIdComanda());
        id.getStyleClass().add("codigo-comanda");
        ComboBox<String> titulo = selectorPreparacion();
        ComboBox<String> proyecto = selectorEstacion();
        ComboBox<String> departamento = selectorAreaResponsable();
        ComboBox<Integer> urgencia = selectorPrioridad();
        DatePicker fecha = new DatePicker();
        fecha.setPromptText("Fecha de entrega");
        TextField horas = campo("Tiempo estimado (minutos)");
        ComboBox<EstadoTarea> estado = selectorEstado();
        ComboBox<String> idEstado = selectorComanda("Selecciona la comanda a actualizar");
        ComboBox<EstadoTarea> nuevoEstado = selectorEstado();
        ComboBox<String> filtro = selectorFiltroComandas();
        TextArea resultado = crearAreaResultado();
        resultado.setPrefRowCount(8);

        Button registrar = new Button("Registrar comanda");
        registrar.setOnAction(evento -> {
            try {
                Tarea tarea = gestor.registrarComanda(titulo.getValue(), proyecto.getValue(),
                        departamento.getValue(), leerPrioridad(urgencia), fecha.getValue(),
                        leerMinutos(horas), estado.getValue());
                actualizarResumenPanel.run();
                refrescarSelectores();
                limpiar(horas);
                titulo.setValue(null);
                proyecto.setValue(null);
                departamento.setValue(null);
                urgencia.setValue(null);
                fecha.setValue(null);
                id.setText("Comanda " + gestor.siguienteIdComanda());
                resultado.setText("Comanda registrada.\n\n" + mostrarTareas(gestor.tareasPorPrioridad()));
            } catch (IllegalArgumentException excepcion) {
                resultado.setText(excepcion.getMessage());
            }
        });

        Button verComandas = new Button("Ver comandas");
        verComandas.setOnAction(evento -> resultado.setText(mostrarTareas(
                gestor.tareasPorEstado(estadoDesdeFiltro(filtro.getValue())))));

        Button verSiguiente = new Button("Ver siguiente comanda");
        verSiguiente.setOnAction(evento -> resultado.setText(mostrarBusqueda(
                gestor.siguienteTareaPrioritaria(), "cola de prioridad")));

        Button actualizarEstado = new Button("Actualizar estado");
        actualizarEstado.setOnAction(evento -> {
            try {
                gestor.actualizarEstadoTarea(leerSeleccion(idEstado, "la comanda"), nuevoEstado.getValue());
                actualizarResumenPanel.run();
                resultado.setText("Estado actualizado.\n\n" + mostrarTareas(gestor.tareasPorPrioridad()));
            } catch (IllegalArgumentException excepcion) {
                resultado.setText(excepcion.getMessage());
            }
        });

        Label tituloFormulario = new Label("Nueva comanda");
        tituloFormulario.getStyleClass().add("encabezado-seccion");
        Label descripcionFormulario = new Label("Registra la preparación y asígnala a su estación de trabajo.");
        descripcionFormulario.getStyleClass().add("texto-suave");
        VBox textosFormulario = new VBox(3, tituloFormulario, descripcionFormulario);
        Region espacioFormulario = new Region();
        HBox.setHgrow(espacioFormulario, Priority.ALWAYS);
        HBox encabezadoFormulario = new HBox(12, textosFormulario, espacioFormulario, id);
        encabezadoFormulario.getStyleClass().add("encabezado-formulario");

        GridPane campos = new GridPane();
        campos.setHgap(14);
        campos.setVgap(13);
        ColumnConstraints primeraColumna = new ColumnConstraints();
        primeraColumna.setPercentWidth(50);
        ColumnConstraints segundaColumna = new ColumnConstraints();
        segundaColumna.setPercentWidth(50);
        campos.getColumnConstraints().addAll(primeraColumna, segundaColumna);
        campos.add(campoFormulario("Bebida o preparación", titulo), 0, 0, 2, 1);
        campos.add(campoFormulario("Estación de trabajo", proyecto), 0, 1);
        campos.add(campoFormulario("Área responsable", departamento), 1, 1);
        campos.add(campoFormulario("Prioridad", urgencia), 0, 2);
        campos.add(campoFormulario("Fecha de entrega", fecha), 1, 2);
        campos.add(campoFormulario("Tiempo de preparación", horas), 0, 3);
        campos.add(campoFormulario("Estado inicial", estado), 1, 3);

        HBox accionesRegistro = new HBox(10, registrar);
        accionesRegistro.getStyleClass().add("acciones-formulario");
        VBox formulario = new VBox(18, encabezadoFormulario, campos, accionesRegistro);
        formulario.getStyleClass().add("tarjeta");
        formulario.getStyleClass().add("formulario-comanda");

        Label tituloActualizacion = new Label("Actualizar estado");
        tituloActualizacion.getStyleClass().add("encabezado-seccion");
        Label descripcionActualizacion = new Label("Selecciona una comanda existente y actualiza su avance.");
        descripcionActualizacion.getStyleClass().add("texto-suave");
        HBox controlesActualizacion = new HBox(10, idEstado, nuevoEstado, actualizarEstado);
        VBox actualizacion = new VBox(10, tituloActualizacion, descripcionActualizacion, controlesActualizacion);
        actualizacion.getStyleClass().add("tarjeta");
        actualizacion.getStyleClass().add("panel-actualizacion");

        Label tituloListado = new Label("Comandas del turno");
        tituloListado.getStyleClass().add("encabezado-seccion");
        Label descripcionListado = new Label("Filtra el listado por estado o consulta la siguiente preparación prioritaria.");
        descripcionListado.getStyleClass().add("texto-suave");
        HBox controlesListado = new HBox(10, filtro, verComandas, verSiguiente);
        VBox listado = new VBox(12, tituloListado, descripcionListado, controlesListado, resultado);
        listado.getStyleClass().add("tarjeta");
        listado.getStyleClass().add("panel-listado");

        VBox contenido = crearContenido(formulario, actualizacion, listado);
        contenido.getStyleClass().add("pagina-comandas");
        return crearTab("Tareas", contenido);
    }

    /** Agrupa una etiqueta y su control para mantener consistencia en los formularios. */
    private VBox campoFormulario(String etiqueta, Node control) {
        Label titulo = new Label(etiqueta);
        titulo.getStyleClass().add("etiqueta-campo");
        if (control instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        VBox campo = new VBox(6, titulo, control);
        campo.getStyleClass().add("campo-formulario");
        GridPane.setHgrow(campo, Priority.ALWAYS);
        return campo;
    }

    /** Configura una cuadrícula reutilizable de dos columnas del mismo ancho. */
    private GridPane cuadriculaDosColumnas() {
        GridPane campos = new GridPane();
        campos.setHgap(14);
        campos.setVgap(13);
        ColumnConstraints primeraColumna = new ColumnConstraints();
        primeraColumna.setPercentWidth(50);
        ColumnConstraints segundaColumna = new ColumnConstraints();
        segundaColumna.setPercentWidth(50);
        campos.getColumnConstraints().addAll(primeraColumna, segundaColumna);
        return campos;
    }

    /** Arma una tarjeta con título, descripción y controles para las pantallas secundarias. */
    private VBox tarjetaMinimalista(String titulo, String descripcion, Node... contenido) {
        VBox tarjeta = new VBox(16);
        tarjeta.getChildren().add(tituloSeccion(titulo, descripcion));
        tarjeta.getChildren().addAll(contenido);
        tarjeta.getStyleClass().add("tarjeta");
        tarjeta.getStyleClass().add("formulario-minimalista");
        return tarjeta;
    }

    /** Crea una tarjeta destinada a mostrar resultados de consultas o algoritmos. */
    private VBox tarjetaResultado(String titulo, String descripcion, TextArea resultado) {
        VBox tarjeta = new VBox(12, tituloSeccion(titulo, descripcion), resultado);
        tarjeta.getStyleClass().add("tarjeta");
        tarjeta.getStyleClass().add("panel-resultados-minimalista");
        return tarjeta;
    }

    /** Crea la pantalla de alta y consulta de baristas por folio o estación. */
    private Tab crearTabEmpleados() {
        Label id = new Label("Barista " + gestor.siguienteIdBarista());
        id.getStyleClass().add("codigo-comanda");
        TextField nombre = campo("Nombre del barista");
        ComboBox<String> departamento = selectorEstacion();
        TextField capacidad = campo("Horas de trabajo semanales (40)");
        ComboBox<String> consultaId = selectorBarista("Selecciona el barista a consultar");
        ComboBox<String> consultaDepartamento = selectorEstacion();
        TextArea resultado = crearAreaResultado();
        resultado.setPrefRowCount(8);

        Button registrar = new Button("Registrar barista");
        registrar.setOnAction(evento -> {
            try {
                gestor.registrarBarista(nombre.getText(), departamento.getValue(), leerHorasTrabajoSemanales(capacidad));
                actualizarResumenPanel.run();
                refrescarSelectores();
                limpiar(nombre, capacidad);
                departamento.setValue(null);
                id.setText("Barista " + gestor.siguienteIdBarista());
                resultado.setText("Barista registrado.\n\n" + mostrarEmpleados(gestor.empleadosEnOrden()));
            } catch (IllegalArgumentException excepcion) {
                resultado.setText(excepcion.getMessage());
            }
        });

        Button buscarDepartamento = new Button("Buscar por estación");
        buscarDepartamento.setOnAction(evento -> {
            try {
                resultado.setText(mostrarEmpleados(gestor.empleadosDepartamento(
                        leerSeleccion(consultaDepartamento, "la estación"))));
            } catch (IllegalArgumentException excepcion) {
                resultado.setText(excepcion.getMessage());
            }
        });

        Button buscarEmpleado = new Button("Buscar barista");
        buscarEmpleado.setOnAction(evento -> {
            try {
                Empleado empleado = gestor.buscarEmpleadoPorHash(leerSeleccion(consultaId, "el barista"));
                resultado.setText(empleado == null ? "No se encontró el barista." : empleado.toString());
            } catch (IllegalArgumentException excepcion) {
                resultado.setText(excepcion.getMessage());
            }
        });

        Button verArbol = new Button("Ver estaciones");
        verArbol.setOnAction(evento -> resultado.setText("Estaciones de trabajo:\n"
                + formatear(gestor.departamentosEnOrden())));

        GridPane camposRegistro = cuadriculaDosColumnas();
        camposRegistro.add(campoFormulario("Nombre del barista", nombre), 0, 0, 2, 1);
        camposRegistro.add(campoFormulario("Estación de trabajo", departamento), 0, 1);
        camposRegistro.add(campoFormulario("Horas semanales", capacidad), 1, 1);
        Region espacioRegistro = new Region();
        HBox.setHgrow(espacioRegistro, Priority.ALWAYS);
        HBox folioRegistro = new HBox(10, espacioRegistro, id);
        VBox registro = tarjetaMinimalista("Nuevo barista",
                "Incorpora al equipo y define su estación de trabajo.", folioRegistro, camposRegistro, registrar);

        GridPane camposConsulta = cuadriculaDosColumnas();
        camposConsulta.add(campoFormulario("Buscar por barista", consultaId), 0, 0);
        camposConsulta.add(campoFormulario("Buscar por estación", consultaDepartamento), 1, 0);
        HBox accionesConsulta = new HBox(10, buscarEmpleado, buscarDepartamento, verArbol);
        VBox consultas = tarjetaMinimalista("Consultar equipo",
                "Localiza al equipo por folio o por estación.", camposConsulta, accionesConsulta);
        VBox resultadoEquipo = tarjetaResultado("Resultado del equipo",
                "Aquí se mostrarán los baristas y estaciones consultadas.", resultado);

        VBox contenido = crearContenido(registro, consultas, resultadoEquipo);
        contenido.getStyleClass().add("pagina-minimalista");
        return crearTab("Empleados", contenido);
    }

    /** Crea la pantalla que registra prerrequisitos y muestra el orden de preparación. */
    private Tab crearTabDependencias() {
        ComboBox<String> tarea = selectorComanda("Selecciona la comanda dependiente");
        ComboBox<String> prerequisito = selectorComanda("Selecciona la preparación previa");
        ComboBox<String> consulta = selectorComanda("Selecciona la comanda a consultar");
        TextArea resultado = crearAreaResultado();
        resultado.setPrefRowCount(8);

        Button agregar = new Button("Agregar preparación previa");
        agregar.setOnAction(evento -> {
            try {
                String idTarea = leerSeleccion(tarea, "la comanda dependiente");
                String idPrerequisito = leerSeleccion(prerequisito, "la preparación previa");
                gestor.agregarDependencia(idTarea, idPrerequisito);
                resultado.setText("Preparación previa agregada: " + idTarea + " requiere " + idPrerequisito);
                tarea.setValue(null);
                prerequisito.setValue(null);
            } catch (IllegalArgumentException excepcion) {
                resultado.setText(excepcion.getMessage());
            }
        });

        Button verDependencias = new Button("Ver preparación previa");
        verDependencias.setOnAction(evento -> {
            try {
                String idComanda = leerSeleccion(consulta, "la comanda");
                resultado.setText("Preparaciones previas de " + idComanda + ":\n"
                        + formatear(gestor.dependenciasDe(idComanda)));
            } catch (IllegalArgumentException excepcion) {
                resultado.setText(excepcion.getMessage());
            }
        });

        Button orden = new Button("Ver orden de preparación");
        orden.setOnAction(evento -> resultado.setText("Orden recomendado de preparación:\n"
                + formatear(gestor.ordenDeEjecucion())));

        GridPane camposPreparacion = cuadriculaDosColumnas();
        camposPreparacion.add(campoFormulario("Comanda dependiente", tarea), 0, 0);
        camposPreparacion.add(campoFormulario("Preparación previa", prerequisito), 1, 0);
        VBox preparacion = tarjetaMinimalista("Preparación previa",
                "Define qué comanda debe estar lista antes de iniciar otra.", camposPreparacion, agregar);

        HBox accionesOrden = new HBox(10, consulta, verDependencias, orden);
        VBox ordenPreparacion = tarjetaMinimalista("Orden de preparación",
                "Consulta los prerrequisitos y el orden recomendado de la barra.", accionesOrden);
        VBox resultadoDependencias = tarjetaResultado("Resultado de preparación",
                "Aquí aparecerán las dependencias y el orden de preparación.", resultado);

        VBox contenido = crearContenido(preparacion, ordenPreparacion, resultadoDependencias);
        contenido.getStyleClass().add("pagina-minimalista");
        return crearTab("Dependencias", contenido);
    }

    /** Crea la pantalla de búsquedas hash, búsqueda binaria y ordenamientos. */
    private Tab crearTabConsultas() {
        ComboBox<String> id = selectorComanda("Selecciona la comanda a buscar");
        TextArea resultado = crearAreaResultado();
        resultado.setPrefRowCount(8);

        Button buscarHash = new Button("Buscar comanda");
        buscarHash.setOnAction(evento -> {
            try {
                resultado.setText(mostrarBusqueda(gestor.buscarTareaPorHash(leerSeleccion(id, "la comanda")),
                        "tabla hash"));
            } catch (IllegalArgumentException excepcion) {
                resultado.setText(excepcion.getMessage());
            }
        });

        Button buscarBinaria = new Button("Buscar por número");
        buscarBinaria.setOnAction(evento -> {
            try {
                resultado.setText(mostrarBusqueda(gestor.buscarTareaBinaria(leerSeleccion(id, "la comanda")),
                        "búsqueda binaria"));
            } catch (IllegalArgumentException excepcion) {
                resultado.setText(excepcion.getMessage());
            }
        });

        Button ordenarPrioridad = new Button("Ordenar comandas por prioridad");
        ordenarPrioridad.setOnAction(evento -> resultado.setText(mostrarTareas(gestor.tareasPorPrioridad())));

        Button ordenarFecha = new Button("Ordenar por entrega");
        ordenarFecha.setOnAction(evento -> resultado.setText(mostrarTareas(gestor.tareasPorFecha())));

        GridPane campoBusqueda = cuadriculaDosColumnas();
        campoBusqueda.add(campoFormulario("Comanda", id), 0, 0, 2, 1);
        HBox accionesBusqueda = new HBox(10, buscarHash, buscarBinaria);
        HBox accionesOrdenamiento = new HBox(10, ordenarPrioridad, ordenarFecha);
        VBox consulta = tarjetaMinimalista("Consulta rápida",
                "Busca una comanda o explora el turno por prioridad y entrega.",
                campoBusqueda, accionesBusqueda, accionesOrdenamiento);
        VBox resultadoConsultas = tarjetaResultado("Resultado de consulta",
                "Aquí se mostrarán los detalles de la comanda seleccionada.", resultado);

        VBox contenido = crearContenido(consulta, resultadoConsultas);
        contenido.getStyleClass().add("pagina-minimalista");
        return crearTab("Consultas", contenido);
    }

    /** Crea el panel operativo con métricas, reparto de comandas y carga del equipo. */
    private Tab crearTabAsignacionYEstadisticas() {
        TextArea resultado = crearAreaResultado();
        resultado.setPrefRowCount(8);
        resultado.setText("Aún no hay comandas registradas para este turno.\n"
                + "Registra tareas y baristas para ver las recomendaciones de asignación.");

        Label total = new Label("0");
        Label pendientes = new Label("0");
        Label sinAsignar = new Label("0");
        Label baristasActivos = new Label("0");
        VBox tarjetaTotal = tarjetaMetrica("COMANDAS REGISTRADAS", total, "Actividades del turno");
        VBox tarjetaPendientes = tarjetaMetrica("PENDIENTES", pendientes, "Por preparar o iniciar");
        VBox tarjetaSinAsignar = tarjetaMetrica("SIN BARISTA", sinAsignar, "Requieren asignación");
        VBox tarjetaCarga = tarjetaMetrica("BARISTAS ACTIVOS", baristasActivos, "Disponibles para asignación");
        HBox tarjetas = new HBox(14, tarjetaTotal, tarjetaPendientes, tarjetaSinAsignar, tarjetaCarga);
        for (VBox tarjeta : List.of(tarjetaTotal, tarjetaPendientes, tarjetaSinAsignar, tarjetaCarga)) {
            tarjeta.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(tarjeta, Priority.ALWAYS);
        }

        ProgressBar barraCarga = new ProgressBar(0);
        barraCarga.setMaxWidth(Double.MAX_VALUE);
        barraCarga.getStyleClass().add("barra-capacidad");
        Label detalleCarga = new Label("Aún no hay baristas registrados.");
        detalleCarga.getStyleClass().add("texto-suave");
        VBox capacidadEquipo = new VBox(10,
                tituloSeccion("Capacidad del equipo", "Carga estimada del turno"),
                barraCarga, detalleCarga);
        capacidadEquipo.getStyleClass().add("tarjeta");
        capacidadEquipo.getStyleClass().add("tarjeta-capacidad");

        VBox tituloAsignaciones = tituloSeccion("Recomendaciones de asignación",
                "Se prioriza el departamento y la menor carga disponible");
        VBox asignaciones = new VBox(12, tituloAsignaciones, resultado);
        asignaciones.getStyleClass().add("tarjeta");
        asignaciones.getStyleClass().add("tarjeta-asignaciones");
        HBox panelInferior = new HBox(14, asignaciones, capacidadEquipo);
        HBox.setHgrow(asignaciones, Priority.ALWAYS);
        asignaciones.setMaxWidth(Double.MAX_VALUE);
        capacidadEquipo.setPrefWidth(260);

        actualizarResumenPanel = () -> {
            EstadisticasTareas.Resumen resumen = gestor.estadisticas();
            total.setText(String.valueOf(resumen.getCantidad()));
            pendientes.setText(String.valueOf(resumen.getPendientes()));
            sinAsignar.setText(String.valueOf(resumen.getSinAsignar()));
            int minutosAsignados = 0;
            int capacidadTotal = 0;
            for (Empleado empleado : gestor.empleadosEnOrden()) {
                minutosAsignados += empleado.getCargaEstimadaMinutos();
                capacidadTotal += empleado.getCapacidadSemanalMinutos();
            }
            double proporcion = capacidadTotal == 0 ? 0 : (double) minutosAsignados / capacidadTotal;
            barraCarga.setProgress(Math.min(1, proporcion));
            baristasActivos.setText(String.valueOf(gestor.empleadosEnOrden().size()));
            detalleCarga.setText(capacidadTotal == 0
                    ? "Aún no hay baristas registrados."
                    : minutosAsignados + " min asignados de " + capacidadTotal + " min disponibles ("
                            + capacidadTotal / 60 + " h semanales).");
        };

        List<Button> botonesAccion = new ArrayList<>();

        Button distribuir = new Button("Asignar comandas pendientes");
        distribuir.getStyleClass().add("boton-miel");
        distribuir.getStyleClass().add("boton-accion");
        botonesAccion.add(distribuir);
        distribuir.setOnAction(evento -> {
            seleccionarAccion(distribuir, botonesAccion);
            List<DistribuidorTareas.Resultado> resultados = gestor.distribuirPendientes();
            resultado.setText(resultados.isEmpty()
                    ? "No hay comandas pendientes por asignar."
                    : "Distribución recursiva (divide y vencerás):\n" + formatear(resultados));
            actualizarResumenPanel.run();
        });

        Button estadisticas = new Button("Calcular estadísticas recursivas");
        estadisticas.getStyleClass().add("boton-secundario");
        estadisticas.getStyleClass().add("boton-accion");
        botonesAccion.add(estadisticas);
        estadisticas.setOnAction(evento -> {
            seleccionarAccion(estadisticas, botonesAccion);
            EstadisticasTareas.Resumen resumen = gestor.estadisticas();
            resultado.setText("Comandas registradas: " + resumen.getCantidad()
                    + "\nMinutos estimados totales: " + resumen.getMinutosTotales() + " min"
                    + "\nPromedio por comanda: %.1f min".formatted(resumen.getPromedioMinutos())
                    + "\nPendientes: " + resumen.getPendientes()
                    + " | En curso: " + resumen.getEnCurso()
                    + " | Completadas: " + resumen.getCompletadas()
                    + "\nSin asignar: " + resumen.getSinAsignar());
            actualizarResumenPanel.run();
        });

        Button cargas = new Button("Ver carga del equipo");
        cargas.getStyleClass().add("boton-secundario");
        cargas.getStyleClass().add("boton-accion");
        botonesAccion.add(cargas);
        cargas.setOnAction(evento -> {
            seleccionarAccion(cargas, botonesAccion);
            resultado.setText(mostrarEmpleados(gestor.empleadosEnOrden()));
        });

        Label bienvenida = new Label("Operación del turno");
        bienvenida.getStyleClass().add("titulo-pagina");
        Label descripcion = new Label("Organiza comandas, asigna al equipo y cuida la capacidad de la barra.");
        descripcion.getStyleClass().add("texto-suave");
        HBox acciones = new HBox(10, distribuir, estadisticas, cargas);
        VBox contenido = crearContenido(bienvenida, descripcion, tarjetas, acciones, panelInferior);
        contenido.getStyleClass().add("panel-operacion");
        actualizarResumenPanel.run();
        return crearTab("Asignación y estadísticas", contenido);
    }

    /** Da formato uniforme a una métrica visible en el panel de barra. */
    private VBox tarjetaMetrica(String etiqueta, Label valor, String detalle) {
        valor.getStyleClass().add("valor-metrica");
        Label titulo = new Label(etiqueta);
        titulo.getStyleClass().add("etiqueta-metrica");
        Label descripcion = new Label(detalle);
        descripcion.getStyleClass().add("detalle-metrica");
        VBox tarjeta = new VBox(7, titulo, valor, descripcion);
        tarjeta.getStyleClass().add("tarjeta");
        tarjeta.getStyleClass().add("tarjeta-metrica");
        return tarjeta;
    }

    /** Mantiene resaltada únicamente la última acción elegida en el panel. */
    private void seleccionarAccion(Button seleccionado, List<Button> botones) {
        for (Button boton : botones) boton.getStyleClass().remove("activo");
        seleccionado.getStyleClass().add("activo");
    }

    /** Construye el encabezado reutilizable de una sección de la interfaz. */
    private VBox tituloSeccion(String titulo, String descripcion) {
        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("encabezado-seccion");
        Label subtitulo = new Label(descripcion);
        subtitulo.getStyleClass().add("texto-suave");
        return new VBox(3, encabezado, subtitulo);
    }

    /** Convierte el resultado de una búsqueda en un mensaje claro para el usuario. */
    private String mostrarBusqueda(Tarea tarea, String metodo) {
        return tarea == null ? "No se encontró la comanda mediante " + metodo + "." : tarea.toString();
    }

    /** Formatea una lista de comandas, incluyendo el caso en que no hay resultados. */
    private String mostrarTareas(List<Tarea> tareas) {
        return tareas.isEmpty() ? "No hay comandas registradas." : "Comandas:\n" + formatear(tareas);
    }

    /** Formatea una lista de baristas para el área de resultados. */
    private String mostrarEmpleados(List<Empleado> empleados) {
        return empleados.isEmpty() ? "No se encontraron baristas." : "Equipo de baristas:\n" + formatear(empleados);
    }

    /** Une los elementos en líneas y comunica cuando una consulta no encontró datos. */
    private String formatear(List<?> elementos) {
        if (elementos.isEmpty()) return "Sin resultados.";
        StringBuilder texto = new StringBuilder();
        for (Object elemento : elementos) texto.append("• ").append(elemento).append('\n');
        return texto.toString();
    }

    /** Lee un número entero del formulario y reporta un mensaje útil si no es válido. */
    private int leerEntero(TextField campo, String nombreCampo) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException excepcion) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " debe ser un número entero");
        }
    }

    /** Valida que el tiempo de preparación sea un número positivo de minutos. */
    private int leerMinutos(TextField campo) {
        try {
            int minutos = Integer.parseInt(campo.getText().trim());
            if (minutos <= 0) throw new NumberFormatException();
            return minutos;
        } catch (NumberFormatException excepcion) {
            throw new IllegalArgumentException("El tiempo estimado debe ser un número entero de minutos mayor que cero");
        }
    }

    /** Convierte las horas semanales capturadas a minutos para el modelo interno. */
    private int leerHorasTrabajoSemanales(TextField campo) {
        int horas = campo.getText().isBlank() ? 40 : leerEntero(campo, "horas de trabajo semanales");
        if (horas <= 0) {
            throw new IllegalArgumentException("Las horas de trabajo semanales deben ser mayores que cero");
        }
        return horas * 60;
    }

    /** Crea el selector con los estados permitidos para una comanda. */
    private ComboBox<EstadoTarea> selectorEstado() {
        ComboBox<EstadoTarea> selector = new ComboBox<>();
        selector.getItems().setAll(EstadoTarea.values());
        selector.setValue(EstadoTarea.PENDIENTE);
        return selector;
    }

    /** Crea el catálogo de bebidas y preparaciones frecuentes de la cafetería. */
    private ComboBox<String> selectorPreparacion() {
        ComboBox<String> selector = new ComboBox<>();
        selector.getItems().setAll("Espresso", "Americano", "Cappuccino", "Latte", "Croissant");
        selector.setPromptText("Selecciona bebida o preparación");
        return selector;
    }

    /** Crea el selector de estaciones de trabajo predefinidas. */
    private ComboBox<String> selectorEstacion() {
        ComboBox<String> selector = new ComboBox<>();
        selector.getItems().setAll("Barra de café", "Cocina", "Repostería", "Caja", "Servicio en mesa");
        selector.setPromptText("Selecciona la estación de trabajo");
        return selector;
    }

    /** Crea el selector de áreas que pueden responsabilizarse de una preparación. */
    private ComboBox<String> selectorAreaResponsable() {
        ComboBox<String> selector = new ComboBox<>();
        selector.getItems().setAll("Barra de café", "Cocina", "Repostería", "Caja", "Servicio en mesa");
        selector.setPromptText("Selecciona el área responsable");
        return selector;
    }

    /** Crea el selector de prioridad, donde cinco es el nivel más urgente. */
    private ComboBox<Integer> selectorPrioridad() {
        ComboBox<Integer> selector = new ComboBox<>();
        selector.getItems().setAll(1, 2, 3, 4, 5);
        selector.setPromptText("Selecciona la prioridad");
        return selector;
    }

    /** Crea las opciones para ver todas las comandas o filtrarlas por estado. */
    private ComboBox<String> selectorFiltroComandas() {
        ComboBox<String> selector = new ComboBox<>();
        selector.getItems().setAll("Todas las comandas", "Pendientes", "Completadas");
        selector.setValue("Todas las comandas");
        return selector;
    }

    /** Traduce la etiqueta elegida en el filtro al estado usado por el dominio. */
    private EstadoTarea estadoDesdeFiltro(String filtro) {
        if ("Pendientes".equals(filtro)) return EstadoTarea.PENDIENTE;
        if ("Completadas".equals(filtro)) return EstadoTarea.COMPLETADA;
        return null;
    }

    /** Crea y registra un selector que siempre se actualizará con las nuevas comandas. */
    private ComboBox<String> selectorComanda(String instruccion) {
        ComboBox<String> selector = new ComboBox<>();
        selector.setPromptText(instruccion);
        selectoresComanda.add(selector);
        refrescarSelectores();
        return selector;
    }

    /** Crea y registra un selector que siempre se actualizará con los nuevos baristas. */
    private ComboBox<String> selectorBarista(String instruccion) {
        ComboBox<String> selector = new ComboBox<>();
        selector.setPromptText(instruccion);
        selectoresBarista.add(selector);
        refrescarSelectores();
        return selector;
    }

    /** Sincroniza los ComboBox con los folios que existen actualmente en el gestor. */
    private void refrescarSelectores() {
        for (ComboBox<String> selector : selectoresComanda) {
            selector.getItems().setAll(gestor.idsComandas());
        }
        for (ComboBox<String> selector : selectoresBarista) {
            selector.getItems().setAll(gestor.idsBaristas());
        }
    }

    /** Obtiene una selección obligatoria y evita enviar valores vacíos al gestor. */
    private String leerSeleccion(ComboBox<String> selector, String nombre) {
        if (selector.getValue() == null) {
            throw new IllegalArgumentException("Selecciona " + nombre);
        }
        return selector.getValue();
    }

    /** Recupera la prioridad seleccionada y exige que el usuario elija una opción. */
    private int leerPrioridad(ComboBox<Integer> selector) {
        if (selector.getValue() == null) {
            throw new IllegalArgumentException("Selecciona la prioridad de la comanda");
        }
        return selector.getValue();
    }

    /** Crea un campo de texto con el mensaje guía recibido. */
    private TextField campo(String texto) {
        TextField campo = new TextField();
        campo.setPromptText(texto);
        return campo;
    }

    /** Limpia los campos indicados después de registrar información correctamente. */
    private void limpiar(TextField... campos) {
        for (TextField campo : campos) campo.clear();
    }

    /** Crea un área de solo lectura para que los resultados no se editen accidentalmente. */
    private TextArea crearAreaResultado() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(14);
        return area;
    }

    /** Apila secciones de una vista y deja que cada una crezca según el espacio disponible. */
    private VBox crearContenido(Node... elementos) {
        VBox contenido = new VBox(12, elementos);
        contenido.setPadding(new Insets(20));
        contenido.getStyleClass().add("contenido-principal");
        return contenido;
    }

    /** Envuelve una página en un contenedor con desplazamiento vertical cuando hace falta. */
    private ScrollPane contenedorDesplazable(Node contenido) {
        ScrollPane desplazable = new ScrollPane(contenido);
        desplazable.setFitToWidth(true);
        desplazable.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazable.getStyleClass().add("contenedor-desplazable");
        return desplazable;
    }

    /** Conserva el contenido de una vista dentro de un Tab usado por la navegación personalizada. */
    private Tab crearTab(String titulo, VBox contenido) {
        Tab tab = new Tab(titulo, contenido);
        tab.setClosable(false);
        return tab;
    }

    /** Punto de entrada que delega a JavaFX el arranque de la aplicación. */
    public static void main(String[] args) {
        launch(args);
    }
}
