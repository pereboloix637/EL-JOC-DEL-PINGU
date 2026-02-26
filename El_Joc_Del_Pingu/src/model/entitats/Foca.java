package model.entitats;

public class Foca extends Jugador {
/// ATRIBUTS
private boolean soborno;

/// CONSTRUCTOR
public Foca(String nickname, String color, boolean soborno) {
	super(nickname, color);   // LLAMADA OBLIGATORIA
	this.soborno = soborno;
	}

/// GETTERS I SETTERS
public boolean isSoborno() {
	return soborno;
}

public void setSoborno(boolean soborno) {
	this.soborno = soborno;
}

/// METODES
public void aplastarPingu (Pingui p) { // La Foca aplasta al Pingui elegit
// treure items
}

public void pegarPingu (Pingui p) { // La Foca atacara al Pingui elegit
// retrocedir al pingu
}

public void sobornarFoca () { // Permet sobornar a la Foca
soborno = true;
}





}
