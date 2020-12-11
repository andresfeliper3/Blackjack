package comunes;

import java.awt.Image;
import java.io.Serializable;

import javax.swing.ImageIcon;

public class Carta implements Serializable{
	
	public static final int WIDTH = 45;
	public static final int HEIGHT = 60;
    private String valor;
    private String palo;
    private transient Image imagen;
    private int coordenadaX, coordenadaY;
	
    public Carta(String valor, String palo) {
		this.valor = valor;
		this.palo = palo;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getPalo() {
		return palo;
	}

	public void setPalo(String palo) {
		this.palo = palo;
	}
	
	public int getCoordenadaX() {
		return coordenadaX;
	}

	public void setCoordenadaX(int coordenadaX) {
		this.coordenadaX = coordenadaX;
	}

	public int getCoordenadaY() {
		return coordenadaY;
	}

	public void setCoordenadaY(int coordenadaY) {
		this.coordenadaY = coordenadaY;
	}

	public String toString() {
		return valor+palo;
	}
	public Image getImagen() {
		return imagen;
	}

	public void setImagen(Image imagen) {
		this.imagen = imagen;
	}
}
