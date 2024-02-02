package unina.delivery;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import net.miginfocom.swing.MigLayout;

public class HomePage extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private Controller myController;
	private JPanel panel;
	private JButton shipmentButton;
	private JButton reportButton;
	
	HomePage(Controller controller) {
		
		myController = controller;
		
		setIconImage(Toolkit.getDefaultToolkit().getImage((HomePage.class.getResource("/unina/delivery/resources/logo.png"))));
		setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));		
		setTitle("UninaDelivery");
		
		panel = new JPanel();
		setContentPane(panel);
		panel.setMinimumSize(new Dimension(640, 400));
		panel.setForeground(new Color(0, 0, 0));
		panel.setLayout(new MigLayout("", "[10px:200px,left][100px:300px,grow,shrink 30][10px:200px,right]", "[10px:200px,top][][20px][][10px:200px,bottom]"));
		
		shipmentButton = new JButton("Genera spedizione");
		shipmentButton.setToolTipText("Clicca qui per generare una nuova spedizione.");
		shipmentButton.setFocusable(false);
		shipmentButton.setFocusPainted(false);
		shipmentButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				shipmentButton.setContentAreaFilled(false);
				shipmentButton.setOpaque(true);
				shipmentButton.setBackground(new Color(255, 213, 213));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				shipmentButton.setContentAreaFilled(true);
				shipmentButton.setBackground(new Color(255, 128, 128));
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				shipmentButton.setContentAreaFilled(false);
				shipmentButton.setOpaque(true);
				shipmentButton.setBackground(new Color(255, 170, 170));
			}
		});
		panel.add(shipmentButton, "cell 1 1,growx");
		
		shipmentButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				shipmentButtonPressed();
			}
			
		});
		
		reportButton = new JButton("Mostra report");
		reportButton.setToolTipText("Clicca qui per visualizzare i report statistici mensili.");
		reportButton.setFocusable(false);
		reportButton.setFocusPainted(false);
		reportButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				reportButton.setContentAreaFilled(false);
				reportButton.setOpaque(true);
				reportButton.setBackground(new Color(255, 213, 213));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				reportButton.setContentAreaFilled(true);
				reportButton.setBackground(new Color(255, 128, 128));
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				reportButton.setContentAreaFilled(false);
				reportButton.setOpaque(true);
				reportButton.setBackground(new Color(255, 170, 170));
			}
		});
		panel.add(reportButton, "cell 1 3,growx");
		
		reportButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				reportButtonPressed();
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
	
	protected void showInformation(String testo, String titolo) {
		JOptionPane.showMessageDialog(this, testo, titolo, JOptionPane.INFORMATION_MESSAGE, null);
	}
	
	private void reportButtonPressed()
	{
		myController.reportButtonPressed();
	}
	
	private void shipmentButtonPressed()
	{
		myController.shipmentButtonPressed();
	}
}
