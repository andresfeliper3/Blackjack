package comunes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

public class Baraja {
	private ArrayList<Carta> mazo;
	private Random aleatorio;

	public Baraja() {
		aleatorio = new Random();
		mazo = new ArrayList<Carta>();
		String valor;
		for (int i = 1; i <= 4; i++) {
			for (int j = 2; j <= 14; j++) {
				switch (j) {
				case 11:
					valor = "J";
					break;
				case 12:
					valor = "Q";
					break;
				case 13:
					valor = "K";
					break;
				case 14:
					valor = "As";
					break;
				default:
					valor = String.valueOf(j);
					break;
				}
				switch (i) {
				case 1:
					mazo.add(new Carta(valor, "C"));
					break;
				case 2:
					mazo.add(new Carta(valor, "D"));
					break;
				case 3:
					mazo.add(new Carta(valor, "P"));
					break;
				case 4:
					mazo.add(new Carta(valor, "T"));
					break;
				}
			}
		}
		asignarImagen();
	}

	private void asignarImagen() {
		try {
			int index = 0;
			BufferedImage imagenesCartas = ImageIO.read(new File("src/recursos/cards.png"));
			for (int i = 0; i <= 180; i += 60) {
				for (int j = 0; j <= 540; j += 45) {
					mazo.get(index).setImagen(imagenesCartas.getSubimage(j, i, 45, 60));
					index++;
				}
			}	

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Carta getCarta() {
		int index = aleatorio.nextInt(mazoSize());
		Carta carta = mazo.get(index);
		mazo.remove(index); // elimina del mazo la carta usada
		return carta;
	}

	public int mazoSize() {
		return mazo.size();
	}
}
