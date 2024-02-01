package unina.delivery;

public class MezzoDiTrasporto {
	private String targa;
	private String tipoMezzo;
	private String patenteRichiesta;
	private float capienza;
	
	MezzoDiTrasporto(String targa, String tipoMezzo, String patenteRichiesta, float capienza)
	{
		this.targa = targa;
		this.tipoMezzo = tipoMezzo;
		this.patenteRichiesta = patenteRichiesta;
		this.capienza = capienza;
	}
	
	public String getTipoMezzo() {
		return tipoMezzo;
	}
	public String getPatenteRichiesta() {
		return patenteRichiesta;
	}
	public String getTarga() {
		return targa;
	}
	public float getCapienza() {
		return capienza;
	}
}
