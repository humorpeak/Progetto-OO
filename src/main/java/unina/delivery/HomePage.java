package unina.delivery;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.GridLayout;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class HomePage extends JFrame {
	private Controller mycontroller;
	
	public HomePage(Controller controller) {
		mycontroller = controller;
		getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		setBounds(500, 230, 0, 0);
		setMinimumSize(new Dimension(500,250));
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(100, 0, 0, 0));
		panel.setBackground(new Color(255, 163, 72));
		getContentPane().add(panel);
		panel.setLayout(new GridLayout(2, 3, 0, 40));
		
		JButton newSpedizioneButton = new JButton("Crea Spedizione");
		newSpedizioneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mycontroller.newSpedizioneButtonPressed();
			}
		});
		panel.add(newSpedizioneButton);
		
		JButton reportButton = new JButton("Report");
		reportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Report button pressed");
			}
		});
		
		panel.add(reportButton);
	}

}
