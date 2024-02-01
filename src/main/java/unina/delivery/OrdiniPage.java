package unina.delivery;

import java.awt.Dimension;
import javax.swing.table.*;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;

import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.Cursor;
import java.awt.FlowLayout;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class OrdiniPage extends JFrame {
	private static final long serialVersionUID = 5710891036621600811L;
	private Controller myController;
	private JTextField usernameField;
	private JTable ordersTable;
	private JScrollPane scrollPane;
	private DatePicker datePicker;
	private TimePicker initialTimePicker;
	private JLabel initialTimePickerLabel;
	private JLabel finalTimePickerLabel;
	private TimePicker finalTimePicker;
	private JButton applyButton;
	private JButton backButton;
	private JButton confirmButton;
	
	public OrdiniPage(Controller controller) {
		myController = controller;
		myController.setFilteredOrdersRows(new ArrayList<OrdineConSelezione>());
		
		getContentPane().setBackground(new Color(0, 0, 0));
		setBounds(250, 220, 0, 0);
		setMinimumSize(new Dimension(1200,550));
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel tablePanel = new JPanel();
		tablePanel.setBackground(new Color(237, 51, 59));
		getContentPane().add(tablePanel, BorderLayout.CENTER);
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
		
		TableModel dataModel = new OrdersTableModel(myController);
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
		
		scrollPane = new JScrollPane(ordersTable);
		scrollPane.setBackground(new Color(255,0,255));
		tablePanel.add(scrollPane);
		
		
		JPanel filtersPanel = new JPanel();
		filtersPanel.setBackground(new Color(255, 163, 72));
		getContentPane().add(filtersPanel, BorderLayout.NORTH);
		filtersPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel usernameLabel = new JLabel("Username:"); //TODO cambiare in email
		filtersPanel.add(usernameLabel);
		
		usernameField = new JTextField();
		usernameField.setMaximumSize(new Dimension(2147483647, 30));
		usernameField.setToolTipText("Inserisci una e-mail per filtrare i risultati in base all'utente che ha effettuato l'ordine.");
		filtersPanel.add(usernameField);
		usernameField.setColumns(20);
		
		JLabel dateLabel = new JLabel("Data:");
		filtersPanel.add(dateLabel);
		
		datePicker = new DatePicker();
		filtersPanel.add(datePicker);
		
		initialTimePickerLabel = new JLabel("Orario inizio:");
		filtersPanel.add(initialTimePickerLabel);
		
		initialTimePicker = new TimePicker();
		filtersPanel.add(initialTimePicker);
		
		finalTimePickerLabel = new JLabel("Orario fine:");
		filtersPanel.add(finalTimePickerLabel);
		
		finalTimePicker = new TimePicker();
		filtersPanel.add(finalTimePicker);
		
		applyButton = new JButton("Applica");
		applyButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				applyFilters();
			}
		});
		filtersPanel.add(applyButton);
		
		backButton = new JButton("Indietro");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				myController.backButtonPressedFromOrdiniToHomePage();
			}
		});
		filtersPanel.add(backButton);
		
		confirmButton = new JButton("Conferma");
		confirmButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				myController.confirmButtonPressed();
			}
		});
		filtersPanel.add(confirmButton);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                myController.exit();
            }
        });
	}

	private boolean doesOrderSatisfyFilters(Ordine o) {
		String username = usernameField.getText();
		boolean sameEmail = username.isEmpty() || o.getAcquirente().equals(username);

		LocalDate date = datePicker.getDate();
		boolean sameDate = date == null || o.getData().equals(date);
		
		LocalTime initialTime = initialTimePicker.getTime();
		boolean afterInitialTime = initialTime == null || o.getOrarioinizio().isAfter(initialTime);

		LocalTime finalTime = finalTimePicker.getTime();
		boolean afterFinalTime = finalTime == null || o.getOrariofine().isBefore(finalTime);
		
		return sameEmail && sameDate && afterInitialTime && afterFinalTime;
	}
	
	private void applyFilters() {
		List<OrdineConSelezione> filteredOrdersRows = new ArrayList<>();
		for (OrdineConSelezione row : myController.getOrdersWithSelection())
		{
			if (doesOrderSatisfyFilters(row.ordine))
			{
				filteredOrdersRows.add(row);
			}
		}
		myController.setFilteredOrdersRows(filteredOrdersRows);
		repaint();
	}
	
	private void toggleClickedOrder(MouseEvent evt) {
		int row = ordersTable.rowAtPoint(evt.getPoint());
        int col = ordersTable.columnAtPoint(evt.getPoint());
        if (row >= 0 && col == 0) {
        	myController.toggleOrder(row);
        }
	}
	
	class OrdersTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 1L;
		private String columnNames[] = { "", "Email", "Data", "Orario Inizio", "Orario Fine", "Peso"  };
		private Controller myController;
		
		OrdersTableModel(Controller controller)
		{
			myController = controller;
		}
		
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
