package com.tecnomovil;

import java.time.LocalDateTime;

public record RegistroTransporte(
        String idUsuario,
        String ruta,
        String estacion,
        String accion,
        LocalDateTime timestamp
) {}