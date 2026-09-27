package GestionTareas;

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
import java.util.List;

/** Pruebas ejecutables sin dependencias externas para las estructuras principales. */
public final class PruebasSistema {
    /** Impide instancias porque esta clase solo agrupa escenarios de prueba. */
    private PruebasSistema() { }

    /** Ejecuta todos los escenarios y muestra un mensaje si ninguno falla. */
    public static void main(String[] args) {
        probarColaPrioridad();
        probarTablaHash();
        probarArbolYDistribucion();
        probarGrafo();
        probarEstadisticas();
        probarEstadosYCapacidad();
        System.out.println("Todas las pruebas del sistema finalizaron correctamente.");
    }

    /** Comprueba que el heap respete urgencia y fecha de entrega como desempate. */
    private static void probarColaPrioridad() {
        Tarea normal = tarea("T-1", "Diseño", "Diseno", 3, 5);
        Tarea urgente = tarea("T-2", "Corrección", "Diseno", 5, 3);
        Tarea urgenteTemprana = tarea("T-3", "Bloqueo", "Diseno", 5, 1);
        ColaPrioridadTareas cola = new ColaPrioridadTareas();
        cola.offer(normal);
        cola.offer(urgente);
        cola.offer(urgenteTemprana);
        verificar("T-3", cola.poll().getId(), "La cola debe priorizar urgencia y fecha");
        verificar("T-2", cola.poll().getId(), "La cola debe mantener la segunda prioridad");
    }

    /** Verifica inserción, actualización y eliminación en la tabla hash propia. */
    private static void probarTablaHash() {
        TablaHash<String, Integer> tabla = new TablaHash<>(3);
        tabla.put("T-1", 2);
        tabla.put("T-1", 8);
        tabla.put("T-2", 4);
        verificar(2, tabla.size(), "Actualizar una clave no debe aumentar el tamaño");
        verificar(8, tabla.get("T-1"), "La tabla hash debe recuperar el valor actualizado");
        verificar(4, tabla.remove("T-2"), "La tabla hash debe eliminar una clave");
    }

    /** Comprueba la búsqueda del árbol y la asignación compatible por estación. */
    private static void probarArbolYDistribucion() {
        ArbolDepartamentos arbol = new ArbolDepartamentos();
        Empleado ana = new Empleado("E-1", "Ana", "Diseno");
        Empleado leo = new Empleado("E-2", "Leo", "Desarrollo");
        arbol.agregar(ana);
        arbol.agregar(leo);
        Tarea tarea = tarea("T-4", "Maqueta", "Diseno", 4, 6);
        List<DistribuidorTareas.Resultado> resultados = DistribuidorTareas.distribuir(List.of(tarea), arbol);
        verificar(1, arbol.buscarDepartamento("Diseno").size(), "El árbol debe localizar el departamento");
        verificar("E-1", tarea.getEmpleadoAsignadoId(), "La asignación debe respetar el departamento");
        verificar(1, resultados.size(), "La distribución debe reportar la asignación");
    }

    /** Confirma que el grafo genera un orden válido para una dependencia simple. */
    private static void probarGrafo() {
        GrafoDependencias grafo = new GrafoDependencias();
        grafo.agregarTarea("T-1");
        grafo.agregarTarea("T-2");
        grafo.agregarDependencia("T-2", "T-1");
        verificar(List.of("T-1", "T-2"), grafo.ordenDeEjecucion(),
                "El grafo debe producir un orden topológico");
    }

    /** Comprueba que el cálculo recursivo sume y cuente correctamente. */
    private static void probarEstadisticas() {
        EstadisticasTareas.Resumen resumen = EstadisticasTareas.calcular(List.of(
                tarea("T-5", "A", "QA", 1, 2),
                tarea("T-6", "B", "QA", 1, 6)));
        verificar(2, resumen.getCantidad(), "La recursión debe contar las tareas");
        verificar(8, resumen.getMinutosTotales(), "La recursión debe sumar los minutos");
    }

    /** Revisa capacidad, estados, folios consecutivos y liberación de carga al completar. */
    private static void probarEstadosYCapacidad() {
        Empleado empleado = new Empleado("E-3", "Mia", "QA", 80);
        Tarea tarea = tarea("T-7", "Pruebas", "QA", 2, 60);
        verificar(true, empleado.puedeAsumir(tarea), "La tarea debe caber en la capacidad semanal");
        empleado.agregarTarea(tarea);
        verificar(20, empleado.getCapacidadDisponibleMinutos(), "La capacidad disponible debe disminuir al asignar");
        verificar(false, empleado.puedeAsumir(tarea("T-8", "Auditoría", "QA", 1, 30)),
                "No se debe superar la capacidad semanal disponible");
        tarea.cambiarEstado(EstadoTarea.EN_CURSO);
        EstadisticasTareas.Resumen resumen = EstadisticasTareas.calcular(List.of(tarea));
        verificar(1, resumen.getEnCurso(), "Las estadísticas deben contar el estado de la tarea");
        verificar(0, resumen.getPendientes(), "Una tarea en curso no debe contarse como pendiente");

        GestorTareas gestor = new GestorTareas();
        Tarea completada = tarea("T-9", "Espresso", "Barra", 5, 10);
        completada.cambiarEstado(EstadoTarea.COMPLETADA);
        Tarea activa = tarea("T-10", "Latte", "Barra", 3, 10);
        gestor.registrarTarea(completada);
        gestor.registrarTarea(activa);
        verificar("T-10", gestor.siguienteTareaPrioritaria().getId(),
                "La siguiente tarea debe ignorar las tareas completadas");

        GestorTareas consecutivos = new GestorTareas();
        Tarea primeraComanda = consecutivos.registrarComanda("Espresso", "Barra", "Barra", 3,
                LocalDate.of(2026, 10, 1), 10, EstadoTarea.PENDIENTE);
        Tarea segundaComanda = consecutivos.registrarComanda("Latte", "Barra", "Barra", 3,
                LocalDate.of(2026, 10, 1), 10, EstadoTarea.PENDIENTE);
        verificar("C-001", primeraComanda.getId(), "La primera comanda debe tener folio C-001");
        verificar("C-002", segundaComanda.getId(), "El folio de comanda debe incrementarse");
        Empleado primerBarista = consecutivos.registrarBarista("Ana", "Barra de café", 2400);
        Empleado segundoBarista = consecutivos.registrarBarista("Leo", "Cocina", 2400);
        verificar("B-001", primerBarista.getId(), "El primer barista debe tener folio B-001");
        verificar("B-002", segundoBarista.getId(), "El folio de barista debe incrementarse");

        GestorTareas cicloDeComanda = new GestorTareas();
        Empleado barista = cicloDeComanda.registrarBarista("Mia", "Barra de café", 60);
        Tarea comandaAsignada = cicloDeComanda.registrarComanda("Latte", "Barra de café", "Barra de café", 3,
                LocalDate.of(2026, 10, 1), 20, EstadoTarea.PENDIENTE);
        cicloDeComanda.distribuirPendientes();
        cicloDeComanda.actualizarEstadoTarea(comandaAsignada.getId(), EstadoTarea.COMPLETADA);
        verificar(0, barista.getCargaEstimadaMinutos(), "Completar una comanda debe liberar la carga del barista");
        verificar(false, comandaAsignada.estaAsignada(), "Una comanda completada debe quedar desasignada");
    }

    /** Construye datos de prueba compactos para no repetir la configuración de una tarea. */
    private static Tarea tarea(String id, String titulo, String departamento, int urgencia, int horas) {
        return new Tarea(id, titulo, "Proyecto", departamento, urgencia,
                LocalDate.of(2026, 10, 1).plusDays(horas), horas);
    }

    /** Lanza un error descriptivo cuando el resultado no coincide con lo esperado. */
    private static void verificar(Object esperado, Object obtenido, String mensaje) {
        if (!esperado.equals(obtenido)) {
            throw new AssertionError(mensaje + ". Esperado: " + esperado + ", obtenido: " + obtenido);
        }
    }
}
