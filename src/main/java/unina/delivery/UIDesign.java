package unina.delivery;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

public class UIDesign {
	
	private static final Color PRIMARY_BACKGROUND_COLOR = new Color(255, 244, 244);
	private static final Color SECONDARY_BACKGROUND_COLOR = new Color(255, 213, 213);
	private static final Color BUTTON_COLOR = new Color(255, 149, 149);
	private static final Font FONT = new Font("Segoe UI Semibold", Font.PLAIN, 12);
		
	public void setup() {
		UIManager.put("ToolTip.background", SECONDARY_BACKGROUND_COLOR);
		UIManager.put("ToolTip.border", false);
		
		UIManager.put("OptionPane.background", PRIMARY_BACKGROUND_COLOR);
		UIManager.put("OptionPane.messageFont", FONT);
		UIManager.put("OptionPane.buttonFont", FONT);
		UIManager.put("OptionPane.border", null);
		UIManager.put("OptionPane.warningIcon", new ImageIcon(getClass().getResource("/unina/delivery/resources/warning.png")));
		UIManager.put("OptionPane.errorIcon", new ImageIcon(getClass().getResource("/unina/delivery/resources/error.png")));

		UIManager.put("Panel.background", PRIMARY_BACKGROUND_COLOR);
		
		UIManager.put("Button.font", FONT);
		UIManager.put("Button.background", BUTTON_COLOR);
		UIManager.put("Button.borderPainted", false);
	}
}
