import java.util.ArrayList;

public class TorneoLiga extends Torneo {

    public TorneoLiga(String nombre, Disciplina disciplina, int numClasificados) {
        super(nombre, disciplina, numClasificados);
        Fase.deEquipos(numClasificados); 
    }

    @Override
    public String getTipo() {
        return "Liga (todos contra todos)";
    }

    @Override
    public void generarCalendario() {
        ArrayList<Equipo> lista = getEquipos();
        if (!getPartidos().isEmpty()) {
            throw new IllegalStateException("El calendario ya fue generado.");
        }
        if (lista.size() < getNumClasificados() || lista.size() < 2) {
            throw new IllegalStateException("Se necesitan al menos " + Math.max(2, getNumClasificados())
                    + " equipos para generar el calendario.");
        }
        for (int i = 0; i < lista.size(); i++) {
            for (int j = i + 1; j < lista.size(); j++) {
                if (!lista.get(i).estaRetirado() && !lista.get(j).estaRetirado()) {
                    registrarPartido(lista.get(i), lista.get(j), Fase.FASE_REGULAR);
                }
            }
        }
    }

    @Override
    public TablaPosiciones calcularPosiciones() {
        return new TablaPosiciones(getDisciplina(), getEquipos(), partidosDe(Fase.FASE_REGULAR));
    }

    @Override
    public ArrayList<Equipo> calcularClasificados() {
        Fase actual = faseActual();
        if (actual == null) {
            throw new IllegalStateException("Primero debe generarse el calendario.");
        }
        if (actual != Fase.FASE_REGULAR) {
            return ganadoresDe(actual);
        }
        exigirFaseTerminada(Fase.FASE_REGULAR);
        ArrayList<Equipo> primeros = calcularPosiciones().obtenerPrimeros(getNumClasificados());
        if (primeros.size() < getNumClasificados()) {
            throw new IllegalStateException("No hay suficientes equipos activos para clasificar.");
        }
        return primeros;
    }

    @Override
    public ArrayList<Partido> generarSiguienteFase() {
        if (faseActual() != Fase.FASE_REGULAR) {
            return generarSiguienteEliminatoria();
        }
        ArrayList<Equipo> clasificados = calcularClasificados();
        ArrayList<Equipo> cruces = new ArrayList<>();
        for (int i = 0; i < clasificados.size() / 2; i++) {
            cruces.add(clasificados.get(i));
            cruces.add(clasificados.get(clasificados.size() - 1 - i));
        }
        return crearCruces(cruces, Fase.deEquipos(clasificados.size()));
    }
}
