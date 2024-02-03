package unina.delivery;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class MezzoDiTrasportoDAO {
	private Controller myController;
	
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
		float pesoOrdini = myController.calculateWeightForSelectedOrders();
		String queryMezziDisponibili = "SELECT * FROM uninadelivery.get_mezzi_di_trasporto_disponibili_con_sede(?, ?, ?) AS M WHERE M.capienza >= "+pesoOrdini;
		List<MezzoDiTrasporto> result = new ArrayList<>();
		try {
			PreparedStatement ps = myController.getMyConnection().prepareStatement(queryMezziDisponibili);
			ps.setTimestamp(1, begin);
			ps.setTimestamp(2, end);
			ps.setInt(3, sede);
			ResultSet rs = ps.executeQuery();

			while (rs.next())
			{
				String targa = rs.getString("targa");
				if (new CorriereDAO(myController).getCorrieriDisponibili(date, inizio, fine, targa).isEmpty()) continue;
				String tipoMezzo = rs.getString("tipoMezzo");
				String patenteRichiesta = rs.getString("patenteRichiesta");
				float capienza = rs.getFloat("capienza");
				MezzoDiTrasporto mezzo = new MezzoDiTrasporto(targa, tipoMezzo, patenteRichiesta, capienza);
				mezzo.setNumeroCorrieriDisponibili(myController.getNumberOfAvailableShippers(date, inizio, fine, targa));
				result.add(mezzo);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
