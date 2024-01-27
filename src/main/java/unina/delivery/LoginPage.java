package unina.delivery;


import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;
import java.awt.Font;
import javax.swing.border.LineBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Toolkit;

public class LoginPage extends JFrame {

	private static final long serialVersionUID = 1L;

	private Controller myController;
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
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(LoginPage.class.getResource("/unina/delivery/logo.png")));
		setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));		
		setTitle("UninaDelivery");
		getContentPane().setMinimumSize(new Dimension(640, 400));
		getContentPane().setForeground(new Color(0, 0, 0));
		getContentPane().setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
		getContentPane().setBackground(new Color(255, 244, 244));
		getContentPane().setLayout(new MigLayout("", "[10px:200px,left][100px:300px,grow,shrink 30][10px:200px,right]", "[10px:200px,top][][][][][20px:n][][10px:200px,bottom]"));
		
		usernameLabel = new JLabel("Username ");
		usernameLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
		usernameLabel.setForeground(new Color(0, 0, 0));
		getContentPane().add(usernameLabel, "cell 1 1");
		
		usernameField = new JTextField();
		usernameField.setSelectionColor(new Color(255, 213, 213));
		usernameField.setToolTipText("Inserisci qui il tuo username.");
		usernameField.setBorder(new LineBorder(new Color(255, 170, 170), 2));
		usernameField.setBackground(new Color(255, 255, 255));
		getContentPane().add(usernameField, "cell 1 2,growx");
		usernameField.setColumns(10);
		
		passwordField = new JPasswordField();
		passwordField.setSelectionColor(new Color(255, 213, 213));
		passwordField.setBorder(new LineBorder(new Color(255, 170, 170), 2));
		getContentPane().add(passwordField, "cell 1 4,growx");
		
		loginButton = new JButton("Login");
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
		loginButton.setFocusPainted(false);
		loginButton.setBorderPainted(false);
		loginButton.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
		loginButton.setBackground(new Color(255, 149, 149));
		getContentPane().add(loginButton, "cell 1 6,alignx center");
		
		loginButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				System.out.println("login button pressed");
				String email = usernameField.getText();
				@SuppressWarnings("deprecation")
				String password = passwordField.getText();
				//c'era getPassword e si usava char[], ma i prepared statement hanno solo getString
				if (email.isEmpty()) {
					showInformation("Inserire email utente", "Email mancante");
				}
				else if (password.isEmpty()) {
					showInformation("Inserire password", "Password mancante");
				}
				else {
					myController.loginButtonPressed(email, password);
				}
			}
			
		});
		
		passwordLabel = new JLabel("Password ");
		passwordLabel.setForeground(Color.BLACK);
		passwordLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
		getContentPane().add(passwordLabel, "cell 1 3");

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
	
	private void showInformation(String testo, String titolo) {
		JOptionPane.showMessageDialog(this, testo, titolo, JOptionPane.INFORMATION_MESSAGE);
	}
	
	protected void showError(String testo, String titolo) {
		JOptionPane.showMessageDialog(this, testo, titolo, JOptionPane.ERROR_MESSAGE);
	}
}
