# StarBugs ☕

Aplicación de escritorio para organizar la operación diaria de una cafetería. Permite registrar comandas, dar seguimiento a su preparación, administrar baristas y consultar la carga de trabajo del equipo.

El proyecto está desarrollado en Java con una interfaz gráfica JavaFX. Los datos se mantienen en memoria durante la ejecución; al cerrar la aplicación, se reinicia la información.

## Funciones principales

- Registrar comandas con folio automático, bebida o preparación, estación, área responsable, prioridad, fecha de entrega, tiempo estimado en minutos y estado.
- Actualizar el estado de una comanda y filtrar el listado por pendientes, completadas o todas.
- Consultar la siguiente comanda prioritaria.
- Registrar baristas con folio automático, estación y capacidad de trabajo semanal.
- Distribuir comandas pendientes entre baristas de la estación correspondiente, considerando su carga y capacidad disponible.
- Definir dependencias entre preparaciones y consultar un orden de ejecución que las respete.
- Consultar estadísticas del turno, carga del equipo, búsquedas y listados ordenados.

## Estructuras de datos y algoritmos

| Concepto | Uso en StarBugs |
|---|---|
| Cola de prioridad con heap binario | Prioriza comandas por urgencia y, en caso de empate, por fecha de entrega. |
| Árbol binario de búsqueda | Agrupa los baristas por estación y permite buscar el equipo de una estación. |
| Tabla hash propia | Almacena comandas y baristas para consultarlos por folio. Usa encadenamiento para colisiones y aumenta su capacidad al llenarse. |
| Grafo dirigido | Representa dependencias entre comandas, detecta ciclos y obtiene un orden topológico de preparación. |
| Merge sort | Ordena comandas por prioridad, fecha de entrega o tiempo estimado. |
| Búsqueda binaria | Busca una comanda por folio después de ordenar una copia de los datos. |
| Recursividad | Calcula estadísticas de las comandas y divide el trabajo para distribuirlo entre baristas. |

## Organización del código

```text
src/
├── App.java                              # Interfaz JavaFX y navegación
├── GestionTareas/
│   ├── GestorTareas.java                 # Coordina las operaciones del sistema
│   ├── Entidades/                        # Comandas, baristas y estados
│   ├── EstructurasDatos/                 # Cola, árbol, tabla hash y grafo
│   ├── Algoritmos/                       # Ordenamiento, estadísticas y distribución
│   └── PruebasSistema.java               # Pruebas ejecutables del proyecto
└── resources/styles/styles.css           # Estilos de la interfaz
```

## Requisitos

- JDK 26
- Gradle instalado (el proyecto no incluye Gradle Wrapper)
- Conexión a internet la primera vez que Gradle descargue JavaFX y sus dependencias

## Ejecutar

Desde la carpeta del proyecto:

```bash
gradle run
```


## Notas

- La prioridad va del 1 al 5; 5 representa la más urgente.
- Los tiempos de preparación se capturan en minutos. La capacidad semanal del barista se indica en horas en la interfaz y se convierte a minutos internamente.
- Los folios se generan automáticamente, por ejemplo `C-001` para comandas y `B-001` para baristas.
