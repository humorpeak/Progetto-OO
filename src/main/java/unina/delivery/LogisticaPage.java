package unina.delivery;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.TimePicker;

import javax.swing.JFrame;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.awt.event.ActionEvent;
import javax.swing.ListSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LogisticaPage extends JFrame {
	private static final long serialVersionUID = 1L;
	private Controller myController;
	private JPanel panel;
	private JTable vehiclesTable;
	private JTable shippersTable;
	private DatePicker datePicker;
	private TimePicker initialTimePicker;
	private JLabel initialTimePickerLabel;
	private JLabel finalTimePickerLabel;
	private TimePicker finalTimePicker;
	private JButton applyButton;
	private JScrollPane vehiclesScrollPane;
	private JScrollPane shippersScrollPane;
	private LocalDate appliedDate;
	private LocalTime appliedInitialTime;
	private LocalTime appliedFinalTime;
	private JPanel filtersPanel;
	private JLabel dateLabel;
	private JLabel vehiclesLabel;
	private JButton backButton;
	private JLabel shippersLabel;
	private JButton saveButton;
	private MezziDiTrasportoTableModel vehiclesDataModel;
	private CorrieriTableModel shippersDataModel;
	
	LogisticaPage(Controller controller) {
		myController = controller;
		
		setIconImage(Toolkit.getDefaultToolkit().getImage((LoginPage.class.getResource("/unina/delivery/resources/logo.png"))));
		setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));		
		setTitle("UninaDelivery - Creazione Spedizione");
		
		panel = new JPanel();
		setContentPane(panel);
		panel.setMinimumSize(new Dimension(940, 400));
		panel.setForeground(new Color(0, 0, 0));
		panel.setLayout(new MigLayout("", "[5px:10px,left][10px:200px,grow][5px:n][10px:200px,grow][5px:10px,right]", "[::60px,grow,top][][][grow][][][10px:n,bottom]"));
		
		filtersPanel = new JPanel();
		panel.add(filtersPanel, "cell 0 0 5 1,alignx center,aligny center");
		filtersPanel.setLayout(new MigLayout("", "[][][][][][][50px][]", "[][]"));
		
		dateLabel = new JLabel("Data programmata:");
		filtersPanel.add(dateLabel, "cell 0 0,alignx right,aligny center");

		DatePickerSettings datePickerSettings = new DatePickerSettings();
		datePicker = new DatePicker(datePickerSettings);
		datePicker.setPreferredSize(new Dimension(160, 19));
		datePickerSettings.setDateRangeLimits(LocalDate.now(), null);
		filtersPanel.add(datePicker, "cell 1 0 2 1,alignx left,aligny center");
		
		applyButton = new JButton("Applica");
		applyButton.setFocusPainted(false);
		applyButton.setBorderPainted(false);
		applyButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				applyButton.setContentAreaFilled(false);
				applyButton.setOpaque(true);
				applyButton.setBackground(new Color(255, 213, 213));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				applyButton.setContentAreaFilled(true);
				applyButton.setBackground(new Color(255, 128, 128));
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				applyButton.setContentAreaFilled(false);
				applyButton.setOpaque(true);
				applyButton.setBackground(new Color(255, 170, 170));
			}
		});
		filtersPanel.add(applyButton, "cell 7 0 1 2,alignx right,aligny center");
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applicaButtonPressed();
			}
		});
		
		initialTimePickerLabel = new JLabel("Orario di partenza:");
		filtersPanel.add(initialTimePickerLabel, "cell 0 1,alignx right,aligny center");
		
		initialTimePicker = new TimePicker();
		filtersPanel.add(initialTimePicker, "cell 1 1,alignx left,aligny top");
		
		finalTimePickerLabel = new JLabel("Orario di ritorno:");
		filtersPanel.add(finalTimePickerLabel, "cell 2 1,alignx right,aligny center");
		
		finalTimePicker = new TimePicker();
		filtersPanel.add(finalTimePicker, "cell 3 1,alignx left,aligny top");
		
		vehiclesLabel = new JLabel("Mezzi di trasporto disponibili");
		panel.add(vehiclesLabel, "cell 1 2");
		
		shippersLabel = new JLabel("Corrieri disponibili");
		panel.add(shippersLabel, "cell 3 2");
		
		vehiclesScrollPane = new JScrollPane();
		panel.add(vehiclesScrollPane, "cell 1 3,grow");
		
		vehiclesDataModel = new MezziDiTrasportoTableModel();
		vehiclesTable = new JTable(vehiclesDataModel);
		vehiclesTable.setRequestFocusEnabled(false);
		vehiclesTable.setFocusable(false);
		vehiclesTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		vehiclesTable.setShowVerticalLines(false);
		vehiclesTable.setShowGrid(false);
		vehiclesTable.setBorder(null);
		vehiclesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		vehiclesScrollPane.setViewportView(vehiclesTable);
		
		shippersScrollPane = new JScrollPane();
		panel.add(shippersScrollPane, "cell 3 3,grow");
		
		shippersDataModel = new CorrieriTableModel();
		shippersTable = new JTable(shippersDataModel);
		shippersTable.setRequestFocusEnabled(false);
		shippersTable.setFocusable(false);
		shippersTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		shippersTable.setShowVerticalLines(false);
		shippersTable.setShowGrid(false);
		shippersTable.setBorder(null);
		shippersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		shippersScrollPane.setViewportView(shippersTable);
		

		vehiclesTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				vehiclesTableButtonClicked(e);
				shippersDataModel.fireTableDataChanged();
			}
		});
		
		backButton = new JButton("Indietro");
		backButton.setFocusPainted(false);
		backButton.setBorderPainted(false);
		backButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				backButton.setContentAreaFilled(false);
				backButton.setOpaque(true);
				backButton.setBackground(new Color(255, 213, 213));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				backButton.setContentAreaFilled(true);
				backButton.setBackground(new Color(255, 128, 128));
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				backButton.setContentAreaFilled(false);
				backButton.setOpaque(true);
				backButton.setBackground(new Color(255, 170, 170));
			}
		});
		panel.add(backButton, "cell 1 5,alignx left,aligny top");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				backButtonPressed();
			}
		});
		
		saveButton = new JButton("Salva");
		saveButton.setFocusPainted(false);
		saveButton.setBorderPainted(false);
		saveButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				saveButton.setContentAreaFilled(false);
				saveButton.setOpaque(true);
				saveButton.setBackground(new Color(255, 213, 213));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				saveButton.setContentAreaFilled(true);
				saveButton.setBackground(new Color(255, 128, 128));
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				saveButton.setContentAreaFilled(false);
				saveButton.setOpaque(true);
				saveButton.setBackground(new Color(255, 170, 170));
			}
		});
		panel.add(saveButton, "cell 3 5,alignx right,aligny top");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				salvaClicked();
			}
		});
		
		
		setSize(new Dimension(940, 480));
		setMinimumSize(new Dimension(1240, 480));
		setLocationRelativeTo(null);				
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
            	exitButtonPressed();
            }
        });
	}
	
	private void backButtonPressed()
	{
		myController.backButtonPressedFromLogisticaToOrdiniPage();
	}

	private void applicaButtonPressed() {
		LocalDate date = datePicker.getDate();
		LocalTime initTime = initialTimePicker.getTime();
		LocalTime finalTime = finalTimePicker.getTime();
		if (finalTime != null && finalTime.isBefore(initTime))
		{
			JOptionPane.showMessageDialog(this, "La data di arrivo deve essere successiva alla data di partenza.", "Impossibile applicare", JOptionPane.WARNING_MESSAGE);
			return;
		}
		appliedDate = date;
		appliedInitialTime = initTime;
		appliedFinalTime = finalTime;
		myController.applicaButtonPressedLogisticaPage(date, initTime, finalTime);
		vehiclesDataModel.fireTableDataChanged();
		shippersDataModel.fireTableDataChanged();
	}
	
	private void vehiclesTableButtonClicked(MouseEvent e) {
    	int selectedVehicleRow = vehiclesTable.getSelectedRow();
    	if (selectedVehicleRow == -1) return;
		String targa = (String) vehiclesTable.getValueAt(selectedVehicleRow, 1);
		myController.retrieveAvailableShippersForVehicle(appliedDate, appliedInitialTime, appliedFinalTime, targa);
	}
	
	private void salvaClicked() {
		int selectedVehicleRow = vehiclesTable.getSelectedRow();
		int selectedShipperRow = shippersTable.getSelectedRow();
		
    	if (selectedVehicleRow == -1)
    	{
    		JOptionPane.showMessageDialog(this, "Selezionare un mezzo di trasporto", "Nessun mezzo selezionato", JOptionPane.WARNING_MESSAGE);
    	}
    	else {
    		if (selectedShipperRow == -1)
	    	{
	    		JOptionPane.showMessageDialog(this, "Selezionare un corriere", "Nessun corriere selezionato", JOptionPane.WARNING_MESSAGE);
	    	}
	    	else 
	    	{
	    		if (appliedDate == null || appliedInitialTime == null || appliedFinalTime == null)
		    	{
	    			JOptionPane.showMessageDialog(this, "Si prega di cliccare 'Applica' dopo aver riempito i campi 'data', 'orario inizio' e 'orario fine'", "Data o orario mancanti", JOptionPane.WARNING_MESSAGE);
		    	}
		    	else
		    	{
		    		String message = "Confermi di voler creare una spedizione per il giorno " + appliedDate + " in partenza alle ore " + appliedInitialTime + " con arrivo stimato alle " + appliedFinalTime + "?";
		    		int noPressed = JOptionPane.showConfirmDialog(this, message, "Conferma", JOptionPane.YES_NO_OPTION);
		    		if (noPressed == 1) return;
		    		
		        	String targa = (String) vehiclesTable.getValueAt(selectedVehicleRow, 1);
		        	String codiceFiscale = (String) shippersTable.getValueAt(selectedShipperRow, 3);
		        	
		        	myController.createShipment(appliedDate, appliedInitialTime, appliedFinalTime, targa, codiceFiscale);
		    	}
	    	}
    	}
	}
	
	class MezziDiTrasportoTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 1L;
		private String columnNames[] = { "Tipo", "Targa", "Capienza", "Corrieri disponibili"};
		
		@Override
		public String getColumnName(int index) {
		    return columnNames[index];
		}
		
		@Override
	      public Class<?> getColumnClass(int col) {
	        return String.class;
	    }
		
	    @Override
	      public boolean isCellEditable(int row, int col) {
	        return false;
	      }
		
	    @Override
	    public int getColumnCount() { return columnNames.length; }
	    
	    @Override
	    public int getRowCount() {return myController.getNumberOfAvailableVehiclesWithShipper();}
	    
	    @Override
	    public Object getValueAt(int row, int col) { 
	    	MezzoDiTrasporto riga = myController.getAvailableVehiclesWithShipper().get(row);
	    	switch(col)
	    	{
	    	case 0:
	    		return riga.getTipoMezzo();
	    	case 1:
	    		return riga.getTarga();
	    	case 2:
	    		return riga.getCapienza();
	    	case 3:
	    		return riga.getNumeroCorrieriDisponibili();
	    	default:
	    		return "error";
	    	}
	    }
	}
	
	class CorrieriTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 1L;
		private String columnNames[] = { "Nome", "Cognome", "Patente", "Codice Fiscale"};
		
		@Override
		public String getColumnName(int index) {
		    return columnNames[index];
		}
		
		@Override
	      public Class<?> getColumnClass(int col) {
	        return String.class;
	    }
		
	    @Override
	      public boolean isCellEditable(int row, int col) {
	        return false;
	      }
		
	    @Override
	    public int getColumnCount() { return columnNames.length; }
	    
	    @Override
	    public int getRowCount() {return myController.getNumberOfAvailableShippers();}
	    
	    @Override
	    public Object getValueAt(int row, int col) {
	    	Corriere riga = myController.getCorrieriDisponibili().get(row);
	    	switch(col)
	    	{
	    	case 0:
	    		return riga.getNome();
	    	case 1:
	    		return riga.getCognome();
	    	case 2:
	    		return riga.getTipoPatente();
	    	case 3:
	    		return riga.getCodiceFiscale();
	    	default:
	    		return "error";
	    	}
	    }
	}

	protected void resetFilters() {
		appliedDate = null;
		appliedFinalTime = null;
		appliedInitialTime = null;
		datePicker.setDate(null);
		initialTimePicker.setTime(null);
		finalTimePicker.setTime(null);
	}
	
	private void exitButtonPressed() {
    	int noSelected = JOptionPane.showConfirmDialog(this, "Sei sicuro di voler uscire dalla tua area di lavoro? Il tuo lavoro andrà perso.", "", JOptionPane.YES_NO_OPTION);
        if (noSelected == 0) myController.exit();
	}
}
