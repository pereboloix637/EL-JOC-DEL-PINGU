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
        // Nerf del oso: movimiento limitado a 1-3
        if (j.isNerfOs()) {
            j.setNerfOs(false); // Consumir el efecto
            int result = new java.util.Random().nextInt(3) + 1;
            vista.PantallaJuego.registrarEventoEstatico(j.getNickname() + " solo mueve " + result + " por el susto del oso.", "log-warning");
            return result;
        }

        if (dauOpcional != null && dauOpcional.esEspecial()) {
            int resultat = dauOpcional.tirarIUsar();
            if (resultat != -1) {
                // Asegurar que se elimine del inventario si se ha agotado (para Pinguino y Foca)
                if (j instanceof Pinguino) {
                    Pinguino p = (Pinguino) j;
                    if (dauOpcional.getQuantitat() <= 0) {
                        p.getInventari().eliminarItem(dauOpcional);
                    }
                } else if (j instanceof model.entitats.Foca) {
                    model.entitats.Foca f = (model.entitats.Foca) j;
                    if (dauOpcional.getQuantitat() <= 0) {
                        f.getInventari().eliminarItem(dauOpcional);
                    }
                }
                return resultat;
            }
        }
        Dau dauNormal = new Dau();
        return dauNormal.tirar();
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
            int numJugadors = partida.getJugadors().size();
            int seguentIndex = (partida.getIndexJugadorActual() + 1) % numJugadors;
            
            // Bucle para buscar el siguiente jugador disponible (que no esté bloqueado)
            boolean encontrado = false;
            int intentos = 0;
            
            while (!encontrado && intentos < numJugadors) {
                Jugador j = partida.getJugadors().get(seguentIndex);
                if (j.getTornsBloquejat() > 0) {
                    // Si está bloqueado, bajamos el contador y pasamos al siguiente
                    j.setTornsBloquejat(j.getTornsBloquejat() - 1);
                    vista.PantallaJuego.registrarEventoEstatico(j.getNickname() + " sigue bloqueado (" + j.getTornsBloquejat() + " turnos restantes).", "log-info");
                    seguentIndex = (seguentIndex + 1) % numJugadors;
                    intentos++;
                } else {
                    encontrado = true;
                }
            }
            
            partida.setIndexJugadorActual(seguentIndex);
            if (seguentIndex == 0) {
                partida.setTorns(partida.getTorns() + 1);
            }
        }
    }


    public void guardarPartida(Connection con) {
        if (partida != null) {
            gestorBBDD.guardarBBDD(partida, con);
        }
    }

    public Partida carregarPartida(int id, Connection con) {
        return gestorBBDD.carregarBBDD(id, con);
    }
}
