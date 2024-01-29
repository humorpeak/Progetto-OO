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
import java.time.Year;
import javax.swing.JLabel;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.Rectangle;
import javax.swing.DefaultComboBoxModel;

public class ReportPage extends JFrame {
	private Controller myController;
	private JPanel panel;
	private JLabel yearLabel;
	private JComboBox yearBox;
	private JLabel monthLabel;
	private JComboBox monthBox;
	private JButton calculateButton;
	private JPanel resultsPanel;
	private JLabel actualAverageNumberOfOrdersLabel;
	private Color selectedButtonColor = new Color(255,0,0);
	private Color normalButtonColor = new Color(0,255,0);
	private CallableStatement preparedStatementForAverageNumberOfOrders;
	
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
		
		//TODO far funzionare per mese selezionato
		try {
			preparedStatementForAverageNumberOfOrders = myController.myconnection.prepareCall("{? = call numero_medio_ordini_in_mese(?)}");
			preparedStatementForAverageNumberOfOrders.registerOutParameter(1, Types.DOUBLE);
			preparedStatementForAverageNumberOfOrders.setDate(2,java.sql.Date.valueOf(LocalDate.now()));
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		resultsPanel = new JPanel();
		resultsPanel.setBounds(new Rectangle(200, 200, 200, 200));
		panel.add(resultsPanel, "cell 1 3 7 1,alignx center,aligny center");
		resultsPanel.setLayout(new MigLayout("", "[][]", "[][][]"));
		
		JLabel averageNumberOfOrdersLabel = new JLabel("Numero medio di ordini:");
		resultsPanel.add(averageNumberOfOrdersLabel, "cell 0 0");
		
		actualAverageNumberOfOrdersLabel = new JLabel("");
		resultsPanel.add(actualAverageNumberOfOrdersLabel, "cell 1 0,alignx right");
		
		JLabel lblNewLabel_1 = new JLabel("New label");
		resultsPanel.add(lblNewLabel_1, "cell 0 1");
		
		JLabel lblNewLabel_4 = new JLabel("New label");
		resultsPanel.add(lblNewLabel_4, "cell 1 1,alignx right");
		
		JLabel lblNewLabel_2 = new JLabel("New label");
		resultsPanel.add(lblNewLabel_2, "cell 0 2");
		
		JLabel lblNewLabel_5 = new JLabel("New label");
		resultsPanel.add(lblNewLabel_5, "cell 1 2,alignx right");
		
		calculateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calculateButtonPressed();
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

		//TODO gestire i metodi seguenti secondo i cambiamenti effettuati
		
//		void createYearButtons() {
//			ActionListener actionListener = new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					if (e.getSource().getClass() == JButton.class)
//					{
//						for (Component button : yearPanel.getComponents()) {
//							button.setBackground(normalButtonColor);
//						}
//						JButton pressedButton = (JButton) e.getSource();
//						pressedButton.setBackground(selectedButtonColor);
//					}
//				}
//			};
//			int currentYear = Year.now().getValue();
//			((GridLayout) yearPanel.getLayout()).setColumns(currentYear - 2019);
//			for (Integer i = 2020; i <= currentYear; i++)
//			{
//				JButton button = new JButton();
//				button.setUI(januaryButton.getUI());
//				button.setText(i.toString());
//				button.addActionListener(actionListener);
//				yearPanel.add(button);
//			}
//		}
//		
		private void calculateButtonPressed()
		{
			try {
				preparedStatementForAverageNumberOfOrders.execute();
				double averageNumberOfOrders = preparedStatementForAverageNumberOfOrders.getDouble(1);
				actualAverageNumberOfOrdersLabel.setText("" + averageNumberOfOrders);
				
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
//
//		private void addEventListenersToMonthsButtons()
//		{
//			ActionListener actionListener = new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					if (e.getSource().getClass() == JButton.class)
//					{
//						for (Component button : monthPanel.getComponents()) {
//							if (button.getClass() == JButton.class) {
//								button.setBackground(normalButtonColor);
//							}
//						}
//						JButton pressedButton = (JButton) e.getSource();
//						pressedButton.setBackground(selectedButtonColor);
//					}
//				}
//			};
//			
//			for (Component monthButton : monthPanel.getComponents()) {
//				if (monthButton.getClass() == JButton.class) {
//					((JButton) monthButton).addActionListener(actionListener);
//				}
//			}
//		}
}
