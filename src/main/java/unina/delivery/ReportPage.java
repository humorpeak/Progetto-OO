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
import java.time.Year;

public class ReportPage extends JFrame {
	Controller myController;
	JPanel yearPanel;
	Color selectedButtonColor = new Color(255,0,0);
	Color normalButtonColor = new Color(0,255,0);
	public ReportPage(Controller controller) {
		myController = controller;
		setBounds(500, 230, 0, 0);
		setMinimumSize(new Dimension(500,250));
		
		yearPanel = new JPanel();
		getContentPane().add(yearPanel, BorderLayout.NORTH);
		yearPanel.setLayout(new GridLayout(0, 4, 0, 0));
		
		initializeUI();
		
		addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                myController.exit();
            }
        });
	}
	
	void initializeUI() {
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
		for (Integer i = 2020; i <= currentYear; i++)
		{
			JButton button = new JButton();
			button.setText(i.toString());
			button.addActionListener(actionListener);
			yearPanel.add(button);
		}
	}

}
