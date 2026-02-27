package model.entitats;

public abstract class Jugador {
	/// ATRIBUTS
	private String nickname;
	private int posicio;
	private String color;
	
	/// CONSTRUCTORS
	// AMB TOTS ELS PARAMETRES
	public Jugador (String nickname, int posicio, String color) {
	this.nickname = nickname;
	this.posicio = posicio;
	this.color = color;
	}
	
	// AUTOMIZAT AMB NICKNAME I COLOR -> Esta pensat per ficar al jugador al inici del tabler
	public Jugador (String nickname, String color) {
		this.nickname = nickname;
		this.posicio = 0;
		this.color = color;
		}
	
	/// GETTERS I SETTERS
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getPosicio() {
		return posicio;
	}

	public void setPosicio(int posicio) {
		this.posicio = posicio;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	/// METODES
	public void mourePosicio(int p) {
    this.posicio = Math.max(0, this.posicio + p);
	}
}
