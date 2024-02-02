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
	
	
	public long create(Spedizione s) throws SQLException
	{
		Statement st = myController.getMyConnection().createStatement();
		st.executeUpdate("INSERT INTO uninadelivery.SPEDIZIONE (partenza, arrivoStimato, codiceFiscaleCorriere, codiceFiscaleOperatore, targa) VALUES ('"
				+ s.getPartenza() + "','" + s.getArrivoStimato() + "','" + s.getCodiceFiscaleCorriere() + "','" + s.getCodiceFiscaleOperatore() + "','" + s.getTarga() + "')", Statement.RETURN_GENERATED_KEYS);
		ResultSet rs = st.getGeneratedKeys();
		rs.next();
		return rs.getInt(1);
	}
}
