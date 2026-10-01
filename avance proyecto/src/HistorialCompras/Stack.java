package HistorialCompras;

import Estructuras.Node;

public class Stack<T> {
    public Node<T> top; // Tope de la pila
    public int size; // Tamaño de la pila

    public Stack() {
        this.top = null;
        this.size = 0;
    }

    public boolean isEnd() {return top == null; }

    public void push(T data) {
        Node<T> nuevoNodo = new Node<>(data);
        nuevoNodo.next = top;  // Puntero de la cabeza
        top = nuevoNodo;    // Actualizar el tope
        size++;     // Aumenta el tamaño de la pila
    }

    public T pop() {
        if(isEnd()) {
            System.out.println("No hay compras en el historial");
            return null;
        }

        T datoRemovido = top.data;
        top = top.next; // Desplazamiento al siguiente nodo
        size--;

        return datoRemovido;
    }

    public T peek() {
        if(isEnd()) {
            System.out.println("No hay compras en el historial");
            return null;
        }
        return top.data;
    }

    public void showStack() {
        if(isEnd()) {
            System.out.println("No hay compras en el historial");
            return;
        }

        Node<T> actual = top;
        while(actual != null) {
            System.out.print(actual.data + " -> ");
            actual = actual.next;
        }
        System.out.println("null");
    }

}
