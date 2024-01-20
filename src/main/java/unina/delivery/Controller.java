package unina.delivery;

import java.sql.*;
import javax.swing.*;

public class Controller {

	LoginPage loginPage;
	Connection myconnection;
	
	public static void main(String[] args) {
		
		Controller controller = new Controller();
	}
	
	Controller() {
		if (!attemptConnection())
		{
			JOptionPane.showMessageDialog(null, "Non è stato possibile stabilire una connessione con il database.", "Connessione fallita", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			loginPage = new LoginPage(this);			
			loginPage.setVisible(true);
		}
	}
	
	protected void loginButtonPressed(String username, char[] password) {
		
		loginPage.setVisible(false);
	}
	
	private void openConnection() throws ClassNotFoundException, SQLException {
		
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost:5432/postgres";
		myconnection = DriverManager.getConnection(url, "postgres", "egg");
		System.out.println("Connessione OK");
		myconnection.close();
	}
	
	private boolean attemptConnection() {
		
		try
		{
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
