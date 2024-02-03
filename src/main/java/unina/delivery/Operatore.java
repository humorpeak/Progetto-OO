package unina.delivery;

public class Operatore {
	
	private String email;
	private String password;
	private int sede;
	private String codiceFiscale;
	
	protected String getEmail() {
		return email;
	}
	protected String getPassword() {
		return password;
	}
	protected int getSede() {
		return sede;
	}
	protected String getCodiceFiscale() {
		return codiceFiscale;
	}
	
	Operatore(String e, String p, int s, String cf) {
		email = e;
		password = p;
		sede = s;
		codiceFiscale = cf;
	}
}
