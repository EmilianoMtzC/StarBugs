package ManejoClientes;

import Estructuras.Node;

public class Queue {
    public Node<String> front;
    public Node<String> back;
    public int size;

    public Queue() {
        this.front = null;
        this.back = null;
        this.size = 0;
    }

    public boolean isEmpty() {return front == null;}

    public void enqueue(String nombre) {
        Node<String> newNode = new Node<>(nombre);
        if (isEmpty()) {
            front = newNode;
            back = newNode;
            size++;
            System.out.println("Se agrego el primer elemento");
            return;
        }
        back.next = newNode;
        back = newNode;
        size++;
        System.out.println("Se agrego el ultimo elemento");
    }

    public String dequeue() {
        if(isEmpty()) {
            System.out.println("No hay elementos en la cola");
            return null;
        }
        String attend = front.data;
        front = front.next;
        if (front == null) back = null;

        size--;
        System.out.println("El elemento atendido es: " + attend);
        return attend;
    }

    public String peek() {
        if(isEmpty()) {
            System.out.println("No hay elementos en la cola");
            return null;
        }
        return front.data;
    }

}
