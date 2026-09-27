package GestionTareas.EstructurasDatos;

import GestionTareas.Algoritmos.AlgoritmosTareas;
import GestionTareas.Entidades.Tarea;

import java.util.ArrayList;
import java.util.List;

/** Cola de prioridad implementada con un heap binario máximo. */
public class ColaPrioridadTareas {
    // En un heap el elemento más prioritario siempre se conserva en la raíz.
    private final List<Tarea> heap = new ArrayList<>();

    /** Indica si la cola no contiene tareas pendientes de consulta. */
    public boolean isEmpty() { return heap.isEmpty(); }
    /** Devuelve el número de tareas almacenadas en el heap. */
    public int size() { return heap.size(); }

    /** Inserta al final y la hace subir hasta recuperar la propiedad del heap. */
    public void offer(Tarea tarea) {
        heap.add(tarea);
        subir(heap.size() - 1);
    }

    /** Consulta la siguiente tarea sin retirarla de la cola. */
    public Tarea peek() {
        return heap.isEmpty() ? null : heap.get(0);
    }

    /** Retira la raíz y reacomoda el último elemento hacia abajo. */
    public Tarea poll() {
        if (heap.isEmpty()) return null;
        Tarea primera = heap.get(0);
        Tarea ultima = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, ultima);
            bajar(0);
        }
        return primera;
    }

    /** Devuelve una vista ordenada sin modificar la cola original. */
    public List<Tarea> vistaOrdenada() {
        List<Tarea> copia = new ArrayList<>(heap);
        return AlgoritmosTareas.ordenarPorPrioridad(copia);
    }

    /** Compara con el padre hasta que la nueva tarea quede en su posición. */
    private void subir(int indice) {
        while (indice > 0) {
            int padre = (indice - 1) / 2;
            if (comparar(heap.get(indice), heap.get(padre)) <= 0) return;
            intercambiar(indice, padre);
            indice = padre;
        }
    }

    /** Elige al hijo con mayor prioridad para restaurar el heap después de extraer. */
    private void bajar(int indice) {
        while (true) {
            int izquierdo = indice * 2 + 1;
            int derecho = izquierdo + 1;
            int mayor = indice;
            if (izquierdo < heap.size() && comparar(heap.get(izquierdo), heap.get(mayor)) > 0) {
                mayor = izquierdo;
            }
            if (derecho < heap.size() && comparar(heap.get(derecho), heap.get(mayor)) > 0) {
                mayor = derecho;
            }
            if (mayor == indice) return;
            intercambiar(indice, mayor);
            indice = mayor;
        }
    }

    /** Mayor urgencia primero; para empate, fecha de entrega más próxima. */
    /** Compara primero urgencia y después fecha para decidir la prioridad relativa. */
    private int comparar(Tarea primera, Tarea segunda) {
        int urgencia = Integer.compare(primera.getUrgencia(), segunda.getUrgencia());
        if (urgencia != 0) return urgencia;
        return segunda.getFechaEntrega().compareTo(primera.getFechaEntrega());
    }

    /** Intercambia dos posiciones del heap sin crear una lista adicional. */
    private void intercambiar(int primero, int segundo) {
        Tarea temporal = heap.get(primero);
        heap.set(primero, heap.get(segundo));
        heap.set(segundo, temporal);
    }
}
