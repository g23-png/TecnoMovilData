package com.tecnomovil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GeneradorDatos {

    public static List<RegistroTransporte> generarDatosSimulados(int cantidad) {
        String[] usuarios = {"U1", "U2", "U3", "U4", "U5"};
        String[] rutas = {"R10", "R22", "R35", "R50"};
        String[] estaciones = {"Norte", "Centro", "Sur", "Occidente", "Poblado"};
        String[] acciones = {"entrada", "salida"};
        Random rnd = new Random(42);
        LocalDateTime base = LocalDateTime.of(2026, 9, 22, 5, 0);

        return IntStream.range(0, cantidad)
                .mapToObj(i -> new RegistroTransporte(
                        usuarios[rnd.nextInt(usuarios.length)],
                        rutas[rnd.nextInt(rutas.length)],
                        estaciones[rnd.nextInt(estaciones.length)],
                        acciones[rnd.nextInt(acciones.length)],
                        base.plusMinutes(rnd.nextInt(60 * 16))
                ))
                .collect(Collectors.toUnmodifiableList());
    }
}