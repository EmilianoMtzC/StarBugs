package GestionTareas.EstructurasDatos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Grafo dirigido: una arista A -> B significa que A depende de B. */
public class GrafoDependencias {
    private final TablaHash<String, List<String>> adyacencias = new TablaHash<>();

    /** Inicializa el vértice de una comanda sin borrar sus dependencias existentes. */
    public void agregarTarea(String idTarea) {
        if (!adyacencias.containsKey(idTarea)) adyacencias.put(idTarea, new ArrayList<>());
    }

    /**
     * Añade la relación "tarea depende de prerequisito" y rechaza ciclos para
     * conservar un orden de preparación posible.
     */
    public void agregarDependencia(String tarea, String prerequisito) {
        verificarTarea(tarea);
        verificarTarea(prerequisito);
        if (tarea.equals(prerequisito)) {
            throw new IllegalArgumentException("Una tarea no puede depender de sí misma");
        }
        List<String> dependencias = adyacencias.get(tarea);
        if (dependencias.contains(prerequisito)) return;
        if (hayCamino(prerequisito, tarea, new HashSet<>())) {
            throw new IllegalArgumentException("La dependencia crearía un ciclo");
        }
        dependencias.add(prerequisito);
    }

    /** Devuelve una copia de los prerrequisitos directos de la comanda indicada. */
    public List<String> dependenciasDe(String tarea) {
        verificarTarea(tarea);
        return new ArrayList<>(adyacencias.get(tarea));
    }

    /** Orden topológico: cada prerequisito aparece antes de la tarea que lo requiere. */
    public List<String> ordenDeEjecucion() {
        List<String> orden = new ArrayList<>();
        Set<String> visitadas = new HashSet<>();
        for (String tarea : adyacencias.keys()) {
            visitaProfundidad(tarea, visitadas, orden);
        }
        return orden;
    }

    /** Busca recursivamente un camino para comprobar si la nueva arista cerraría un ciclo. */
    private boolean hayCamino(String origen, String destino, Set<String> visitadas) {
        if (origen.equals(destino)) return true;
        if (!visitadas.add(origen)) return false;
        for (String vecina : adyacencias.get(origen)) {
            if (hayCamino(vecina, destino, visitadas)) return true;
        }
        return false;
    }

    /** DFS postorden: agrega cada tarea después de todos sus prerrequisitos. */
    private void visitaProfundidad(String tarea, Set<String> visitadas, List<String> orden) {
        if (!visitadas.add(tarea)) return;
        for (String prerequisito : adyacencias.get(tarea)) {
            visitaProfundidad(prerequisito, visitadas, orden);
        }
        orden.add(tarea);
    }

    /** Confirma que el vértice solicitado ya fue registrado en el grafo. */
    private void verificarTarea(String tarea) {
        if (!adyacencias.containsKey(tarea)) {
            throw new IllegalArgumentException("La tarea '" + tarea + "' no existe");
        }
    }
}
