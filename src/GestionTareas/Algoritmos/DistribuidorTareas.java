package GestionTareas.Algoritmos;

import GestionTareas.EstructurasDatos.ArbolDepartamentos;
import GestionTareas.Entidades.Empleado;
import GestionTareas.Entidades.Tarea;

import java.util.ArrayList;
import java.util.List;

/**
 * Distribuye tareas grandes con divide y vencerás: las ordena por esfuerzo y
 * divide el rango de trabajo recursivamente; en cada caso base elige al
 * empleado del departamento con menor carga acumulada.
 */
public final class DistribuidorTareas {
    /** Evita instancias de la clase que concentra el algoritmo de reparto. */
    private DistribuidorTareas() { }

    /** Filtra las comandas asignables, las ordena por esfuerzo y comienza el reparto. */
    public static List<Resultado> distribuir(List<Tarea> tareas, ArbolDepartamentos arbol) {
        List<Tarea> pendientes = new ArrayList<>();
        for (Tarea tarea : tareas) {
            if (tarea.pendienteDeAsignacion()) pendientes.add(tarea);
        }
        List<Tarea> ordenadas = AlgoritmosTareas.ordenarPorHorasDescendente(pendientes);
        List<Resultado> resultados = new ArrayList<>();
        distribuirRango(ordenadas, 0, ordenadas.size(), arbol, resultados);
        return resultados;
    }

    /** Divide el rango hasta una tarea; en el caso base selecciona al mejor barista. */
    private static void distribuirRango(List<Tarea> tareas, int inicio, int fin,
                                        ArbolDepartamentos arbol, List<Resultado> resultados) {
        if (inicio >= fin) return;
        if (fin - inicio == 1) {
            Tarea tarea = tareas.get(inicio);
            Empleado empleado = elegirMenorCarga(arbol.buscarDepartamento(tarea.getDepartamento()), tarea);
            if (empleado == null) {
                resultados.add(new Resultado(tarea, null));
            } else {
                tarea.asignarA(empleado.getId());
                empleado.agregarTarea(tarea);
                resultados.add(new Resultado(tarea, empleado));
            }
            return;
        }
        int medio = inicio + (fin - inicio) / 2;
        distribuirRango(tareas, inicio, medio, arbol, resultados);
        distribuirRango(tareas, medio, fin, arbol, resultados);
    }

    /** Escoge al candidato con capacidad suficiente y menor carga acumulada. */
    private static Empleado elegirMenorCarga(List<Empleado> candidatos, Tarea tarea) {
        Empleado seleccionado = null;
        for (Empleado empleado : candidatos) {
            if (empleado.puedeAsumir(tarea)
                    && (seleccionado == null || empleado.getCargaEstimadaMinutos() < seleccionado.getCargaEstimadaMinutos())) {
                seleccionado = empleado;
            }
        }
        return seleccionado;
    }

    public static final class Resultado {
        private final Tarea tarea;
        private final Empleado empleado;

        /** Conserva la comanda evaluada y el barista elegido, si hubo capacidad. */
        private Resultado(Tarea tarea, Empleado empleado) {
            this.tarea = tarea;
            this.empleado = empleado;
        }

        /** Resume el resultado de la asignación para mostrarlo en el panel. */
        @Override
        public String toString() {
            return empleado == null
                    ? tarea.getId() + " no tiene un empleado con capacidad disponible en " + tarea.getDepartamento()
                    : tarea.getId() + " -> " + empleado.getNombre() + " (" + empleado.getDepartamento() + ")";
        }
    }
}
