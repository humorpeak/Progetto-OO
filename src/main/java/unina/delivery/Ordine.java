package unina.delivery;

import java.time.*;

public class Ordine {
	private LocalDate data;
	private LocalDateTime orarioinizio;
	private LocalDateTime orariofine;
	private String acquirente;
	private String indirizzo;
	//TODO peso
	
	Ordine(LocalDate d, LocalDateTime oi, LocalDateTime of, String a, String i) {
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