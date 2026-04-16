package model.entitats;

public abstract class Jugador {
	private int id;
	private String nickname;
	private int posicio;
	private String color;
	private int tornsBloquejat;
	private boolean nerfOs;
	private int idPartida; // ID lógico para usar como índice en arrays
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIdPartida() {
		return idPartida;
	}

	public void setIdPartida(int idPartida) {
		this.idPartida = idPartida;
	}

	public int getTornsBloquejat() {
		return tornsBloquejat;
	}

	public void setTornsBloquejat(int tornsBloquejat) {
		this.tornsBloquejat = tornsBloquejat;
	}

	public boolean isNerfOs() {
		return nerfOs;
	}

	public void setNerfOs(boolean nerfOs) {
		this.nerfOs = nerfOs;
	}
	
	/// CONSTRUCTORES
	// CON TODOS LOS PARÁMETROS
	public Jugador (String nickname, int posicio, String color) {
		this.id = 0;
		this.nickname = nickname;
		this.posicio = posicio;
		this.color = color;
		this.tornsBloquejat = 0;
		this.nerfOs = false;
	}
	
	// AUTOMÁTICO CON NICKNAME Y COLOR -> Pensado para poner al jugador al inicio del tablero
	public Jugador (String nickname, String color) {
		this.id = 0;
		this.nickname = nickname;
		this.posicio = 0;
		this.color = color;
		this.tornsBloquejat = 0;
		this.nerfOs = false;
	}
	
	/// GETTERS Y SETTERS
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
	
	/// MÉTODOS
	public void mourePosicio(int p) {
		this.posicio = Math.max(0, this.posicio + p);
	}

	
}
