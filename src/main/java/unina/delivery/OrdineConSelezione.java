package unina.delivery;

public class OrdineConSelezione {
	public Ordine ordine;
	public boolean selected;
	public OrdineConSelezione(Ordine o)
	{
		selected = false;
		ordine = o;
	}
	
	public void toggle() {
		selected = !selected;
	}
}
