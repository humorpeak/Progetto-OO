package unina.delivery;

public class Corriere {
	private String codiceFiscale;
	private String nome;
	private String cognome;
	private String tipoPatente;
	
	Corriere(String cf, String n, String c, String p)
	{
		codiceFiscale = cf;
		nome = n;
		cognome = c;
		tipoPatente = p;
	}
	
	public String getCodiceFiscale() {
		return codiceFiscale;
	}
	public String getNome() {
		return nome;
	}
	public String getCognome() {
		return cognome;
	}
	public String getTipoPatente() {
		return tipoPatente;
	}
}
