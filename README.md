# Sistema de gestión de tareas

Aplicación JavaFX para administrar tareas, empleados y dependencias de proyectos.

## Estructuras y algoritmos aplicados

- Cola de prioridad (heap binario): prioriza tareas por urgencia y fecha de entrega.
- Árbol binario de búsqueda: organiza empleados por departamento.
- Tabla hash propia: consulta tareas y empleados por ID, con encadenamiento y redimensionamiento.
- Grafo dirigido: modela dependencias e impide ciclos; genera un orden topológico de ejecución.
- Recursividad: calcula el total y promedio de minutos estimados.
- Divide y vencerás: merge sort para ordenamiento y distribución recursiva de tareas por carga.
- Búsqueda binaria: busca tareas por ID sobre una copia ordenada.

## Ejecución

Se requiere JDK 26 y JavaFX 26, de acuerdo con `build.gradle`.

```bash
./gradlew run
```

Para ejecutar las pruebas sin bibliotecas externas:

```bash
./gradlew classes
java -cp build/classes/java/main GestionTareas.PruebasSistema
```
