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
	protected ArrayList<Ordine> getOrdiniDaSpedireUnfiltered (int sede) {
		
		ArrayList<Ordine> listaordini = new ArrayList<Ordine>();
		LocalDate data;
		LocalTime orarioinizio;
		LocalTime orariofine;
		String indirizzo;
		
		System.out.println(sede);
		try
		{	
			String query = "SELECT * FROM uninadelivery.get_ordini_da_spedire_by_sede(null, null, null, ?)";
			PreparedStatement ps = controller.getMyConnection().prepareStatement(query);
			ps.setInt(1, sede);
			ResultSet rs = ps.executeQuery();

			PreparedStatement get_peso = controller.getMyConnection().prepareStatement("SELECT * FROM uninadelivery.get_peso_totale(?)");
			int peso;
			
			while (rs.next())
			{
				get_peso.setInt(1, rs.getInt("idordine"));
				ResultSet risultato_peso = get_peso.executeQuery();
				risultato_peso.next();
				peso = risultato_peso.getInt(1);
				Ordine ordine = new Ordine (
								rs.getDate("data").toLocalDate(),
								rs.getTimestamp("orarioinizio").toLocalDateTime().toLocalTime(),
								rs.getTimestamp("orariofine").toLocalDateTime().toLocalTime(),
								rs.getString("emailacquirente"),
								getIndirizzo(rs.getString("cap"), rs.getString("città"), rs.getString("via"), rs.getString("civico"), rs.getString("edificio")),
								peso,
								rs.getInt("idordine"));
				
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
	
	protected double getAverageNumberOfOrders(int year, int month) {
		
		double averageNumberOfOrders = 0;
		Date date = Date.valueOf(LocalDate.of(year,  month, 2));
		
		try
		{
			String call = "{? = call numero_medio_ordini_in_mese_by_sede(?,?)}";		
			CallableStatement cs = controller.getMyConnection().prepareCall(call);
			cs.registerOutParameter(1, Types.DOUBLE);
			cs.setDate(2, date);
			cs.setInt(3, controller.getOperatore().getSede());
			
			cs.execute();
			averageNumberOfOrders = cs.getDouble(1);
			
			System.out.println("average " + averageNumberOfOrders);
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, e, "Errore", JOptionPane.ERROR_MESSAGE);
		}
		
		return averageNumberOfOrders;
	}
	
	protected ArrayList<Ordine> getOrdiniWithMaxNumOfProducts (int year, int month) {
		
		ArrayList<Ordine> listaordini = new ArrayList<Ordine>();
		String acquirente;
		LocalDate data;
		String indirizzo;

		Date date = Date.valueOf(LocalDate.of(year,  month, 2));
		
		try
		{	
			String query = "SELECT * FROM uninadelivery.get_ordini_max_numero_prodotti_in_mese_per_sede(?, ?)";
			PreparedStatement ps = controller.getMyConnection().prepareStatement(query);
			ps.setDate(1, date);
			ps.setInt(2, controller.getOperatore().getSede());
			ResultSet rs = ps.executeQuery();
			
			while (rs.next())
			{
				Ordine ordine = new Ordine (
								rs.getString("emailacquirente"),
								rs.getDate("data").toLocalDate(),
								getIndirizzo(rs.getString("cap"), rs.getString("città"), rs.getString("via"), rs.getString("civico"), rs.getString("edificio")));
				
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
	
	protected ArrayList<Ordine> getOrdiniWithMinNumOfProducts (int year, int month) {
		
		ArrayList<Ordine> listaordini = new ArrayList<Ordine>();
		String acquirente;
		LocalDate data;
		String indirizzo;

		Date date = Date.valueOf(LocalDate.of(year,  month, 2));
		
		try
		{	
			String query = "SELECT * FROM uninadelivery.get_ordini_min_numero_prodotti_in_mese_per_sede(?, ?)";
			PreparedStatement ps = controller.getMyConnection().prepareStatement(query);
			ps.setDate(1, date);
			ps.setInt(2, controller.getOperatore().getSede());
			ResultSet rs = ps.executeQuery();
			
			while (rs.next())
			{
				Ordine ordine = new Ordine (
								rs.getString("emailacquirente"),
								rs.getDate("data").toLocalDate(),
								getIndirizzo(rs.getString("cap"), rs.getString("città"), rs.getString("via"), rs.getString("civico"), rs.getString("edificio")));
				
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

	public void shipOrder(Ordine ordine, int idSpedizione) throws SQLException {
		Statement st = controller.getMyConnection().createStatement();
		st.executeQuery("UPDATE uninadelivery.ORDINE AS O SET idspedizione = " + idSpedizione + " stato = 'Spedito'"
				+ "WHERE O.idordine = " + ordine.getIdOrdine());
	}
}
