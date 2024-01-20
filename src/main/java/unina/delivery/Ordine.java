package unina.delivery;

import java.time.*;

public class Ordine {
	private LocalDate data;
	private LocalTime orarioinizio;
	private LocalTime orariofine;
	private String acquirente;
	private String indirizzo;
	//TODO peso
	
	Ordine(LocalDate d, LocalTime oi, LocalTime of, String a, String i) {
		data = d;
		orarioinizio = oi;
		orariofine = of;
		acquirente = a;
		indirizzo = i;
	}

	@Override
	public String toString() {
		return "Ordine [data=" + data + ", orarioinizio=" + orarioinizio + ", orariofine=" + orariofine
				+ ", acquirente=" + acquirente + ", indirizzo=" + indirizzo + "]";
	}
}