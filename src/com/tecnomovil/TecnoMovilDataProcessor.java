package com.tecnomovil;

import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TecnoMovilDataProcessor {

    public static Map<String, Long> afluenciaPorEstacion(List<RegistroTransporte> registros) {
        return registros.stream()
                .filter(r -> "entrada".equalsIgnoreCase(r.accion()))
                .collect(Collectors.groupingBy(
                        RegistroTransporte::estacion,
                        Collectors.counting()
                ));
    }

    public static Map<Integer, Long> registrosPorHora(List<RegistroTransporte> registros) {
        return registros.stream()
                .collect(Collectors.groupingBy(
                        r -> r.timestamp().getHour(),
                        Collectors.counting()
                ));
    }

    public static List<Map.Entry<Integer, Long>> horasPico(List<RegistroTransporte> registros, int topN) {
        return registrosPorHora(registros).entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<Map.Entry<String, Long>> rutasMasUtilizadas(List<RegistroTransporte> registros) {
        Map<String, Long> conteoPorRuta = registros.stream()
                .collect(Collectors.groupingBy(RegistroTransporte::ruta, Collectors.counting()));

        return conteoPorRuta.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toUnmodifiableList());
    }

    public static Map<String, List<String>> patronesDeViaje(List<RegistroTransporte> registros) {
        return registros.stream()
                .sorted(Comparator.comparing(RegistroTransporte::timestamp))
                .collect(Collectors.groupingBy(
                        RegistroTransporte::idUsuario,
                        LinkedHashMap::new,
                        Collectors.mapping(RegistroTransporte::estacion, Collectors.toUnmodifiableList())
                ));
    }

    public static Map<String, Double> tiempoPromedioEntreEstaciones(List<RegistroTransporte> registros) {
        Map<String, List<RegistroTransporte>> porUsuarioOrdenado = registros.stream()
                .sorted(Comparator.comparing(RegistroTransporte::timestamp))
                .collect(Collectors.groupingBy(
                        RegistroTransporte::idUsuario,
                        LinkedHashMap::new,
                        Collectors.toUnmodifiableList()
                ));

        return porUsuarioOrdenado.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> promedioMinutosEntreConsecutivos(e.getValue())
                ));
    }

    private static double promedioMinutosEntreConsecutivos(List<RegistroTransporte> registrosUsuario) {
        if (registrosUsuario.size() < 2) {
            return 0.0;
        }
        return IntStream.range(1, registrosUsuario.size())
                .mapToLong(i -> Duration.between(
                        registrosUsuario.get(i - 1).timestamp(),
                        registrosUsuario.get(i).timestamp()
                ).toMinutes())
                .average()
                .orElse(0.0);
    }

    public static List<EstadoRuta> detectarSobrecarga(List<RegistroTransporte> registros, long umbral) {
        Map<String, Long> ocupacionPorRuta = registros.stream()
                .collect(Collectors.groupingBy(RegistroTransporte::ruta, Collectors.counting()));

        return ocupacionPorRuta.entrySet().stream()
                .map(e -> new EstadoRuta(
                        e.getKey(),
                        e.getValue(),
                        e.getValue() > umbral ? "CRÍTICA" : "NORMAL"
                ))
                .sorted(Comparator.comparing(EstadoRuta::ocupacion).reversed())
                .collect(Collectors.toUnmodifiableList());
    }
}