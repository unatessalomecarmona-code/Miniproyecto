import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private static final Scanner teclado = new Scanner(System.in);
    private static final ArrayList<Torneo> torneos = new ArrayList<>();
    private static Torneo actual;

    public static void main(String[] args) {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Opción: ");
            try {
                ejecutar(opcion);
            } catch (RuntimeException error) {
                System.out.println("ERROR: " + error.getMessage());
            }
        } while (opcion != 0);
    }

    private static void mostrarMenu() {
        System.out.println("\n===== GESTIÓN DE TORNEOS =====");
        System.out.println("Torneo actual: " + (actual == null ? "(ninguno)" : actual));
        System.out.println(" 1. Crear torneo                 2. Registrar equipo");
        System.out.println(" 3. Agregar jugador a un equipo  4. Generar calendario");
        System.out.println(" 5. Programar partidos           6. Registrar resultado");
        System.out.println(" 7. Registrar estadística        8. Ver tabla de posiciones");
        System.out.println(" 9. Partidos programados        10. Partidos finalizados");
        System.out.println("11. Estadísticas y destacados   12. Historial de un equipo");
        System.out.println("13. Clasificados y siguiente fase 14. Retirar equipo");
        System.out.println("15. Cargar situación especial (8 equipos)");
        System.out.println("16. Simular resultados de los partidos programados");
        System.out.println("17. Cambiar de torneo            0. Salir");
    }

    private static void ejecutar(int opcion) {
        if (opcion >= 2 && opcion <= 14 && actual == null) {
            throw new IllegalStateException("Primero cree o cargue un torneo.");
        }
        switch (opcion) {
            case 1: crearTorneo(); break;
            case 2: registrarEquipo(); break;
            case 3: agregarJugador(); break;
            case 4:
                actual.generarCalendario();
                System.out.println("Partidos creados: " + actual.getPartidos().size());
                break;
            case 5: programarPartidos(); break;
            case 6: registrarResultado(); break;
            case 7: registrarEstadistica(); break;
            case 8: verTabla(); break;
            case 9: imprimir(actual.consultarPartidosProgramados()); break;
            case 10: imprimir(actual.consultarPartidosFinalizados()); break;
            case 11: verEstadisticas(); break;
            case 12: verHistorial(); break;
            case 13: avanzarFase(); break;
            case 14: actual.retirarEquipo(pedirEquipo()); System.out.println("Equipo retirado."); break;
            case 15: cargarSituacionEspecial(); break;
            case 16: simularResultados(); break;
            case 17: cambiarTorneo(); break;
            case 0: System.out.println("Hasta pronto."); break;
            default: System.out.println("Opción no válida.");
        }
    }


    private static void crearTorneo() {
        String nombre = leerTexto("Nombre del torneo: ");
        Disciplina[] disciplinas = Disciplina.values();
        for (int i = 0; i < disciplinas.length; i++) {
            System.out.println("  " + (i + 1) + ". " + disciplinas[i]);
        }
        Disciplina disciplina = disciplinas[leerEntero("Disciplina: ") - 1];
        Torneo torneo;
        if (leerEntero("Tipo (1 = Liga, 2 = Eliminación directa): ") == 1) {
            torneo = new TorneoLiga(nombre, disciplina, leerEntero("Equipos que clasifican (2, 4 u 8): "));
        } else {
            torneo = new TorneoEliminacion(nombre, disciplina);
        }
        torneos.add(torneo);
        actual = torneo;
        System.out.println("Torneo creado: " + torneo);
    }

    private static void registrarEquipo() {
        actual.registrarEquipo(new Equipo(leerTexto("Nombre del equipo: "), actual.getDisciplina()));
        System.out.println("Equipo registrado.");
    }

    private static void agregarJugador() {
        Equipo equipo = pedirEquipo();
        actual.agregarJugador(equipo, new Jugador(leerTexto("Documento: "), leerTexto("Nombre: ")));
        System.out.println("Jugador agregado a " + equipo.getNombre() + ".");
    }

    private static void programarPartidos() {
        String escenario = leerTexto("Escenario: ");
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        System.out.println("Partidos programados: " + actual.programarCalendario(inicio, escenario));
    }

    private static void registrarResultado() {
        Partido partido = pedirPartido();
        int golesLocal = leerEntero("Goles de " + partido.getLocal().getNombre() + ": ");
        int golesVisitante = leerEntero("Goles de " + partido.getVisitante().getNombre() + ": ");
        actual.registrarResultado(partido, golesLocal, golesVisitante);
        System.out.println("Resultado registrado.");
    }

    private static void registrarEstadistica() {
        Partido partido = pedirPartido();
        Jugador jugador = actual.buscarJugador(leerTexto("Documento del jugador: "));
        if (jugador == null) {
            throw new IllegalArgumentException("Jugador no encontrado.");
        }
        actual.registrarEstadistica(partido, jugador, leerEntero("Goles: "), leerEntero("Asistencias: "),
                leerEntero("Amarillas: "), leerEntero("Rojas: "), leerEntero("Puntos: "), leerEntero("Faltas: "));
        System.out.println("Estadística registrada.");
    }

    private static void verTabla() {
        System.out.println(actual.calcularPosiciones());
        Equipo lider = actual.determinarLider();
        System.out.println("Líder: " + (lider == null ? "(aún no hay partidos jugados)" : lider.getNombre()));
    }

    private static void verEstadisticas() {
        System.out.println("--- Estadísticas de los jugadores ---");
        for (EstadisticaJugador estadistica : actual.obtenerEstadisticas()) {
            System.out.println(estadistica);
        }
        System.out.println("--- Jugadores destacados (top 3) ---");
        for (EstadisticaJugador estadistica : actual.obtenerJugadoresDestacados(3)) {
            System.out.println(estadistica);
        }
    }

    private static void verHistorial() {
        Equipo equipo = pedirEquipo();
        for (String linea : actual.obtenerHistorialEquipo(equipo)) {
            System.out.println(linea);
        }
    }

    private static void avanzarFase() {
        if (actual.obtenerCampeon() != null) {
            System.out.println("El torneo terminó. Campeón: " + actual.obtenerCampeon().getNombre());
            return;
        }
        System.out.println("Clasificados: " + actual.calcularClasificados());
        System.out.println("Partidos de la siguiente fase:");
        imprimir(actual.generarSiguienteFase());
    }

    private static void cambiarTorneo() {
        if (torneos.isEmpty()) {
            throw new IllegalStateException("Todavía no hay torneos.");
        }
        for (int i = 0; i < torneos.size(); i++) {
            System.out.println((i + 1) + ". " + torneos.get(i));
        }
        actual = torneos.get(leerEntero("Torneo: ") - 1);
    }

    /** Liga de fútbol con 8 equipos de 7 jugadores, todos contra todos; clasifican 4 a semifinales. */
    private static void cargarSituacionEspecial() {
        String[] nombres = {"Águilas", "Titanes", "Halcones", "Pumas", "Tigres", "Lobos", "Cóndores", "Panteras"};
        Torneo torneo = new TorneoLiga("Copa TdeA", Disciplina.FUTBOL, 4);
        for (int i = 0; i < nombres.length; i++) {
            Equipo equipo = new Equipo(nombres[i], Disciplina.FUTBOL);
            for (int j = 1; j <= 7; j++) {
                equipo.agregarJugador(new Jugador("D" + i + "-" + j, nombres[i] + " J" + j));
            }
            torneo.registrarEquipo(equipo);
        }
        torneos.add(torneo);
        actual = torneo;
        System.out.println("Cargado: " + torneo);
    }

    private static void simularResultados() {
        for (Partido partido : actual.consultarPartidosProgramados()) {
            int golesLocal = (partido.getId() * 3) % 4;
            int golesVisitante = (partido.getId() * 5) % 3;
            if (golesLocal == golesVisitante && !actual.permiteEmpates()) {
                golesLocal++;
            }
            actual.registrarResultado(partido, golesLocal, golesVisitante);
            registrarGoleador(partido, partido.getLocal().getJugadores().get(0), golesLocal, 0);
            Jugador visitante = partido.getVisitante().getJugadores().get(1 + partido.getId() % 5);
            registrarGoleador(partido, visitante, golesVisitante, partido.getId() % 4 == 0 ? 1 : 0);
            System.out.println(partido);
        }
    }

    private static void registrarGoleador(Partido partido, Jugador jugador, int goles, int amarillas) {
        if (goles > 0 && jugador.puedeParticipar()) {
            actual.registrarEstadistica(partido, jugador, goles, 0, amarillas, 0, 0, 1);
        }
    }

    private static void imprimir(ArrayList<Partido> lista) {
        if (lista.isEmpty()) {
            System.out.println("(no hay partidos)");
        }
        for (Partido partido : lista) {
            System.out.println(partido);
        }
    }

    private static Equipo pedirEquipo() {
        Equipo equipo = actual.buscarEquipo(leerTexto("Nombre del equipo: "));
        if (equipo == null) {
            throw new IllegalArgumentException("Equipo no encontrado.");
        }
        return equipo;
    }

    private static Partido pedirPartido() {
        int id = leerEntero("Número del partido: ");
        for (Partido partido : actual.getPartidos()) {
            if (partido.getId() == id) {
                return partido;
            }
        }
        throw new IllegalArgumentException("Partido no encontrado.");
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return teclado.hasNextLine() ? teclado.nextLine().trim() : "";
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            if (!teclado.hasNextLine()) {
                return 0;
            }
            try {
                return Integer.parseInt(teclado.nextLine().trim());
            } catch (NumberFormatException error) {
                System.out.println("Escriba un número entero.");
            }
        }
    }
}
