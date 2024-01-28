package unina.delivery;

import java.awt.Dimension;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.Year;
import javax.swing.JLabel;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.SwingConstants;

public class ReportPage extends JFrame {
	private Controller myController;
	private JPanel yearPanel;
	private JPanel monthPanel;
	private JPanel resultsPanel;
	private JLabel averageNumberOfOrdersLabel;
	private Color selectedButtonColor = new Color(255,0,0);
	private Color normalButtonColor = new Color(0,255,0);
	private CallableStatement preparedStatementForAverageNumberOfOrders;
	private JButton januaryButton;
	private JButton februaryButton;
	private JButton marchButton;
	private JButton aprilButton;
	private JButton mayButton;
	private JButton juneButton;
	private JButton julyButton;
	private JButton augustButton;
	private JButton septemberButton;
	private JButton octoberButton;
	private JButton novemberButton;
	private JButton decemberButton;
	
	public ReportPage(Controller controller) {
		myController = controller;
		setBounds(500, 230, 0, 0);
		setMinimumSize(new Dimension(1200,550));
		
		JPanel monthPickerPanel = new JPanel();
		getContentPane().add(monthPickerPanel, BorderLayout.NORTH);
		monthPickerPanel.setLayout(new BorderLayout(0, 0));
		
		yearPanel = new JPanel();
		monthPickerPanel.add(yearPanel, BorderLayout.NORTH);
		yearPanel.setLayout(new GridLayout(0, 4, 0, 0));
		
		
		monthPanel = new JPanel();
		monthPickerPanel.add(monthPanel, BorderLayout.SOUTH);
		monthPanel.setLayout(new GridLayout(0, 12, 0, 0));
		
		januaryButton = new JButton("Gen");
		januaryButton.setBackground(new Color(153, 193, 241));
		januaryButton.setRequestFocusEnabled(false);
		januaryButton.setFocusPainted(false);
		januaryButton.setFocusable(false);
		monthPanel.add(januaryButton);
		
		februaryButton = new JButton("Feb");
		monthPanel.add(februaryButton);
		
		marchButton = new JButton("Mar");
		monthPanel.add(marchButton);
		
		aprilButton = new JButton("Apr");
		monthPanel.add(aprilButton);
		
		mayButton = new JButton("Mag");
		monthPanel.add(mayButton);
		
		juneButton = new JButton("Giu");
		monthPanel.add(juneButton);
		
		julyButton = new JButton("Lug");
		monthPanel.add(julyButton);
		
		augustButton = new JButton("Ago");
		monthPanel.add(augustButton);
		
		septemberButton = new JButton("Set");
		monthPanel.add(septemberButton);
		
		octoberButton = new JButton("Ott");
		monthPanel.add(octoberButton);
		
		novemberButton = new JButton("Nov");
		monthPanel.add(novemberButton);
		
		decemberButton = new JButton("Dic");
		monthPanel.add(decemberButton);
		
		try {
			preparedStatementForAverageNumberOfOrders = myController.myconnection.prepareCall("{? = call numero_medio_ordini_in_mese(?)}");
			preparedStatementForAverageNumberOfOrders.registerOutParameter(1, Types.DOUBLE);
			preparedStatementForAverageNumberOfOrders.setDate(2,java.sql.Date.valueOf(LocalDate.now()));
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		resultsPanel = new JPanel();
		getContentPane().add(resultsPanel, BorderLayout.CENTER);
		resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
		
		JButton calculateButton = new JButton("Calcola");
		resultsPanel.add(calculateButton);
		
		averageNumberOfOrdersLabel = new JLabel("Numero medio di ordini: ");
		averageNumberOfOrdersLabel.setHorizontalAlignment(SwingConstants.CENTER);
		resultsPanel.add(averageNumberOfOrdersLabel);
		calculateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calculateButtonPressed();
			}
		});
		
		createYearButtons();
		addEventListenersToMonthsButtons();
		
		addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                myController.exit();
            }
        });
	}
	
	void createYearButtons() {
		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().getClass() == JButton.class)
				{
					for (Component button : yearPanel.getComponents()) {
						button.setBackground(normalButtonColor);
					}
					JButton pressedButton = (JButton) e.getSource();
					pressedButton.setBackground(selectedButtonColor);
				}
			}
		};
		int currentYear = Year.now().getValue();
		((GridLayout) yearPanel.getLayout()).setColumns(currentYear - 2019);
		for (Integer i = 2020; i <= currentYear; i++)
		{
			JButton button = new JButton();
			button.setUI(januaryButton.getUI());
			button.setText(i.toString());
			button.addActionListener(actionListener);
			yearPanel.add(button);
		}
	}
	
	private void calculateButtonPressed()
	{
		try {
			preparedStatementForAverageNumberOfOrders.execute();
			double averageNumberOfOrders = preparedStatementForAverageNumberOfOrders.getDouble(1);
			averageNumberOfOrdersLabel.setText("Numero medio di ordini: " + averageNumberOfOrders);
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
	}

	private void addEventListenersToMonthsButtons()
	{
		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource().getClass() == JButton.class)
				{
					for (Component button : monthPanel.getComponents()) {
						if (button.getClass() == JButton.class) {
							button.setBackground(normalButtonColor);
						}
					}
					JButton pressedButton = (JButton) e.getSource();
					pressedButton.setBackground(selectedButtonColor);
				}
			}
		};
		
		for (Component monthButton : monthPanel.getComponents()) {
			if (monthButton.getClass() == JButton.class) {
				((JButton) monthButton).addActionListener(actionListener);
			}
		}
	}
}
