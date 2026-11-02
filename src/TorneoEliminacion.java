import java.util.ArrayList;

public class TorneoEliminacion extends Torneo {

    public TorneoEliminacion(String nombre, Disciplina disciplina) {
        super(nombre, disciplina, 1);
    }

    @Override
    public String getTipo() {
        return "Eliminación directa";
    }

    @Override
    public boolean permiteEmpates() {
        return false;
    }

    @Override
    public void generarCalendario() {
        if (!getPartidos().isEmpty()) {
            throw new IllegalStateException("El cuadro de eliminación ya fue generado.");
        }
        ArrayList<Equipo> activos = new ArrayList<>();
        for (Equipo equipo : getEquipos()) {
            if (!equipo.estaRetirado()) {
                activos.add(equipo);
            }
        }
        crearCruces(activos, Fase.deEquipos(activos.size()));
    }

    @Override
    public TablaPosiciones calcularPosiciones() {
        return new TablaPosiciones(getDisciplina(), getEquipos(), getPartidos());
    }

    @Override
    public ArrayList<Equipo> calcularClasificados() {
        if (faseActual() == null) {
            throw new IllegalStateException("Primero debe generarse el cuadro de eliminación.");
        }
        return ganadoresDe(faseActual());
    }

    @Override
    public ArrayList<Partido> generarSiguienteFase() {
        return generarSiguienteEliminatoria();
    }
}
