import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Partido {
    private static final int DURACION_MINUTOS = 120;

    private final int id;
    private final Equipo local;
    private final Equipo visitante;
    private final Fase fase;
    private LocalDateTime fecha;
    private String escenario;
    private EstadoPartido estado;
    private int golesLocal;
    private int golesVisitante;
    private final ArrayList<Jugador> participantes;

    public Partido(int id, Equipo local, Equipo visitante, Fase fase) {
        if (local == null || visitante == null || fase == null) {
            throw new IllegalArgumentException("El partido necesita dos equipos y una fase.");
        }
        if (local.getNombre().equalsIgnoreCase(visitante.getNombre())) {
            throw new IllegalArgumentException("Un equipo no puede enfrentarse consigo mismo.");
        }
        this.id = id;
        this.local = local;
        this.visitante = visitante;
        this.fase = fase;
        this.estado = EstadoPartido.SIN_PROGRAMAR;
        this.participantes = new ArrayList<>();
    }


    public void programar(LocalDateTime fecha, String escenario) {
        if (fecha == null || escenario == null || escenario.isBlank()) {
            throw new IllegalArgumentException("Se necesitan fecha, hora y escenario.");
        }
        if (estado == EstadoPartido.FINALIZADO || estado == EstadoPartido.CANCELADO) {
            throw new IllegalStateException("El partido #" + id + " está " + estado + " y no se puede programar.");
        }
        this.fecha = fecha;
        this.escenario = escenario.trim();
        this.estado = EstadoPartido.PROGRAMADO;
    }

    public void registrarResultado(int golesLocal, int golesVisitante) {
        if (estado == EstadoPartido.SIN_PROGRAMAR) {
            throw new IllegalStateException("No se puede registrar el resultado: el partido #" + id
                    + " todavía no ha sido programado.");
        }
        if (estado == EstadoPartido.FINALIZADO) {
            throw new IllegalStateException("El partido #" + id + " ya está finalizado; modificarlo requiere autorización.");
        }
        if (estado == EstadoPartido.CANCELADO) {
            throw new IllegalStateException("El partido #" + id + " fue cancelado.");
        }
        validarMarcador(golesLocal, golesVisitante);
        this.golesLocal = golesLocal;
        this.golesVisitante = golesVisitante;
        this.estado = EstadoPartido.FINALIZADO;
    }

    public void corregirResultado(int golesLocal, int golesVisitante) {
        if (estado != EstadoPartido.FINALIZADO) {
            throw new IllegalStateException("Solo se puede corregir un partido finalizado.");
        }
        validarMarcador(golesLocal, golesVisitante);
        this.golesLocal = golesLocal;
        this.golesVisitante = golesVisitante;
    }

    public void cancelar() {
        if (estado == EstadoPartido.FINALIZADO) {
            throw new IllegalStateException("No se puede cancelar un partido ya finalizado.");
        }
        estado = EstadoPartido.CANCELADO;
    }

    private void validarMarcador(int golesLocal, int golesVisitante) {
        if (golesLocal < 0 || golesVisitante < 0) {
            throw new IllegalArgumentException("El resultado no puede tener valores negativos.");
        }
    }

    public boolean haParticipado(Jugador jugador) {
        return participantes.contains(jugador);
    }

    public void agregarParticipante(Jugador jugador) {
        if (estado != EstadoPartido.FINALIZADO) {
            throw new IllegalStateException("Las estadísticas se registran después del resultado.");
        }
        if (haParticipado(jugador)) {
            throw new IllegalStateException(jugador.getNombre() + " ya tiene estadística en el partido #" + id + ".");
        }
        participantes.add(jugador);
    }

    public boolean involucra(Equipo equipo) {
        return local == equipo || visitante == equipo;
    }

    public Equipo rivalDe(Equipo equipo) {
        return local == equipo ? visitante : local;
    }

    public int golesDe(Equipo equipo) {
        return local == equipo ? golesLocal : golesVisitante;
    }

    public int golesContra(Equipo equipo) {
        return local == equipo ? golesVisitante : golesLocal;
    }

    public Equipo getGanador() {
        if (estado != EstadoPartido.FINALIZADO || golesLocal == golesVisitante) {
            return null;
        }
        return golesLocal > golesVisitante ? local : visitante;
    }

    public boolean chocaConHorario(LocalDateTime otraFecha) {
        if (fecha == null || (estado != EstadoPartido.PROGRAMADO && estado != EstadoPartido.FINALIZADO)) {
            return false;
        }
        long minutos = Math.abs(Duration.between(fecha, otraFecha).toMinutes());
        return minutos < DURACION_MINUTOS;
    }

    public int getId() { return id; }
    public Equipo getLocal() { return local; }
    public Equipo getVisitante() { return visitante; }
    public Fase getFase() { return fase; }
    public LocalDateTime getFecha() { return fecha; }
    public String getEscenario() { return escenario; }
    public EstadoPartido getEstado() { return estado; }

    @Override
    public String toString() {
        String cuando = (fecha == null) ? "sin fecha" : fecha.toString().replace('T', ' ') + " en " + escenario;
        String marcador = (estado == EstadoPartido.FINALIZADO) ? golesLocal + " - " + golesVisitante : "vs";
        return "#" + id + " [" + fase + "] " + local.getNombre() + " " + marcador + " "
                + visitante.getNombre() + " | " + cuando + " | " + estado;
    }
}
