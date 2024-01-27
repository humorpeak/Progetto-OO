package unina.delivery;

import java.awt.Dimension;
import javax.swing.table.*;

import org.httprpc.sierra.DatePicker;

import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
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
import org.httprpc.sierra.TimePicker;

public class OrdiniPage extends JFrame {
	private static final long serialVersionUID = 5710891036621600811L;
	private Controller myController;
	private JTextField usernameField;
	private JTable ordersTable;
	private JScrollPane scrollPane;
	private static ArrayList<RigaOrdine> orderList;
	private DatePicker datePicker;
	private TimePicker initialTimePicker;
	private JLabel initialTimePickerLabel;
	private JLabel finalTimePickerLabel;
	private TimePicker finalTimePicker;
	
	public OrdiniPage(Controller controller) {
		orderList = new ArrayList<RigaOrdine>();
		getContentPane().setBackground(new Color(0, 0, 0));
		myController = controller;
		setBounds(500, 230, 0, 0);
		setMinimumSize(new Dimension(1200,550));
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(237, 51, 59));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
		
		TableModel dataModel = createDataModelForOrdersTable();
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
		        int row = ordersTable.rowAtPoint(evt.getPoint());
		        int col = ordersTable.columnAtPoint(evt.getPoint());
		        if (row >= 0 && col == 0) {
		        	orderList.get(row).toggle();
		        }
		    }
		});
		panel_1.add(ordersTable);
		
		scrollPane = new JScrollPane();
		panel_1.add(scrollPane);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(255, 163, 72));
		getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel usernameLabel = new JLabel("Username:");
		panel.add(usernameLabel);
		
		usernameField = new JTextField();
		usernameField.setMaximumSize(new Dimension(2147483647, 30));
		usernameField.setToolTipText("Inserisci una e-mail per filtrare i risultati in base all'utente che ha effettuato l'ordine.");
		panel.add(usernameField);
		usernameField.setColumns(20);
		
		JLabel dateLabel = new JLabel("Data:");
		panel.add(dateLabel);
		
		datePicker = new DatePicker();
		panel.add(datePicker);
		
		initialTimePickerLabel = new JLabel("Orario inizio:");
		panel.add(initialTimePickerLabel);
		
		initialTimePicker = new TimePicker();
		panel.add(initialTimePicker);
		
		finalTimePickerLabel = new JLabel("Orario fine:");
		panel.add(finalTimePickerLabel);
		
		finalTimePicker = new TimePicker();
		panel.add(finalTimePicker);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                myController.exit();
            }
        });
	}

	/**
	 * @return an appropriate AbstractTableModel for the table "ordersTable"
	 */
	private TableModel createDataModelForOrdersTable() {
		return new AbstractTableModel() {
			private String columnNames[] = { "Email", "Data", "Orario Inizio", "Orario Fine"  };
			@Override
			public String getColumnName(int index) {
			    return columnNames[index];
			}
			
			@Override
		      public Class getColumnClass(int col) {
		        if (col == 0)       //second column accepts only Integer values
		            return Boolean.class;
		        else return String.class;  //other columns accept String values
		    }
		 
		    @Override
		      public boolean isCellEditable(int row, int col) {
		        return col == 0;
		      }
			
		    public int getColumnCount() { return columnNames.length; }
		    public int getRowCount() { if (OrdiniPage.orderList == null) return 0; else return OrdiniPage.orderList.size();}
		    public Object getValueAt(int row, int col) { 
		    	RigaOrdine riga = OrdiniPage.orderList.get(row);
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
		    	default:
		    		return "error";
		    	}
		    }
		};
	}

	public void setOrderList(ArrayList<Ordine> listaordini) {
		OrdiniPage.orderList = new ArrayList<RigaOrdine>(listaordini.size());
		System.out.println("Restart");
		for (Ordine o : listaordini)
		{
			RigaOrdine nuovaRiga = new RigaOrdine(o);
			orderList.add(nuovaRiga);
		}
	}
}
