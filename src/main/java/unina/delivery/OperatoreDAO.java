package unina.delivery;

import java.sql.*;

import javax.swing.JOptionPane;

public class OperatoreDAO {
	
	private Controller controller;
	
	/**
	 * Checks whether the table operatore contains a row with given email and password
	 * @param email
	 * @param password
	 * @return
	 */
	protected boolean isOperatoreValid(String email, String password) {
		
		try
		{
			String query = "SELECT * FROM operatore WHERE email = ? AND password = ?";
			PreparedStatement ps = controller.getMyConnection().prepareStatement(query);
			ps.setString(1, email);
			ps.setString(2, password);
			
			ResultSet rs = ps.executeQuery();
			boolean hasResults = rs.isBeforeFirst();
			return hasResults;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e, "Errore", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	
	
	/**
	 * @param email
	 * @param password
	 * @return value of "sede" for the operatore with given credentials, 0 if the Operatore is not found
	 */
	protected int getSede(String email, String password) {
		
		int numerosede = 0;
		
		try
		{
			String query = "SELECT * FROM operatore WHERE email = ? AND password = ?";
			PreparedStatement ps = controller.getMyConnection().prepareStatement(query);
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
	
	protected String getCodiceFiscale (String email, String password)
	{
		String cf = null;
		try
		{
			String query = "SELECT * FROM operatore WHERE email = ? AND password = ?";
			PreparedStatement ps = controller.getMyConnection().prepareStatement(query);
			ps.setString(1, email);
			ps.setString(2, password);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				cf = rs.getString("codiceFiscale");
			}
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, e, "Errore", JOptionPane.ERROR_MESSAGE);
		}
		
		return cf;
	}
	
	OperatoreDAO(Controller c){
		controller = c;
	}
}
