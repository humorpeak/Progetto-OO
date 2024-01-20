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
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.BorderLayout;

public class HomePage extends JFrame {
	
	private Controller mycontroller;
	private JPanel contentPane;
	private JPanel panel;
	private JButton shipmentButton;
	private JButton reportButton;
	
	
	public HomePage(Controller controller) {
		
		mycontroller = controller;
		
		setBounds(500, 230, 0, 0);
		setMinimumSize(new Dimension(500,250));
		contentPane = new JPanel();
		contentPane.setBorder(null);
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWeights = new double[]{1.0};
//TODO cancellare commenti
//		gbl_panel.columnWidths = new int[] {300};
//		gbl_panel.rowHeights = new int[] {0, 0, 0, 100};
//		gbl_panel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
//		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		shipmentButton = new JButton("Genera spedizione");
		GridBagConstraints gbc_shipmentButton = new GridBagConstraints();
		gbc_shipmentButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_shipmentButton.insets = new Insets(0, 20, 10, 20);
		gbc_shipmentButton.gridx = 0;
		gbc_shipmentButton.gridy = 0;
		shipmentButton.setToolTipText("Clicca qui per generare una nuova spedizione.");
		panel.add(shipmentButton, gbc_shipmentButton);
		
		reportButton = new JButton("Mostra report");
		GridBagConstraints gbc_reportButton = new GridBagConstraints();
		gbc_reportButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_reportButton.insets = new Insets(0, 20, 10, 20);
		gbc_reportButton.gridx = 0;
		gbc_reportButton.gridy = 2;
		reportButton.setToolTipText("Clicca qui per visualizzare i report statistici mensili.");
		panel.add(reportButton, gbc_reportButton);
	}
}
