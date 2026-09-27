package com.tecnomovil;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class TecnoMovilDataProcessor {

    private TecnoMovilDataProcessor() {}

    private static Stream<RegistroTransporte> flujo(List<RegistroTransporte> registros, boolean paralelo) {
        return paralelo ? registros.parallelStream() : registros.stream();
    }

    // a) Afluencia por estación

    public static Map<String, Long> afluenciaPorEstacion(List<RegistroTransporte> registros) {
        return afluenciaPorEstacion(registros, false);
    }

    public static Map<String, Long> afluenciaPorEstacion(List<RegistroTransporte> registros, boolean paralelo) {
        return flujo(registros, paralelo)
                .filter(RegistroTransporte::esEntrada)
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(RegistroTransporte::estacion, Collectors.counting()),
                        Map::copyOf
                ));
    }

    public static Map<String, Long> usuariosUnicosPorEstacion(List<RegistroTransporte> registros) {
        return usuariosUnicosPorEstacion(registros, false);
    }

    public static Map<String, Long> usuariosUnicosPorEstacion(List<RegistroTransporte> registros, boolean paralelo) {
        return flujo(registros, paralelo)
                .filter(RegistroTransporte::esEntrada)
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(
                                RegistroTransporte::estacion,
                                Collectors.collectingAndThen(
                                        Collectors.mapping(RegistroTransporte::idUsuario, Collectors.toSet()),
                                        usuarios -> (long) usuarios.size()
                                )
                        ),
                        Map::copyOf
                ));
    }

    // b) Horas pico

    public static Map<Integer, Long> registrosPorHora(List<RegistroTransporte> registros) {
        return registrosPorHora(registros, false);
    }

    public static Map<Integer, Long> registrosPorHora(List<RegistroTransporte> registros, boolean paralelo) {
        return flujo(registros, paralelo)
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(r -> r.timestamp().getHour(), Collectors.counting()),
                        Map::copyOf
                ));
    }

    public static List<Map.Entry<Integer, Long>> horasPico(List<RegistroTransporte> registros, int topN) {
        return horasPico(registros, topN, false);
    }

    public static List<Map.Entry<Integer, Long>> horasPico(List<RegistroTransporte> registros, int topN, boolean paralelo) {
        return registrosPorHora(registros, paralelo).entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(topN)
                .collect(Collectors.toUnmodifiableList());
    }

    // c) Rutas más utilizadas

    public static List<Map.Entry<String, Long>> rutasMasUtilizadas(List<RegistroTransporte> registros) {
        return rutasMasUtilizadas(registros, false);
    }

    public static List<Map.Entry<String, Long>> rutasMasUtilizadas(List<RegistroTransporte> registros, boolean paralelo) {
        return viajesPorRuta(registros, paralelo).entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toUnmodifiableList());
    }

    private static Map<String, Long> viajesPorRuta(List<RegistroTransporte> registros, boolean paralelo) {
        return flujo(registros, paralelo)
                .filter(RegistroTransporte::esEntrada)
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(RegistroTransporte::ruta, Collectors.counting()),
                        Map::copyOf
                ));
    }

    // d) Patrones de viaje por usuario

    public static Map<String, List<String>> patronesDeViaje(List<RegistroTransporte> registros) {
        return patronesDeViaje(registros, false);
    }

    public static Map<String, List<String>> patronesDeViaje(List<RegistroTransporte> registros, boolean paralelo) {
        return historialPorUsuario(registros, paralelo).entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().map(RegistroTransporte::estacion).toList()
                ));
    }

    private static Map<String, List<RegistroTransporte>> historialPorUsuario(List<RegistroTransporte> registros, boolean paralelo) {
        return flujo(registros, paralelo)
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(
                                RegistroTransporte::idUsuario,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        lista -> lista.stream()
                                                .sorted(Comparator.comparing(RegistroTransporte::timestamp))
                                                .toList()
                                )
                        ),
                        Map::copyOf
                ));
    }

    // e) Tiempo promedio entre estaciones

    public static List<Tramo> tramos(List<RegistroTransporte> registros) {
        return tramos(registros, false);
    }

    public static List<Tramo> tramos(List<RegistroTransporte> registros, boolean paralelo) {
        Collection<List<RegistroTransporte>> historiales = historialPorUsuario(registros, paralelo).values();

        return (paralelo ? historiales.parallelStream() : historiales.stream())
                .flatMap(TecnoMovilDataProcessor::tramosDeUsuario)
                .collect(Collectors.toUnmodifiableList());
    }

    private static Stream<Tramo> tramosDeUsuario(List<RegistroTransporte> historial) {
        return IntStream.range(1, historial.size())
                .mapToObj(i -> Map.entry(historial.get(i - 1), historial.get(i)))
                .filter(par -> par.getKey().esEntrada()
                        && par.getValue().esSalida()
                        && !par.getKey().estacion().equals(par.getValue().estacion()))
                .map(par -> new Tramo(
                        par.getKey().idUsuario(),
                        par.getKey().estacion(),
                        par.getValue().estacion(),
                        Duration.between(par.getKey().timestamp(), par.getValue().timestamp()).toMinutes()
                ));
    }

    public static Map<String, Double> tiempoPromedioEntreEstaciones(List<RegistroTransporte> registros) {
        return tiempoPromedioEntreEstaciones(registros, false);
    }

    public static Map<String, Double> tiempoPromedioEntreEstaciones(List<RegistroTransporte> registros, boolean paralelo) {
        return tramos(registros, paralelo).stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(Tramo::nombre, Collectors.averagingLong(Tramo::minutos)),
                        Map::copyOf
                ));
    }

    public static Map<String, Double> tiempoPromedioPorUsuario(List<RegistroTransporte> registros) {
        return tiempoPromedioPorUsuario(registros, false);
    }

    public static Map<String, Double> tiempoPromedioPorUsuario(List<RegistroTransporte> registros, boolean paralelo) {
        return tramos(registros, paralelo).stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(Tramo::idUsuario, Collectors.averagingLong(Tramo::minutos)),
                        Map::copyOf
                ));
    }

    public static double tiempoPromedioGeneral(List<RegistroTransporte> registros) {
        return tiempoPromedioGeneral(registros, false);
    }

    public static double tiempoPromedioGeneral(List<RegistroTransporte> registros, boolean paralelo) {
        return tramos(registros, paralelo).stream()
                .mapToLong(Tramo::minutos)
                .average()
                .orElse(0.0);
    }

    // f) Detección de sobrecarga en rutas

    public static List<EstadoRuta> detectarSobrecarga(List<RegistroTransporte> registros, long umbral) {
        return detectarSobrecarga(registros, umbral, false);
    }

    public static List<EstadoRuta> detectarSobrecarga(List<RegistroTransporte> registros, long umbral, boolean paralelo) {
        return viajesPorRuta(registros, paralelo).entrySet().stream()
                .map(e -> new EstadoRuta(
                        e.getKey(),
                        e.getValue(),
                        e.getValue() > umbral ? "CRÍTICA" : "NORMAL"
                ))
                .sorted(Comparator.comparing(EstadoRuta::ocupacion).reversed()
                        .thenComparing(EstadoRuta::ruta))
                .collect(Collectors.toUnmodifiableList());
    }

    public static long umbralSugerido(List<RegistroTransporte> registros, double porcentaje) {
        long totalViajes = registros.stream().filter(RegistroTransporte::esEntrada).count();
        return Math.round(totalViajes * porcentaje);
    }
}
