package unina.delivery;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import javax.swing.JLabel;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.Rectangle;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;

public class ReportPage extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private Controller myController;
	private JPanel panel;
	private JLabel yearLabel;
	@SuppressWarnings("rawtypes")
	private JComboBox yearBox;
	private JLabel monthLabel;
	@SuppressWarnings("rawtypes")
	private JComboBox monthBox;
	private JButton calculateButton;
	private JPanel resultsPanel;
	private JLabel actualAverageNumberOfOrdersLabel;
	private JTable maxtable;
	private JTable mintable;
	
//	class OrdersTableModel extends AbstractTableModel{
//		
//		private static final long serialVersionUID = 1L;
//		private String columnNames[] = { "Email", "Data", "Orario Inizio", "Orario Fine"  };
//		
//		@Override
//		public String getColumnName(int index) {
//		    return columnNames[index];
//		}	
//		@Override
//	    public Class getColumnClass(int col) {
//			return String.class;
//	    }
//	}
	
	public ReportPage(Controller controller) {
		myController = controller;
		
		setIconImage(Toolkit.getDefaultToolkit().getImage((LoginPage.class.getResource("/unina/delivery/resources/logo.png"))));
		setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));		
		setTitle("UninaDelivery");
		
		panel = new JPanel();
		setContentPane(panel);
		panel.setMinimumSize(new Dimension(640, 400));
		panel.setForeground(new Color(0, 0, 0));
		panel.setLayout(new MigLayout("", "[][][grow][][][grow][][][]", "[][][][grow][]"));
		
		yearLabel = new JLabel("Anno: ");
		panel.add(yearLabel, "cell 1 1,alignx right");
		
		yearBox = new JComboBox();
		yearBox.setModel(new DefaultComboBoxModel(new String[] {"2024", "2023", "2022", "2021"}));
		yearBox.setToolTipText("Seleziona l'anno di cui desideri visualizzare i report.");
		panel.add(yearBox, "cell 2 1,growx");
		
		monthLabel = new JLabel("Mese: ");
		panel.add(monthLabel, "cell 4 1,alignx right");
		
		monthBox = new JComboBox();
		monthBox.setModel(new DefaultComboBoxModel(new String[] {"Gen", "Feb", "Mar", "Apr", "Mag", "Giu", "Lug", "Ago", "Set", "Ott", "Nov", "Dic"}));
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
		resultsPanel.setLayout(new MigLayout("", "[grow][]", "[][][][][][][grow][]"));
		
		JLabel averageNumberOfOrdersLabel = new JLabel("Numero medio di ordini giornalieri:");
		resultsPanel.add(averageNumberOfOrdersLabel, "cell 0 0");
		
		actualAverageNumberOfOrdersLabel = new JLabel("");
		resultsPanel.add(actualAverageNumberOfOrdersLabel, "cell 1 0,alignx right");
		
		JLabel maxProductsOrderLabel = new JLabel("Ordini con il maggior numero di prodotti");
		resultsPanel.add(maxProductsOrderLabel, "cell 0 3");
		

		TableModel dataModel = new OrdersTableModel(); //TODO fix!!
		
		maxtable = new JTable(dataModel);
		maxtable.setFocusable(false);
		maxtable.setShowVerticalLines(false);
		maxtable.setShowGrid(false);
		maxtable.setRowSelectionAllowed(false);
		resultsPanel.add(maxtable, "cell 0 5,grow");
		
		JLabel minProductsOrderLabel = new JLabel("Ordini con il minor numero di prodotti");
		resultsPanel.add(minProductsOrderLabel, "flowy,cell 0 6");

		mintable = new JTable(dataModel);
		mintable.setFocusable(false);
		mintable.setShowVerticalLines(false);
		mintable.setShowGrid(false);
		mintable.setRowSelectionAllowed(false);
		resultsPanel.add(mintable, "cell 0 7,grow");
		
		calculateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				int year = Integer.valueOf(String.valueOf(yearBox.getSelectedItem()));
				int month = Integer.valueOf(monthBox.getSelectedIndex()) + 1;
				
				System.out.println(year + " / " + month);
				
				myController.calculateButtonPressed(year, month);
				
				//calculateButtonPressed();
			}
		});
		
		setBackground(new Color(255, 234, 234));
		setSize(new Dimension(640, 480));
		setLocationRelativeTo(null);				
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(640, 480));
		
		addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                myController.exit();
            }
        });
	}

	protected void showResults(double averageNumberOfOrders) { //TODO aggiungere altri param
		actualAverageNumberOfOrdersLabel.setText("     " + averageNumberOfOrders);
		
	}
}
