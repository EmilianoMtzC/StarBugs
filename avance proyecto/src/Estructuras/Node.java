package Estructuras;

public class Node<T> {
    public T data; // El dato del nodo
    public Node<T> next; // El enlace al siguiente nodo (Apuntador)
    public Node<T> prev; // El enlace al anterior nodo (Apuntador)

    // Constructor
    public Node(T data) {
        this.data = data;
        this.next = null;
        this.prev = null;
    }

    @Override
    public String toString() {
        // Operadores ternarios
        return data != null ? data.toString() : "null";
    }

}
