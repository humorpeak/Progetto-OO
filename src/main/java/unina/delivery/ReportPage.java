package unina.delivery;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;

import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import java.awt.Rectangle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.JScrollPane;

public class ReportPage extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private Controller myController;
	private JPanel panel;
	private JLabel yearLabel;
	private JComboBox<Object> yearBox;
	private JLabel monthLabel;
	private JComboBox<Object> monthBox;
	private JButton calculateButton;
	private JPanel resultsPanel;
	private JLabel averageNumberOfOrdersLabel;
	private JLabel actualAverageNumberOfOrdersLabel;
	private JTable ordersWithMaxNumOfProductsTable;
	private JTable ordersWithMinNumOfProductsTable;
	private JLabel maxProductsOrderLabel;
	private JLabel minProductsOrderLabel;
	private JPanel maxtablePanel;
	private JPanel mintablePanel;
	private JScrollPane maxscrollPane;
	private JScrollPane minscrollPane;
	private JButton backButton;
	private MaxOrdersTableModel maxDataModel;
	MinOrdersTableModel minDataModel;
	
	abstract class OrdersReportTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 1L;
		
		private String columnNames[] = { "Acquirente", "Data", "Indirizzo"};
		
		@Override
		public String getColumnName(int index) {
		    return columnNames[index];
		}
		
		@Override
	      public Class<?> getColumnClass(int col) {
	        return String.class;
	    }
		
	    @Override
	    public int getColumnCount() {
	    	return columnNames.length;
	    }
	}
	
	class MaxOrdersTableModel extends OrdersReportTableModel {
		private static final long serialVersionUID = 1L;

	    @Override
	    public int getRowCount() {
	    	return myController.countOrdersWithMaxNumOfProducts();
	    }
	    
	    @Override
	    public Object getValueAt(int row, int col) { 
	    	Ordine riga = myController.getOrdiniWithMaxNumOfProductsRows().get(row);
	    	System.out.println(riga);
	    	switch(col)
	    	{
	    	case 0:
	    		return riga.getAcquirente();
	    	case 1:
	    		return riga.getData().toString();
	    	case 2:
	    		return riga.getIndirizzo();
	    	default:
	    		return "error";
	    	}
	    }
	}
	
	class MinOrdersTableModel extends OrdersReportTableModel {
		private static final long serialVersionUID = 1L;
		
	    @Override
	    public int getRowCount() {
	    	return myController.countOrdersWithMinNumOfProducts();
	    }
	    
	    @Override
	    public Object getValueAt(int row, int col) { 
	    	Ordine riga = myController.getOrdiniWithMinNumOfProductsRows().get(row);
	    	switch(col)
	    	{
	    	case 0:
	    		return riga.getAcquirente();
	    	case 1:
	    		return riga.getData().toString();
	    	case 2:
	    		return riga.getIndirizzo();
	    	default:
	    		return "error";
	    	}
	    }
	}
	
	ReportPage(Controller controller) {
		myController = controller;
		setIconImage(Toolkit.getDefaultToolkit().getImage((ReportPage.class.getResource("/unina/delivery/resources/logo.png"))));
		setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));		
		setTitle("UninaDelivery - Report");
		
		panel = new JPanel();
		setContentPane(panel);
		panel.setMinimumSize(new Dimension(640, 400));
		panel.setForeground(new Color(0, 0, 0));
		panel.setLayout(new MigLayout("", "[][][grow][][][grow][][][]", "[][][][grow][][][]"));
		
		yearLabel = new JLabel("Anno: ");
		panel.add(yearLabel, "cell 1 1,alignx right");
		
		yearBox = new JComboBox<Object>();
		yearBox.setModel(new DefaultComboBoxModel<Object>(new String[] {"2024", "2023", "2022", "2021"}));
		yearBox.setToolTipText("Seleziona l'anno di cui desideri visualizzare i report.");
		panel.add(yearBox, "cell 2 1,growx");
		
		monthLabel = new JLabel("Mese: ");
		panel.add(monthLabel, "cell 4 1,alignx right");
		
		monthBox = new JComboBox<Object>();
		monthBox.setModel(new DefaultComboBoxModel<Object>(new String[] {"Gen", "Feb", "Mar", "Apr", "Mag", "Giu", "Lug", "Ago", "Set", "Ott", "Nov", "Dic"}));
		monthBox.setToolTipText("Seleziona il mese di cui desideri visualizzare i report.");
		panel.add(monthBox, "cell 5 1,growx");
		
		calculateButton = new JButton("Calcola");
		calculateButton.setFocusPainted(false);
		calculateButton.setFocusable(false);
		calculateButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				calculateButton.setContentAreaFilled(false);
				calculateButton.setOpaque(true);
				calculateButton.setBackground(new Color(255, 213, 213));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				calculateButton.setContentAreaFilled(true);
				calculateButton.setBackground(new Color(255, 128, 128));
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				calculateButton.setContentAreaFilled(false);
				calculateButton.setOpaque(true);
				calculateButton.setBackground(new Color(255, 170, 170));
			}
		});
		panel.add(calculateButton, "cell 7 1");
		
		resultsPanel = new JPanel();
		resultsPanel.setBounds(new Rectangle(200, 200, 200, 200));
		panel.add(resultsPanel, "cell 1 3 7 1,alignx center,aligny center");
		resultsPanel.setLayout(new MigLayout("", "[grow][grow]", "[][][][grow][grow][][][grow]"));
		
		averageNumberOfOrdersLabel = new JLabel("Numero medio di ordini effettuati ogni giorno:");
		averageNumberOfOrdersLabel.setVisible(false);
		resultsPanel.add(averageNumberOfOrdersLabel, "cell 0 0");
		
		actualAverageNumberOfOrdersLabel = new JLabel("");
		resultsPanel.add(actualAverageNumberOfOrdersLabel, "cell 1 0,alignx right");
		
		maxProductsOrderLabel = new JLabel("Ordini (consegnati/da consegnare) con il maggior numero di prodotti");
		maxProductsOrderLabel.setVisible(false);
		resultsPanel.add(maxProductsOrderLabel, "cell 0 2");
		
		maxtablePanel = new JPanel();
		maxtablePanel.setVisible(false);
		resultsPanel.add(maxtablePanel, "cell 0 3 2 1,grow");
		
		maxDataModel = new MaxOrdersTableModel();
		ordersWithMaxNumOfProductsTable = new JTable(maxDataModel);
		ordersWithMaxNumOfProductsTable.setFocusable(false);
		ordersWithMaxNumOfProductsTable.setShowVerticalLines(false);
		ordersWithMaxNumOfProductsTable.setShowGrid(false);
		ordersWithMaxNumOfProductsTable.setRowSelectionAllowed(false);
		
		maxscrollPane = new JScrollPane(ordersWithMaxNumOfProductsTable);
		maxscrollPane.setPreferredSize(new Dimension(550, 100));
		maxtablePanel.add(maxscrollPane);
		
		minProductsOrderLabel = new JLabel("Ordini (consegnati/da consegnare) con il minor numero di prodotti");
		minProductsOrderLabel.setVisible(false);
		resultsPanel.add(minProductsOrderLabel, "flowy,cell 0 6");

		mintablePanel = new JPanel();
		mintablePanel.setVisible(false);
		resultsPanel.add(mintablePanel, "cell 0 7 2 1,grow");
		
		minDataModel = new MinOrdersTableModel();
		ordersWithMinNumOfProductsTable = new JTable(minDataModel);
		ordersWithMinNumOfProductsTable.setFocusable(false);
		ordersWithMinNumOfProductsTable.setShowVerticalLines(false);
		ordersWithMinNumOfProductsTable.setShowGrid(false);
		ordersWithMinNumOfProductsTable.setRowSelectionAllowed(false);
		
		minscrollPane = new JScrollPane(ordersWithMinNumOfProductsTable);
		minscrollPane.setPreferredSize(new Dimension(550, 100));
		mintablePanel.add(minscrollPane);
		
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
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				backButtonPressed();
			}
		});
		panel.add(backButton, "cell 1 5");
		
		calculateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calcolaButtonPressed();
			}
		});
		
		setBackground(new Color(255, 234, 234));
		setSize(new Dimension(640, 480));
		setLocationRelativeTo(null);				
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setMinimumSize(new Dimension(640, 480));
		
		addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitButtonPressed();
            }
        });
	}
	
	private void backButtonPressed()
	{
		myController.backButtonPressedFromReportToHomePage();
	}
	
	private void calcolaButtonPressed()
	{
		int year = Integer.valueOf(String.valueOf(yearBox.getSelectedItem()));
		int month = Integer.valueOf(monthBox.getSelectedIndex()) + 1;
		myController.calculateButtonPressed(year, month);
	}

	protected void showResults(double averageNumberOfOrders) {
		averageNumberOfOrdersLabel.setVisible(true);
		actualAverageNumberOfOrdersLabel.setText(averageNumberOfOrders + " ");
		
		maxProductsOrderLabel.setVisible(true);
		maxtablePanel.setVisible(true);
		maxDataModel.fireTableDataChanged();

		minProductsOrderLabel.setVisible(true);
		mintablePanel.setVisible(true);
		minDataModel.fireTableDataChanged();
	}
	
	private void exitButtonPressed() {
    	int noSelected = JOptionPane.showConfirmDialog(this, "Sei sicuro di voler uscire dalla tua area di lavoro?", "", JOptionPane.YES_NO_OPTION);
        if (noSelected == 0) myController.exit();
	}
}
