package com.tecnomovil;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

public class Main {

    private static final int CANTIDAD_POR_DEFECTO = 1_000_000;
    private static final double PORCENTAJE_SOBRECARGA = 0.20;
    private static final int USUARIOS_A_MOSTRAR = 10;
    private static final int TRAMOS_A_MOSTRAR = 10;

    public static void main(String[] args) {
        int cantidad = args.length > 0 ? Integer.parseInt(args[0]) : CANTIDAD_POR_DEFECTO;

        System.out.println("==================================================");
        System.out.println("   SISTEMA DE ANÁLISIS DE DATOS - TECNOMÓVIL      ");
        System.out.println("==================================================");
        System.out.printf("Generando %,d registros simulados...%n", cantidad);

        long inicio = System.nanoTime();
        List<RegistroTransporte> registros = GeneradorDatos.generarDatosSimulados(cantidad);
        System.out.printf("%,d registros generados en %,d ms.%n", registros.size(), milisDesde(inicio));

        long umbralSugerido = TecnoMovilDataProcessor.umbralSugerido(registros, PORCENTAJE_SOBRECARGA);
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        while (!salir) {
            System.out.println("\nSELECCIONE UNA OPCIÓN:");
            System.out.println("1. Afluencia por Estación (Entradas)");
            System.out.println("2. Top Horas Pico");
            System.out.println("3. Rutas Más Utilizadas");
            System.out.println("4. Patrones de Viaje por Usuario");
            System.out.println("5. Tiempo Promedio entre Estaciones");
            System.out.println("6. Detección de Sobrecarga en Rutas");
            System.out.println("7. Reporte Consolidado Completo");
            System.out.println("8. Comparar Rendimiento Secuencial vs Paralelo");
            System.out.println("0. Salir");
            System.out.print("Opción > ");

            if (!scanner.hasNextLine()) {
                break;
            }
            String opcion = scanner.nextLine().trim();

            switch (opcion) {
                case "1":
                    imprimirAfluencia(registros);
                    break;

                case "2":
                    System.out.println("\n--- TOP 3 HORAS PICO ---");
                    TecnoMovilDataProcessor.horasPico(registros, 3)
                            .forEach(e -> System.out.printf("Hora: %02d:00 | Flujo: %,d registros%n", e.getKey(), e.getValue()));
                    break;

                case "3":
                    System.out.println("\n--- RUTAS MÁS UTILIZADAS ---");
                    TecnoMovilDataProcessor.rutasMasUtilizadas(registros)
                            .forEach(e -> System.out.printf("Ruta: %-5s | Viajes: %,d%n", e.getKey(), e.getValue()));
                    break;

                case "4":
                    System.out.printf("%n--- PATRONES DE VIAJE (primeros %d usuarios) ---%n", USUARIOS_A_MOSTRAR);
                    TecnoMovilDataProcessor.patronesDeViaje(registros).entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .limit(USUARIOS_A_MOSTRAR)
                            .forEach(e -> System.out.printf("Usuario %-7s -> %s%n", e.getKey(), String.join(" -> ", e.getValue())));
                    break;

                case "5":
                    imprimirTiempos(registros);
                    break;

                case "6":
                    System.out.printf("Ingrese el umbral de sobrecarga (Enter = %,d, el %.0f%% de los viajes): ",
                            umbralSugerido, PORCENTAJE_SOBRECARGA * 100);
                    String entrada = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
                    try {
                        long umbral = entrada.isEmpty() ? umbralSugerido : Long.parseLong(entrada);
                        imprimirSobrecarga(registros, umbral);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Ingrese un valor numérico entero.");
                    }
                    break;

                case "7":
                    imprimirReporteCompleto(registros, umbralSugerido);
                    break;

                case "8":
                    compararRendimiento(registros);
                    break;

                case "0":
                    salir = true;
                    break;

                default:
                    System.out.println("Opción inválida.");
            }
        }
        System.out.println("Ejecución finalizada.");
        scanner.close();
    }

    private static void imprimirAfluencia(List<RegistroTransporte> registros) {
        Map<String, Long> usuariosUnicos = TecnoMovilDataProcessor.usuariosUnicosPorEstacion(registros);
        System.out.println("\n--- AFLUENCIA POR ESTACIÓN ---");
        TecnoMovilDataProcessor.afluenciaPorEstacion(registros).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> System.out.printf("Estación: %-10s | Entradas: %,8d | Usuarios distintos: %,8d%n",
                        e.getKey(), e.getValue(), usuariosUnicos.getOrDefault(e.getKey(), 0L)));
    }

    private static void imprimirTiempos(List<RegistroTransporte> registros) {
        System.out.printf("%n--- TIEMPO PROMEDIO ENTRE ESTACIONES (%d tramos más largos) ---%n", TRAMOS_A_MOSTRAR);
        TecnoMovilDataProcessor.tiempoPromedioEntreEstaciones(registros).entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(TRAMOS_A_MOSTRAR)
                .forEach(e -> System.out.printf("Tramo: %-22s | Promedio: %6.2f min%n", e.getKey(), e.getValue()));
        System.out.printf("Promedio general de todos los tramos: %.2f min%n",
                TecnoMovilDataProcessor.tiempoPromedioGeneral(registros));
    }

    private static void imprimirSobrecarga(List<RegistroTransporte> registros, long umbral) {
        System.out.printf("%n--- ESTADO DE SOBRECARGA EN RUTAS (UMBRAL = %,d) ---%n", umbral);
        TecnoMovilDataProcessor.detectarSobrecarga(registros, umbral)
                .forEach(est -> System.out.printf("Ruta: %-5s | Ocupación: %,8d | Estado: %s%n",
                        est.ruta(), est.ocupacion(), est.estado()));
    }

    private static void imprimirReporteCompleto(List<RegistroTransporte> registros, long umbral) {
        System.out.println("\n==================================================");
        System.out.println("       REPORTE COMPLETO DE OPERACIÓN DIARIA       ");
        System.out.println("==================================================");

        imprimirAfluencia(registros);

        System.out.println("\n--- TOP 3 HORAS PICO ---");
        TecnoMovilDataProcessor.horasPico(registros, 3)
                .forEach(e -> System.out.printf("Hora: %02d:00 | Flujo: %,d registros%n", e.getKey(), e.getValue()));

        System.out.println("\n--- RUTAS MÁS UTILIZADAS ---");
        TecnoMovilDataProcessor.rutasMasUtilizadas(registros)
                .forEach(e -> System.out.printf("Ruta: %-5s | Viajes: %,d%n", e.getKey(), e.getValue()));

        imprimirTiempos(registros);
        imprimirSobrecarga(registros, umbral);
    }

    private static void compararRendimiento(List<RegistroTransporte> registros) {
        System.out.printf("%n--- RENDIMIENTO SOBRE %,d REGISTROS (%d núcleos) ---%n",
                registros.size(), Runtime.getRuntime().availableProcessors());
        System.out.printf("%-32s | %12s | %12s | %8s%n", "Operación", "Secuencial", "Paralelo", "Mejora");

        medir("a) Afluencia por estación", paralelo -> TecnoMovilDataProcessor.afluenciaPorEstacion(registros, paralelo));
        medir("b) Horas pico", paralelo -> TecnoMovilDataProcessor.horasPico(registros, 3, paralelo));
        medir("c) Rutas más utilizadas", paralelo -> TecnoMovilDataProcessor.rutasMasUtilizadas(registros, paralelo));
        medir("d) Patrones de viaje", paralelo -> TecnoMovilDataProcessor.patronesDeViaje(registros, paralelo));
        medir("e) Tiempo entre estaciones", paralelo -> TecnoMovilDataProcessor.tiempoPromedioEntreEstaciones(registros, paralelo));
        medir("f) Detección de sobrecarga", paralelo -> TecnoMovilDataProcessor.detectarSobrecarga(registros, 0, paralelo));
    }

    // Ejecuta la operación una vez en cada modo para calentar la JVM y luego mide la segunda
    // ejecución. También verifica que ambos modos producen exactamente el mismo resultado.
    private static void medir(String nombre, Function<Boolean, Object> operacion) {
        operacion.apply(false);
        operacion.apply(true);

        long inicio = System.nanoTime();
        Object secuencial = operacion.apply(false);
        long msSecuencial = milisDesde(inicio);

        inicio = System.nanoTime();
        Object paralelo = operacion.apply(true);
        long msParalelo = milisDesde(inicio);

        String mejora = msParalelo == 0 ? "-" : String.format("%.1fx", (double) msSecuencial / msParalelo);
        String coincide = secuencial.equals(paralelo) ? "" : "  (¡resultados distintos!)";
        System.out.printf("%-32s | %9d ms | %9d ms | %8s%s%n", nombre, msSecuencial, msParalelo, mejora, coincide);
    }

    private static long milisDesde(long inicioNanos) {
        return (System.nanoTime() - inicioNanos) / 1_000_000;
    }
}
