public enum Fase {
    FASE_REGULAR(0),
    CUARTOS(8),
    SEMIFINAL(4),
    FINAL(2);

    private final int equipos;

    Fase(int equipos) {
        this.equipos = equipos;
    }

    /** Fase eliminatoria que juegan esa cantidad de equipos (2, 4 u 8). */
    public static Fase deEquipos(int cantidad) {
        for (Fase fase : values()) {
            if (fase.equipos == cantidad && cantidad > 0) {
                return fase;
            }
        }
        throw new IllegalArgumentException("No hay una fase eliminatoria para " + cantidad
                + " equipos (use 2, 4 u 8).");
    }

    /** La fase que sigue a esta; null si es la fase regular o la final. */
    public Fase siguiente() {
        if (this == FASE_REGULAR || this == FINAL) {
            return null;
        }
        return deEquipos(equipos / 2);
    }
}
