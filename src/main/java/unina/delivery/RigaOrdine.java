package unina.delivery;

public class RigaOrdine {
	public Ordine ordine;
	public boolean selected;
	public RigaOrdine(Ordine o)
	{
		selected = false;
		ordine = o;
	}
	
	public void toggle() {
		selected = !selected;
	}
}
