package unina.delivery;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import com.github.lgooddatepicker.components.DatePicker;
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
import javax.swing.table.TableModel;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.awt.event.ActionEvent;
import javax.swing.ListSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LogisticaPage extends JFrame {
	
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
	
	public LogisticaPage(Controller controller) {
		myController = controller;
		
		setIconImage(Toolkit.getDefaultToolkit().getImage((LoginPage.class.getResource("/unina/delivery/resources/logo.png"))));
		setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));		
		setTitle("UninaDelivery");
		
		panel = new JPanel();
		setContentPane(panel);
		panel.setMinimumSize(new Dimension(640, 400));
		panel.setForeground(new Color(0, 0, 0));
		panel.setLayout(new MigLayout("", "[5px:10px,left][10px:200px,grow][5px:n][10px:200px,grow][5px:10px,right]", "[::60px,grow,top][][][grow][][][10px:n,bottom]"));
		
		JPanel filtersPanel = new JPanel();
		panel.add(filtersPanel, "cell 0 0 5 1,alignx center,aligny center");
		filtersPanel.setLayout(new MigLayout("", "[][][][][][][50px][]", "[][]"));
		
		JLabel dateLabel = new JLabel("Data programmata:");
		filtersPanel.add(dateLabel, "cell 0 0,alignx right,aligny center");
		
		datePicker = new DatePicker();
		datePicker.setPreferredSize(new Dimension(160, 19));
		filtersPanel.add(datePicker, "cell 1 0 2 1,alignx left,aligny center");
		
		applyButton = new JButton("Applica");
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applicaButtonPressed();
			}
		});
		filtersPanel.add(applyButton, "cell 7 0 1 2,alignx right,aligny center");
		
		initialTimePickerLabel = new JLabel("Orario di partenza:");
		filtersPanel.add(initialTimePickerLabel, "cell 0 1,alignx right,aligny center");
		
		initialTimePicker = new TimePicker();
		filtersPanel.add(initialTimePicker, "cell 1 1,alignx left,aligny top");
		
		finalTimePickerLabel = new JLabel("Orario di ritorno:");
		filtersPanel.add(finalTimePickerLabel, "cell 2 1,alignx right,aligny center");
		
		finalTimePicker = new TimePicker();
		filtersPanel.add(finalTimePicker, "cell 3 1,alignx left,aligny top");
		
		JLabel vehiclesLabel = new JLabel("Mezzi di trasporto disponibili");
		panel.add(vehiclesLabel, "cell 1 2");
		
		JLabel shippersLabel = new JLabel("Corrieri disponibili");
		panel.add(shippersLabel, "cell 3 2");
		
		vehiclesScrollPane = new JScrollPane();
		panel.add(vehiclesScrollPane, "cell 1 3,grow");
		
		TableModel vehiclesDataModel = new MezziDiTrasportoTableModel(myController);
		vehiclesTable = new JTable(vehiclesDataModel);
		vehiclesTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				vehiclesTableButtonClicked(e);
			}
		});
		vehiclesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		vehiclesScrollPane.setViewportView(vehiclesTable);
		
		shippersScrollPane = new JScrollPane();
		panel.add(shippersScrollPane, "cell 3 3,grow");
		
		TableModel shippersDataModel = new CorrieriTableModel(myController);
		shippersTable = new JTable(shippersDataModel);
		shippersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		shippersScrollPane.setViewportView(shippersTable);
		
		JButton backButton = new JButton("Indietro");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				backButtonPressed();
			}
		});
		panel.add(backButton, "cell 1 5,alignx left,aligny top");
		
		JButton saveButton = new JButton("Salva");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				salvaClicked();
			}
		});
		panel.add(saveButton, "cell 3 5,alignx right,aligny top");
		
		
		//setBackground(new Color(255, 234, 234));
		setSize(new Dimension(640, 480));
		setLocationRelativeTo(null);				
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(640, 480));
	}
	
	private void backButtonPressed()
	{
		myController.backButtonPressedFromLogisticaToOrdiniPage();
	}

	private void applicaButtonPressed() {
		LocalDate date = datePicker.getDate();
		LocalTime initTime = initialTimePicker.getTime();
		LocalTime finalTime = finalTimePicker.getTime();
		appliedDate = date;
		appliedInitialTime = initTime;
		appliedFinalTime = finalTime;
		vehiclesTable.clearSelection();
		myController.applicaButtonPressedLogisticaPage(date, initTime, finalTime);
		vehiclesTable.invalidate();
		vehiclesTable.repaint();
	}
	
	private void vehiclesTableButtonClicked(MouseEvent e) {
    	int selectedVehicleRow = vehiclesTable.getSelectedRow();
    	if (selectedVehicleRow == -1) return;
		String targa = (String) vehiclesTable.getValueAt(selectedVehicleRow, 1);
		myController.retrieveCorrieriDisponibiliPerMezzoDiTrasporto(appliedDate, appliedInitialTime, appliedFinalTime, targa);
		shippersTable.invalidate();
		shippersTable.repaint();
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
	    			JOptionPane.showMessageDialog(this, "Si prega di riempire i campi 'data', 'orario inizio' e 'orario fine'", "Data o orario mancanti", JOptionPane.WARNING_MESSAGE);
		    	}
		    	else
		    	{
		        	String targa = (String) vehiclesTable.getValueAt(selectedVehicleRow, 1);
		        	String codiceFiscale = (String) shippersTable.getValueAt(selectedShipperRow, 3);
		        	
		        	myController.creaSpedizione(appliedDate, appliedInitialTime, appliedFinalTime, targa, codiceFiscale);
		    	}
	    	}
    	}
	}
	
	class MezziDiTrasportoTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 1L;
		private String columnNames[] = { "Tipo", "Targa", "Capienza", "Corrieri disponibili"};
		private Controller myController;
		
		MezziDiTrasportoTableModel(Controller controller)
		{
			myController = controller;
		}
		
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
	    public int getRowCount() {return myController.countMezziDiTrasportoWithCorrieri();}
	    
	    @Override
	    public Object getValueAt(int row, int col) { 
	    	MezzoDiTrasporto riga = myController.getMezziDiTrasportoDisponibiliConCorriere().get(row);
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
		private Controller myController;
		
		CorrieriTableModel(Controller controller)
		{
			myController = controller;
		}
		
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
	    public int getRowCount() {return myController.getNumberOfCorrieriDisponibili();}
	    
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
}
