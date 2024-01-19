package unina.delivery;

import java.sql.*;

public class Controller {

	LoginPage loginPage;
	Connection connessione;
	
	public static void main(String[] args) {
		Controller controller = new Controller();
		
	}
	
	Controller(){
//TODO un-commentare quando si risolve il problema		
//		if (!attemptConnection()) {
//			//TODO popup errore
//		}
//		else {
			loginPage = new LoginPage(this);
			
			loginPage.setVisible(true);
//		}
	}
	
	protected void loginButtonPressed(String username, char[] password)
	{
		
		loginPage.setVisible(false);
	}
	
	private void openConnection() throws ClassNotFoundException, SQLException {
		
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:posgresql://localhost:5432/uninadelivery";
		connessione = DriverManager.getConnection(url, "fab", "egg");
		System.out.println("Connessione OK");
		connessione.close();
	}
	
	private boolean attemptConnection() {
		try {
			openConnection();
			return true;
		}
		catch(ClassNotFoundException e)
		{
			System.out.println("Driver non trovato");
			System.out.println(e);
		}
		catch(SQLException e)
		{
			System.out.println("Connessione fallita");
			System.out.println(e);
		}
		return false;
	}
}
