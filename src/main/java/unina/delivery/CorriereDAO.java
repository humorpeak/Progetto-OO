package unina.delivery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CorriereDAO {
	private Controller myController;
	
	CorriereDAO(Controller controller)
	{
		myController = controller;
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
			e.printStackTrace();
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
