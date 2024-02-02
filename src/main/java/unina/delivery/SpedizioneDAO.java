package unina.delivery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class SpedizioneDAO {
	Controller myController;
	
	SpedizioneDAO(Controller controller)
	{
		myController = controller;
	}
	
	
	public long create(Timestamp partenza, Timestamp arrivoStimato, String targa, String codiceFiscaleCorriere, String codiceFiscaleOperatore) throws SQLException
	{
		Statement st = myController.getMyConnection().createStatement();
		st.executeUpdate("INSERT INTO uninadelivery.SPEDIZIONE (partenza, arrivoStimato, codiceFiscaleCorriere, codiceFiscaleOperatore, targa) VALUES ('"
				+ partenza + "','" + arrivoStimato + "','" + codiceFiscaleCorriere + "','" + codiceFiscaleOperatore + "','" + targa + "')", Statement.RETURN_GENERATED_KEYS);
		ResultSet rs = st.getGeneratedKeys();
		rs.next();
		return rs.getInt(1);
	}
}
