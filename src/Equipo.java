import java.util.ArrayList;

public class Equipo {
    private final String nombre;
    private final Disciplina disciplina;
    private boolean retirado;
    private final ArrayList<Jugador> jugadores;

    public Equipo(String nombre, Disciplina disciplina) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del equipo es obligatorio.");
        }
        if (disciplina == null) {
            throw new IllegalArgumentException("El equipo debe tener una disciplina.");
        }
        this.nombre = nombre.trim();
        this.disciplina = disciplina;
        this.retirado = false;
        this.jugadores = new ArrayList<>();
    }

    public void agregarJugador(Jugador jugador) {
        if (jugador == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo.");
        }
        if (retirado) {
            throw new IllegalStateException("El equipo " + nombre + " está retirado.");
        }
        if (buscarJugador(jugador.getDocumento()) != null) {
            throw new IllegalStateException(jugador.getNombre() + " ya está en " + nombre + ".");
        }
        jugadores.add(jugador);
    }

    /** Busca un jugador del equipo por documento; devuelve null si no está. */
    public Jugador buscarJugador(String documento) {
        for (Jugador jugador : jugadores) {
            if (jugador.getDocumento().equalsIgnoreCase(documento.trim())) {
                return jugador;
            }
        }
        return null;
    }
    
    public boolean tieneMinimoJugadores() {
        return jugadores.size() >= disciplina.getMinimoJugadores();
    }

    public void retirar() {
        retirado = true;
    }

    public boolean estaRetirado() { return retirado; }
    public String getNombre() { return nombre; }
    public Disciplina getDisciplina() { return disciplina; }

    /** Devuelve una copia para que nadie modifique la lista interna. */
    public ArrayList<Jugador> getJugadores() {
        return new ArrayList<>(jugadores);
    }

    @Override
    public String toString() {
        return nombre + " (" + jugadores.size() + " jugadores" + (retirado ? ", retirado" : "") + ")";
    }
}
