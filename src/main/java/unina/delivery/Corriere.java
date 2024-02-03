package unina.delivery;

public class Corriere {
	private String codiceFiscale;
	private String nome;
	private String cognome;
	private String tipoPatente;
	
	protected String getCodiceFiscale() {
		return codiceFiscale;
	}
	protected String getNome() {
		return nome;
	}
	protected String getCognome() {
		return cognome;
	}
	protected String getTipoPatente() {
		return tipoPatente;
	}
	
	Corriere(String cf, String n, String c, String p)
	{
		codiceFiscale = cf;
		nome = n;
		cognome = c;
		tipoPatente = p;
	}
}
