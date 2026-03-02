package controlador;

import java.util.ArrayList;
import java.util.Random;

import model.caselles.Casella;
import model.core.Partida;
import model.core.Taulell;
import model.entitats.Jugador;
import model.entitats.Pingui;
import model.items.Dau;

public class GestorPartida {

    private Partida partida;
    private GestorTaulell gestorTaulell;
    private GestorJugador gestorJugador;
    private GestorBBDD gestorBBDD;
    private Random random;

    /**
     * Constructor que inicialitza els gestors necessaris per controlar la partida.
     */
    public GestorPartida() {
        this.gestorTaulell = new GestorTaulell();
        this.gestorJugador = new GestorJugador();
        this.gestorBBDD = new GestorBBDD();
        this.random = new Random();
    }

    /**
     * Inicialitza una nova partida amb els jugadors i el taulell especificats.
     */
    public void novaPartida(ArrayList<Jugador> jugadors, Taulell taulell) {
        this.partida = new Partida(taulell, jugadors);
    }

    /**
     * Tira un dau especial si s'indica, altrament tira un dau normal (1-6).
     */
    public int tirarDau(Jugador j, Dau dauOpcional) {
        if (dauOpcional != null && dauOpcional.esEspecial()) {
            int resultat = dauOpcional.tirarIUsar(random);
            if (resultat != -1) {
                return resultat;
            }
        }
        Dau dauNormal = new Dau();
        return dauNormal.tirar(random);
    }

    /**
     * Gestiona el cicle complet d'un torn: moviment, acció i canvi de jugador.
     */
    public void executarTornComplet() {
        if (partida == null || partida.isFinalitzada()) {
            System.out.println("La partida no ha començat o ja ha finalitzat.");
            return;
        }
        
        Jugador jugadorActual = partida.getJugadorActual();
        if (jugadorActual != null) {
            processarTornJugador(jugadorActual);
            actualitzarEstatTaulell();
            seguentTorn();
        }
    }

    /**
     * S'encarrega de tirar el dau, moure el jugador i executar l'acció de la casella.
     */
    public void processarTornJugador(Jugador j) {
        // Obtenim la tirada amb un dau normal per defecte
        int tirada = tirarDau(j, null);
        System.out.println("El jugador " + j.getNickname() + " ha tret un " + tirada + ".");
        
        j.mourePosicio(tirada);
        
        int posicio = j.getPosicio();
        ArrayList<Casella> caselles = partida.getTaulell().getCaselles();
        
        if (posicio >= caselles.size() - 1) {
            posicio = caselles.size() - 1;
            j.setPosicio(posicio);
        }
        
        System.out.println(j.getNickname() + " es mou a la casella " + posicio + ".");

        if (j instanceof Pingui) {
            gestorTaulell.executarCasella(partida, (Pingui) j, caselles.get(posicio));
        }
        
        gestorTaulell.comprovarFiTorn(partida);
    }

    /**
     * Comprova si la partida ha finalitzat i anuncia el guanyador si n'hi ha.
     */
    public void actualitzarEstatTaulell() {
        System.out.println("S'ha actualitzat l'estat del taulell.");
        if (partida.isFinalitzada()) {
            System.out.println("Partida finalitzada! Guanyador: " + partida.getGuanyador().getNickname());
        }
    }

    /**
     * Passa el torn al següent jugador. Si tots han jugat, incrementa el número de torns.
     */
    public void seguentTorn() {
        if (!partida.isFinalitzada()) {
            int seguentIndex = (partida.getIndexJugadorActual() + 1) % partida.getJugadors().size();
            partida.setIndexJugadorActual(seguentIndex);
            
            // Si hem donat la volta completa a la llista de jugadors, incrementem els torns globals de la partida
            if (seguentIndex == 0) {
                partida.setTorns(partida.getTorns() + 1);
            }
        }
    }

    /**
     * Retorna la instància de la partida actual.
     */
    public Partida getPartida() {
        return partida;
    }

    /**
     * Guarda l'estat actual de la partida a la base de dades.
     */
    public void guardarPartida() {
        System.out.println("Guardant la partida a la base de dades...");
        gestorBBDD.guardarBBDD(partida);
    }

    /**
     * Carrega una partida existent des de la base de dades mitjançant la seva ID.
     */
    public Partida carregarPartida(int id) {
        System.out.println("Carregant la partida amb ID " + id + " des de la base de dades...");
        return gestorBBDD.carregarBBDD(id);
    }
}
