package unina.delivery;

import java.sql.*;
import javax.swing.*;

public class Controller {

	LoginPage loginPage;
	HomePage homePage;
	Connection myconnection;
	Operatore operatore;
	OperatoreDAO operatoredao; //deve essere instanziato o metodi statici?
	
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
			homePage = new HomePage(this);
			loginPage.setVisible(true);
		}
	}
	
	/**
	 * Opens connection to the database
	 * TODO store credentials in config file
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private void openConnection() throws ClassNotFoundException, SQLException {
		
		Class.forName("org.postgresql.Driver");
		String url = "jdbc:postgresql://localhost:5432/postgres?currentSchema=uninadelivery";
		myconnection = DriverManager.getConnection(url, "postgres", "egg");
		System.out.println("Connessione OK");
	}
	
	/**
	 * @return true if connection was successful, false otherwise
	 */
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
	
	protected void loginButtonPressed(String email, String password) {

		operatoredao = new OperatoreDAO(this);
		
		if (operatoredao.isOperatoreValid(email, password))
		{
			System.out.println("valido");
			loginPage.setVisible(false);
			int sede = operatoredao.getSede(email, password);
			operatore = new Operatore(email, password, sede);
			System.out.println(operatore.getEmail() + operatore.getPassword() + operatore.getSede());
			homePage.setVisible(true);
		}
		else
		{
			loginPage.showErrore("Le credenziali inserite non sono corrette. La invitiamo a riprovare.", "Credenziali errate");
		}
	}
	
	protected void newSpedizioneButtonPressed()
	{
		//todo
		System.out.println("newSpedizioneButton pressed");
	}
	
	/**
	 * exit button pressed
	 * closes connection
	 * kills program
	 */
	protected void exit() {
		// prova a chiudere la connessione, se la connessione non era stata aperta cattura un'eccezione
		try {
			myconnection.close();
		} catch (SQLException e) {
			System.out.println(e);
		}
		System.exit(0);
	}
}
