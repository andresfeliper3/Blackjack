package comunes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class DatosBlackJack implements Serializable{
	private String[] idJugadores;
	private int[] capitalJugadores;
	private ArrayList<Carta> manoJugador1, manoJugador2, manoJugador3, manoDealer;
	private int[] valorManos;
	private Carta carta;
	private String mensaje;
	private String jugador,jugadorEstado;
	private boolean enJuego = true;
	private boolean prueba = true;
	private String idJugador1, idJugador2, idJugador3;
	private int count=0;
	
	public int getCount() {
		return count;
	}
	
	public String getIdJugador1() {
		return idJugador1;
	}

	public void setIdJugador1(String idJugador1) {
		this.idJugador1 = idJugador1;
	}

	public String getIdJugador2() {
		return idJugador2;
	}

	public void setIdJugador2(String idJugador2) {
		this.idJugador2 = idJugador2;
	}

	public String getIdJugador3() {
		return idJugador3;
	}

	public void setIdJugador3(String idJugador3) {
		this.idJugador3 = idJugador3;
	}

	public boolean isPrueba() {
		return prueba;
	}

	public void setPrueba(boolean prueba) {
		this.prueba = prueba;
	}


	public boolean isEnJuego() {
		return enJuego;
	}
	public void setEnJuego(boolean enJuego) {
		this.enJuego = enJuego;
	}
	
	public int[] getCapitalJugadores() {
		return capitalJugadores;
	}
	public void setCapitalJugadores(int[] capitalJugadores) {
		this.capitalJugadores = capitalJugadores;
	}
	public String getJugador() {
		return jugador;
	}
	public void setJugador(String jugador) {
		this.jugador = jugador;
	}
	
	public String getJugadorEstado() {
		return jugadorEstado;
	}
	public void setJugadorEstado(String jugadorEstado) {
		this.jugadorEstado = jugadorEstado;
	}
		
	public String[] getIdJugadores() {
		System.out.println("En DatosBlackJack, getIdJugadores");
	
		return idJugadores;
	}
	public void setIdJugadores(String[] idJugadores) {
		this.idJugadores = idJugadores;
	}
	
	public ArrayList<Carta> getManoJugador1() {
		return manoJugador1;
	}
	public void setManoJugador1(ArrayList<Carta> manoJugador1) {
		this.manoJugador1 = manoJugador1;
	}
	
	public ArrayList<Carta> getManoJugador2() {
		return manoJugador2;
	}
	public void setManoJugador2(ArrayList<Carta> manoJugador2) {
		this.manoJugador2 = manoJugador2;
	}
	
	public ArrayList<Carta> getManoJugador3() {
		return manoJugador3;
	}
	public void setManoJugador3(ArrayList<Carta> manoJugador3) {
		this.manoJugador3 = manoJugador3;
	}
	public ArrayList<Carta> getManoDealer() {
		return manoDealer;
	}
	public void setManoDealer(ArrayList<Carta> manoDealer) {
		this.manoDealer = manoDealer;
	}
	
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	public void setValorManos(int[] valorManos) {
		this.valorManos=valorManos;
	}
	public int[] getValorManos() {
		return valorManos;	
	}
	public void setCarta(Carta carta) {
		this.carta=carta;
	}
	public Carta getCarta() {
		return carta;
	}
}
