package unina.delivery;

public class Operatore {
	
	private String email;
	private String password;
	private int sede;
	private String codiceFiscale;
	
	Operatore(String e, String p, int s, String cf) {
		email = e;
		password = p;
		sede = s;
		codiceFiscale = cf;
	}
	
	public String getEmail() {
		return email;
	}
	public String getPassword() {
		return password;
	}
	public int getSede() {
		return sede;
	}
	public String getCodiceFiscale() {
		return codiceFiscale;
	}
}
