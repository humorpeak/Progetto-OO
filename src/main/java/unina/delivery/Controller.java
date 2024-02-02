package unina.delivery;

import java.sql.*;
import java.time.*;

import javax.swing.*;
import java.util.*;

public class Controller {

	public static void main(String[] args) {
		new Controller();
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
	private MezzoDiTrasportoDAO mezzoDiTrasportoDAO;
	private ArrayList<Ordine> listaordini;
	private ArrayList<Ordine> listaordinimax;
	private ArrayList<Ordine> listaordinimin;
	private List<OrdineConSelezione> ordersWithSelection;	
	private List<OrdineConSelezione> filteredOrdersRows;
	private List<MezzoDiTrasporto> mezziDiTrasportoDisponibiliConCorriere;
	private List<Corriere> corrieriDisponibili;
	
	Controller() {
		try {
			UIDesign uidesign = new UIDesign();
			uidesign.setup();
		}
		catch (Exception e){
			System.out.println("errore, default design");
		}
		
		if (!attemptConnection())
		{
			JOptionPane.showMessageDialog(null, "Non è stato possibile stabilire una connessione con il database.", "Connessione fallita", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			mezzoDiTrasportoDAO = new MezzoDiTrasportoDAO(this);
			loginPage = new LoginPage(this);
			homePage = new HomePage(this);
			ordiniPage = new OrdiniPage(this);
			//reportPage = new ReportPage(this);
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
			String codiceFiscale = operatoredao.getCodiceFiscale(email, password);
			operatore = new Operatore(email, password, sede, codiceFiscale);
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
	
	protected void setOrdersWithSelection(List<OrdineConSelezione> ordersWithSelection) {
		this.ordersWithSelection = ordersWithSelection;
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

	protected void backButtonPressedFromOrdiniToHomePage() {
		ordiniPage.setVisible(false);
		homePage.setVisible(true);
	}
	
	protected void backButtonPressedFromLogisticaToOrdiniPage()
	{
		corrieriDisponibili = new ArrayList<>();
		mezziDiTrasportoDisponibiliConCorriere = new ArrayList<>();
		logisticaPage.setVisible(false);
		ordiniPage.setVisible(true);
	}
	
	protected void backButtonPressedFromReportToHomePage() {
		reportPage.setVisible(false);
		homePage.setVisible(true);
		//TODO altro
	}
	
	protected void openLogisticaPage() {
		ordiniPage.setVisible(false);
		logisticaPage.setVisible(true);
		retrieveMezziDiTrasportoDisponibili(null, null, null);
	}
	
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

	public int countMezziDiTrasportoWithCorrieri() {
		if (mezziDiTrasportoDisponibiliConCorriere == null) return 0;
		return mezziDiTrasportoDisponibiliConCorriere.size();
	}

	public List<MezzoDiTrasporto> getMezziDiTrasportoDisponibiliConCorriere() {
		return mezziDiTrasportoDisponibiliConCorriere;
	}
	
	public int getNumberOfCorrieriDisponibili()
	{
		if (corrieriDisponibili == null) return 0;
		return corrieriDisponibili.size();
	}
	
	public int getNumberOfCorrieriDisponibili(LocalDate data, LocalTime inizio, LocalTime fine, String targa)
	{
		return mezzoDiTrasportoDAO.getNumeroDiCorrieriDisponibili(data, inizio, fine, targa);
	}
	
	public void retrieveMezziDiTrasportoDisponibili(LocalDate data, LocalTime inizio, LocalTime fine)
	{
		mezziDiTrasportoDisponibiliConCorriere = mezzoDiTrasportoDAO.getMezziDiTrasportoDisponibili(data, inizio, fine, operatore.getSede());
	}

	public void applicaButtonPressedLogisticaPage(LocalDate data, LocalTime inizio, LocalTime fine) {
		retrieveMezziDiTrasportoDisponibili(data,inizio,fine);
		corrieriDisponibili = new ArrayList<Corriere>();
	}
	
	public List<Corriere> getCorrieriDisponibili() {
		return corrieriDisponibili;
	}
	
	public void retrieveCorrieriDisponibiliPerMezzoDiTrasporto(LocalDate data, LocalTime inizio, LocalTime fine, String targa)
	{
		corrieriDisponibili = mezzoDiTrasportoDAO.getCorrieriDisponibili(data, inizio, fine, targa);
	}

	public void creaSpedizione(LocalDate appliedDate, LocalTime appliedInitialTime, LocalTime appliedFinalTime, String targa, String codiceFiscale) {
		SpedizioneDAO spedizioneDAO = new SpedizioneDAO(this);
		Timestamp partenza = Timestamp.valueOf(LocalDateTime.of(appliedDate, appliedInitialTime));
		Timestamp arrivoStimato = Timestamp.valueOf(LocalDateTime.of(appliedDate, appliedFinalTime));
		
		long idSpedizione = -1;
		try {
			Spedizione s = new Spedizione (partenza, arrivoStimato, targa, codiceFiscale, this.operatore.getCodiceFiscale());
			idSpedizione = spedizioneDAO.create(s);
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("forse hai dimenticato di eseguire questa riga di codice dopo aver effettuato il backup: "
					+ "SELECT setval('uninadelivery.spedizione_idspedizione_seq',(SELECT max(idspedizione) "
					+ "FROM uninadelivery.SPEDIZIONE));");
			JOptionPane.showMessageDialog(this.logisticaPage, "Errore durante la creazione della spedizione. "
					+ "Forse hai dimenticato di aggiornare il Database?", "Errore", JOptionPane.ERROR_MESSAGE);
		}
		if (idSpedizione == -1) return;
		
		setSelectedOrdersStateToShipped(idSpedizione);
		
		corrieriDisponibili = new ArrayList<>();
		mezziDiTrasportoDisponibiliConCorriere = new ArrayList<>();
		this.ordiniPage.resetFilters();
	}

	private void setSelectedOrdersStateToShipped(long idSpedizione) {
		for (OrdineConSelezione o : ordersWithSelection) {
			if (o.selected)
			{
				try {
					ordinedao.shipOrder(o.ordine, idSpedizione);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
					JOptionPane.showMessageDialog(this.logisticaPage, "Errore durante la spedizione del prodotto codice " + o.ordine.getIdOrdine(), "Errore", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	protected float calculateWeightForSelectedOrders()
	{
		float tot = 0;
		for (OrdineConSelezione o : ordersWithSelection) {
			if (o.selected) tot += o.ordine.getPeso();
		}
		return tot;
	}

	public int getNumberOfMezziDiTrasportoDisponibili() {
		if (mezziDiTrasportoDisponibiliConCorriere == null) return 0;
		return getMezziDiTrasportoDisponibiliConCorriere().size();
	}

	public LocalDate getSelectedOrdersDate() {
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
				if (result == null || result.isAfter(orarioInizio))
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
				if (result == null || result.isBefore(orarioFine))
				{
					result = orarioFine;
				}
			}
		}
		return result;
	}

	public boolean existsMezzoDiTrasportoForWeight() {
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
