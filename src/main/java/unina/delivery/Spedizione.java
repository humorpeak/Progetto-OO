package unina.delivery;

import java.sql.Timestamp;

public class Spedizione {
	private Timestamp partenza;
	private Timestamp arrivoStimato;
	private String targa;
	private String codiceFiscaleCorriere;
	private String codiceFiscaleOperatore;
	
	Spedizione(Timestamp partenza, Timestamp arrivoStimato, String targa, String codiceFiscaleCorriere,
			String codiceFiscaleOperatore) {
		this.partenza = partenza;
		this.arrivoStimato = arrivoStimato;
		this.targa = targa;
		this.codiceFiscaleCorriere = codiceFiscaleCorriere;
		this.codiceFiscaleOperatore = codiceFiscaleOperatore;
	}
	protected Timestamp getPartenza() {
		return partenza;
	}
	protected void setPartenza(Timestamp partenza) {
		this.partenza = partenza;
	}
	protected Timestamp getArrivoStimato() {
		return arrivoStimato;
	}
	protected void setArrivoStimato(Timestamp arrivoStimato) {
		this.arrivoStimato = arrivoStimato;
	}
	protected String getTarga() {
		return targa;
	}
	protected void setTarga(String targa) {
		this.targa = targa;
	}
	protected String getCodiceFiscaleCorriere() {
		return codiceFiscaleCorriere;
	}
	protected void setCodiceFiscaleCorriere(String codiceFiscaleCorriere) {
		this.codiceFiscaleCorriere = codiceFiscaleCorriere;
	}
	protected String getCodiceFiscaleOperatore() {
		return codiceFiscaleOperatore;
	}
	protected void setCodiceFiscaleOperatore(String codiceFiscaleOperatore) {
		this.codiceFiscaleOperatore = codiceFiscaleOperatore;
	}
}
