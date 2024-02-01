package unina.delivery;

import java.awt.Dimension;
import java.sql.*;
import javax.swing.*;
import java.util.*;

public class Controller {

	public static void main(String[] args) {
		
		Controller controller = new Controller();
	}

	private LoginPage loginPage;
	private HomePage homePage;
	private OrdiniPage ordiniPage;
	private ReportPage reportPage;
	private LogisticaPage logisticaPage;;
	private Connection myConnection;
	private Operatore operatore;
	private OperatoreDAO operatoredao;
	private OrdineDAO ordinedao;
	private ArrayList<Ordine> listaordini;
	private ArrayList<Ordine> listaordinimax;
	private ArrayList<Ordine> listaordinimin;
	private List<OrdineConSelezione> ordersWithSelection;	
	private List<OrdineConSelezione> filteredOrdersRows;
	
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
			logisticaPage = new LogisticaPage(this);
			
			loginPage.setVisible(true);
		}
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

	protected void calculateButtonPressed(int year, int month) {
		
		ordinedao = new OrdineDAO(this);		
		double averagenum = ordinedao.getAverageNumberOfOrders(year, month);
		listaordinimax = ordinedao.getOrdiniWithMaxNumOfProducts(year, month);
		listaordinimin = ordinedao.getOrdiniWithMinNumOfProducts(year, month);
		
		reportPage.showResults(averagenum);
		//TODO il resto
	}

	/**
	 * @return the number of orders that are shown to the user according to the filters
	 */
	protected int countFilteredOrders() {
		if (filteredOrdersRows == null) return 0;
		else return filteredOrdersRows.size();
	}

	protected int countOrdersWithMaxNumOfProducts() {
		if (listaordinimax == null) return 0;
		else return listaordinimax.size();
	}
	
	protected int countOrdersWithMinNumOfProducts() {
		if (listaordinimin == null) return 0;
		else return listaordinimin.size();
	}
	
	/**
	 * exit button pressed
	 * closes connection
	 * kills program
	 */
	protected void exit() {
		// prova a chiudere la connessione, se la connessione non era stata aperta cattura un'eccezione
		try {
			myConnection.close();
		} catch (SQLException e) {
			System.out.println(e);
		}
		System.exit(0);
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
			loginPage.showError("Le credenziali inserite non sono corrette. Riprova.", "Credenziali errate");
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
		myConnection = DriverManager.getConnection(url, "postgres", "egg");
		System.out.println("Connessione OK");
	}
	
	protected void reportButtonPressed()
	{
		homePage.setVisible(false);
		reportPage.setVisible(true);
	}
	
	protected void setFilteredOrdersRows(List<OrdineConSelezione> filteredOrdersRows) {
		this.filteredOrdersRows = filteredOrdersRows;
	}
	
	/**
	 * aggiorna la lista degli ordini
	 * @param listaordini
	 */
	public void setOrderList(ArrayList<Ordine> listaordini) {
		ordersWithSelection = new ArrayList<OrdineConSelezione>(listaordini.size());
		for (Ordine o : listaordini)
		{
			OrdineConSelezione nuovaRiga = new OrdineConSelezione(o);
			ordersWithSelection.add(nuovaRiga);
		}
		setFilteredOrdersRows (ordersWithSelection);
	}
	
	protected void setOrdersWithSelection(List<OrdineConSelezione> ordersWithSelection2) {
		this.ordersWithSelection = ordersWithSelection2;
	}
	
	protected void shipmentButtonPressed()
	{
		ordinedao = new OrdineDAO(this);
		listaordini = ordinedao.getOrdiniDaSpedireUnfiltered(operatore.getSede());

		if (listaordini.isEmpty())
		{
			homePage.showInformation("Non sono presenti nuovi ordini da spedire.", "Nessun nuovo ordine");
		}
		else
		{
			homePage.setVisible(false);
			setOrderList(listaordini);
			ordiniPage.setVisible(true);
		}
	}
	
	
	protected void toggleOrder(int row)
	{
		filteredOrdersRows.get(row).toggle();
	}

	public void backButtonPressedFromOrdiniToHomePage() {
		ordiniPage.setVisible(false);
		homePage.setVisible(true);
	}
	
	public void confirmButtonPressed() {
		ordiniPage.setVisible(false);
		//TODO warnings etc
		logisticaPage.setVisible(true);
	}
	
	
	protected List<OrdineConSelezione> getFilteredOrdersRows() {
		return filteredOrdersRows;
	}
	
	protected List<OrdineConSelezione> getOrdersWithSelection() {
		return ordersWithSelection;
	}
	
	protected List<Ordine> getOrdiniWithMaxNumOfProductsRows() {
		return listaordinimax;
	}
	
	protected List<Ordine> getOrdiniWithMinNumOfProductsRows() {
		return listaordinimin;
	}
	
	
	/**
	 * @return the operatore
	 */
	protected Operatore getOperatore() {
		return operatore;
	}

	/**
	 * @param operatore the operatore to set
	 */
	protected void setOperatore(Operatore operatore) {
		this.operatore = operatore;
	}
	
	/**
	 * @return the myConnection
	 */
	protected Connection getMyConnection() {
		return myConnection;
	}

	/**
	 * @param myConnection the myConnection to set
	 */
	protected void setMyConnection(Connection myConnection) {
		this.myConnection = myConnection;
	}

}
