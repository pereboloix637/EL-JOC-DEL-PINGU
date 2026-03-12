package controlador;

import java.sql.Connection;
import java.util.ArrayList;

import model.caselles.Casella;
import model.core.Partida;
import model.core.Taulell;
import model.entitats.Jugador;
import model.entitats.Pinguino;
import model.items.Dau;

public class GestorPartida {

    private Partida partida;
    private GestorTaulell gestorTaulell;
    private GestorBBDD gestorBBDD;

    public GestorPartida() {
        this.gestorTaulell = new GestorTaulell();
        new GestorJugador();
        this.gestorBBDD = new GestorBBDD();
    }

    public void setPartida(Partida p) {
        this.partida = p;
    }

    public Partida getPartida() {
        return this.partida;
    }

    public void novaPartida(ArrayList<Jugador> jugadors, Taulell taulell) {
        this.partida = new Partida(taulell, jugadors);
    }

    public int tirarDau(Jugador j, Dau dauOpcional) {
        if (dauOpcional != null && dauOpcional.esEspecial()) {
            int resultat = dauOpcional.tirarIUsar();
            if (resultat != -1) return resultat;
        }
        Dau dauNormal = new Dau();
        return dauNormal.tirar();
    }

    public void executarTornComplet() {
        if (partida == null || partida.isFinalitzada()) return;
        Jugador jugadorActual = partida.getJugadorActual();
        if (jugadorActual != null) {
            processarTornJugador(jugadorActual);
            actualitzarEstatTaulell();
            seguentTorn();
        }
    }

    public void processarTornJugador(Jugador j) {
        int tirada = tirarDau(j, null);
        j.mourePosicio(tirada);
        int posicio = j.getPosicio();
        ArrayList<Casella> caselles = partida.getTaulell().getCaselles();
        if (posicio >= caselles.size() - 1) {
            posicio = caselles.size() - 1;
            j.setPosicio(posicio);
        }
        if (j instanceof Pinguino) {
            gestorTaulell.executarCasella(partida, (Pinguino) j, caselles.get(posicio));
        }
        gestorTaulell.comprovarFiTorn(partida);
    }

    public void actualitzarEstatTaulell() {
        if (partida.isFinalitzada()) {
            System.out.println("Partida finalitzada!");
        }
    }

    public void seguentTorn() {
        if (!partida.isFinalitzada()) {
            int seguentIndex = (partida.getIndexJugadorActual() + 1) % partida.getJugadors().size();
            partida.setIndexJugadorActual(seguentIndex);
            if (seguentIndex == 0) {
                partida.setTorns(partida.getTorns() + 1);
            }
        }
    }

    public void guardarPartida(Connection con) {
        if (partida != null) gestorBBDD.guardarBBDD(partida, con);
    }

    public Partida carregarPartida(int id, Connection con) {
        return gestorBBDD.carregarBBDD(id, con);
    }
}
