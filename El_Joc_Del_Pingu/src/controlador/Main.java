package controlador;

import java.sql.Connection;
import java.util.Scanner;
import model.items.Inventari;
import model.items.Item;
import model.entitats.Pingui;
import model.items.BolaNeu;

public class Main {
	BolaNeu BN = new BolaNeu ("BolaNeu", 2, 3);
	
	Inventari inv = new Inventari();

	Pingui pingu = new Pingui("PereBC", "azul", inv);
}
