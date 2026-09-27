package GestionTareas.Entidades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Empleado agrupado dentro del árbol binario por su departamento. */
public class Empleado {
    private final String id;
    private final String nombre;
    private final String departamento;
    private final int capacidadSemanalMinutos;
    private final List<String> tareasAsignadas = new ArrayList<>();
    private int cargaEstimadaMinutos;

    /** Crea un empleado con una jornada semanal predeterminada de 40 horas. */
    public Empleado(String id, String nombre, String departamento) {
        this(id, nombre, departamento, 2400);
    }

    /** Crea un empleado validando sus datos y la capacidad máxima que puede asumir. */
    public Empleado(String id, String nombre, String departamento, int capacidadSemanalMinutos) {
        this.id = textoObligatorio(id, "El ID del empleado");
        this.nombre = textoObligatorio(nombre, "El nombre");
        this.departamento = textoObligatorio(departamento, "El departamento");
        if (capacidadSemanalMinutos <= 0) {
            throw new IllegalArgumentException("La capacidad semanal debe ser mayor que cero");
        }
        this.capacidadSemanalMinutos = capacidadSemanalMinutos;
    }

    /** Limpia un texto obligatorio o informa qué campo no fue proporcionado. */
    private static String textoObligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio");
        }
        return valor.trim();
    }

    /** Devuelve el identificador único del empleado. */
    public String getId() { return id; }
    /** Devuelve el nombre visible del empleado. */
    public String getNombre() { return nombre; }
    /** Devuelve la estación o área en la que trabaja el empleado. */
    public String getDepartamento() { return departamento; }
    /** Devuelve los minutos ya comprometidos con comandas asignadas. */
    public int getCargaEstimadaMinutos() { return cargaEstimadaMinutos; }
    /** Devuelve el límite de minutos que el empleado puede trabajar por semana. */
    public int getCapacidadSemanalMinutos() { return capacidadSemanalMinutos; }
    /** Calcula los minutos que todavía puede recibir sin exceder su jornada. */
    public int getCapacidadDisponibleMinutos() { return Math.max(0, capacidadSemanalMinutos - cargaEstimadaMinutos); }
    /** Convierte la carga actual en un porcentaje de su capacidad semanal. */
    public double getPorcentajeCarga() { return (double) cargaEstimadaMinutos * 100 / capacidadSemanalMinutos; }
    /** Expone las comandas asignadas sin permitir que se modifiquen desde fuera. */
    public List<String> getTareasAsignadas() { return Collections.unmodifiableList(tareasAsignadas); }

    /** Indica si los minutos de la comanda caben en la capacidad aún disponible. */
    public boolean puedeAsumir(Tarea tarea) {
        return tarea != null && cargaEstimadaMinutos + tarea.getMinutosEstimados() <= capacidadSemanalMinutos;
    }

    /** Registra una comanda una sola vez y actualiza la carga del empleado. */
    public void agregarTarea(Tarea tarea) {
        if (!tareasAsignadas.contains(tarea.getId())) {
            if (!puedeAsumir(tarea)) {
                throw new IllegalArgumentException("La tarea supera la capacidad semanal disponible de " + nombre);
            }
            tareasAsignadas.add(tarea.getId());
            cargaEstimadaMinutos += tarea.getMinutosEstimados();
        }
    }

    /** Libera la carga de una comanda terminada o retirada del barista. */
    public void removerTarea(Tarea tarea) {
        if (tarea != null && tareasAsignadas.remove(tarea.getId())) {
            cargaEstimadaMinutos -= tarea.getMinutosEstimados();
        }
    }

    /** Construye un resumen legible para las listas de la interfaz. */
    @Override
    public String toString() {
        return id + " | " + nombre + " | " + departamento + " | Carga: %d/%d min (%.0f%%)"
                .formatted(cargaEstimadaMinutos, capacidadSemanalMinutos, getPorcentajeCarga());
    }
}
