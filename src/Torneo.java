import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

public abstract class Torneo {
    private static final String CLAVE_AUTORIZACION = "ADMIN";

    private final String nombre;
    private final Disciplina disciplina;
    private final int numClasificados;
    private final ArrayList<Equipo> equipos;
    private final ArrayList<Partido> partidos;

    protected Torneo(String nombre, Disciplina disciplina, int numClasificados) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del torneo es obligatorio.");
        }
        if (disciplina == null) {
            throw new IllegalArgumentException("El torneo debe definir una disciplina.");
        }
        if (numClasificados < 1) {
            throw new IllegalArgumentException("Los clasificados deben ser al menos 1.");
        }
        this.nombre = nombre.trim();
        this.disciplina = disciplina;
        this.numClasificados = numClasificados;
        this.equipos = new ArrayList<>();
        this.partidos = new ArrayList<>();
    }

    public abstract String getTipo();

    public abstract void generarCalendario();

    public abstract TablaPosiciones calcularPosiciones();

    public abstract ArrayList<Equipo> calcularClasificados();

    public abstract ArrayList<Partido> generarSiguienteFase();

    public boolean permiteEmpates() {
        return disciplina.permiteEmpate();
    }

    public void registrarEquipo(Equipo equipo) {
        if (equipo == null) {
            throw new IllegalArgumentException("El equipo no puede ser nulo.");
        }
        if (equipo.getDisciplina() != disciplina) {
            throw new IllegalArgumentException("El equipo " + equipo.getNombre() + " es de "
                    + equipo.getDisciplina() + " y el torneo es de " + disciplina + ".");
        }
        if (!partidos.isEmpty()) {
            throw new IllegalStateException("El calendario ya fue generado; no se aceptan más equipos.");
        }
        if (buscarEquipo(equipo.getNombre()) != null) {
            throw new IllegalStateException("Ya existe un equipo llamado " + equipo.getNombre() + ".");
        }
        for (Jugador jugador : equipo.getJugadores()) {
            if (equipoDeJugador(jugador.getDocumento()) != null) {
                throw new IllegalStateException(jugador.getNombre() + " ya pertenece a otro equipo de este torneo.");
            }
        }
        equipos.add(equipo);
    }

    public void agregarJugador(Equipo equipo, Jugador jugador) {
        if (equipo == null || !equipos.contains(equipo)) {
            throw new IllegalArgumentException("El equipo no está registrado en este torneo.");
        }
        if (jugador == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo.");
        }
        Equipo otro = equipoDeJugador(jugador.getDocumento());
        if (otro != null) {
            throw new IllegalStateException(jugador.getNombre() + " ya pertenece a " + otro.getNombre()
                    + " en este torneo; no puede estar en dos equipos.");
        }
        equipo.agregarJugador(jugador);
    }

    public void retirarEquipo(Equipo equipo) {
        if (equipo == null || !equipos.contains(equipo)) {
            throw new IllegalArgumentException("El equipo no está registrado en este torneo.");
        }
        equipo.retirar();
        for (Partido partido : partidos) {
            boolean pendiente = partido.getEstado() == EstadoPartido.SIN_PROGRAMAR
                    || partido.getEstado() == EstadoPartido.PROGRAMADO;
            if (partido.involucra(equipo) && pendiente) {
                partido.cancelar();
            }
        }
    }

    public Equipo buscarEquipo(String nombreEquipo) {
        for (Equipo equipo : equipos) {
            if (equipo.getNombre().equalsIgnoreCase(nombreEquipo.trim())) {
                return equipo;
            }
        }
        return null;
    }

    public Equipo equipoDeJugador(String documento) {
        for (Equipo equipo : equipos) {
            if (equipo.buscarJugador(documento) != null) {
                return equipo;
            }
        }
        return null;
    }

    public Jugador buscarJugador(String documento) {
        Equipo equipo = equipoDeJugador(documento);
        return (equipo == null) ? null : equipo.buscarJugador(documento);
    }

    public void verificarSuspension(Jugador jugador) {
        if (!jugador.puedeParticipar()) {
            throw new IllegalStateException(jugador.getNombre() + " está " + jugador.getEstado()
                    + " y no puede participar en un partido.");
        }
    }

    public Partido registrarPartido(Equipo local, Equipo visitante, Fase fase) {
        if (!equipos.contains(local) || !equipos.contains(visitante)) {
            throw new IllegalArgumentException("Ambos equipos deben estar registrados en el torneo.");
        }
        if (local.estaRetirado() || visitante.estaRetirado()) {
            throw new IllegalStateException("Un equipo retirado no puede continuar participando.");
        }
        Partido partido = new Partido(partidos.size() + 1, local, visitante, fase);
        partidos.add(partido);
        return partido;
    }

    public boolean verificarDisponibilidad(Equipo equipo, LocalDateTime fecha, Partido ignorar) {
        for (Partido otro : partidos) {
            if (otro != ignorar && otro.involucra(equipo) && otro.chocaConHorario(fecha)) {
                return false;
            }
        }
        return true;
    }

    public void programarPartido(Partido partido, LocalDateTime fecha, String escenario) {
        if (partido == null || !partidos.contains(partido)) {
            throw new IllegalArgumentException("El partido no pertenece a este torneo.");
        }
        Equipo[] participantes = {partido.getLocal(), partido.getVisitante()};
        for (Equipo equipo : participantes) {
            if (equipo.estaRetirado()) {
                throw new IllegalStateException("Un equipo retirado no puede continuar participando: " + equipo.getNombre());
            }
            if (!equipo.tieneMinimoJugadores()) {
                throw new IllegalStateException(equipo.getNombre() + " no tiene el mínimo de "
                        + disciplina.getMinimoJugadores() + " jugadores para " + disciplina + ".");
            }
            if (!verificarDisponibilidad(equipo, fecha, partido)) {
                throw new IllegalStateException(equipo.getNombre() + " ya tiene un partido en ese horario.");
            }
        }
        for (Partido otro : partidos) {
            boolean mismoEscenario = escenario != null && escenario.equalsIgnoreCase(otro.getEscenario());
            if (otro != partido && mismoEscenario && otro.chocaConHorario(fecha)) {
                throw new IllegalStateException("El escenario " + escenario + " está ocupado en ese horario.");
            }
        }
        partido.programar(fecha, escenario);
    }

    public int programarCalendario(LocalDateTime inicio, String escenario) {
        LocalDateTime primeraFecha = inicio;
        for (Partido partido : partidos) {
            if (partido.getFecha() != null && !partido.getFecha().isBefore(primeraFecha)) {
                primeraFecha = partido.getFecha().toLocalDate().plusDays(1).atTime(inicio.toLocalTime());
            }
        }
        int programados = 0;
        for (Partido partido : partidos) {
            if (partido.getEstado() == EstadoPartido.SIN_PROGRAMAR) {
                LocalDateTime fecha = primeraFecha.plusDays(programados / 3).plusHours(3 * (programados % 3));
                programarPartido(partido, fecha, escenario);
                programados++;
            }
        }
        return programados;
    }

    public void registrarResultado(Partido partido, int golesLocal, int golesVisitante) {
        if (partido == null || !partidos.contains(partido)) {
            throw new IllegalArgumentException("El partido no pertenece a este torneo.");
        }
        if (golesLocal == golesVisitante && !permiteEmpates()) {
            throw new IllegalArgumentException("Este torneo no admite empates: debe haber un ganador.");
        }
        partido.registrarResultado(golesLocal, golesVisitante);
    }

    public void corregirResultado(Partido partido, int golesLocal, int golesVisitante, String clave) {
        if (!CLAVE_AUTORIZACION.equals(clave)) {
            throw new SecurityException("Un partido finalizado no puede modificarse sin autorización.");
        }
        if (partido.getFase() != faseActual()) {
            throw new IllegalStateException("Ya se generó la siguiente fase; no se puede corregir este resultado.");
        }
        if (golesLocal == golesVisitante && !permiteEmpates()) {
            throw new IllegalArgumentException("Este torneo no admite empates: debe haber un ganador.");
        }
        partido.corregirResultado(golesLocal, golesVisitante);
    }

    public void cancelarPartido(Partido partido) {
        if (partido == null || !partidos.contains(partido)) {
            throw new IllegalArgumentException("El partido no pertenece a este torneo.");
        }
        partido.cancelar();
    }

    public void registrarEstadistica(Partido partido, Jugador jugador, int goles, int asistencias,
                                     int amarillas, int rojas, int puntos, int faltas) {
        if (partido == null || !partidos.contains(partido)) {
            throw new IllegalArgumentException("El partido no pertenece a este torneo.");
        }
        if (partido.getEstado() != EstadoPartido.FINALIZADO) {
            throw new IllegalStateException("Las estadísticas se registran después del resultado.");
        }
        Equipo equipo = equipoDeJugador(jugador.getDocumento());
        if (equipo == null || !partido.involucra(equipo)) {
            throw new IllegalArgumentException(jugador.getNombre() + " no juega en el partido #" + partido.getId() + ".");
        }
        verificarSuspension(jugador);
        if (partido.haParticipado(jugador)) {
            throw new IllegalStateException(jugador.getNombre() + " ya tiene estadística en este partido.");
        }
        jugador.getEstadistica().registrarActuacion(goles, asistencias, amarillas, rojas, puntos, faltas);
        partido.agregarParticipante(jugador);

        if (rojas > 0 || amarillas >= 2) {
            jugador.expulsar();
        } else if (amarillas > 0 && jugador.getEstadistica().getTarjetasAmarillas() % 3 == 0) {
            jugador.suspender(); 
        }
    }

    public ArrayList<Partido> consultarPartidosProgramados() {
        return partidosConEstado(EstadoPartido.PROGRAMADO);
    }

    public ArrayList<Partido> consultarPartidosFinalizados() {
        return partidosConEstado(EstadoPartido.FINALIZADO);
    }

    private ArrayList<Partido> partidosConEstado(EstadoPartido estado) {
        ArrayList<Partido> lista = new ArrayList<>();
        for (Partido partido : partidos) {
            if (partido.getEstado() == estado) {
                lista.add(partido);
            }
        }
        return lista;
    }

    public ArrayList<Partido> obtenerPartidosEquipo(Equipo equipo) {
        ArrayList<Partido> lista = new ArrayList<>();
        for (Partido partido : partidos) {
            if (partido.involucra(equipo)) {
                lista.add(partido);
            }
        }
        return lista;
    }

    public ArrayList<String> obtenerHistorialEquipo(Equipo equipo) {
        ArrayList<String> historial = new ArrayList<>();
        for (Partido partido : obtenerPartidosEquipo(equipo)) {
            if (partido.getEstado() == EstadoPartido.FINALIZADO) {
                int favor = partido.golesDe(equipo);
                int contra = partido.golesContra(equipo);
                String letra = favor > contra ? "G" : (favor == contra ? "E" : "P");
                historial.add("[" + partido.getFase() + "] vs " + partido.rivalDe(equipo).getNombre()
                        + "  " + favor + " - " + contra + "  (" + letra + ")");
            }
        }
        return historial;
    }

    public ArrayList<EstadisticaJugador> obtenerEstadisticas() {
        ArrayList<EstadisticaJugador> lista = new ArrayList<>();
        for (Equipo equipo : equipos) {
            for (Jugador jugador : equipo.getJugadores()) {
                if (jugador.getEstadistica().getPartidosJugados() > 0) {
                    lista.add(jugador.getEstadistica());
                }
            }
        }
        Collections.sort(lista);
        return lista;
    }

    public ArrayList<EstadisticaJugador> obtenerJugadoresDestacados(int cantidad) {
        ArrayList<EstadisticaJugador> todas = obtenerEstadisticas();
        return new ArrayList<>(todas.subList(0, Math.min(cantidad, todas.size())));
    }

    public Equipo determinarLider() {
        if (consultarPartidosFinalizados().isEmpty()) {
            return null;
        }
        return calcularPosiciones().getLider();
    }

    public Equipo obtenerCampeon() {
        for (Partido partido : partidosDe(Fase.FINAL)) {
            if (partido.getEstado() == EstadoPartido.FINALIZADO) {
                return partido.getGanador();
            }
        }
        return null;
    }

    protected ArrayList<Partido> partidosDe(Fase fase) {
        ArrayList<Partido> lista = new ArrayList<>();
        for (Partido partido : partidos) {
            if (partido.getFase() == fase) {
                lista.add(partido);
            }
        }
        return lista;
    }

    protected Fase faseActual() {
        if (partidos.isEmpty()) {
            return null;
        }
        return partidos.get(partidos.size() - 1).getFase();
    }

    protected void exigirFaseTerminada(Fase fase) {
        for (Partido partido : partidosDe(fase)) {
            if (partido.getEstado() == EstadoPartido.SIN_PROGRAMAR
                    || partido.getEstado() == EstadoPartido.PROGRAMADO) {
                throw new IllegalStateException("Todavía hay partidos sin terminar en: " + fase + ".");
            }
        }
    }

    protected ArrayList<Equipo> ganadoresDe(Fase fase) {
        exigirFaseTerminada(fase);
        ArrayList<Equipo> ganadores = new ArrayList<>();
        for (Partido partido : partidosDe(fase)) {
            if (partido.getEstado() == EstadoPartido.FINALIZADO) {
                ganadores.add(partido.getGanador());
            } else if (partido.getLocal().estaRetirado()) {
                ganadores.add(partido.getVisitante());
            } else {
                ganadores.add(partido.getLocal());
            }
        }
        return ganadores;
    }

    protected ArrayList<Partido> crearCruces(ArrayList<Equipo> lista, Fase fase) {
        ArrayList<Partido> creados = new ArrayList<>();
        for (int i = 0; i < lista.size(); i += 2) {
            creados.add(registrarPartido(lista.get(i), lista.get(i + 1), fase));
        }
        return creados;
    }

    protected ArrayList<Partido> generarSiguienteEliminatoria() {
        Fase actual = faseActual();
        if (actual == null || actual == Fase.FINAL) {
            throw new IllegalStateException("No hay una fase siguiente que generar.");
        }
        return crearCruces(ganadoresDe(actual), actual.siguiente());
    }

    public String getNombre() { return nombre; }
    public Disciplina getDisciplina() { return disciplina; }
    public int getNumClasificados() { return numClasificados; }
    public ArrayList<Equipo> getEquipos() { return new ArrayList<>(equipos); }
    public ArrayList<Partido> getPartidos() { return new ArrayList<>(partidos); }

    @Override
    public String toString() {
        return nombre + " | " + getTipo() + " | " + disciplina + " | " + equipos.size() + " equipos";
    }
}
