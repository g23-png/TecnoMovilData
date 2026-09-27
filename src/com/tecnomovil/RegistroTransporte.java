package com.tecnomovil;

import java.time.LocalDateTime;

public record RegistroTransporte(
        String idUsuario,
        String ruta,
        String estacion,
        String accion,
        LocalDateTime timestamp
) {
    public static final String ENTRADA = "entrada";
    public static final String SALIDA = "salida";

    public boolean esEntrada() {
        return ENTRADA.equalsIgnoreCase(accion);
    }

    public boolean esSalida() {
        return SALIDA.equalsIgnoreCase(accion);
    }
}
