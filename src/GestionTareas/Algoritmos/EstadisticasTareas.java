package GestionTareas.Algoritmos;

import GestionTareas.Entidades.Tarea;
import GestionTareas.Entidades.EstadoTarea;

import java.util.List;

/** Cálculo recursivo de total, promedio y cantidad de minutos estimados. */
public final class EstadisticasTareas {
    /** Evita instancias de una clase que solo agrupa operaciones estáticas. */
    private EstadisticasTareas() { }

    /** Inicia el cálculo recursivo con todo el rango de tareas disponible. */
    public static Resumen calcular(List<Tarea> tareas) {
        return calcular(tareas, 0, tareas.size());
    }

    /** Divide el rango en dos y combina las métricas parciales de cada mitad. */
    private static Resumen calcular(List<Tarea> tareas, int inicio, int fin) {
        if (inicio >= fin) return new Resumen(0, 0, 0, 0, 0, 0);
        if (fin - inicio == 1) {
            Tarea tarea = tareas.get(inicio);
            return new Resumen(1, tarea.getMinutosEstimados(),
                    tarea.getEstado() == EstadoTarea.PENDIENTE ? 1 : 0,
                    tarea.getEstado() == EstadoTarea.EN_CURSO ? 1 : 0,
                    tarea.getEstado() == EstadoTarea.COMPLETADA ? 1 : 0,
                    tarea.pendienteDeAsignacion() ? 1 : 0);
        }
        int medio = inicio + (fin - inicio) / 2;
        Resumen izquierda = calcular(tareas, inicio, medio);
        Resumen derecha = calcular(tareas, medio, fin);
        return new Resumen(izquierda.cantidad + derecha.cantidad,
                izquierda.minutosTotales + derecha.minutosTotales,
                izquierda.pendientes + derecha.pendientes,
                izquierda.enCurso + derecha.enCurso,
                izquierda.completadas + derecha.completadas,
                izquierda.sinAsignar + derecha.sinAsignar);
    }

    public static final class Resumen {
        private final int cantidad;
        private final int minutosTotales;
        private final int pendientes;
        private final int enCurso;
        private final int completadas;
        private final int sinAsignar;

        /** Guarda los acumulados combinados por cada llamada recursiva. */
        private Resumen(int cantidad, int minutosTotales, int pendientes, int enCurso,
                        int completadas, int sinAsignar) {
            this.cantidad = cantidad;
            this.minutosTotales = minutosTotales;
            this.pendientes = pendientes;
            this.enCurso = enCurso;
            this.completadas = completadas;
            this.sinAsignar = sinAsignar;
        }

        /** Devuelve el número de tareas consideradas en el resumen. */
        public int getCantidad() { return cantidad; }
        /** Devuelve la suma de todos los tiempos estimados. */
        public int getMinutosTotales() { return minutosTotales; }
        /** Calcula el tiempo promedio, evitando una división entre cero. */
        public double getPromedioMinutos() { return cantidad == 0 ? 0 : (double) minutosTotales / cantidad; }
        /** Devuelve cuántas tareas aún no inician. */
        public int getPendientes() { return pendientes; }
        /** Devuelve cuántas tareas están en preparación. */
        public int getEnCurso() { return enCurso; }
        /** Devuelve cuántas tareas ya terminaron. */
        public int getCompletadas() { return completadas; }
        /** Devuelve cuántas tareas activas aún no tienen barista. */
        public int getSinAsignar() { return sinAsignar; }
    }
}
