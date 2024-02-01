package unina.delivery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class MezzoDiTrasportoDAO {
	Controller myController;
	
	MezzoDiTrasportoDAO(Controller controller)
	{
		myController = controller;
	}
	
	protected List<MezzoDiTrasporto> getMezziDiTrasportoDisponibili(LocalDate date, LocalTime inizio, LocalTime fine, int sede)
	{
		Timestamp begin = null,end = null;
		if (date != null)
		{
			if (inizio == null) inizio = LocalTime.of(0, 0);
			if (fine == null) fine = LocalTime.of(23, 59, 59);
			begin = Timestamp.valueOf(LocalDateTime.of(date, inizio));
			end = Timestamp.valueOf(LocalDateTime.of(date, fine));
		}
		System.out.println(begin);
		System.out.println(end);
		String queryMezziDisponibili = "SELECT * FROM uninadelivery.get_mezzi_di_trasporto_disponibili_con_sede(?, ?, ?)";
		List<MezzoDiTrasporto> result = new ArrayList<>();
		try {
			PreparedStatement ps = myController.getMyConnection().prepareStatement(queryMezziDisponibili);
			ps.setTimestamp(1, begin);
			ps.setTimestamp(2, end);
			ps.setInt(3, sede);
			System.out.println(sede);
			ResultSet rs = ps.executeQuery();

			while (rs.next())
			{
				String targa = rs.getString("targa");
				if (getCorrieriDisponibili(date, inizio, fine, targa).isEmpty())
				{
					continue;
				}
				String tipoMezzo = rs.getString("tipoMezzo");
				String patenteRichiesta = rs.getString("patenteRichiesta");
				float capienza = rs.getFloat("capienza");
				MezzoDiTrasporto mezzo = new MezzoDiTrasporto(targa, tipoMezzo, patenteRichiesta, capienza);
				mezzo.setNumeroCorrieriDisponibili(myController.getNumberOfCorrieriDisponibili(date, inizio, fine, targa));
				result.add(mezzo);
				System.out.println(mezzo);
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getStackTrace());
		}
		return result;
	}
	
	protected List<Corriere> getCorrieriDisponibili(LocalDate date, LocalTime inizio, LocalTime fine, String targa)
	{
		Timestamp begin = null,end = null;
		if (date != null)
		{
			if (inizio == null) inizio = LocalTime.of(0, 0);
			if (fine == null) fine = LocalTime.of(23, 59, 59);
			begin = Timestamp.valueOf(LocalDateTime.of(date, inizio));
			end = Timestamp.valueOf(LocalDateTime.of(date, fine));
		}
		String query = "SELECT * FROM uninadelivery.get_corrieri_disponibili_con_mezzo_di_trasporto(?, ?, ?)";
		List<Corriere> result = new ArrayList<>();
		try {
			PreparedStatement ps = myController.getMyConnection().prepareStatement(query);
			ps.setTimestamp(1, begin);
			ps.setTimestamp(2, end);
			ps.setString(3, targa);
			ResultSet rs = ps.executeQuery();

			while (rs.next())
			{
				String CF = rs.getString("codiceFiscale");
				String nome = rs.getString("nome");
				String cognome = rs.getString("cognome");
				String tipoPatente = rs.getString("tipoPatente");
				
				Corriere corriere = new Corriere(CF, nome, cognome, tipoPatente);
				result.add(corriere);
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getStackTrace());
		}
		return result;
	}
	
	protected int getNumeroDiCorrieriDisponibili(LocalDate data, LocalTime inizio, LocalTime fine, String targa)
	{
		List<Corriere> disponibili = getCorrieriDisponibili(data, inizio, fine, targa);
		if (disponibili == null) return 0;
		return disponibili.size();
	}
}
