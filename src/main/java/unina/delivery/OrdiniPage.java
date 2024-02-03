package unina.delivery;

import java.awt.Dimension;
import javax.swing.table.*;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;

import javax.swing.JFrame;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import net.miginfocom.swing.MigLayout;


public class OrdiniPage extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private Controller myController;
	private JPanel panel;
	private JPanel filtersPanel;
	private JLabel dateLabel;
	private DatePicker datePicker;
	private TimePicker initialTimePicker;
	private JLabel initialTimePickerLabel;
	private JLabel finalTimePickerLabel;
	private TimePicker finalTimePicker;
	private JScrollPane scrollPane;
	private JTable ordersTable;
	private JButton resetButton;
	private JButton applyButton;
	private JButton backButton;
	private JLabel actualVehiclesLabel;
	private JLabel weightLabel;
	private JLabel actualWeightLabel;
	private JLabel vehiclesLabel;
	private JLabel usernameLabel;
	private JTextField usernameField;
	private JButton confirmButton;
	private OrdersTableModel dataModel;
	
	OrdiniPage(Controller controller) {
		myController = controller;
		
		setIconImage(Toolkit.getDefaultToolkit().getImage((ReportPage.class.getResource("/unina/delivery/resources/logo.png"))));
		setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));		
		setTitle("UninaDelivery - Creazione Spedizione");
		
		panel = new JPanel();
		setContentPane(panel);
		panel.setMinimumSize(new Dimension(640, 400));
		panel.setForeground(new Color(0, 0, 0));
		panel.setLayout(new MigLayout("", "[10px:n,left][200px,grow][grow][grow][grow][40px,grow][grow][grow][grow][200px,grow][10px:n,right]", "[10px:n,top][][grow][][][10px:n,bottom]"));
		
		filtersPanel = new JPanel();
		panel.add(filtersPanel, "cell 1 1 9 1,alignx center,growy");
		filtersPanel.setLayout(new MigLayout("", "[][grow][][][][][][][][][]", "[]"));
		
		usernameLabel = new JLabel("E-mail dell'acquirente:");
		filtersPanel.add(usernameLabel, "flowx,cell 0 0,alignx trailing");
		
		usernameField = new JTextField();
		usernameField.setMinimumSize(new Dimension(30, 19));
		usernameField.setToolTipText("Inserisci una e-mail per filtrare i risultati in base all'utente che ha effettuato l'ordine.");
		filtersPanel.add(usernameField, "cell 1 0,growx");
		usernameField.setColumns(10);
		
		dateLabel = new JLabel("Data:");
		filtersPanel.add(dateLabel, "cell 0 0");
		filtersPanel.add(dateLabel);
		
		datePicker = new DatePicker();
		filtersPanel.add(datePicker, "cell 3 0");
		
		initialTimePickerLabel = new JLabel("Orario di inizio:");
		filtersPanel.add(initialTimePickerLabel, "cell 4 0");
		
		initialTimePicker = new TimePicker();
		filtersPanel.add(initialTimePicker, "cell 5 0");
		
		finalTimePickerLabel = new JLabel("Orario di fine:");
		filtersPanel.add(finalTimePickerLabel, "cell 6 0");
		
		finalTimePicker = new TimePicker();
		filtersPanel.add(finalTimePicker, "flowx,cell 7 0");
		
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
		applyButton.setToolTipText("Clicca qui per applicare i filtri.");
		filtersPanel.add(applyButton, "cell 9 0");
		applyButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				applyFilters();
			}
		});
		
		resetButton = new JButton("Reset");
		resetButton.setFocusPainted(false);
		resetButton.setBorderPainted(false);
		resetButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				resetButton.setContentAreaFilled(false);
				resetButton.setOpaque(true);
				resetButton.setBackground(new Color(255, 213, 213));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				resetButton.setContentAreaFilled(true);
				resetButton.setBackground(new Color(255, 128, 128));
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				resetButton.setContentAreaFilled(false);
				resetButton.setOpaque(true);
				resetButton.setBackground(new Color(255, 170, 170));
			}
		});
		resetButton.setToolTipText("Clicca qui per resettare i tuoi filtri.");
		filtersPanel.add(resetButton, "cell 10 0");
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetFilters();
			}
		});
		
		scrollPane = new JScrollPane();
		panel.add(scrollPane, "cell 1 2 9 1,grow");
		
		dataModel = new OrdersTableModel();
		ordersTable = new JTable(dataModel);
		ordersTable.setRowSelectionAllowed(false);
		ordersTable.setRequestFocusEnabled(false);
		ordersTable.setFocusable(false);
		ordersTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		ordersTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		ordersTable.setShowVerticalLines(false);
		ordersTable.setShowGrid(false);
		ordersTable.setForeground(new Color(36, 31, 49));
		ordersTable.setBorder(null);
		ordersTable.setBackground(new Color(246, 245, 244));
		ordersTable.addMouseListener(new java.awt.event.MouseAdapter() {
		    @Override
		    public void mouseClicked(java.awt.event.MouseEvent evt) {
		        toggleClickedOrder(evt);
		    }
		});
		ordersTable.getColumnModel().getColumn(0).setMaxWidth(30);
		scrollPane.setViewportView(ordersTable);
		
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
		backButton.setToolTipText("Clicca qui per tornare alla Home.");
		panel.add(backButton, "cell 1 4,alignx left");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				backButtonPressed();
			}
		});
		
		weightLabel = new JLabel("Peso totale:");
		panel.add(weightLabel, "cell 3 4");
		
		actualWeightLabel = new JLabel("");
		panel.add(actualWeightLabel, "cell 4 4");
		
		vehiclesLabel = new JLabel("Mezzi di trasporto disponibili:");
		panel.add(vehiclesLabel, "cell 6 4");
		
		actualVehiclesLabel = new JLabel("");
		panel.add(actualVehiclesLabel, "cell 7 4");
		
		confirmButton = new JButton("Conferma");
		confirmButton.setFocusPainted(false);
		confirmButton.setBorderPainted(false);
		confirmButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				confirmButton.setContentAreaFilled(false);
				confirmButton.setOpaque(true);
				confirmButton.setBackground(new Color(255, 213, 213));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				confirmButton.setContentAreaFilled(true);
				confirmButton.setBackground(new Color(255, 128, 128));
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				confirmButton.setContentAreaFilled(false);
				confirmButton.setOpaque(true);
				confirmButton.setBackground(new Color(255, 170, 170));
			}
		});
		confirmButton.setToolTipText("Clicca qui per confermare gli ordini selezionati e andare avanti.");
		panel.add(confirmButton, "cell 9 4,alignx right");
		confirmButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				confermaClicked();
			}
		});
		
		setBackground(new Color(255, 234, 234));
		setSize(new Dimension(940, 480));
		setLocationRelativeTo(null);				
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setMinimumSize(new Dimension(940, 480));
		
		addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitButtonPressed();
            }
        });
	}
	
	private void backButtonPressed()
	{
		int noSelected = JOptionPane.showConfirmDialog(this, "Tornando indietro perderai tutto il lavoro fatto, continuare?", "Richiesta conferma", JOptionPane.YES_NO_OPTION);
		if (noSelected == 0) myController.backButtonPressedFromOrdiniToHomePage();
	}
	
	private void confermaClicked()
	{
		if (myController.noOrdersSelected())
		{
			JOptionPane.showMessageDialog(this, "Non puoi creare una spedizione vuota", "Nessun ordine selezionato", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			if (!myController.existsMezzoDiTrasportoForWeight())
			{
				JOptionPane.showMessageDialog(this, "Il peso complessivo della spedizione che si vuole creare è superiore alla "
						+ "capienza di ogni mezzo di trasporto della sede.", "Nessun mezzo disponibile", JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				boolean confirm = generateWarningsBeforeConfirm();
				if (confirm == false) return;
				myController.openLogisticaPage();
			}
		}
	}
	
	private boolean generateWarningsBeforeConfirm() {
		LocalDate ordersDate = myController.getSelectedOrdersDate();
		if (ordersDate == null)
		{
			int noSelected = JOptionPane.showConfirmDialog(this, "Si sta creando una spedizione per ordini la cui consegna è prevista in giorni diversi, è sicuro di voler continuare?", "Richiesta conferma", JOptionPane.YES_NO_OPTION);
			if (noSelected == 1) return false;
		}
		else 
		{
			if (myController.getSuggestedDepartureTimeForSelectedOrders().isAfter(myController.getSuggestedArrivalTimeForSelectedOrders()))
			{
				int noSelected = JOptionPane.showConfirmDialog(this, "Probabilmente non sarà possibile recapitare in orario gli ordini selezionati con un'unica spedizione perché questi hanno orari troppo differenti, è sicuro di voler continuare?", "Richiesta conferma", JOptionPane.YES_NO_OPTION);
				if (noSelected == 1) return false;
			}
		}
		return true;
	}

	protected boolean doesOrderSatisfyFilters(Ordine o) {
		String username = usernameField.getText();
		boolean sameEmail = username.isEmpty() || o.getAcquirente().equals(username);

		LocalDate date = datePicker.getDate();
		boolean sameDate = date == null || o.getData().equals(date);
		
		LocalTime initialTime = initialTimePicker.getTime();
		boolean afterInitialTime = initialTime == null || o.getOrarioinizio().isAfter(initialTime) || o.getOrarioinizio().equals(initialTime);

		LocalTime finalTime = finalTimePicker.getTime();
		boolean afterFinalTime = finalTime == null || o.getOrariofine().isBefore(finalTime) || o.getOrariofine().equals(finalTime);
		
		return sameEmail && sameDate && afterInitialTime && afterFinalTime;
	}
	
	private void applyFilters() {
		myController.updateFilteredRowsOrdiniPage();
		dataModel.fireTableDataChanged();
	}
	
	protected void resetFilters() {
		datePicker.clear();
		initialTimePicker.clear();
		finalTimePicker.clear();
		usernameField.setText("");
		applyFilters();
	}
	
	private void toggleClickedOrder(MouseEvent evt) {
		int row = ordersTable.rowAtPoint(evt.getPoint());
        int col = ordersTable.columnAtPoint(evt.getPoint());
        if (row >= 0 && col == 0) {
        	myController.toggleOrder(row);
    		dataModel.fireTableDataChanged();
        }
        
        updateFeedback();
    }
	
	private void updateFeedback() {
		LocalDate ordersDate = myController.getSelectedOrdersDate();
		String mezziDisponibili = "warning";
		actualVehiclesLabel.setToolTipText("Le date degli ordini selezionati non coincidono");
		if (myController.noOrdersSelected())
		{
			actualVehiclesLabel.setToolTipText("Non è possibile generare una spedizione senza prima selezionare ordini");
		}
		if (!myController.existsMezzoDiTrasportoForWeight())
		{
			mezziDisponibili = "error";
			actualVehiclesLabel.setToolTipText("Non esistono mezzi abbastanza capienti da contenere tutti gli ordini selezionati");
		}
		else
		{
			if (ordersDate != null) {
				LocalTime depTime = myController.getSuggestedDepartureTimeForSelectedOrders();
				LocalTime arrivalTime = myController.getSuggestedArrivalTimeForSelectedOrders();
				myController.retrieveAvailableVehicles(ordersDate, depTime, arrivalTime);
				mezziDisponibili = ((Integer) myController.getNumberOfAvailableVehiclesWithShipper()).toString();
				actualVehiclesLabel.setToolTipText("Il numero di mezzi di trasporto che saranno certamente in grado di recapitare gli "
						+ "ordini selezionati in orario");
			}
		}
		
		actualWeightLabel.setText(" " + myController.calculateWeightForSelectedOrders());
		setVehiclesLabelCustomText(mezziDisponibili);
		
	}
	
	private void setVehiclesLabelCustomText(String testo) {
		actualVehiclesLabel.setIcon(null);
		actualVehiclesLabel.setText(" ");
		if (testo.equals("error")) {
			actualVehiclesLabel.setIcon(new ImageIcon(getClass().getResource("/unina/delivery/resources/error.png")));
			
		}
		else if (testo.equals("warning")) {
			actualVehiclesLabel.setIcon(new ImageIcon(getClass().getResource("/unina/delivery/resources/warning.png")));
		}
		else {
			actualVehiclesLabel.setText(" " + testo);
		}
	}
	
	private void exitButtonPressed() {
    	int noSelected = JOptionPane.showConfirmDialog(this, "Sei sicuro di voler uscire dalla tua area di lavoro? Il tuo lavoro andrà perso.", "", JOptionPane.YES_NO_OPTION);
        if (noSelected == 0) myController.exit();
	}

	class OrdersTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 1L;
		private String columnNames[] = { "", "Email", "Data", "Orario Inizio", "Orario Fine", "Peso"  };
		
		@Override
		public String getColumnName(int index) {
		    return columnNames[index];
		}
		
		@Override
	      public Class<?> getColumnClass(int col) {
	        if (col == 0)       //first column accepts only Boolean values (checkbox)
	            return Boolean.class;
	        else return String.class;  //other columns accept String values
	    }
		
	    @Override
	      public boolean isCellEditable(int row, int col) {
	        return col == 0;
	      }
		
	    @Override
	    public int getColumnCount() { return columnNames.length; }
	    
	    @Override
	    public int getRowCount() {return myController.countFilteredOrders();}
	    
	    @Override
	    public Object getValueAt(int row, int col) { 
	    	OrdineConSelezione riga = myController.getFilteredOrdersRows().get(row);
	    	switch(col)
	    	{
	    	case 0:
	    		return riga.selected;
	    	case 1:
	    		return riga.ordine.getAcquirente();
	    	case 2:
	    		return riga.ordine.getData().toString();
	    	case 3:
	    		return riga.ordine.getOrarioinizio().toString();
	    	case 4:
	    		return riga.ordine.getOrariofine().toString();
	    	case 5:
	    		return riga.ordine.getPeso();
	    	default:
	    		return "error";
	    	}
	    }
	}
}
