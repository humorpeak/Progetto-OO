package unina.delivery;

public class MezzoDiTrasporto {
	private String targa;
	private String tipoMezzo;
	private String patenteRichiesta;
	private float capienza;
	private int numeroCorrieriDisponibili;
	
	MezzoDiTrasporto(String targa, String tipoMezzo, String patenteRichiesta, float capienza)
	{
		this.targa = targa;
		this.tipoMezzo = tipoMezzo;
		this.patenteRichiesta = patenteRichiesta;
		this.capienza = capienza;
		this.setNumeroCorrieriDisponibili(0);
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

	public int getNumeroCorrieriDisponibili() {
		return numeroCorrieriDisponibili;
	}

	public void setNumeroCorrieriDisponibili(int numeroCorrieriDisponibili) {
		this.numeroCorrieriDisponibili = numeroCorrieriDisponibili;
	}
}
