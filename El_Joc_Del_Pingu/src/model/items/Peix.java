package model.items;

public class Peix extends Item {

  // Constructor
public Peix(String nom, int quantitat) {
    super(nom, quantitat);
}

public void usarPeix() {
    if (getQuantitat() > 0) {
        setQuantitat(getQuantitat() - 1);
        System.out.println("Has usat un peix! Et queden " + getQuantitat() + " peixos.");
    } else {
        System.out.println("No tens peixos!");
    }
}
}