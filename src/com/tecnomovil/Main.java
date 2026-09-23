package com.tecnomovil;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        List<RegistroTransporte> registros = GeneradorDatos.generarDatosSimulados(50);
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        System.out.println("==================================================");
        System.out.println("   SISTEMA DE ANÁLISIS DE DATOS - TECNOMÓVIL      ");
        System.out.println("==================================================");

        while (!salir) {
            System.out.println("\nSELECCIONE UNA OPCIÓN:");
            System.out.println("1. Afluencia por Estación (Entradas)");
            System.out.println("2. Top Horas Pico");
            System.out.println("3. Rutas Más Utilizadas");
            System.out.println("4. Patrones de Viaje por Usuario");
            System.out.println("5. Tiempo Promedio entre Estaciones");
            System.out.println("6. Detección de Sobrecarga en Rutas");
            System.out.println("7. Reporte Consolidado Completo");
            System.out.println("0. Salir");
            System.out.print("Opción > ");

            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    System.out.println("\n--- AFLUENCIA POR ESTACIÓN ---");
                    TecnoMovilDataProcessor.afluenciaPorEstacion(registros)
                            .forEach((est, cant) -> System.out.printf("Estación: %-12s | Entradas: %d%n", est, cant));
                    break;

                case "2":
                    System.out.println("\n--- TOP 3 HORAS PICO ---");
                    TecnoMovilDataProcessor.horasPico(registros, 3)
                            .forEach(e -> System.out.printf("Hora: %02d:00 | Flujo: %d registros%n", e.getKey(), e.getValue()));
                    break;

                case "3":
                    System.out.println("\n--- RUTAS MÁS UTILIZADAS ---");
                    TecnoMovilDataProcessor.rutasMasUtilizadas(registros)
                            .forEach(e -> System.out.printf("Ruta: %-5s | Registros: %d%n", e.getKey(), e.getValue()));
                    break;

                case "4":
                    System.out.println("\n--- PATRONES DE VIAJE POR USUARIO ---");
                    TecnoMovilDataProcessor.patronesDeViaje(registros)
                            .forEach((usr, ests) -> System.out.printf("Usuario %-3s -> Ruta de estaciones: %s%n", usr, String.join(" -> ", ests)));
                    break;

                case "5":
                    System.out.println("\n--- TIEMPO PROMEDIO ENTRE ESTACIONES ---");
                    TecnoMovilDataProcessor.tiempoPromedioEntreEstaciones(registros)
                            .forEach((usr, prom) -> System.out.printf("Usuario %-3s -> Promedio: %.2f minutos%n", usr, prom));
                    break;

                case "6":
                    System.out.print("Ingrese el umbral de sobrecarga (ej. 10): ");
                    try {
                        long umbral = Long.parseLong(scanner.nextLine());
                        System.out.println("\n--- ESTADO DE SOBRECARGA EN RUTAS ---");
                        TecnoMovilDataProcessor.detectarSobrecarga(registros, umbral)
                                .forEach(est -> System.out.printf("Ruta: %-5s | Ocupación: %2d | Estado: %s%n",
                                        est.ruta(), est.ocupacion(), est.estado()));
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Ingrese un valor numérico entero.");
                    }
                    break;

                case "7":
                    imprimirReporteCompleto(registros);
                    break;

                case "0":
                    salir = true;
                    System.out.println("Ejecución finalizada.");
                    break;

                default:
                    System.out.println("Opción inválida.");
            }
        }
        scanner.close();
    }

    private static void imprimirReporteCompleto(List<RegistroTransporte> registros) {
        System.out.println("\n==================================================");
        System.out.println("       REPORTE COMPLETO DE OPERACIÓN DIARIA       ");
        System.out.println("==================================================");

        System.out.println("\n[1] AFLUENCIA POR ESTACIÓN");
        TecnoMovilDataProcessor.afluenciaPorEstacion(registros)
                .forEach((k, v) -> System.out.println(" - " + k + ": " + v + " entradas"));

        System.out.println("\n[2] TOP 3 HORAS PICO");
        TecnoMovilDataProcessor.horasPico(registros, 3)
                .forEach(e -> System.out.println(" - " + e.getKey() + ":00h: " + e.getValue() + " registros"));

        System.out.println("\n[3] RUTAS MÁS UTILIZADAS");
        TecnoMovilDataProcessor.rutasMasUtilizadas(registros)
                .forEach(e -> System.out.println(" - Ruta " + e.getKey() + ": " + e.getValue() + " uso(s)"));

        System.out.println("\n[4] DIAGNÓSTICO DE SOBRECARGA (UMBRAL = 10)");
        TecnoMovilDataProcessor.detectarSobrecarga(registros, 10)
                .forEach(e -> System.out.println(" - Ruta " + e.ruta() + ": " + e.ocupacion() + " (" + e.estado() + ")"));
    }
}