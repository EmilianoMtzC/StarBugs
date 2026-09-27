package GestionTareas.EstructurasDatos;

import GestionTareas.Entidades.Empleado;

import java.util.ArrayList;
import java.util.List;

/** Árbol binario de búsqueda que indexa a los empleados por departamento. */
public class ArbolDepartamentos {
    private Nodo raiz;

    /** Inserta al empleado en el nodo de su estación, o lo agrega a la lista si ya existe. */
    public void agregar(Empleado empleado) {
        raiz = agregar(raiz, empleado);
    }

    /** Recorre el árbol comparando estaciones hasta encontrar la solicitada. */
    public List<Empleado> buscarDepartamento(String departamento) {
        Nodo actual = raiz;
        while (actual != null) {
            int comparacion = departamento.trim().compareToIgnoreCase(actual.departamento);
            if (comparacion == 0) return new ArrayList<>(actual.empleados);
            actual = comparacion < 0 ? actual.izquierdo : actual.derecho;
        }
        return List.of();
    }

    /** Recorre el árbol en orden para mostrar las estaciones alfabéticamente. */
    public List<String> departamentosEnOrden() {
        List<String> departamentos = new ArrayList<>();
        recorrerEnOrden(raiz, departamentos);
        return departamentos;
    }

    /** Reúne a todo el equipo respetando el orden de las estaciones del árbol. */
    public List<Empleado> empleadosEnOrden() {
        List<Empleado> empleados = new ArrayList<>();
        recorrerEmpleados(raiz, empleados);
        return empleados;
    }

    /** Inserción recursiva propia de un árbol binario de búsqueda. */
    private Nodo agregar(Nodo nodo, Empleado empleado) {
        if (nodo == null) return new Nodo(empleado.getDepartamento(), empleado);
        int comparacion = empleado.getDepartamento().compareToIgnoreCase(nodo.departamento);
        if (comparacion == 0) nodo.empleados.add(empleado);
        else if (comparacion < 0) nodo.izquierdo = agregar(nodo.izquierdo, empleado);
        else nodo.derecho = agregar(nodo.derecho, empleado);
        return nodo;
    }

    /** Visita izquierda, nodo y derecha para obtener las estaciones ordenadas. */
    private void recorrerEnOrden(Nodo nodo, List<String> departamentos) {
        if (nodo == null) return;
        recorrerEnOrden(nodo.izquierdo, departamentos);
        departamentos.add(nodo.departamento + " (" + nodo.empleados.size() + " empleados)");
        recorrerEnOrden(nodo.derecho, departamentos);
    }

    /** Recorre recursivamente cada nodo y añade sus baristas al resultado. */
    private void recorrerEmpleados(Nodo nodo, List<Empleado> empleados) {
        if (nodo == null) return;
        recorrerEmpleados(nodo.izquierdo, empleados);
        empleados.addAll(nodo.empleados);
        recorrerEmpleados(nodo.derecho, empleados);
    }

    private static class Nodo {
        private final String departamento;
        private final List<Empleado> empleados = new ArrayList<>();
        private Nodo izquierdo;
        private Nodo derecho;

        /** Crea un nodo para una estación y registra a su primer empleado. */
        private Nodo(String departamento, Empleado empleado) {
            this.departamento = departamento;
            empleados.add(empleado);
        }
    }
}
