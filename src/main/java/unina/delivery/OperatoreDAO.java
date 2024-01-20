package unina.delivery;

import java.sql.*;

import javax.swing.JOptionPane;

public class OperatoreDAO {
	
	Controller controller;
	
	public boolean isOperatoreValid(Operatore operatore) {
		
		try
		{
			String query = "SELECT * FROM operatore WHERE email = ? AND password = ?";
			PreparedStatement ps = controller.myconnection.prepareStatement(query);
			ps.setString(1, operatore.getEmail());
			ps.setString(2, operatore.getPassword());
			
			ResultSet rs = ps.executeQuery();
			int numOperatoriCorrispondenti = 0;
			while (rs.next())
			{
				numOperatoriCorrispondenti++;
			}
			
			if (numOperatoriCorrispondenti == 1)
			{
				return true; //TODO salvare la sede
			}
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, e, "Errore", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	
	OperatoreDAO(Controller c){
		controller = c;
	}
}
