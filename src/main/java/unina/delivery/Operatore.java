package unina.delivery;

public class Operatore {
	
	private String email;
	private String password;
	private int sede;
	
	Operatore(String e, String p) {
		email = e;
		password = p;
	}
	
	public String getEmail() {
		return email;
	}
	public String getPassword() {
		return password;
	}
	public void setSede(int s) {
		sede = s;
	}
}
