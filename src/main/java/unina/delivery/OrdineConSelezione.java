package unina.delivery;

public class OrdineConSelezione {
	public Ordine ordine;
	public boolean selected;
	OrdineConSelezione(Ordine o)
	{
		selected = false;
		ordine = o;
	}
	
	protected void toggle() {
		selected = !selected;
	}
}
