package com.tecnomovil;

//Desplazamiento de un usuario entre dos estaciones consecutivas
public record Tramo(
        String idUsuario,
        String origen,
        String destino,
        long minutos
) {
    public String nombre() {
        return origen + " -> " + destino;
    }
}
