# 🚌 TecnoMovil Data - Sistema de Procesamiento de Datos de Transporte Urbano

Sistema de análisis y procesamiento de datos en tiempo real para el transporte urbano de TecnoValle, desarrollado en Java utilizando paradigmas de Programación Orientada a Objetos (POO) y Programación Funcional.

---

## 📌 Descripción del Proyecto

**TecnoMovil Data** es un módulo de análisis de datos diseñado para procesar el flujo diario de pasajeros en el sistema de transporte. Aplica procesamiento de datos expresado mediante *Pipelines de Streams*, priorizando la inmutabilidad de la información y la ausencia de efectos secundarios.

---

## ⚙️ Principios de Diseño Aplicados

- **Inmutabilidad:** La lista de registros nunca se modifica durante el procesamiento, dado que todas las operaciones devuelven colecciones inmutables (`toUnmodifiableList`, `toUnmodifiableMap`, `Map.copyOf`).
- **Funciones Puras:** Cada método del procesador depende solo de sus parámetros, no usa estado global y no produce efectos secundarios.
- **Programación Declarativa:** Todo el procesamiento se expresa como pipelines de `java.util.stream.Stream` con lambdas y referencias a métodos.
- **Paralelización:** Cada operación tiene una variante con el parámetro `paralelo` que ejecuta el mismo pipeline con `parallelStream()`.
- **Modelado Moderno:** Uso de `record` para definir estructuras de datos concisas e inmutables.

---

## 🚀 Funcionalidades del Módulo Data Processor

1. **Afluencia por Estación:** Cuenta las entradas registradas en cada estación y la cantidad de usuarios distintos que ingresaron.
2. **Identificación de Horas Pico:** Agrupa los registros por hora del día y devuelve el *Top N* con mayor flujo.
3. **Rutas más Utilizadas:** Ordena las rutas de mayor a menor según la cantidad de viajes.
4. **Patrones de Viaje por Usuario:** Lista, para cada usuario, las estaciones visitadas en orden cronológico.
5. **Tiempo Promedio entre Estaciones:** Identifica cada desplazamiento (una entrada seguida de una salida en otra estación) y calcula el tiempo promedio por tramo (`Norte -> Centro`), por usuario y general.
6. **Detección de Sobrecarga en Rutas:** Marca como `CRÍTICA` toda ruta cuya ocupación supera un umbral simulado (se definió que por defecto fuera el 20% de los viajes del día) y como `NORMAL` las demás.

---

## 🧪 Datos Simulados

`GeneradorDatos` produce por defecto **1.000.000 de registros** de forma determinista (siempre los mismos datos) y en paralelo, sin estado compartido: cada valor aleatorio sale de un `SplittableRandom` propio del usuario y del viaje.

- Cada usuario realiza 4 viajes encadenados que no se solapan; cada viaje genera una `entrada` y una `salida`, y el siguiente viaje empieza en la estación donde terminó el anterior.
- La duración de un viaje depende de la distancia entre estaciones.
- Se simularon las siguientes horas pico (6–9 h y 17–20 h)

---

## ▶️ Ejecución

```bash
javac -d bin src/com/tecnomovil/*.java
java -cp bin com.tecnomovil.Main            # 1.000.000 de registros
java -cp bin com.tecnomovil.Main 5000000    # cantidad personalizada
```

---

## 📂 Estructura del Proyecto

```text
TecnoMovilData/
├── .gitignore
├── README.md
└── src/
    └── com/
        └── tecnomovil/
            ├── RegistroTransporte.java       # Registro de entrada/salida (Record)
            ├── Tramo.java                    # Desplazamiento entre dos estaciones (Record)
            ├── EstadoRuta.java               # Estado de ocupación de una ruta (Record)
            ├── GeneradorDatos.java           # Generador de datos simulados a gran escala
            ├── TecnoMovilDataProcessor.java  # Procesamiento funcional con Streams
            └── Main.java                     # Menú de consola interactivo
```

---

##  Autores

* **James Emnauel Machado Taborda**
* **Daniel Gonzalez Ospina**
* **Jhonatan Restrepo Montoya**
* **Juan Andres Sanchez Londoño**
