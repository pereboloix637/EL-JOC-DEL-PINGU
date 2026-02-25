package model.entitats;

public class Jugador {
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
	
	// AUTOMIZAT AMB NICKNAME
	public Jugador (String nickname, String color) {
		this.nickname = nickname;
		this.posicio = 0;
		this.color = color;
	
	/// METODES
	// PROXIMAMENT
	
	}
}
