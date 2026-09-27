package GestionTareas;

import GestionTareas.Algoritmos.AlgoritmosTareas;
import GestionTareas.Algoritmos.DistribuidorTareas;
import GestionTareas.Algoritmos.EstadisticasTareas;
import GestionTareas.EstructurasDatos.ArbolDepartamentos;
import GestionTareas.EstructurasDatos.ColaPrioridadTareas;
import GestionTareas.EstructurasDatos.GrafoDependencias;
import GestionTareas.EstructurasDatos.TablaHash;
import GestionTareas.Entidades.Empleado;
import GestionTareas.Entidades.EstadoTarea;
import GestionTareas.Entidades.Tarea;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GestorTareas {
    // Índices de acceso directo: evitan recorrer todas las colecciones al buscar un ID.
    private final TablaHash<String, Tarea> tareasPorId = new TablaHash<>();
    private final TablaHash<String, Empleado> empleadosPorId = new TablaHash<>();
    // Cada estructura atiende una necesidad distinta del flujo de trabajo.
    private final ColaPrioridadTareas colaPrioridad = new ColaPrioridadTareas();
    private final ArbolDepartamentos arbolDepartamentos = new ArbolDepartamentos();
    private final GrafoDependencias grafoDependencias = new GrafoDependencias();

    public void registrarTarea(Tarea tarea) {
        if (tareasPorId.containsKey(tarea.getId())) {
            throw new IllegalArgumentException("Ya existe una tarea con el ID " + tarea.getId());
        }
        // Al registrarla se sincroniza con los tres mecanismos que la necesitan.
        tareasPorId.put(tarea.getId(), tarea);
        colaPrioridad.offer(tarea);
        grafoDependencias.agregarTarea(tarea.getId());
    }

    /** Crea una comanda con un folio consecutivo C-001, C-002, etc. */
    public Tarea registrarComanda(String titulo, String estacion, String areaResponsable,
                                  int prioridad, LocalDate fechaEntrega, int minutosEstimados,
                                  EstadoTarea estado) {
        Tarea comanda = new Tarea(siguienteIdComanda(), titulo, estacion, areaResponsable,
                prioridad, fechaEntrega, minutosEstimados, estado);
        registrarTarea(comanda);
        return comanda;
    }

    /** Busca el primer folio disponible para conservar la numeración consecutiva. */
    public String siguienteIdComanda() {
        int numero = 1;
        while (tareasPorId.containsKey("C-%03d".formatted(numero))) numero++;
        return "C-%03d".formatted(numero);
    }

    public void registrarEmpleado(Empleado empleado) {
        if (empleadosPorId.containsKey(empleado.getId())) {
            throw new IllegalArgumentException("Ya existe un empleado con el ID " + empleado.getId());
        }
        // El árbol permite después localizar al personal de una estación rápidamente.
        empleadosPorId.put(empleado.getId(), empleado);
        arbolDepartamentos.agregar(empleado);
    }

    /** Crea un barista con un folio consecutivo B-001, B-002, etc. */
    public Empleado registrarBarista(String nombre, String estacion, int capacidadSemanalMinutos) {
        Empleado barista = new Empleado(siguienteIdBarista(), nombre, estacion, capacidadSemanalMinutos);
        registrarEmpleado(barista);
        return barista;
    }

    /** Genera el siguiente folio de barista sin depender de un contador externo. */
    public String siguienteIdBarista() {
        int numero = 1;
        while (empleadosPorId.containsKey("B-%03d".formatted(numero))) numero++;
        return "B-%03d".formatted(numero);
    }

    /** Devuelve los folios ordenados para mostrarlos de forma estable en los selectores. */
    public List<String> idsComandas() {
        List<String> ids = tareasPorId.keys();
        Collections.sort(ids);
        return ids;
    }

    /** Devuelve los folios de barista ordenados para los controles de consulta. */
    public List<String> idsBaristas() {
        List<String> ids = empleadosPorId.keys();
        Collections.sort(ids);
        return ids;
    }

    /** Consulta de acceso promedio O(1) mediante la tabla hash. */
    public Tarea buscarTareaPorHash(String id) { return tareasPorId.get(id); }
    public Empleado buscarEmpleadoPorHash(String id) { return empleadosPorId.get(id); }
    /** Alternativa didáctica: ordena una copia y realiza búsqueda binaria por folio. */
    public Tarea buscarTareaBinaria(String id) {
        return AlgoritmosTareas.buscarPorIdBinaria(tareasPorId.values(), id);
    }
    /** Devuelve la siguiente comanda activa, ignorando las que ya se completaron. */
    public Tarea siguienteTareaPrioritaria() {
        for (Tarea tarea : colaPrioridad.vistaOrdenada()) {
            if (!tarea.estaCompletada()) return tarea;
        }
        return null;
    }
    /** Expone el turno en el mismo orden que utilizaría la cola de prioridad. */
    public List<Tarea> tareasPorPrioridad() { return colaPrioridad.vistaOrdenada(); }
    public List<Tarea> tareasPorEstado(EstadoTarea estado) {
        List<Tarea> resultado = new ArrayList<>();
        for (Tarea tarea : tareasPorPrioridad()) {
            if (estado == null || tarea.getEstado() == estado) resultado.add(tarea);
        }
        return resultado;
    }
    /** Ordena una copia por fecha para no alterar el índice hash ni la cola. */
    public List<Tarea> tareasPorFecha() { return AlgoritmosTareas.ordenarPorFecha(tareasPorId.values()); }
    public List<Empleado> empleadosDepartamento(String departamento) {
        return arbolDepartamentos.buscarDepartamento(departamento);
    }
    public List<String> departamentosEnOrden() { return arbolDepartamentos.departamentosEnOrden(); }
    public List<Empleado> empleadosEnOrden() { return arbolDepartamentos.empleadosEnOrden(); }
    public EstadisticasTareas.Resumen estadisticas() { return EstadisticasTareas.calcular(tareasPorId.values()); }
    /**
     * Actualiza el ciclo de vida de la comanda. Al completarla se libera la carga
     * del barista para que pueda recibir una nueva preparación.
     */
    public void actualizarEstadoTarea(String id, EstadoTarea estado) {
        Tarea tarea = buscarTareaPorHash(id);
        if (tarea == null) throw new IllegalArgumentException("No existe una tarea con el ID " + id);
        if (estado == EstadoTarea.COMPLETADA && tarea.estaAsignada()) {
            Empleado empleado = buscarEmpleadoPorHash(tarea.getEmpleadoAsignadoId());
            if (empleado != null) empleado.removerTarea(tarea);
            tarea.desasignar();
        }
        tarea.cambiarEstado(estado);
    }
    /** Ejecuta el reparto recursivo de las comandas todavía disponibles. */
    public List<DistribuidorTareas.Resultado> distribuirPendientes() {
        return DistribuidorTareas.distribuir(tareasPorId.values(), arbolDepartamentos);
    }

    /** Registra que una comanda no puede prepararse hasta terminar su prerrequisito. */
    public void agregarDependencia(String tarea, String prerequisito) {
        grafoDependencias.agregarDependencia(tarea, prerequisito);
    }
    public List<String> dependenciasDe(String idTarea) { return grafoDependencias.dependenciasDe(idTarea); }
    public List<String> ordenDeEjecucion() { return grafoDependencias.ordenDeEjecucion(); }
    public int cantidadTareas() { return tareasPorId.size(); }
    public int cantidadEmpleados() { return empleadosPorId.size(); }
}
