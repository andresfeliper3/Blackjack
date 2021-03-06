package clientebj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import comunes.Carta;
import comunes.FileIO;

public class PanelJugador extends JPanel {
	//constantes de clase
	private static final int ANCHO = 206;
	private static final int ALTO = 135;
	
	
	//variables para control del graficado
	private ArrayList<Recuerdo> dibujoRecordar;
	private int x;
	private Titulos capitalJugador;
	    
	public PanelJugador(String nombreJugador, int capitalJugador) {
		this.setBackground(Color.GREEN);
		String monto = String.valueOf(capitalJugador);
		this.capitalJugador = new Titulos("Capital: " + monto, 20, Color.YELLOW, Color.BLACK);
		//this.capitalJugador.setText(String.valueOf(capitalJugador));

		dibujoRecordar = new ArrayList<Recuerdo>();
		this.setPreferredSize(new Dimension(ANCHO,ALTO));
		TitledBorder bordes;
		bordes = BorderFactory.createTitledBorder(nombreJugador);
		//bordes.isBorderOpaque();
		this.setBorder(bordes);
		
		add(this.capitalJugador,BorderLayout.NORTH);
		
		
	}
	
	public void pintarCartasInicio(ArrayList<Carta> manoJugador) {
		x=5;
	    for(int i=0;i<manoJugador.size();i++) {
	    	dibujoRecordar.add(new Recuerdo(manoJugador.get(i),x,manoJugador.get(i).getImagen()));
	    	x+=27;
	    }			
	    repaint();
	}
	
	public void pintarLaCarta (Carta carta) {
		dibujoRecordar.add(new Recuerdo(carta,x,carta.getImagen()));
		x+=27;
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Font font =new Font(Font.DIALOG,Font.BOLD,12);
		g.setFont(font);
				
		for(int i=0;i<dibujoRecordar.size();i++) {
			g.drawImage(dibujoRecordar.get(i).getImagenRecordar(), dibujoRecordar.get(i).getxRecordar(),50, this);
		}
	}
	
	private class Recuerdo{
		public static final String RUTA_FILE = "/recursos/cards.png";
		private Carta cartaRecordar;
		private int xRecordar;
		private Image imagenRecordar;
		private BufferedImage imagenCarta;

		public Recuerdo(Carta cartaRecordar, int xRecordar,Image ImagenRecordar) {
			this.cartaRecordar = cartaRecordar;
			this.xRecordar = xRecordar;
		}
		public Image getImagenRecordar() {
			
			BufferedImage imagenesCartas = FileIO.readImageFile(this, RUTA_FILE);
			imagenCarta = imagenesCartas.getSubimage(cartaRecordar.getCoordenadaX(), cartaRecordar.getCoordenadaY(), Carta.WIDTH,Carta.HEIGHT);
			return imagenCarta;
		}
		
		public int getxRecordar() {
			return xRecordar;
		}
	}

}
