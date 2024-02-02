package unina.delivery;


import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import net.miginfocom.swing.MigLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Toolkit;

public class LoginPage extends JFrame {

	private static final long serialVersionUID = 1L;

	private Controller myController;
	private JPanel panel;
	private JLabel usernameLabel;
	private JTextField usernameField;
	private JLabel passwordLabel;
	private JPasswordField passwordField;
	private JButton loginButton;

	/**
	 * Create the frame.
	 */
	public LoginPage(Controller controller) {
		myController = controller;
		
		setIconImage(Toolkit.getDefaultToolkit().getImage((LoginPage.class.getResource("/unina/delivery/resources/logo.png"))));
		setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));		
		setTitle("UninaDelivery");
		
		panel = new JPanel();
		setContentPane(panel);
		panel.setMinimumSize(new Dimension(640, 400));
		panel.setForeground(new Color(0, 0, 0));
		panel.setLayout(new MigLayout("", "[10px:200px,left][100px:300px,grow,shrink 30][10px:200px,right]", "[10px:200px,top][][][][][20px:n][][10px:200px,bottom]"));
		
		usernameLabel = new JLabel("Username ");
		usernameLabel.setForeground(new Color(0, 0, 0));
		panel.add(usernameLabel, "cell 1 1");
		
		usernameField = new JTextField();
		usernameField.setToolTipText("Inserisci qui il tuo username.");
		panel.add(usernameField, "cell 1 2,growx");
		usernameField.setColumns(10);
		
		passwordField = new JPasswordField();
		passwordField.setToolTipText("Inserisci qui la tua password.");
		panel.add(passwordField, "cell 1 4,growx");
		
		loginButton = new JButton("Login");
		loginButton.setFocusPainted(false);
		loginButton.setBorderPainted(false);
		loginButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				loginButton.setContentAreaFilled(false);
				loginButton.setOpaque(true);
				loginButton.setBackground(new Color(255, 213, 213));
			}
			@Override
			public void mouseExited(MouseEvent e) {
				loginButton.setContentAreaFilled(true);
				loginButton.setBackground(new Color(255, 128, 128));
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				loginButton.setContentAreaFilled(false);
				loginButton.setOpaque(true);
				loginButton.setBackground(new Color(255, 170, 170));
			}
		});
		panel.add(loginButton, "cell 1 6,alignx center");
		
		loginButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				System.out.println("login button pressed");
				String email = usernameField.getText();
				@SuppressWarnings("deprecation")
				String password = passwordField.getText();
				//c'era getPassword e si usava char[], ma i prepared statement hanno solo getString
				if (email.isEmpty()) {
					showWarning("Inserire email utente", "Email mancante");
				}
				else if (password.isEmpty()) {
					showWarning("Inserire password", "Password mancante");
				}
				else {
					myController.loginButtonPressed(email, password);
				}
			}
			
		});
		
		passwordLabel = new JLabel("Password ");
		passwordLabel.setForeground(Color.BLACK);
		panel.add(passwordLabel, "cell 1 3");

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
	
	private void showWarning(String testo, String titolo) {
		JOptionPane.showMessageDialog(this, testo, titolo, JOptionPane.WARNING_MESSAGE);
	}
	
	protected void showError(String testo, String titolo) {
		JOptionPane.showMessageDialog(this, testo, titolo, JOptionPane.ERROR_MESSAGE);
	}
}
