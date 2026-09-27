package com.tecnomovil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class GeneradorDatos {

    private static final long SEMILLA = 42L;
    private static final int VIAJES_POR_USUARIO = 4;
    private static final int MINUTOS_ENTRE_VIAJES = 5;
    private static final LocalDateTime INICIO_DIA = LocalDateTime.of(2026, 9, 22, 0, 0);
    private static final List<String> ESTACIONES = List.of(
            "Terminal", "Norte", "Estadio", "Centro", "Poblado", "Oriente", "Occidente", "Sur");

    private static final List<String> RUTAS = List.of("R10", "R22", "R35", "R50", "R61", "R72");
    private static final List<Integer> PESOS_RUTAS = List.of(35, 25, 15, 10, 10, 5);

    private static final List<String> TABLA_RUTAS = IntStream.range(0, RUTAS.size())
            .boxed()
            .flatMap(i -> Collections.nCopies(PESOS_RUTAS.get(i), RUTAS.get(i)).stream())
            .toList();

    private record Viaje(int numero, String ruta, int origen, int destino, int inicio, int fin) {}

    private GeneradorDatos() {}

    public static List<RegistroTransporte> generarDatosSimulados(int cantidad) {
        int usuarios = Math.max(1, cantidad / (2 * VIAJES_POR_USUARIO));
        return IntStream.range(0, usuarios)
                .parallel()
                .boxed()
                .flatMap(GeneradorDatos::registrosDeUsuario)
                .collect(Collectors.toUnmodifiableList());
    }

    private static Stream<RegistroTransporte> registrosDeUsuario(int usuario) {
        String idUsuario = "U" + usuario;
        int[] horasDeSalida = IntStream.range(0, VIAJES_POR_USUARIO)
                .map(n -> minutoDelDia(aleatorio(usuario, n)))
                .sorted()
                .toArray();

        Viaje primero = crearViaje(usuario, 0, aleatorio(usuario, 0).nextInt(ESTACIONES.size()), horasDeSalida[0]);

        return Stream.iterate(primero, v -> crearViaje(usuario, v.numero() + 1, v.destino(),
                        Math.max(horasDeSalida[v.numero() + 1], v.fin() + MINUTOS_ENTRE_VIAJES)))
                .limit(VIAJES_POR_USUARIO)
                .flatMap(v -> Stream.of(
                        new RegistroTransporte(idUsuario, v.ruta(), ESTACIONES.get(v.origen()),
                                RegistroTransporte.ENTRADA, INICIO_DIA.plusMinutes(v.inicio())),
                        new RegistroTransporte(idUsuario, v.ruta(), ESTACIONES.get(v.destino()),
                                RegistroTransporte.SALIDA, INICIO_DIA.plusMinutes(v.fin()))
                ));
    }

    private static Viaje crearViaje(int usuario, int numero, int origen, int inicio) {
        SplittableRandom rnd = aleatorio(usuario, numero + VIAJES_POR_USUARIO);
        String ruta = TABLA_RUTAS.get(rnd.nextInt(TABLA_RUTAS.size()));
        int destino = (origen + 1 + rnd.nextInt(ESTACIONES.size() - 1)) % ESTACIONES.size();
        int duracion = Math.abs(origen - destino) * 6 + 3 + rnd.nextInt(10);
        return new Viaje(numero, ruta, origen, destino, inicio, inicio + duracion);
    }

    private static SplittableRandom aleatorio(int usuario, int numero) {
        return new SplittableRandom(SEMILLA + usuario * 16L + numero);
    }

    private static int minutoDelDia(SplittableRandom rnd) {
        double p = rnd.nextDouble();
        if (p < 0.30) {
            return 6 * 60 + rnd.nextInt(3 * 60);
        }
        if (p < 0.55) {
            return 17 * 60 + rnd.nextInt(3 * 60);
        }
        return 5 * 60 + rnd.nextInt(17 * 60);
    }
}
