package unina.delivery;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import javax.swing.JOptionPane;
import java.time.*;

public class OrdineDAO {
	
	Controller controller;
	
	public ArrayList<Ordine> getOrdiniDaSpedireUnfiltered (int sede) {
		

		ArrayList<Ordine> listaordini = new ArrayList<Ordine>();
		LocalDate data;
		LocalTime orarioinizio;
		LocalTime orariofine;
		String indirizzo;
		
		System.out.println(sede);
		try
		{	
			String query = "SELECT uninadelivery.get_ordini_da_spedire_by_sede(null, null, null, ?)";
			//TODO funzione get peso!
			PreparedStatement ps = controller.myconnection.prepareStatement(query);
			ps.setInt(1, sede);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next())
			{	
			//TODO FIXARE NECESSARIAMENTE get_ordini_da_spedire_by_sede
				Ordine ordine = new Ordine (
								rs.getDate("data").toLocalDate(),
								rs.getTimestamp("orarioinizio").toLocalDateTime(),
								rs.getTimestamp("orariofine").toLocalDateTime(),
								rs.getString("emailacquirente"),
								"da fare");
				
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
	
	//TODO get indirizzo
	
	
	OrdineDAO(Controller c){
		controller = c;
	}
}
