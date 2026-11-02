public enum Disciplina {
    FUTBOL("Fútbol", 7, 3, 1, true),
    BALONCESTO("Baloncesto", 5, 2, 0, false),
    VOLEIBOL("Voleibol", 6, 2, 0, false);

    private final String nombre;
    private final int minimoJugadores;
    private final int puntosVictoria;
    private final int puntosEmpate;
    private final boolean permiteEmpate;

    Disciplina(String nombre, int minimoJugadores, int puntosVictoria,
               int puntosEmpate, boolean permiteEmpate) {
        this.nombre = nombre;
        this.minimoJugadores = minimoJugadores;
        this.puntosVictoria = puntosVictoria;
        this.puntosEmpate = puntosEmpate;
        this.permiteEmpate = permiteEmpate;
    }

    public int getMinimoJugadores() { return minimoJugadores; }
    public int getPuntosVictoria() { return puntosVictoria; }
    public int getPuntosEmpate() { return puntosEmpate; }
    public boolean permiteEmpate() { return permiteEmpate; }

    @Override
    public String toString() {
        return nombre;
    }
}
