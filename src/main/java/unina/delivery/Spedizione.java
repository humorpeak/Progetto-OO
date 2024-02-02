package unina.delivery;

import java.sql.Timestamp;

public class Spedizione {
	private Timestamp partenza;
	private Timestamp arrivoStimato;
	private String targa;
	private String codiceFiscaleCorriere;
	private String codiceFiscaleOperatore;
	
	public Timestamp getPartenza() {
		return partenza;
	}
	public void setPartenza(Timestamp partenza) {
		this.partenza = partenza;
	}
	public Timestamp getArrivoStimato() {
		return arrivoStimato;
	}
	public void setArrivoStimato(Timestamp arrivoStimato) {
		this.arrivoStimato = arrivoStimato;
	}
	public String getTarga() {
		return targa;
	}
	public void setTarga(String targa) {
		this.targa = targa;
	}
	public String getCodiceFiscaleCorriere() {
		return codiceFiscaleCorriere;
	}
	public void setCodiceFiscaleCorriere(String codiceFiscaleCorriere) {
		this.codiceFiscaleCorriere = codiceFiscaleCorriere;
	}
	public String getCodiceFiscaleOperatore() {
		return codiceFiscaleOperatore;
	}
	public void setCodiceFiscaleOperatore(String codiceFiscaleOperatore) {
		this.codiceFiscaleOperatore = codiceFiscaleOperatore;
	}
}
