package clientebj;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import comunes.Carta;

public class PanelJugador extends JPanel {
	//constantes de clase
	private static final int ANCHO = 206;
	private static final int ALTO = 89;
	
	//variables para control del graficado
	private ArrayList<Recuerdo> dibujoRecordar;
	private int x;
	    
	public PanelJugador(String nombreJugador) {
		//this.setBackground(Color.GREEN);
		dibujoRecordar = new ArrayList<Recuerdo>();
		this.setPreferredSize(new Dimension(ANCHO,ALTO));
		TitledBorder bordes;
		bordes = BorderFactory.createTitledBorder(nombreJugador);
		this.setBorder(bordes);
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
				
		//pinta la mano inicial
//		for(int i=0;i<dibujoRecordar.size();i++) {
//			g.drawString(dibujoRecordar.get(i).getCartaRecordar(), dibujoRecordar.get(i).getxRecordar(),35);
//		}	
		for(Recuerdo carta : dibujoRecordar) {
			g.drawImage(carta.getImagenRecordar(), carta.getxRecordar(),20, this);
		}
	}
	
	private class Recuerdo{
		private Carta cartaRecordar;
		private int xRecordar;
		private Image imagenRecordar;

		public Recuerdo(Carta cartaRecordar, int xRecordar,Image ImagenRecordar) {
			this.cartaRecordar = cartaRecordar;
			this.xRecordar = xRecordar;
			this.imagenRecordar=imagenRecordar;
		}

		public String getCartaRecordar() {
			return cartaRecordar.toString();
		}
		public Image getImagenRecordar() {
			return imagenRecordar;
		}
		
		public int getxRecordar() {
			return xRecordar;
		}
	}

}
