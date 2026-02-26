package model.caselles;

import model.core.Partida;
import model.entitats.Jugador;
import model.entitats.Pingui;
import model.items.Peix;

public class Os extends Casella {

    public Os(int posicio) {
        super(posicio);
    }

    @Override
    public void realitzarAccio(Partida partida, Jugador jugador) {
        if (!(jugador instanceof Pingui)) return;
        Pingui pingui = (Pingui) jugador;

        // Busca un peix a l'inventari
        Peix peix = (Peix) pingui.getInventari().getLlista().stream()
            .filter(i -> i instanceof Peix && i.getQuantitat() > 0)
            .findFirst().orElse(null);

        if (peix != null) {
            pingui.getInventari().usarItem(peix);
            System.out.println(pingui.getNickname() + " ha estat atacat per un ós! Però tenia un peix i s'ha salvat.");
        } else {
            pingui.setPosicio(0);
            System.out.println(pingui.getNickname() + " ha estat atacat per un ós! Torna a l'inici.");
        }
    }
}
