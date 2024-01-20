package unina.delivery;

import java.sql.*;

import javax.swing.JOptionPane;

public class OperatoreDAO {
	
	Controller controller;
	
	public boolean isOperatoreValid(String email, String password) {
		
		try
		{
			String query = "SELECT * FROM operatore WHERE email = ? AND password = ?";
			PreparedStatement ps = controller.myconnection.prepareStatement(query);
			ps.setString(1, email);
			ps.setString(2, password);
			
			ResultSet rs = ps.executeQuery();
			int numOperatoriCorrispondenti = 0;
			while (rs.next())
			{
				numOperatoriCorrispondenti++;
			}
			
			if (numOperatoriCorrispondenti == 1)
			{
				return true;
			}
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, e, "Errore", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	
	public int getSede(String email, String password) {
		
		int numerosede = 0;
		
		try
		{
			String query = "SELECT * FROM operatore WHERE email = ? AND password = ?";
			PreparedStatement ps = controller.myconnection.prepareStatement(query);
			ps.setString(1, email);
			ps.setString(2, password);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				numerosede = rs.getInt("idsede");
			}
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, e, "Errore", JOptionPane.ERROR_MESSAGE);
		}
		
		return numerosede;
	}
	
	OperatoreDAO(Controller c){
		controller = c;
	}
}
