package unina.delivery;

import java.sql.*;
import java.time.*;

import javax.swing.*;
import java.util.*;

public class Controller {

	public static void main(String[] args) {
		new Controller();
	}

	private Connection myConnection;
	private UIDesign uidesign;
	private LoginPage loginPage;
	private HomePage homePage;
	private OrdiniPage ordiniPage;
	private ReportPage reportPage;
	private LogisticaPage logisticaPage;;
	private Operatore operatore;
	private OperatoreDAO operatoredao;
	private OrdineDAO ordinedao;
	private MezzoDiTrasportoDAO mezzoDiTrasportoDAO;
	private Spedizione spedizione;
	private SpedizioneDAO spedizionedao;
	private ArrayList<Ordine> ordersList;
	private ArrayList<Ordine> ordersWithMaxNumberOfProducts;
	private ArrayList<Ordine> ordersWithMinNumberOfProducts;
	private List<OrdineConSelezione> ordersWithSelection;	
	private List<OrdineConSelezione> filteredOrdersRows;
	private List<MezzoDiTrasporto> availableVehiclesWithShipper;
	private List<Corriere> availableShippers;
	
	Controller() {
		try {
			uidesign = new UIDesign();
			uidesign.setup();
		}
		catch (Exception e){
			JOptionPane.showMessageDialog(null, "Non è possibile applicare le proprietà grafiche.", "Setup UI fallito", JOptionPane.ERROR_MESSAGE);
			System.err.println("Errore, default design");
		}
		
		if (!attemptConnection())
		{
			JOptionPane.showMessageDialog(null, "Non è stato possibile stabilire una connessione con il database.", "Connessione fallita", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			mezzoDiTrasportoDAO = new MezzoDiTrasportoDAO(this);
			spedizionedao = new SpedizioneDAO(this);
			loginPage = new LoginPage(this);
			homePage = new HomePage(this);
			ordiniPage = new OrdiniPage(this);
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
			System.err.println("Driver non trovato");
			e.printStackTrace();
		}
		catch(SQLException e)
		{
			System.err.println("Connessione fallita");
			e.printStackTrace();
		}
		return false;
	}

	
	/**
	 * updates reportPage after calculating the correct results for given year and month
	 * @param year
	 * @param month
	 */
	protected void calculateButtonPressed(int year, int month) {
		ordinedao = new OrdineDAO(this);
		double averagenum = ordinedao.getAverageNumberOfOrders(year, month);
		ordersWithMaxNumberOfProducts = ordinedao.getOrdiniWithMaxNumOfProducts(year, month);
		ordersWithMinNumberOfProducts = ordinedao.getOrdiniWithMinNumOfProducts(year, month);
		
		reportPage.showResults(averagenum);
	}

	/**
	 * @return the number of orders that are shown to the user according to the filters
	 */
	protected int countFilteredOrders() {
		if (filteredOrdersRows == null) return 0;
		else return filteredOrdersRows.size();
	}

	/**
	 * @return the number of orders that have the maximum amount of product
	 */
	protected int countOrdersWithMaxNumOfProducts() {
		if (ordersWithMaxNumberOfProducts == null) return 0;
		else return ordersWithMaxNumberOfProducts.size();
	}
	
	/**
	 * @return the number of orders that have the minimum amount of product
	 */
	protected int countOrdersWithMinNumOfProducts() {
		if (ordersWithMinNumberOfProducts == null) return 0;
		else return ordersWithMinNumberOfProducts.size();
	}
	
	/**
	 * exit button pressed
	 * closes connection
	 * kills program
	 */
	protected void exit() {
		// tries to close connection that might not be open
		try {
			myConnection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	
	/**
	 * Called when Login button is pressed in LoginPage
	 * @param email
	 * @param password
	 */
	protected void loginButtonPressed(String email, String password) {

		if (!email.contains("@")) {
			email = email + "@unina.delivery.it";
		}
		operatoredao = new OperatoreDAO(this);
		
		if (operatoredao.isOperatoreValid(email, password))
		{
			loginPage.setVisible(false);
			int sede = operatoredao.getSede(email, password);
			String codiceFiscale = operatoredao.getCodiceFiscale(email, password);
			operatore = new Operatore(email, password, sede, codiceFiscale);
			homePage.setVisible(true);
		}
		else
		{
			loginPage.showError("Le credenziali inserite non sono corrette. Riprova.", "Credenziali errate");
		}
	}
	
	/**
	 * Opens connection to the database
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
		reportPage = new ReportPage(this);
		reportPage.setVisible(true);
	}
	
	protected void setFilteredOrdersRows(List<OrdineConSelezione> filteredOrdersRows) {
		this.filteredOrdersRows = filteredOrdersRows;
	}


	protected void setOrderList(ArrayList<Ordine> listaordini) {
		ordersWithSelection = new ArrayList<OrdineConSelezione>(listaordini.size());
		for (Ordine o : listaordini)
		{
			OrdineConSelezione nuovaRiga = new OrdineConSelezione(o);
			ordersWithSelection.add(nuovaRiga);
		}
		setFilteredOrdersRows (ordersWithSelection);
	}
	
	protected void setOrdersWithSelection(List<OrdineConSelezione> ordersWithSelection) {
		this.ordersWithSelection = ordersWithSelection;
	}
	
	protected void shipmentButtonPressed()
	{
		ordinedao = new OrdineDAO(this);
		ordersList = ordinedao.getOrdiniDaSpedireUnfiltered(operatore.getSede());

		if (ordersList.isEmpty())
		{
			homePage.showInformation("Non sono presenti nuovi ordini da spedire.", "Nessun nuovo ordine");
		}
		else
		{
			homePage.setVisible(false);
			setOrderList(ordersList);
			ordiniPage.setVisible(true);
		}
	}
	
	
	/**
	 * toggled an order from filteredOrdersRows in OrdiniPage
	 * @param row
	 */
	protected void toggleOrder(int row)
	{
		filteredOrdersRows.get(row).toggle();
	}

	protected void backButtonPressedFromOrdiniToHomePage() {
		ordiniPage.setVisible(false);
		homePage.setVisible(true);
		availableShippers = new ArrayList<>();
		availableVehiclesWithShipper = new ArrayList<>();
		this.logisticaPage.resetFilters();
		this.ordiniPage.resetFilters();
	}
	
	protected void backButtonPressedFromLogisticaToOrdiniPage()
	{
		availableShippers = new ArrayList<>();
		availableVehiclesWithShipper = new ArrayList<>();
		logisticaPage.setVisible(false);
		ordiniPage.setVisible(true);
	}
	
	protected void backButtonPressedFromReportToHomePage() {
		reportPage.setVisible(false);
		homePage.setVisible(true);
	}
	
	protected void openLogisticaPage() {
		ordiniPage.setVisible(false);
		logisticaPage.setVisible(true);
		retrieveAvailableVehicles(null, null, null);
	}
	
	/**
	 * @return true if none of the confirmed orders that are shown in the OrdiniPage is selected, false otherwise.
	 */
	protected boolean noOrdersSelected()
	{
		Iterator<OrdineConSelezione> iter = ordersWithSelection.iterator();
		boolean noOrdersSelected = true;
		while (iter.hasNext())
		{
			if(iter.next().selected)
			{
				noOrdersSelected = false;
			}
		}
		return noOrdersSelected;
	}
	
	protected List<OrdineConSelezione> getFilteredOrdersRows() {
		return filteredOrdersRows;
	}
	
	protected List<OrdineConSelezione> getOrdersWithSelection() {
		return ordersWithSelection;
	}
	
	protected List<Ordine> getOrdiniWithMaxNumOfProductsRows() {
		return ordersWithMaxNumberOfProducts;
	}
	
	protected List<Ordine> getOrdiniWithMinNumOfProductsRows() {
		return ordersWithMinNumberOfProducts;
	}
	
	protected Operatore getOperatore() {
		return operatore;
	}

	protected void setOperatore(Operatore operatore) {
		this.operatore = operatore;
	}
	
	protected Connection getMyConnection() {
		return myConnection;
	}

	protected void setMyConnection(Connection myConnection) {
		this.myConnection = myConnection;
	}

	/**
	 * @return the number of available vehicles that have at least one shipper
	 */
	protected int getNumberOfAvailableVehiclesWithShipper() {
		if (availableVehiclesWithShipper == null) return 0;
		return availableVehiclesWithShipper.size();
	}

	protected List<MezzoDiTrasporto> getAvailableVehiclesWithShipper() {
		return availableVehiclesWithShipper;
	}
	
	/**
	 * @return the number of available shippers for the filters applied in OrdiniPage
	 */
	protected int getNumberOfAvailableShippers()
	{
		if (availableShippers == null) return 0;
		return availableShippers.size();
	}
	
	/**
	 * @return the number of available shippers for the given filters
	 */
	protected int getNumberOfAvailableShippers(LocalDate date, LocalTime beginning, LocalTime end, String targa)
	{
		return mezzoDiTrasportoDAO.getNumeroDiCorrieriDisponibili(date, beginning, end, targa);
	}
	
	/**
	 * updates controller's field "availableVehiclesWithShipper"
	 */
	protected void retrieveAvailableVehicles(LocalDate date, LocalTime beginning, LocalTime end)
	{
		availableVehiclesWithShipper = mezzoDiTrasportoDAO.getMezziDiTrasportoDisponibili(date, beginning, end, operatore.getSede());
	}

	protected void applicaButtonPressedLogisticaPage(LocalDate date, LocalTime beginning, LocalTime end) {
		retrieveAvailableVehicles(date,beginning,end);
		availableShippers = new ArrayList<Corriere>();
	}
	
	protected List<Corriere> getCorrieriDisponibili() {
		return availableShippers;
	}
	
	protected void retrieveAvailableShippersForVehicle(LocalDate date, LocalTime beginning, LocalTime end, String targa)
	{
		availableShippers = mezzoDiTrasportoDAO.getCorrieriDisponibili(date, beginning, end, targa);
	}

	protected void createShipment(LocalDate appliedDate, LocalTime appliedInitialTime, LocalTime appliedFinalTime, String targa, String codiceFiscale) {
		Timestamp departure = Timestamp.valueOf(LocalDateTime.of(appliedDate, appliedInitialTime));
		Timestamp estimatedArrival = Timestamp.valueOf(LocalDateTime.of(appliedDate, appliedFinalTime));
		
		long idSpedizione = -1;
		try {
			spedizione = new Spedizione (departure, estimatedArrival, targa, codiceFiscale, this.operatore.getCodiceFiscale());
			idSpedizione = spedizionedao.create(spedizione);
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Errore: Forse hai dimenticato di eseguire questa riga di codice dopo aver effettuato il backup: "
					+ "SELECT setval('uninadelivery.spedizione_idspedizione_seq',(SELECT max(idspedizione) "
					+ "FROM uninadelivery.SPEDIZIONE));");
			JOptionPane.showMessageDialog(this.logisticaPage, "Errore durante la creazione della spedizione. "
					+ "Impossibile creare una spedizione che dura più di 9 ore.", "Errore", JOptionPane.ERROR_MESSAGE);
		}
		if (idSpedizione == -1) return;
		
		boolean ok = setSelectedOrdersStateToShipped(idSpedizione);
		availableShippers = new ArrayList<>();
		availableVehiclesWithShipper = new ArrayList<>();
		this.ordiniPage.resetFilters();
		
		if (ok)
		{
			JOptionPane.showMessageDialog(this.logisticaPage, "Spedizione creata correttamente.", "Successo", JOptionPane.INFORMATION_MESSAGE);
		}
		this.logisticaPage.resetFilters();
	}

	private boolean setSelectedOrdersStateToShipped(long idSpedizione) {
		boolean result = true;
		for (OrdineConSelezione o : ordersWithSelection) {
			if (o.selected)
			{
				try {
					ordinedao.shipOrder(o.ordine, idSpedizione);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(this.logisticaPage, "Errore durante la spedizione del prodotto codice " + o.ordine.getSerialeOrdine(), "Errore", JOptionPane.ERROR_MESSAGE);
					result = false;
				}
			}
		}
		return result;
	}
	
	/**
	 * @return the sum of the weights of the selected orders in OrdiniPage
	 */
	protected float calculateWeightForSelectedOrders()
	{
		float tot = 0;
		for (OrdineConSelezione o : ordersWithSelection) {
			if (o.selected) tot += o.ordine.getPeso();
		}
		return tot;
	}

	protected LocalDate getSelectedOrdersDate() {
		LocalDate result = null;
		for (OrdineConSelezione o : ordersWithSelection)
		{
			if (o.selected)
			{
				if (result == null)
				{
					result = o.ordine.getData();
				}
				else {
					if (!result.equals(o.ordine.getData()))
					{
						return null;
					}
				}
			}
		}
		return result;
	}

	protected LocalTime getSuggestedDepartureTimeForSelectedOrders() {
		LocalTime result = null;
		for (OrdineConSelezione o : ordersWithSelection)
		{
			if (o.selected)
			{
				LocalTime orarioInizio = o.ordine.getOrarioinizio();
				if (result == null || result.isBefore(orarioInizio))
				{
					result = orarioInizio;
				}
			}
		}
		return result;
	}
	
	protected LocalTime getSuggestedArrivalTimeForSelectedOrders() {
		LocalTime result = null;
		for (OrdineConSelezione o : ordersWithSelection)
		{
			if (o.selected)
			{
				LocalTime orarioFine = o.ordine.getOrariofine();
				if (result == null || result.isAfter(orarioFine))
				{
					result = orarioFine;
				}
			}
		}
		return result;
	}

	protected boolean existsMezzoDiTrasportoForWeight() {
		List<MezzoDiTrasporto> lista = mezzoDiTrasportoDAO.getMezziDiTrasportoDisponibili(null, null, null, this.operatore.getSede());
		float peso = calculateWeightForSelectedOrders();
		for (MezzoDiTrasporto mezzo : lista)
		{
			if (mezzo.getCapienza() >= peso)
			{
				return true;
			}
		}
		return false;
	}
}
