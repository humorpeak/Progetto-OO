package unina.delivery;

import java.time.*;

public class Ordine {
	private LocalDate data;
	private LocalTime orarioinizio;
	private LocalTime orariofine;
	private String acquirente;
	private String indirizzo;
	private int peso;
	private int idOrdine;
	
	public int getPeso() {
		return peso;
	}

	public LocalDate getData() {
		return data;
	}

	public LocalTime getOrarioinizio() {
		return orarioinizio;
	}

	public LocalTime getOrariofine() {
		return orariofine;
	}

	public String getAcquirente() {
		return acquirente;
	}

	public String getIndirizzo() {
		return indirizzo;
	}
	
	public int getIdOrdine()
	{
		return idOrdine;
	}
	
	Ordine(LocalDate d, LocalTime oi, LocalTime of, String a, String i, int p, int id) {
		data = d;
		orarioinizio = oi;
		orariofine = of;
		acquirente = a;
		indirizzo = i;
		peso = p;
		idOrdine = id;
	}
	
	Ordine(String a, LocalDate d, String i) {
		acquirente = a;
		data = d;
		indirizzo = i;
	}

	@Override
	public String toString() {
		return "Ordine [data=" + data + ", orarioinizio=" + orarioinizio + ", orariofine=" + orariofine
				+ ", acquirente=" + acquirente + ", indirizzo=" + indirizzo + ", peso=" + peso + "]";
	}
}