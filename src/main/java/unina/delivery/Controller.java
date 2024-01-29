package unina.delivery;

import java.sql.*;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

import java.util.*;

public class Controller {

	LoginPage loginPage;
	HomePage homePage;
	OrdiniPage ordiniPage;
	ReportPage reportPage;
	Connection myconnection;
	Operatore operatore;
	OperatoreDAO operatoredao; //deve essere istanziato o metodi statici?
	OrdineDAO ordinedao;
	ArrayList<Ordine> listaordini;
	
	public static void main(String[] args) {
		
		Controller controller = new Controller();
	}
	
	Controller() {
		
		UIDesign uidesign = new UIDesign();
		uidesign.setup();
		
		if (!attemptConnection())
		{
			JOptionPane.showMessageDialog(null, "Non è stato possibile stabilire una connessione con il database.", "Connessione fallita", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			loginPage = new LoginPage(this);
			homePage = new HomePage(this);
			ordiniPage = new OrdiniPage(this);
			reportPage = new ReportPage(this);
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

		if (!email.contains("@")) {
			email = email + "@unina.delivery.it";
		}
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
			loginPage.showError("Le credenziali inserite non sono corrette. La invitiamo a riprovare.", "Credenziali errate");
		}
	}
	
	protected void shipmentButtonPressed()
	{
		//TODO mostrare shipment page con le info della sede: ordini confermati
		ordinedao = new OrdineDAO(this);
		listaordini = ordinedao.getOrdiniDaSpedireUnfiltered(operatore.getSede());

		if (listaordini.isEmpty())
		{
			homePage.showInformation("Non sono presenti nuovi ordini da spedire.", "Nessun nuovo ordine");
		}
		else
		{
			homePage.setVisible(false);
			ordiniPage.setVisible(true);
			ordiniPage.setOrderList(listaordini);
		}
	}
	
	protected void reportButtonPressed()
	{
		homePage.setVisible(false);
		reportPage.setVisible(true);
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
