package unina.delivery;

public class Operatore {
	
	private String email;
	private String password;
	private int sede;
	
	Operatore(String e, String p, int s) {
		email = e;
		password = p;
		sede = s;
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
	
//	public void setSede(int s) {
//		sede = s;
//	}
}
