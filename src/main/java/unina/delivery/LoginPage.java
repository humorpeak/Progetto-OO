package unina.delivery;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

public class LoginPage extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel panel;
	private JTextField usernameField;
	private JLabel usernameLabel;
	private JPasswordField passwordField;
	private JLabel passwordLabel;
	private JButton loginButton;

	private Controller myController;

	/**
	 * Create the frame.
	 */
	public LoginPage(Controller controller) {
		myController = controller;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(500, 230, 0, 0);
		setMinimumSize(new Dimension(500,250));
		contentPane = new JPanel();
		contentPane.setBorder(null);

		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] {213, 0};
		gbl_contentPane.rowHeights = new int[]{114, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		panel = new JPanel();
		panel.setBorder(null);
		//TODO sistemare il layout, i campi sono troppo decentrati
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		contentPane.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{193, 193, 0};
		gbl_panel.rowHeights = new int[] {25, 25, 45, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		usernameLabel = new JLabel("Username: ");
		usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_usernameLabel = new GridBagConstraints();
		gbc_usernameLabel.anchor = GridBagConstraints.EAST;
		gbc_usernameLabel.fill = GridBagConstraints.VERTICAL;
		gbc_usernameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_usernameLabel.gridx = 0;
		gbc_usernameLabel.gridy = 0;
		panel.add(usernameLabel, gbc_usernameLabel);
		
		usernameField = new JTextField();
		usernameField.setHorizontalAlignment(SwingConstants.LEFT);
		usernameField.setColumns(10);
		GridBagConstraints gbc_usernameField = new GridBagConstraints();
		gbc_usernameField.anchor = GridBagConstraints.WEST;
		gbc_usernameField.insets = new Insets(0, 0, 5, 0);
		gbc_usernameField.gridx = 1;
		gbc_usernameField.gridy = 0;
		panel.add(usernameField, gbc_usernameField);
		
		passwordLabel = new JLabel("Password: ");
		passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_passwordLabel = new GridBagConstraints();
		gbc_passwordLabel.anchor = GridBagConstraints.EAST;
		gbc_passwordLabel.fill = GridBagConstraints.VERTICAL;
		gbc_passwordLabel.insets = new Insets(0, 0, 5, 5);
		gbc_passwordLabel.gridx = 0;
		gbc_passwordLabel.gridy = 1;
		panel.add(passwordLabel, gbc_passwordLabel);
		
		passwordField = new JPasswordField();
		passwordField.setColumns(10);
		passwordField.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_passwordField = new GridBagConstraints();
		gbc_passwordField.anchor = GridBagConstraints.WEST;
		gbc_passwordField.insets = new Insets(0, 0, 5, 0);
		gbc_passwordField.gridx = 1;
		gbc_passwordField.gridy = 1;
		panel.add(passwordField, gbc_passwordField);
		
		loginButton = new JButton("Log in");
		loginButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				System.out.println("login button pressed");
				String email = usernameField.getText();
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
		
		GridBagConstraints gbc_loginButton = new GridBagConstraints();
		gbc_loginButton.anchor = GridBagConstraints.SOUTH;
		gbc_loginButton.gridwidth = 2;
		gbc_loginButton.insets = new Insets(0, 0, 0, 5);
		gbc_loginButton.gridx = 0;
		gbc_loginButton.gridy = 2;
		panel.add(loginButton, gbc_loginButton);
	}
	
	private void showInformation(String testo, String titolo) {
		JOptionPane.showMessageDialog(this, testo, titolo, JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void showErrore(String testo, String titolo) {
		JOptionPane.showMessageDialog(this, testo, titolo, JOptionPane.ERROR_MESSAGE);
	}
}
