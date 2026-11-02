public class Jugador {
    private final String documento;
    private final String nombre;
    private EstadoJugador estado;
    private final EstadisticaJugador estadistica;

    public Jugador(String documento, String nombre) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento del jugador es obligatorio.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del jugador es obligatorio.");
        }
        this.documento = documento.trim();
        this.nombre = nombre.trim();
        this.estado = EstadoJugador.ACTIVO;
        this.estadistica = new EstadisticaJugador(this);
    }

    public boolean puedeParticipar() {
        return estado == EstadoJugador.ACTIVO;
    }

    public void suspender() {
        if (estado == EstadoJugador.EXPULSADO) {
            throw new IllegalStateException(nombre + " ya está expulsado.");
        }
        estado = EstadoJugador.SUSPENDIDO;
    }

    public void expulsar() {
        estado = EstadoJugador.EXPULSADO;
    }

    public String getDocumento() { return documento; }
    public String getNombre() { return nombre; }
    public EstadoJugador getEstado() { return estado; }
    public EstadisticaJugador getEstadistica() { return estadistica; }

    @Override
    public String toString() {
        return nombre + " (" + documento + ", " + estado + ")";
    }
}
