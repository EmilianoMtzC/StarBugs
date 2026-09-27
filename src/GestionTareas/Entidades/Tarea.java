package GestionTareas.Entidades;

import java.time.LocalDate;
import java.util.Objects;

/** Una tarea del proyecto, ordenable por urgencia y fecha de entrega. */
public class Tarea {
    private final String id;
    private final String titulo;
    private final String proyecto;
    private final String departamento;
    private final int urgencia;
    private final LocalDate fechaEntrega;
    private final int minutosEstimados;
    private String empleadoAsignadoId;
    private EstadoTarea estado;

    /** Crea una tarea pendiente cuando no se especifica un estado inicial. */
    public Tarea(String id, String titulo, String proyecto, String departamento,
                 int urgencia, LocalDate fechaEntrega, int minutosEstimados) {
        this(id, titulo, proyecto, departamento, urgencia, fechaEntrega, minutosEstimados,
                EstadoTarea.PENDIENTE);
    }

    /** Crea una tarea con todos sus datos operativos y valida sus valores básicos. */
    public Tarea(String id, String titulo, String proyecto, String departamento,
                 int urgencia, LocalDate fechaEntrega, int minutosEstimados, EstadoTarea estado) {
        this.id = textoObligatorio(id, "El ID de la tarea");
        this.titulo = textoObligatorio(titulo, "El titulo");
        this.proyecto = textoObligatorio(proyecto, "El proyecto");
        this.departamento = textoObligatorio(departamento, "El departamento");
        if (urgencia < 1 || urgencia > 5) {
            throw new IllegalArgumentException("La urgencia debe estar entre 1 y 5");
        }
        if (fechaEntrega == null) {
            throw new IllegalArgumentException("La fecha de entrega es obligatoria");
        }
        if (minutosEstimados <= 0) {
            throw new IllegalArgumentException("Los minutos estimados deben ser mayores que cero");
        }
        this.urgencia = urgencia;
        this.fechaEntrega = fechaEntrega;
        this.minutosEstimados = minutosEstimados;
        if (estado == null) {
            throw new IllegalArgumentException("El estado de la tarea es obligatorio");
        }
        this.estado = estado;
    }

    /** Normaliza un texto requerido para que no se guarden campos vacíos. */
    private static String textoObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio");
        }
        return valor.trim();
    }

    /** Devuelve el folio único de la tarea. */
    public String getId() { return id; }
    /** Devuelve el nombre de la bebida o preparación. */
    public String getTitulo() { return titulo; }
    /** Devuelve la estación de trabajo asociada a la tarea. */
    public String getProyecto() { return proyecto; }
    /** Devuelve el área responsable de preparar la comanda. */
    public String getDepartamento() { return departamento; }
    /** Devuelve la prioridad, donde cinco representa la mayor urgencia. */
    public int getUrgencia() { return urgencia; }
    /** Devuelve la fecha límite de la preparación. */
    public LocalDate getFechaEntrega() { return fechaEntrega; }
    /** Devuelve el tiempo estimado de preparación en minutos. */
    public int getMinutosEstimados() { return minutosEstimados; }
    /** Devuelve el folio del barista asignado, si existe. */
    public String getEmpleadoAsignadoId() { return empleadoAsignadoId; }
    /** Devuelve el estado actual del ciclo de vida de la comanda. */
    public EstadoTarea getEstado() { return estado; }
    /** Comprueba si un barista ya tomó esta tarea. */
    public boolean estaAsignada() { return empleadoAsignadoId != null; }
    /** Comprueba si la comanda ya finalizó. */
    public boolean estaCompletada() { return estado == EstadoTarea.COMPLETADA; }
    /** Identifica las comandas activas que todavía pueden ser distribuidas. */
    public boolean pendienteDeAsignacion() { return !estaAsignada() && !estaCompletada(); }

    /** Asocia la tarea a un barista validando que se reciba un folio válido. */
    public void asignarA(String empleadoId) {
        this.empleadoAsignadoId = textoObligatorio(empleadoId, "El ID del empleado");
    }

    /** Quita la referencia al barista cuando la tarea se completa o se retira. */
    public void desasignar() {
        this.empleadoAsignadoId = null;
    }

    /** Cambia el avance de la tarea sin modificar sus demás datos. */
    public void cambiarEstado(EstadoTarea nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El estado de la tarea es obligatorio");
        }
        this.estado = nuevoEstado;
    }

    /** Genera la representación que aparece en los listados de comandas. */
    @Override
    public String toString() {
        String asignacion = estaAsignada() ? " | Asignada: " + empleadoAsignadoId : " | Sin asignar";
        return id + " | " + titulo + " | " + estado + " | P" + urgencia + " | " + fechaEntrega
                + " | " + minutosEstimados + " min | " + departamento + asignacion;
    }

    /** Considera iguales dos objetos que representan el mismo folio de tarea. */
    @Override
    public boolean equals(Object objeto) {
        return objeto instanceof Tarea otra && id.equals(otra.id);
    }

    /** Produce un hash coherente con la igualdad basada en el folio. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
