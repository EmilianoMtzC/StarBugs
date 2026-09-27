package GestionTareas.EstructurasDatos;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabla hash genérica con encadenamiento separado para resolver colisiones.
 * Se usa como índice de acceso rápido por ID para tareas y empleados.
 */
public class TablaHash<K, V> {
    // Un número primo ayuda a distribuir mejor los hash en la tabla inicial.
    private static final int CAPACIDAD_INICIAL = 17;
    private Nodo<K, V>[] buckets;
    private int size;

    /** Crea una tabla con la capacidad inicial elegida para el índice del sistema. */
    @SuppressWarnings("unchecked")
    public TablaHash() {
        buckets = (Nodo<K, V>[]) new Nodo[CAPACIDAD_INICIAL];
    }

    /** Crea una tabla con una capacidad específica, útil para pruebas controladas. */
    @SuppressWarnings("unchecked")
    public TablaHash(int capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero");
        }
        buckets = (Nodo<K, V>[]) new Nodo[capacidad];
    }

    /** Devuelve el número de pares clave-valor almacenados. */
    public int size() { return size; }
    /** Indica si todavía no se ha almacenado ningún elemento. */
    public boolean isEmpty() { return size == 0; }

    /** Comprueba la existencia de una clave sin exponer los nodos internos. */
    public boolean containsKey(K clave) {
        return buscarNodo(clave) != null;
    }

    /** Recupera el valor asociado; el recorrido queda limitado a su bucket. */
    public V get(K clave) {
        Nodo<K, V> nodo = buscarNodo(clave);
        return nodo == null ? null : nodo.valor;
    }

    /** Inserta una clave nueva o reemplaza su valor manteniendo la tabla consistente. */
    public V put(K clave, V valor) {
        validarClave(clave);
        if ((size + 1.0) / buckets.length > 0.75) {
            redimensionar();
        }
        int indice = indice(clave, buckets.length);
        // Las colisiones se resuelven recorriendo únicamente la cadena del bucket.
        for (Nodo<K, V> actual = buckets[indice]; actual != null; actual = actual.siguiente) {
            if (actual.clave.equals(clave)) {
                V anterior = actual.valor;
                actual.valor = valor;
                return anterior;
            }
        }
        Nodo<K, V> nuevo = new Nodo<>(clave, valor);
        nuevo.siguiente = buckets[indice];
        buckets[indice] = nuevo;
        size++;
        return null;
    }

    /** Desenlaza el nodo encontrado y devuelve su valor para confirmar la eliminación. */
    public V remove(K clave) {
        validarClave(clave);
        int indice = indice(clave, buckets.length);
        Nodo<K, V> anterior = null;
        Nodo<K, V> actual = buckets[indice];
        while (actual != null) {
            if (actual.clave.equals(clave)) {
                if (anterior == null) buckets[indice] = actual.siguiente;
                else anterior.siguiente = actual.siguiente;
                size--;
                return actual.valor;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return null;
    }

    /** Reúne las claves para las vistas de interfaz y los recorridos de otras estructuras. */
    public List<K> keys() {
        List<K> claves = new ArrayList<>();
        for (Nodo<K, V> bucket : buckets) {
            for (Nodo<K, V> actual = bucket; actual != null; actual = actual.siguiente) {
                claves.add(actual.clave);
            }
        }
        return claves;
    }

    /** Reúne los valores para procesarlos sin exponer la estructura de buckets. */
    public List<V> values() {
        List<V> valores = new ArrayList<>();
        for (Nodo<K, V> bucket : buckets) {
            for (Nodo<K, V> actual = bucket; actual != null; actual = actual.siguiente) {
                valores.add(actual.valor);
            }
        }
        return valores;
    }

    /** Localiza un nodo dentro de la cadena calculada por el hash de la clave. */
    private Nodo<K, V> buscarNodo(K clave) {
        validarClave(clave);
        for (Nodo<K, V> actual = buckets[indice(clave, buckets.length)]; actual != null;
             actual = actual.siguiente) {
            if (actual.clave.equals(clave)) return actual;
        }
        return null;
    }

    /** Evita hashes indefinidos rechazando claves nulas desde el inicio. */
    private void validarClave(K clave) {
        if (clave == null) throw new IllegalArgumentException("La clave no puede ser null");
    }

    /** Convierte el hash de una clave en un índice válido dentro del arreglo. */
    private static int indice(Object clave, int capacidad) {
        return Math.floorMod(clave.hashCode(), capacidad);
    }

    /** Amplía la tabla y recalcula los buckets porque el índice depende de la capacidad. */
    @SuppressWarnings("unchecked")
    private void redimensionar() {
        Nodo<K, V>[] anterior = buckets;
        buckets = (Nodo<K, V>[]) new Nodo[anterior.length * 2 + 1];
        int tamanioAnterior = size;
        size = 0;
        // Reinsertar garantiza que cada clave use su nuevo índice.
        for (Nodo<K, V> bucket : anterior) {
            for (Nodo<K, V> actual = bucket; actual != null; actual = actual.siguiente) {
                put(actual.clave, actual.valor);
            }
        }
        size = tamanioAnterior;
    }

    private static class Nodo<K, V> {
        private final K clave;
        private V valor;
        private Nodo<K, V> siguiente;

        /** Guarda un par clave-valor y deja listo el enlace para una posible colisión. */
        private Nodo(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }
    }
}
