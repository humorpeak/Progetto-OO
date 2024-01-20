package unina.delivery;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import javax.swing.JOptionPane;
import java.time.*;

public class OrdineDAO {
	
	Controller controller;
	
	/**
	 * Calls a SQL function to get ready to ship orders of sede and arranges them in an ArrayList
	 * @param sede
	 * @return ArrayList of orders, empty if there are no orders to ship
	 */
	public ArrayList<Ordine> getOrdiniDaSpedireUnfiltered (int sede) {
		

		ArrayList<Ordine> listaordini = new ArrayList<Ordine>();
		LocalDate data;
		LocalTime orarioinizio;
		LocalTime orariofine;
		String indirizzo;
		
		System.out.println(sede);
		try
		{	
			String query = "SELECT * FROM uninadelivery.get_ordini_da_spedire_by_sede(null, null, null, ?)";
			//TODO funzione get peso!
			PreparedStatement ps = controller.myconnection.prepareStatement(query);
			ps.setInt(1, sede);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next())
			{	
				Ordine ordine = new Ordine (
								rs.getDate("data").toLocalDate(),
								rs.getTimestamp("orarioinizio").toLocalDateTime().toLocalTime(),
								rs.getTimestamp("orariofine").toLocalDateTime().toLocalTime(),
								rs.getString("emailacquirente"),
								getIndirizzo(rs.getString("cap"), rs.getString("città"), rs.getString("via"), rs.getString("civico"), rs.getString("edificio"))
								);
				
				System.out.println(ordine.toString());
				listaordini.add(ordine);
			}
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, e, "Errore", JOptionPane.ERROR_MESSAGE);
		}
		return listaordini;
	}
	

	private String getIndirizzo(String cap, String città, String via, String civico, String edificio) {
		
		String indirizzo;
		//TODO testare
		if (edificio == null) {
			indirizzo = cap + " " + città + ", " + via + ", n° " + civico;
		}
		else {
			indirizzo = cap + " " + città + ", " + via + ", " + civico + ", Edificio " + edificio;
		}
		
		return indirizzo;
	}
	
	OrdineDAO(Controller c){
		controller = c;
	}
}
