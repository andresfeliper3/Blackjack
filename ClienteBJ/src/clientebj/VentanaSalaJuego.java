package clientebj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import comunes.DatosBlackJack;

public class VentanaSalaJuego extends JInternalFrame {

	private PanelJugador dealer, yo, jugador2, jugador3;
	private JTextArea areaMensajes;
	private JButton pedir, plantar, otraRonda;
	private JPanel panelYo, panelBotones, yoFull, panelDealer, panelJugador2, panelJugador3, panelOtraRonda;

	private String yoId, jugador2Id, jugador3Id;
	// private DatosBlackJack datosRecibidos;
	private Escucha escucha;

	public VentanaSalaJuego(String yoId,int apuestaYo, String jugador2Id,int apuesta2, String jugador3Id, int apuesta3, int desktop_Width,int desktop_height) {
		this.yoId = yoId;
		this.jugador2Id = jugador2Id;
		this.jugador3Id = jugador3Id;
		// this.datosRecibidos=datosRecibidos;

		initGUI(apuestaYo, apuesta2,apuesta3);

		// default window settings
		this.setTitle("Sala de juego BlackJack - Jugador: " + yoId);
		this.pack();
		this.setLocation((desktop_Width/2) - this.getWidth()/2, (desktop_height/2) - this.getHeight()/2);
		this.setResizable(false);
		this.show();
	}

	private void initGUI(int apuestaYo,int apuesta2,int apuesta3) {
		// TODO Auto-generated method stub
		// set up JFrame Container y Layout
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		// Create Listeners objects
		escucha = new Escucha();
		// Create Control objects

		// Set up JComponents
		panelDealer = new JPanel();
		dealer = new PanelJugador("Dealer",99999);
		panelDealer.add(dealer);
		constraints.gridx = 1;
		constraints.gridy = 0;
		add(panelDealer, constraints);

		panelJugador2 = new JPanel();
		jugador2 = new PanelJugador(jugador2Id,apuesta2);
		panelJugador2.add(jugador2);
		constraints.gridx = 2;
		constraints.gridy = 1;
		add(panelJugador2, constraints);

		panelJugador3 = new JPanel();
		jugador3 = new PanelJugador(jugador3Id,apuesta3);
		panelJugador3.add(jugador3);
		constraints.gridx = 1;
		constraints.gridy = 2;
		add(panelJugador3, constraints);

		areaMensajes = new JTextArea(8, 18);
		JScrollPane scroll = new JScrollPane(areaMensajes);
		Border blackline;
		blackline = BorderFactory.createLineBorder(Color.black);
		TitledBorder bordes;
		bordes = BorderFactory.createTitledBorder(blackline, "Area de Mensajes");
		bordes.setTitleJustification(TitledBorder.CENTER);
		scroll.setBorder(bordes);
		areaMensajes.setOpaque(false);
		areaMensajes.setBackground(new Color(0, 0, 0, 0));
		areaMensajes.setEditable(false);

		scroll.getViewport().setOpaque(false);
		scroll.setOpaque(false);
		constraints.gridx = 1;
		constraints.gridy = 1;
		add(scroll, constraints);

		panelYo = new JPanel();
		panelYo.setLayout(new BorderLayout());
		yo = new PanelJugador(yoId,apuestaYo);
		panelYo.add(yo);

		pedir = new JButton("Carta");
		pedir.setEnabled(false);
		pedir.addActionListener(escucha);
		plantar = new JButton("Plantar");
		plantar.setEnabled(false);
		plantar.addActionListener(escucha);
		panelBotones = new JPanel();
		panelBotones.add(pedir);
		panelBotones.add(plantar);

		yoFull = new JPanel();
		yoFull.setPreferredSize(new Dimension(206, 180));
		yoFull.add(panelYo);
		yoFull.add(panelBotones);
		constraints.gridx = 0;
		constraints.gridy = 1;
		add(yoFull, constraints);
		
		otraRonda = new JButton("Otra ronda");
		panelOtraRonda = new JPanel(new BorderLayout());
		panelOtraRonda.add(otraRonda, BorderLayout.EAST);
		constraints.gridx = 2;
		constraints.gridy = 2;
		constraints.anchor = GridBagConstraints.SOUTHEAST;
		add(panelOtraRonda, constraints);
			
	}

	public void activarBotones(boolean turno) {
		pedir.setEnabled(turno);
		plantar.setEnabled(turno);
	}

	// Se llama cuando se crea la sala de juego por primera vez
	public void pintarCartasInicio(DatosBlackJack datosRecibidos) {
		System.out.println("pintarcartasinicio");
		//MENSAJE: Le están llegando en el orden incorrecto al reiniciar
		System.out.println(datosRecibidos.getIdJugadores()[0]);
		System.out.println(datosRecibidos.getIdJugadores()[1]);
		System.out.println(datosRecibidos.getIdJugadores()[2]);
		if (datosRecibidos.getIdJugadores()[0].equals(yoId)) {// Si yo estoy en la posición 0
			yo.pintarCartasInicio(datosRecibidos.getManoJugador1());// pinte cartas de inicio del jugador yo
			jugador2.pintarCartasInicio(datosRecibidos.getManoJugador2());// pinte las cartas del jugador 2
			jugador3.pintarCartasInicio(datosRecibidos.getManoJugador3());
		} else if (datosRecibidos.getIdJugadores()[1].equals(yoId)) { // si yo estoy en la posición 1
			yo.pintarCartasInicio(datosRecibidos.getManoJugador2()); 
			jugador2.pintarCartasInicio(datosRecibidos.getManoJugador1());
			jugador3.pintarCartasInicio(datosRecibidos.getManoJugador3());
		} else { // Si yo estoy en la posición 2
			yo.pintarCartasInicio(datosRecibidos.getManoJugador3());
			jugador2.pintarCartasInicio(datosRecibidos.getManoJugador1());
			jugador3.pintarCartasInicio(datosRecibidos.getManoJugador2());
		}
		dealer.pintarCartasInicio(datosRecibidos.getManoDealer());

		areaMensajes.append(datosRecibidos.getMensaje() + "\n");
	}

	// ver min 36
	public void pintarTurno(DatosBlackJack datosRecibidos) {
		areaMensajes.append(datosRecibidos.getMensaje() + "\n");
		ClienteBlackJack cliente = (ClienteBlackJack) this.getTopLevelAncestor();//

		if(datosRecibidos.isEnJuego()) {
			if (datosRecibidos.getJugador().equals(yoId)) {
				// Si me manda "iniciar" activo botones
				if (datosRecibidos.getJugadorEstado().equals("iniciar")) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							activarBotones(true);
						}
					});
				} // Si me manda "plantó" desactivo botones
				else if (datosRecibidos.getJugadorEstado().equals("plantó")) {
					cliente.setTurno(false);
				} else { // Pintar la carta porque no inicié ni planté
					yo.pintarLaCarta(datosRecibidos.getCarta());
					// Si me manda "voló", desactivar botones y ceder turno
					if (datosRecibidos.getJugadorEstado().equals("voló")) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								activarBotones(false);
								cliente.setTurno(false);
							}
						});
					}
				}
	
			} else if (datosRecibidos.getJugador().equals(jugador2Id)) {
				// mensaje para PanelJuego jugador2
				// Si sigue o vuela, pinta la carta
				// D: Y SI PLANTÓ???
				if (datosRecibidos.getJugadorEstado().equals("sigue") || datosRecibidos.getJugadorEstado().equals("voló")) {
					jugador2.pintarLaCarta(datosRecibidos.getCarta());
				}
			} else if (datosRecibidos.getJugador().equals(jugador3Id)) {
				if (datosRecibidos.getJugadorEstado().equals("sigue") || datosRecibidos.getJugadorEstado().equals("voló")) {
					jugador3.pintarLaCarta(datosRecibidos.getCarta());
				}
			} else {
				// mensaje para PanelJuego dealer
				if (datosRecibidos.getJugadorEstado().equals("sigue") || datosRecibidos.getJugadorEstado().equals("voló")
						|| datosRecibidos.getJugadorEstado().equals("plantó")) {
					dealer.pintarLaCarta(datosRecibidos.getCarta());
				}
	
			}
		}
	}
	
	private void enviarDatos(String mensaje) {
		// TODO Auto-generated method stub
		ClienteBlackJack cliente = (ClienteBlackJack) this.getTopLevelAncestor();
		cliente.enviarMensajeServidor(mensaje);
	}
	
	public void cerrarVentanaSalaJuego() {
		this.dispose();
	}
	public Container getContainerFrames() {
		return this.getParent();
	}
    
	private class Escucha implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			// TODO Auto-generated method stub
			if (actionEvent.getSource() == pedir) {
				// enviar pedir carta al servidor
				enviarDatos("pedir");
			} else {
				// enviar plantar al servidor
				enviarDatos("plantar");
				activarBotones(false);
			}
		}
	}
}
