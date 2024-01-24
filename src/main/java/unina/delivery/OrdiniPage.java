package unina.delivery;

import java.awt.Dimension;
import javax.swing.table.*;

import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.SwingConstants;

import java.awt.FlowLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import javax.swing.JInternalFrame;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JSeparator;

public class OrdiniPage extends JFrame {
	private static final long serialVersionUID = 5710891036621600811L;
	private Controller mycontroller;
	private JTextField usernameField;
	private JTable ordersTable;
	private JScrollPane scrollPane;
	private static ArrayList<Ordine> orderList;
	
	public OrdiniPage(Controller controller) {
		getContentPane().setBackground(new Color(0, 0, 0));
		mycontroller = controller;
		setBounds(500, 230, 0, 0);
		setMinimumSize(new Dimension(500,250));
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(new Color(237, 51, 59));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
		
		TableModel dataModel = createDataModelForOrdersTable();
		ordersTable = new JTable(dataModel);
		ordersTable.setBackground(new Color(143, 240, 164));
		panel_1.add(ordersTable);
		
		scrollPane = new JScrollPane();
		panel_1.add(scrollPane);
		
		
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(255, 163, 72));
		getContentPane().add(panel, BorderLayout.EAST);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JLabel filtriLabel = new JLabel("Filtri");
		panel.add(filtriLabel);
		
		JLabel usernameLabel = new JLabel("Username:");
		panel.add(usernameLabel);
		
		usernameField = new JTextField();
		usernameField.setMaximumSize(new Dimension(2147483647, 30));
		usernameField.setToolTipText("Inserisci una e-mail per filtrare i risultati in base all'utente che ha effettuato l'ordine.");
		panel.add(usernameField);
		usernameField.setColumns(20);
	}

	private TableModel createDataModelForOrdersTable() {
		return new AbstractTableModel() {
			private String columnNames[] = { "Email", "Data", "Orario Inizio", "Orario Fine"  };
			@Override
			public String getColumnName(int index) {
			    return columnNames[index];
			}
		    public int getColumnCount() { return columnNames.length; }
		    public int getRowCount() { if (OrdiniPage.orderList == null) return 0; else return OrdiniPage.orderList.size();}
		    public Object getValueAt(int row, int col) { 
		    	Ordine ordine = OrdiniPage.orderList.get(row);
		    	switch(col)
		    	{
		    	case 0:
		    		return ordine.getAcquirente();
		    	case 1:
		    		return ordine.getData().toString();
		    	case 2:
		    		return ordine.getOrarioinizio().toString();
		    	case 3:
		    		return ordine.getOrariofine().toString();
		    	default:
		    		return "error";
		    	}
		    }
		};
	}

	public void setOrderList(ArrayList<Ordine> listaordini) {
		OrdiniPage.orderList = listaordini;
	}
}
