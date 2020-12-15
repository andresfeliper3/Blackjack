package servidorbj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JOptionPane;

import comunes.Baraja;
import comunes.Carta;
import comunes.DatosBlackJack;

/* Clase encargada de realizar la gestión del juego, esto es, el manejo de turnos y estado del juego.
 * También gestiona al jugador Dealer. 
 * El Dealer tiene una regla de funcionamiento definida:
 * Pide carta con 16 o menos y Planta con 17 o mas.
 */
public class ServidorBJ implements Runnable {
	// constantes para manejo de la conexion.
	public static final int PUERTO = 7377;
	public static final String IP = "127.0.0.1";
	public static final int LONGITUD_COLA = 3;

	// variables para funcionar como servidor
	private ServerSocket server;
	private Socket conexionJugador;

	// variables para manejo de hilos
	private ExecutorService manejadorHilos;
	private Lock bloqueoJuego;
	private Condition esperarInicio, esperarTurno, finalizar;
	private Jugador[] jugadores;

	// variables de control del juego
	private String[] idJugadores;
	private int[] apuesta = new int[4];
	private String[] estadosJugadores = new String[4];
	private int jugadorEnTurno;
	private int contador=0;
	// private boolean iniciarJuego;
	private Baraja mazo;
	private ArrayList<ArrayList<Carta>> manosJugadores;
	private ArrayList<Carta> manoJugador1;
	private ArrayList<Carta> manoJugador2;
	private ArrayList<Carta> manoJugador3;
	private ArrayList<Carta> manoDealer;
	private int[] valorManos;
	private DatosBlackJack datosEnviar;
	private boolean seTerminoRonda = false;

	public ServidorBJ() {
		// inicializar variables de control del juego
		inicializarVariablesControlRonda();
		// inicializar las variables de manejo de hilos
		inicializareVariablesManejoHilos();
		// crear el servidor
		try {
			mostrarMensaje("Iniciando el servidor...");
			server = new ServerSocket(PUERTO, LONGITUD_COLA); // Establecer la instancia como servidor
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void inicializareVariablesManejoHilos() {
		// TODO Auto-generated method stub
		manejadorHilos = Executors.newFixedThreadPool(LONGITUD_COLA);
		bloqueoJuego = new ReentrantLock();
		esperarInicio = bloqueoJuego.newCondition();
		esperarTurno = bloqueoJuego.newCondition();
		finalizar = bloqueoJuego.newCondition();
		jugadores = new Jugador[LONGITUD_COLA];
	}

	private void inicializarVariablesControlRonda() {
		// TODO Auto-generated method stub
		// Variables de control del juego.

		idJugadores = new String[LONGITUD_COLA]; // Jugadores en juego sin contar el dealer, clientes
		valorManos = new int[LONGITUD_COLA + 1]; // 3 jugadores y 1 dealer

		mazo = new Baraja();
		Carta carta;

		// Creación de las manos
		manoJugador1 = new ArrayList<Carta>();
		manoJugador2 = new ArrayList<Carta>();
		manoJugador3 = new ArrayList<Carta>();
		manoDealer = new ArrayList<Carta>();

		// reparto inicial jugadores 1 y 2
		for (int i = 1; i <= 2; i++) {
			// jugador 1
			carta = mazo.getCarta();
			manoJugador1.add(carta);
			calcularValorMano(manoJugador1, carta, 0);
			// jugador 2
			carta = mazo.getCarta();
			manoJugador2.add(carta);
			calcularValorMano(manoJugador2, carta, 1);
			// jugador 3
			carta = mazo.getCarta();
			manoJugador3.add(carta);
			calcularValorMano(manoJugador3, carta, 2);
		}
		// Carta inicial Dealer
		carta = mazo.getCarta();
		manoDealer.add(carta);
		calcularValorMano(manoDealer, carta, 3);

		// gestiona las tres manos en un solo objeto para facilitar el manejo del hilo
		manosJugadores = new ArrayList<ArrayList<Carta>>(LONGITUD_COLA + 1);// JUgadores y el dealer
		manosJugadores.add(manoJugador1);
		manosJugadores.add(manoJugador2);
		manosJugadores.add(manoJugador3);
		manosJugadores.add(manoDealer);
	}

	private void calcularValorMano(ArrayList<Carta> mano, Carta carta, int i) {
		// TODO Auto-generated method stub
		if (carta.getValor().equals("As")) {

			valorManos[i] += 11;

		} else {
			if (carta.getValor().equals("J") || carta.getValor().equals("Q") || carta.getValor().equals("K")) {
				valorManos[i] += 10;
			} else {
				valorManos[i] += Integer.parseInt(carta.getValor());
			}
		}
		// Revisar si tiene una carta As, su valor puede variar
		if (contieneAs(mano) && valorManos[i] > 21) {

			revisarAsMano(mano, i);
		}

		if (mano.size() == 2 && valorManos[i] == 21) {// Guarda si el jugador tiene un Black Jack, es decir un As, y una
														// J, Q, K

			estadosJugadores[i] = "blackJack";

		} else {

			estadosJugadores[i] = "normal";
		}
	}

	// Analizar cuántos cambios de As debe hacer
	private void revisarAsMano(ArrayList<Carta> mano, int i) {

		for (int j = 0; j < mano.size(); j++) {
			if (mano.get(j).getValor().equals("As")) {
				if (valorManos[i] > 21 && !mano.get(j).isValorCambiado()) {
					mano.get(j).setValorCambiado(true);
					valorManos[i] -= 10;
				}
			}
		}
	}

	// retorna true si la lista contiene una carta As
	private boolean contieneAs(ArrayList<Carta> mano) {
		for (Carta carta : mano) {
			if (carta.getValor().equals("As")) {
				return true;
			}
		}
		return false;
	}

	public void iniciar() {
		// esperar a los clientes
		mostrarMensaje("Esperando a los jugadores...");

		for (int i = 0; i < LONGITUD_COLA; i++) {
			try {
				conexionJugador = server.accept();// estar pendiente a que llegue un cliente
				mostrarMensaje("Antes de crear el jugador " + i);
				jugadores[i] = new Jugador(conexionJugador, i); // crea el hilo y lo agrega al arreglo de hilos
				mostrarMensaje("Hizo conexión el indexJugador " + i);
				manejadorHilos.execute(jugadores[i]); // Ejecutamos el hilo recién creado
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void mostrarMensaje(String mensaje) {
		System.out.println(mensaje);
	}

	// Despierta al jugador 0 para iniciar la ronda de juego (después de sala de
	// espera)
	private void iniciarRondaJuego() {

		this.mostrarMensaje("bloqueando al servidor para despertar al jugador 1");
		bloqueoJuego.lock();

		// despertar al jugador 1 porque es su turno
		try {
			this.mostrarMensaje("Despertando al jugador 1 para que inicie el juego");
			jugadores[0].setSuspendido(false);
			jugadores[1].setSuspendido(false);
			esperarInicio.signalAll(); // POR QUÉ?
		} catch (Exception e) {

		} finally {
			this.mostrarMensaje("Desbloqueando al servidor luego de despertar al jugador 1 para que inicie el juego");
			bloqueoJuego.unlock();
		}
	}


	private void analizarMensaje(String entrada, int indexJugador) {
		// TODO Auto-generated method stub
		// garantizar que solo se analice la petición del jugador en turno.
		mostrarMensaje("jugador " + idJugadores[indexJugador] + "entró a analizarMensaje, " + entrada);
		while (indexJugador != jugadorEnTurno) {

			bloqueoJuego.lock();
			try {
				mostrarMensaje("jugador " + idJugadores[indexJugador] + "Se echó a mimir");
				esperarTurno.await();
				mostrarMensaje("jugador " + idJugadores[indexJugador] + "Se despertó");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//PUSE EL FINALLY
			} finally {
				contador++;
				bloqueoJuego.unlock();
			}
			
		}
		// valida turnos para jugador 0 o 1

		if (entrada.equals("pedir")) {
			// dar carta
			mostrarMensaje("Se envió carta al jugador " + idJugadores[indexJugador]);
			Carta carta = mazo.getCarta();
			// adicionar la carta a la mano del jugador en turno
			manosJugadores.get(indexJugador).add(carta);
			calcularValorMano(manosJugadores.get(indexJugador), carta, indexJugador);

			datosEnviar = new DatosBlackJack();
			datosEnviar.setIdJugadores(idJugadores);
			datosEnviar.setValorManos(valorManos);
			datosEnviar.setCarta(carta);
			datosEnviar.setJugador(idJugadores[indexJugador]);
			// determinar qué sucede con la carta dada en la mano del jugador y
			// mandar mensaje a todos los jugadores
			if (valorManos[indexJugador] > 21) {
				// jugador Voló

				estadosJugadores[indexJugador] = "voló";

				datosEnviar
						.setMensaje(idJugadores[indexJugador] + " tienes " + valorManos[indexJugador] + " volaste :(");
				datosEnviar.setJugadorEstado("voló");

				jugadores[0].enviarMensajeCliente(datosEnviar);
				jugadores[1].enviarMensajeCliente(datosEnviar);
				jugadores[2].enviarMensajeCliente(datosEnviar);

				// notificar a todos cuál jugador sigue
				if (jugadorEnTurno == 0) {

					datosEnviar = new DatosBlackJack();
					datosEnviar.setIdJugadores(idJugadores);
					datosEnviar.setValorManos(valorManos);
					// avisa cuál es el jugador siguiente
					datosEnviar.setJugador(idJugadores[1]);
					datosEnviar.setJugadorEstado("iniciar");
					datosEnviar.setMensaje(idJugadores[1] + " te toca jugar y tienes " + valorManos[1]);

					jugadores[0].enviarMensajeCliente(datosEnviar);
					jugadores[1].enviarMensajeCliente(datosEnviar);
					jugadores[2].enviarMensajeCliente(datosEnviar);

					// levantar al jugador en espera de turno

					bloqueoJuego.lock();
					try {
						// esperarInicio.await();
						jugadores[0].setSuspendido(true);
						esperarTurno.signalAll();
						jugadorEnTurno++;
					} finally {
						bloqueoJuego.unlock();
						determinarRondaJuego(indexJugador);
					}
				} else if (jugadorEnTurno == 1) {
					datosEnviar = new DatosBlackJack();
					datosEnviar.setIdJugadores(idJugadores);
					datosEnviar.setValorManos(valorManos);
					// avisa cuál es el jugador siguiente
					datosEnviar.setJugador(idJugadores[2]);
					datosEnviar.setJugadorEstado("iniciar");
					datosEnviar.setMensaje(idJugadores[2] + " te toca jugar y tienes " + valorManos[2]);

					jugadores[0].enviarMensajeCliente(datosEnviar);
					jugadores[1].enviarMensajeCliente(datosEnviar);
					jugadores[2].enviarMensajeCliente(datosEnviar);

					// levantar al jugador en espera de turno

					bloqueoJuego.lock();
					try {
						// esperarInicio.await();
						jugadores[1].setSuspendido(true);
						esperarTurno.signalAll();
						jugadorEnTurno++;
					} finally {
						bloqueoJuego.unlock();
						determinarRondaJuego(indexJugador);
					}
				} else {// era el jugador 3 entonces se debe iniciar el dealer
						// notificar a todos que le toca jugar al dealer
					datosEnviar = new DatosBlackJack();
					datosEnviar.setIdJugadores(idJugadores);
					datosEnviar.setValorManos(valorManos);
					datosEnviar.setJugador("dealer");
					datosEnviar.setJugadorEstado("iniciar");
					datosEnviar.setMensaje("Dealer se repartirá carta");

					jugadores[0].enviarMensajeCliente(datosEnviar);
					jugadores[1].enviarMensajeCliente(datosEnviar);
					jugadores[2].enviarMensajeCliente(datosEnviar);

					iniciarDealer();
					determinarRondaJuego(indexJugador);
				}
			} else {// jugador no se pasa de 21 puede seguir jugando
				datosEnviar.setCarta(carta);
				datosEnviar.setJugador(idJugadores[indexJugador]);
				datosEnviar.setMensaje(idJugadores[indexJugador] + " ahora tienes " + valorManos[indexJugador]);
				datosEnviar.setJugadorEstado("sigue");

				jugadores[0].enviarMensajeCliente(datosEnviar);
				jugadores[1].enviarMensajeCliente(datosEnviar);
				jugadores[2].enviarMensajeCliente(datosEnviar);

			}
		} else {
			// jugador en turno plantó
			datosEnviar = new DatosBlackJack();
			datosEnviar.setIdJugadores(idJugadores);
			datosEnviar.setValorManos(valorManos);
			datosEnviar.setJugador(idJugadores[indexJugador]);
			datosEnviar.setMensaje(idJugadores[indexJugador] + " se plantó");
			datosEnviar.setJugadorEstado("plantó");

			jugadores[0].enviarMensajeCliente(datosEnviar);
			jugadores[1].enviarMensajeCliente(datosEnviar);
			jugadores[2].enviarMensajeCliente(datosEnviar);

			// notificar a todos el jugador que sigue en turno
			if (jugadorEnTurno == 0) {

				datosEnviar = new DatosBlackJack();
				datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				datosEnviar.setJugador(idJugadores[1]);
				datosEnviar.setJugadorEstado("iniciar");
				datosEnviar.setMensaje(idJugadores[1] + " te toca jugar y tienes " + valorManos[1]);

				jugadores[0].enviarMensajeCliente(datosEnviar);
				jugadores[1].enviarMensajeCliente(datosEnviar);
				jugadores[2].enviarMensajeCliente(datosEnviar);
				// levantar al jugador en espera de turno

				bloqueoJuego.lock();
				try {
					// esperarInicio.await();
					jugadores[indexJugador].setSuspendido(true);
					esperarTurno.signalAll();
					jugadorEnTurno++;
				} finally {
					bloqueoJuego.unlock();
					determinarRondaJuego(indexJugador);
				}
			} else if (jugadorEnTurno == 1) {

				datosEnviar = new DatosBlackJack();
				datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				datosEnviar.setJugador(idJugadores[2]);
				datosEnviar.setJugadorEstado("iniciar");
				datosEnviar.setMensaje(idJugadores[2] + " te toca jugar y tienes " + valorManos[2]);

				jugadores[0].enviarMensajeCliente(datosEnviar);
				jugadores[1].enviarMensajeCliente(datosEnviar);
				jugadores[2].enviarMensajeCliente(datosEnviar);
				// levantar al jugador en espera de turno

				bloqueoJuego.lock();
				try {
					// esperarInicio.await();
					jugadores[indexJugador].setSuspendido(true);
					esperarTurno.signalAll();
					jugadorEnTurno++;
				} finally {
					bloqueoJuego.unlock();
					determinarRondaJuego(indexJugador);
				}
			} else {
				// notificar a todos que le toca jugar al dealer
				datosEnviar = new DatosBlackJack();
				datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setValorManos(valorManos);
				datosEnviar.setJugador("dealer");
				datosEnviar.setJugadorEstado("iniciar");
				datosEnviar.setMensaje("Dealer se repartirá carta");

				jugadores[0].enviarMensajeCliente(datosEnviar);
				jugadores[1].enviarMensajeCliente(datosEnviar);
				jugadores[2].enviarMensajeCliente(datosEnviar);

				iniciarDealer();
				determinarRondaJuego(indexJugador);
			}
		}
	}

	private void determinarRondaJuego(int indexJugador) {
		mostrarMensaje("Entró a determinarRonda");
		datosEnviar = new DatosBlackJack();
		
		if (indexJugador != 3) {

			// Avisar al cliente para que coloque el JOptionPane
			bloqueoJuego.lock();
			try {
				mostrarMensaje("jugador " + idJugadores[indexJugador] + "Se echó a mimir en determinar ronda juego");
				finalizar.await();
				mostrarMensaje("jugador " + idJugadores[indexJugador] + "Se despertó en determinar ronda juego");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				mostrarMensaje("Desbloquea al servidor después de que " + idJugadores[indexJugador] + " pasó");
				bloqueoJuego.unlock();
			}		
		}
		/*
		 * EMPATE Ambos blackjack Ambos mismo valor Ambos pierden3
		 */
		if(indexJugador == 3) {
			for (int i = 0; i < 3; i++) {
				// Empate
				if ((estadosJugadores[i].equals(estadosJugadores[3]) && estadosJugadores[i].equals("blackjack"))
						|| (estadosJugadores[i].equals(estadosJugadores[3]) && estadosJugadores[i].equals("voló"))
						|| (valorManos[i] == valorManos[3])) {
					// Se le devuelve la apuesta
					datosEnviar.setMensaje("Empató el jugador " + idJugadores[i] + " con el dealer");
				}
				// Vuela el dealer
				else if (estadosJugadores[3].equals("voló")) {
					// Gana el que no voló
					datosEnviar.setMensaje("Gana el jugador " + idJugadores[i] + " porque el dealer voló");
				}
				// Vuelva el jugador
				else if (estadosJugadores[i].equals("voló")) {
					datosEnviar.setMensaje("Gana el dealer, porque " + idJugadores[i] + " voló");
				}
				// El dealer tiene blackjack
				else if (estadosJugadores[3].equals("blackjack")) {
					// Gana el que tenga blackjack
					datosEnviar.setMensaje("Gana el dealer y tiene blackjack, pierde " + idJugadores[i]);
				}
				// El jugador tiene blackjack
				else if (estadosJugadores[i].equals("blackjack")) {
					datosEnviar.setMensaje("Gana el jugador" + idJugadores[i] + " y tiene blackjack");

				}
				// Gana quien esté más cerca del 21
				else if (valorManos[i] > valorManos[3]) {
					datosEnviar.setMensaje("Gana el jugador " + idJugadores[i] + " pues tiene " + valorManos[i]);
				} else {
					datosEnviar.setMensaje("Gana el dealer, porque " + idJugadores[i] + " tiene " + valorManos[i]
							+ " y el dealer tiene " + valorManos[3]);
				}
				datosEnviar.setIdJugadores(idJugadores);
				datosEnviar.setJugador(idJugadores[i]);
				datosEnviar.setJugadorEstado("finalizar");
				datosEnviar.setEnJuego(false);
				mostrarMensaje("El booleano enJuego es " + datosEnviar.isEnJuego());
				jugadores[i].enviarMensajeCliente(datosEnviar);
				jugadores[0].setSuspendido(true);
				jugadores[1].setSuspendido(true);
				jugadores[2].setSuspendido(true);
				
			}
			// Dealer despierta los hilos.
			seTerminoRonda = true;
			mostrarMensaje("seTerminoRonda se vuelve " + seTerminoRonda);
			bloqueoJuego.lock();
			finalizar.signalAll();
			bloqueoJuego.unlock();
			
		}
		mostrarMensaje("Al final de determinarRondaJuego con jugador " + indexJugador);
	}
	private void reiniciarVariables() {
		estadosJugadores = new String[4];
		//jugadorEnTurno = 0;
		contador =0;
		seTerminoRonda = false;
		valorManos = new int[LONGITUD_COLA + 1]; // 3 jugadores y 1 dealer

		mazo = new Baraja();
		Carta carta;

		// Creación de las manos
		manoJugador1 = new ArrayList<Carta>();
		manoJugador2 = new ArrayList<Carta>();
		manoJugador3 = new ArrayList<Carta>();
		manoDealer = new ArrayList<Carta>();

		// reparto inicial jugadores 1 y 2
		for (int i = 1; i <= 2; i++) {
			// jugador 1
			carta = mazo.getCarta();
			manoJugador1.add(carta);
			calcularValorMano(manoJugador1, carta, 0);
			// jugador 2
			carta = mazo.getCarta();
			manoJugador2.add(carta);
			calcularValorMano(manoJugador2, carta, 1);
			// jugador 3
			carta = mazo.getCarta();
			manoJugador3.add(carta);
			calcularValorMano(manoJugador3, carta, 2);
		}
		// Carta inicial Dealer
		carta = mazo.getCarta();
		manoDealer.add(carta);
		calcularValorMano(manoDealer, carta, 3);

		// gestiona las tres manos en un solo objeto para facilitar el manejo del hilo
		manosJugadores = new ArrayList<ArrayList<Carta>>(LONGITUD_COLA + 1);// JUgadores y el dealer
		manosJugadores.add(manoJugador1);
		manosJugadores.add(manoJugador2);
		manosJugadores.add(manoJugador3);
		manosJugadores.add(manoDealer);
		
	}

	public void iniciarDealer() {
		// le toca turno al dealer.
		Thread dealer = new Thread(this);
		dealer.start();
	}

	/*
	 * The Class Jugador. Clase interna que maneja el servidor para gestionar la
	 * comunicación con cada cliente Jugador que se conecte
	 */
	private class Jugador implements Runnable {

		// varibles para gestionar la comunicación con el cliente (Jugador) conectado
		private Socket conexionCliente;
		private ObjectOutputStream out;
		private ObjectInputStream in;
		private String entrada;

		// variables de control
		private int indexJugador;
		private boolean suspendido; // booleano de control del hilo jugador

		public Jugador(Socket conexionCliente, int indexJugador) {
			this.conexionCliente = conexionCliente;
			this.indexJugador = indexJugador;
			suspendido = true;
			// crear los flujos de E/S
			try {
				out = new ObjectOutputStream(conexionCliente.getOutputStream());
				out.flush();
				in = new ObjectInputStream(conexionCliente.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Cambiar el estado de suspendido del hilo
		private void setSuspendido(boolean suspendido) {
			this.suspendido = suspendido;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true) {
				
				while(seTerminoRonda) {
					//mostrarMensaje("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA " + indexJugador);
				}
				// procesar los mensajes eviados por el cliente
				mostrarMensaje("Entró al run el indexJugador " + indexJugador);
				// ver cual jugador es
				if (indexJugador == 0) {
					// es jugador 1, debe ponerse en espera a la llegada del otro jugador

					try {
						// guarda el nombre del primer jugador
						mostrarMensaje("Jugador con indexJugador " + indexJugador + " va a esperar para leer");
						if(idJugadores[0] == null) {
							idJugadores[0] = (String) in.readObject();// Recoger el nombre del jugador
							apuesta[0] = (int) in.readObject();
						}
						
						mostrarMensaje("Hilo establecido con jugador (0) " + idJugadores[0] + " con apuesta " + apuesta);
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					mostrarMensaje("bloquea servidor para poner en espera de inicio al jugador 1");
					bloqueoJuego.lock(); // bloquea el servidor

					while (suspendido) {// Si el hilo está suspendido, se irá a dormir el hilo
						mostrarMensaje("Parando al Jugador 1 en espera del otro jugador...");
						try {
							esperarInicio.await();// Hilo duerme y despierta aquí
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							mostrarMensaje("Desbloquea Servidor luego de bloquear al jugador 1");
							bloqueoJuego.unlock();
						}
					}

					// ya se conectaron todos los jugadores
					// le manda al jugador 1 todos los datos para montar la sala de Juego
					// le toca el turno a jugador 1
					mostrarMensaje("manda al jugador 1 todos los datos para montar SalaJuego");
					datosEnviar.setEnJuego(true);
					datosEnviar = new DatosBlackJack();
					datosEnviar.setManoDealer(manosJugadores.get(3));
					datosEnviar.setManoJugador1(manosJugadores.get(0));
					datosEnviar.setManoJugador2(manosJugadores.get(1));
					datosEnviar.setManoJugador3(manosJugadores.get(2));
					datosEnviar.setApuestasJugadores(apuesta);
					datosEnviar.setIdJugadores(idJugadores);
					datosEnviar.setValorManos(valorManos);
					datosEnviar.setMensaje("Inicias " + idJugadores[0] + " tienes " + valorManos[0]);
					datosEnviar.setMensaje(idJugadores[0] + " Apostaste: " + apuesta[0]);
					enviarMensajeCliente(datosEnviar); // con esto construye la mesa
					jugadorEnTurno = 0;
				} else if (indexJugador == 1) {
					// es jugador 2, debe ponerse en espera a la llegada del otro jugador

					try {
						// guarda el nombre del primer jugador
						if(idJugadores[1] == null) {
							idJugadores[1] = (String) in.readObject();// Recoger el nombre del jugador
							apuesta[1] = (int) in.readObject();
						}
						mostrarMensaje("Hilo establecido con jugador (1) " + idJugadores[1] + " con apuesta " + apuesta);
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					mostrarMensaje("bloquea servidor para poner en espera de inicio al jugador 2");
					bloqueoJuego.lock(); // bloquea el servidor

					while (suspendido) {// Si el hilo está suspendido, se irá a dormir el hilo
						mostrarMensaje("Parando al Jugador 2 en espera del otro jugador...");
						try {
							esperarInicio.await();// Hilo duerme y despierta aquí
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							mostrarMensaje("Desbloquea Servidor luego de bloquear al jugador 2");
							bloqueoJuego.unlock();
						}
					}

					// ya se conectaron todos los jugadores
					// le manda al jugador 2 todos los datos para montar la sala de Juego
					// le toca el turno a jugador 2

					mostrarMensaje("manda al jugador 2 todos los datos para montar SalaJuego");
					datosEnviar = new DatosBlackJack();
					datosEnviar.setManoDealer(manosJugadores.get(3));
					datosEnviar.setManoJugador1(manosJugadores.get(0));
					datosEnviar.setManoJugador2(manosJugadores.get(1));
					datosEnviar.setManoJugador3(manosJugadores.get(2));
					datosEnviar.setApuestasJugadores(apuesta);
					datosEnviar.setIdJugadores(idJugadores);
					datosEnviar.setValorManos(valorManos);
					datosEnviar.setMensaje("Inicias " + idJugadores[1] + " tienes " + valorManos[1]);
					datosEnviar.setMensaje(idJugadores[1] + " Apostaste: " + apuesta[1]);
					enviarMensajeCliente(datosEnviar);
					jugadorEnTurno = 0;
				} else {
					// Es jugador 2
					// le manda al jugador 2 todos los datos para montar la sala de Juego
					// jugador 2 debe esperar su turno
					try {
						if(idJugadores[2] == null) {
							idJugadores[2] = (String) in.readObject();
							apuesta[2] = (int) in.readObject();
						}		
						mostrarMensaje("Hilo jugador (3)" + idJugadores[2] + " con apuesta " + apuesta);
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					mostrarMensaje("manda al jugador 3 el nombre del jugador 1");

					datosEnviar = new DatosBlackJack();
					datosEnviar.setManoDealer(manosJugadores.get(3));
					datosEnviar.setManoJugador1(manosJugadores.get(0));
					datosEnviar.setManoJugador2(manosJugadores.get(1));
					datosEnviar.setManoJugador3(manosJugadores.get(2));
					datosEnviar.setApuestasJugadores(apuesta);
					datosEnviar.setIdJugadores(idJugadores);
					datosEnviar.setValorManos(valorManos);
					datosEnviar.setMensaje("Inicias " + idJugadores[2] + " tienes " + valorManos[2]);
					datosEnviar.setMensaje(idJugadores[2] + " Apostaste: " + apuesta[2]);
					enviarMensajeCliente(datosEnviar);

					iniciarRondaJuego(); // despertar al jugador 1 para iniciar el juego
					mostrarMensaje("Bloquea al servidor para poner en espera de turno al jugador 3");
					bloqueoJuego.lock();
					try {
						mostrarMensaje("Pone en espera de turno al jugador 3");
						esperarTurno.await();
						mostrarMensaje("Despierta de la espera de inicio del juego al jugador 1");
						//
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						bloqueoJuego.unlock();
					}
				}

				while (!seTerminoRonda) {
					try {
						//mostrarMensaje(indexJugador + " AYUDAAAAAAAAAAAAAAAAAAAAAAAA" + seTerminoRonda); 
						entrada = (String) in.readObject();
						analizarMensaje(entrada, indexJugador);
						mostrarMensaje(indexJugador + " salió de analizar mensaje y " + seTerminoRonda); 
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						// controlar cuando se cierra un cliente
					}
				}
				mostrarMensaje("CONTADOR VALE: "+ contador); 
				mostrarMensaje(indexJugador + " salió del pinche while");
				if(contador==3) {
					mostrarMensaje(indexJugador + " VOY A REINICIAR LAS VARIABLES" + seTerminoRonda); 
					reiniciarVariables();			
				}
				// cerrar conexión
			}
		}

		public void enviarMensajeCliente(Object mensaje) {
			try {
				out.writeObject(mensaje);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}// fin inner class Jugador

	// Jugador dealer emulado por el servidor
	@Override
	public void run() {
		// TODO Auto-generated method stub
		mostrarMensaje("Inicia el dealer ...");
		boolean pedir = true;

		while (pedir) {
			Carta carta = mazo.getCarta();
			// adicionar la carta a la mano del dealer
			manosJugadores.get(3).add(carta);
			calcularValorMano(manosJugadores.get(3), carta, 3);

			mostrarMensaje("El dealer recibe " + carta.toString() + " suma " + valorManos[3]);

			datosEnviar = new DatosBlackJack();
			datosEnviar.setCarta(carta);
			datosEnviar.setJugador("dealer");

			if (valorManos[3] <= 16) {
				datosEnviar.setJugadorEstado("sigue");
				datosEnviar.setMensaje("Dealer ahora tiene " + valorManos[3]);
				mostrarMensaje("El dealer sigue jugando");
			} else {
				if (valorManos[3] > 21) {
					datosEnviar.setJugadorEstado("voló");
					estadosJugadores[3] = "voló";
					datosEnviar.setMensaje("Dealer ahora tiene " + valorManos[3] + " voló :(");
					pedir = false;
					mostrarMensaje("El dealer voló");
				} else {
					datosEnviar.setJugadorEstado("plantó");
					datosEnviar.setMensaje("Dealer ahora tiene " + valorManos[3] + " plantó");
					pedir = false;
					mostrarMensaje("El dealer plantó");
				}
			}
			// envia la jugada a los otros jugadores

			datosEnviar.setCarta(carta);
			jugadores[0].enviarMensajeCliente(datosEnviar);
			jugadores[1].enviarMensajeCliente(datosEnviar);
			jugadores[2].enviarMensajeCliente(datosEnviar);

		} // fin while
		determinarRondaJuego(3);
		
	}

}// Fin class ServidorBJ
