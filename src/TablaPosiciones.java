import java.util.ArrayList;

public class TablaPosiciones {
    private final Disciplina disciplina;
    private final ArrayList<Equipo> equipos;
    private final ArrayList<Partido> partidos;

    public TablaPosiciones(Disciplina disciplina, ArrayList<Equipo> equipos, ArrayList<Partido> partidos) {
        this.disciplina = disciplina;
        this.equipos = new ArrayList<>(equipos);
        this.partidos = new ArrayList<>(partidos);
    }

    private ArrayList<Partido> finalizadosDe(Equipo equipo) {
        ArrayList<Partido> lista = new ArrayList<>();
        for (Partido partido : partidos) {
            if (partido.getEstado() == EstadoPartido.FINALIZADO && partido.involucra(equipo)) {
                lista.add(partido);
            }
        }
        return lista;
    }

    public int calcularPartidosJugados(Equipo equipo) {
        return finalizadosDe(equipo).size();
    }

    private int contarResultados(Equipo equipo, int resultado) {
        int cantidad = 0;
        for (Partido partido : finalizadosDe(equipo)) {
            if (Integer.compare(partido.golesDe(equipo), partido.golesContra(equipo)) == resultado) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public int calcularGanados(Equipo equipo) { return contarResultados(equipo, 1); }
    public int calcularEmpatados(Equipo equipo) { return contarResultados(equipo, 0); }
    public int calcularPerdidos(Equipo equipo) { return contarResultados(equipo, -1); }

    public int calcularGolesFavor(Equipo equipo) {
        int total = 0;
        for (Partido partido : finalizadosDe(equipo)) {
            total += partido.golesDe(equipo);
        }
        return total;
    }

    public int calcularGolesContra(Equipo equipo) {
        int total = 0;
        for (Partido partido : finalizadosDe(equipo)) {
            total += partido.golesContra(equipo);
        }
        return total;
    }

    public int calcularDiferencia(Equipo equipo) {
        return calcularGolesFavor(equipo) - calcularGolesContra(equipo);
    }

    public int calcularPuntos(Equipo equipo) {
        return calcularGanados(equipo) * disciplina.getPuntosVictoria()
                + calcularEmpatados(equipo) * disciplina.getPuntosEmpate();
    }

    private int puntosEnfrentamientoDirecto(Equipo a, Equipo b) {
        int puntos = 0;
        for (Partido partido : finalizadosDe(a)) {
            if (partido.involucra(b)) {
                if (partido.golesDe(a) > partido.golesContra(a)) {
                    puntos += disciplina.getPuntosVictoria();
                } else if (partido.golesDe(a) == partido.golesContra(a)) {
                    puntos += disciplina.getPuntosEmpate();
                }
            }
        }
        return puntos;
    }

    private int comparar(Equipo a, Equipo b) {
        int resultado = Integer.compare(calcularPuntos(b), calcularPuntos(a));
        if (resultado == 0) {
            resultado = Integer.compare(calcularDiferencia(b), calcularDiferencia(a));
        }
        if (resultado == 0) {
            resultado = Integer.compare(calcularGolesFavor(b), calcularGolesFavor(a));
        }
        if (resultado == 0) {
            resultado = Integer.compare(puntosEnfrentamientoDirecto(b, a), puntosEnfrentamientoDirecto(a, b));
        }
        if (resultado == 0) {
            resultado = a.getNombre().compareToIgnoreCase(b.getNombre());
        }
        return resultado;
    }

    public ArrayList<Equipo> ordenar() {
        ArrayList<Equipo> ordenados = new ArrayList<>(equipos);
        ordenados.sort((a, b) -> comparar(a, b));
        return ordenados;
    }

    public Equipo getLider() {
        return ordenar().get(0);
    }

    public ArrayList<Equipo> obtenerPrimeros(int cantidad) {
        ArrayList<Equipo> primeros = new ArrayList<>();
        for (Equipo equipo : ordenar()) {
            if (primeros.size() < cantidad && !equipo.estaRetirado()) {
                primeros.add(equipo);
            }
        }
        return primeros;
    }

    @Override
    public String toString() {
        String texto = String.format("%-3s %-14s %3s %3s %3s %3s %4s %4s %4s %4s%n",
                "#", "Equipo", "PJ", "PG", "PE", "PP", "GF", "GC", "DG", "Pts");
        texto += "-".repeat(56) + "\n";
        int posicion = 1;
        for (Equipo e : ordenar()) {
            texto += String.format("%-3d %-14s %3d %3d %3d %3d %4d %4d %+4d %4d%n", posicion,
                    e.getNombre(), calcularPartidosJugados(e), calcularGanados(e),
                    calcularEmpatados(e), calcularPerdidos(e), calcularGolesFavor(e),
                    calcularGolesContra(e), calcularDiferencia(e), calcularPuntos(e));
            posicion++;
        }
        return texto;
    }
}
