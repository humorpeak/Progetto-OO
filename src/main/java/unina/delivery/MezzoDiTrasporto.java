package unina.delivery;

public class MezzoDiTrasporto {
	private String targa;
	private String tipoMezzo;
	private String patenteRichiesta;
	private float capienza;
	private int numeroCorrieriDisponibili;
	
	protected String getTipoMezzo() {
		return tipoMezzo;
	}
	protected String getPatenteRichiesta() {
		return patenteRichiesta;
	}
	protected String getTarga() {
		return targa;
	}
	protected float getCapienza() {
		return capienza;
	}

	protected int getNumeroCorrieriDisponibili() {
		return numeroCorrieriDisponibili;
	}

	protected void setNumeroCorrieriDisponibili(int numeroCorrieriDisponibili) {
		this.numeroCorrieriDisponibili = numeroCorrieriDisponibili;
	}
	
	MezzoDiTrasporto(String targa, String tipoMezzo, String patenteRichiesta, float capienza)
	{
		this.targa = targa;
		this.tipoMezzo = tipoMezzo;
		this.patenteRichiesta = patenteRichiesta;
		this.capienza = capienza;
		this.setNumeroCorrieriDisponibili(0);
	}
}
