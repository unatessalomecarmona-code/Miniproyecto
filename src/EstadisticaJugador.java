public class EstadisticaJugador implements Comparable<EstadisticaJugador> {
    private final Jugador jugador;
    private int partidosJugados;
    private int goles;
    private int asistencias;
    private int tarjetasAmarillas;
    private int tarjetasRojas;
    private int puntos;
    private int faltas;

    public EstadisticaJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public void registrarActuacion(int goles, int asistencias, int amarillas,
                                   int rojas, int puntos, int faltas) {
        if (goles < 0 || asistencias < 0 || amarillas < 0 || rojas < 0 || puntos < 0 || faltas < 0) {
            throw new IllegalArgumentException("Las estadísticas no pueden ser negativas.");
        }
        if (amarillas > 2 || rojas > 1) {
            throw new IllegalArgumentException("Máximo 2 amarillas y 1 roja por partido.");
        }
        this.partidosJugados++;
        this.goles += goles;
        this.asistencias += asistencias;
        this.tarjetasAmarillas += amarillas;
        this.tarjetasRojas += rojas;
        this.puntos += puntos;
        this.faltas += faltas;
    }

    public int calcularRendimiento() {
        return goles * 3 + asistencias * 2 + puntos - tarjetasAmarillas - 3 * tarjetasRojas;
    }

    public Jugador getJugador() { return jugador; }
    public int getPartidosJugados() { return partidosJugados; }
    public int getGoles() { return goles; }
    public int getAsistencias() { return asistencias; }
    public int getTarjetasAmarillas() { return tarjetasAmarillas; }
    public int getTarjetasRojas() { return tarjetasRojas; }
    public int getPuntos() { return puntos; }
    public int getFaltas() { return faltas; }

    @Override
    public int compareTo(EstadisticaJugador otra) {
        return Integer.compare(otra.calcularRendimiento(), this.calcularRendimiento());
    }

    @Override
    public String toString() {
        return String.format("%-14s PJ:%d  Goles:%d  Asist:%d  TA:%d  TR:%d  Pts:%d  Faltas:%d  Rend:%d",
                jugador.getNombre(), partidosJugados, goles, asistencias,
                tarjetasAmarillas, tarjetasRojas, puntos, faltas, calcularRendimiento());
    }
}
