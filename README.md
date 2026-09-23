# 🚌 TecnoMovil Data - Sistema de Procesamiento de Datos de Transporte Urbano

Sistema de análisis y procesamiento de datos en tiempo real para el transporte urbano de TecnoValle, desarrollado en Java utilizando paradigmas de Programación Orientada a Objetos (POO) y Programación Funcional.

---

## 📌 Descripción del Proyecto

**TecnoMovil Data** es un módulo de análisis de datos diseñado para procesar el flujo diario de pasajeros en el sistema de transporte. Aplica procesamiento de datos expresado mediante *Pipelines de Streams*, priorizando la inmutabilidad de la información y la ausencia de efectos secundarios.

---

## ⚙️ Principios de Diseño Aplicados

- **Inmutabilidad:** Los registros de entrada no sufren alteraciones durante el procesamiento. La salida se genera mediante colecciones inmutables (`toUnmodifiableList`, `toUnmodifiableMap`)[cite: 1].
- **Funciones Puras:** Cada método del procesador es independiente de estados externos globales y no produce efectos secundarios en la consola o memoria[cite: 1].
- **Programación Declarativa:** Todo el procesamiento de datos se realiza a través del pipeline de `java.util.stream.Stream`[cite: 1].
- **Modelado Moderno:** Implementación de `record` para definir estructuras de datos concisas e inmutables.

---

## 🚀 Funcionalidades Módulo Módulo Data Processor

1. **Afluencia por Estación:** Mide y agrupa la cantidad de entradas registradas en cada estación[cite: 1].
2. **Identificación de Horas Pico:** Agrupa eventos por hora y determina el *Top N* con mayor tráfico de pasajeros[cite: 1].
3. **Rutas más Utilizadas:** Ordena y jerarquiza las rutas de transporte con mayor nivel de ocupación[cite: 1].
4. **Patrones de Viaje por Usuario:** Rastrea el historial secuencial de estaciones visitadas por cada usuario[cite: 1].
5. **Tiempo Promedio entre Estaciones:** Calcula la duración promedio (en minutos) entre registros consecutivos de los usuarios[cite: 1].
6. **Detección de Sobrecarga en Rutas:** Evalúa si el volumen de ocupación de una ruta supera un umbral dinámico definible, categorizándola como `NORMAL` o `CRÍTICA`[cite: 1].

---

## 📂 Estructura del Proyecto

```text
TecnoMovilData/
├── .gitignore
├── README.md
└── src/
    └── com/
        └── tecnomovil/
            ├── RegistroTransporte.java       # Modelado de registros de entrada/salida (Record)
            ├── EstadoRuta.java               # Modelado del estado y saturación de rutas (Record)
            ├── GeneradorDatos.java           # Generador de datos de prueba simulados
            ├── TecnoMovilDataProcessor.java  # Lógica de negocio y procesamiento funcional con Streams
            └── Main.java                     # Interfaz de consola accionable e interactiva

##  Autores

* **James Emnauel Machado Taborda**
* **Daniel Gonzalez Ospina**
* **Jhonatan Restrepo Montoya**
* **Juan Andres Sanchez Londoño**
