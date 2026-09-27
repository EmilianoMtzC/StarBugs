package GestionTareas.Algoritmos;

import GestionTareas.Entidades.Tarea;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Ordenamientos merge sort y búsqueda binaria para consultas de tareas. */
public final class AlgoritmosTareas {
    /** Evita que esta clase utilitaria se instancie por accidente. */
    private AlgoritmosTareas() { }

    /** Ordena de mayor a menor prioridad y usa la fecha como desempate. */
    public static List<Tarea> ordenarPorPrioridad(List<Tarea> tareas) {
        return mergeSort(tareas, Comparator
                .comparingInt(Tarea::getUrgencia).reversed()
                .thenComparing(Tarea::getFechaEntrega));
    }

    /** Ordena por fecha próxima y usa la prioridad para desempatar. */
    public static List<Tarea> ordenarPorFecha(List<Tarea> tareas) {
        return mergeSort(tareas, Comparator
                .comparing(Tarea::getFechaEntrega)
                .thenComparing(Comparator.comparingInt(Tarea::getUrgencia).reversed()));
    }

    /** Deja primero las preparaciones que requieren más minutos. */
    public static List<Tarea> ordenarPorHorasDescendente(List<Tarea> tareas) {
        return mergeSort(tareas, Comparator.comparingInt(Tarea::getMinutosEstimados).reversed());
    }

    /** Búsqueda binaria por ID tras ordenar una copia de la colección. */
    public static Tarea buscarPorIdBinaria(List<Tarea> tareas, String id) {
        List<Tarea> ordenadas = mergeSort(tareas, Comparator.comparing(Tarea::getId));
        int inicio = 0;
        int fin = ordenadas.size() - 1;
        while (inicio <= fin) {
            int medio = inicio + (fin - inicio) / 2;
            int comparacion = ordenadas.get(medio).getId().compareTo(id);
            if (comparacion == 0) return ordenadas.get(medio);
            if (comparacion < 0) inicio = medio + 1;
            else fin = medio - 1;
        }
        return null;
    }

    /** Divide la lista en mitades hasta llegar a casos de una sola tarea. */
    private static List<Tarea> mergeSort(List<Tarea> tareas, Comparator<Tarea> comparador) {
        if (tareas.size() <= 1) return new ArrayList<>(tareas);
        int medio = tareas.size() / 2;
        List<Tarea> izquierda = mergeSort(tareas.subList(0, medio), comparador);
        List<Tarea> derecha = mergeSort(tareas.subList(medio, tareas.size()), comparador);
        return mezclar(izquierda, derecha, comparador);
    }

    /** Combina dos mitades ordenadas conservando el criterio recibido. */
    private static List<Tarea> mezclar(List<Tarea> izquierda, List<Tarea> derecha,
                                       Comparator<Tarea> comparador) {
        List<Tarea> resultado = new ArrayList<>(izquierda.size() + derecha.size());
        int i = 0;
        int j = 0;
        while (i < izquierda.size() && j < derecha.size()) {
            if (comparador.compare(izquierda.get(i), derecha.get(j)) <= 0) {
                resultado.add(izquierda.get(i++));
            } else {
                resultado.add(derecha.get(j++));
            }
        }
        while (i < izquierda.size()) resultado.add(izquierda.get(i++));
        while (j < derecha.size()) resultado.add(derecha.get(j++));
        return resultado;
    }
}
