package GestionTareas.Entidades;

/** Estados operativos que puede tener una tarea durante su ciclo de trabajo. */
public enum EstadoTarea {
    PENDIENTE("Pendiente"),
    EN_CURSO("En curso"),
    COMPLETADA("Completada");

    private final String etiqueta;

    EstadoTarea(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Muestra el estado con una etiqueta amigable para la interfaz. */
    @Override
    public String toString() {
        return etiqueta;
    }
}
