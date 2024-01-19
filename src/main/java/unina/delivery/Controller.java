package unina.delivery;

import java.sql.*;

class Controller {

	LoginPage loginPage;
	
	public static void main(String[] args) {
		Controller controller = new Controller();
		
		try {
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:posgresql://localhost:5432/uninadelivery";
			Connection conn = DriverManager.getConnection(url, "fab", "egg");
			System.out.println("Connessione OK");
			conn.close();
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
		
	}
	
	Controller(){
		loginPage = new LoginPage(this);
		
		loginPage.setVisible(true);
	}
	
	protected void loginButtonPressed(String username, char[] password)
	{
		
		loginPage.setVisible(false);
	}

}
