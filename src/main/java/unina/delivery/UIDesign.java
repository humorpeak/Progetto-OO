package unina.delivery;

import java.awt.Color;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class UIDesign {
	
	private static final Color PRIMARY_BACKGROUND_COLOR = new Color(255, 244, 244);
	private static final Color SECONDARY_BACKGROUND_COLOR = new Color(255, 213, 213);
	private static final Color PRESSED_COLOR = new Color(255, 213, 213);
	private static final Color BUTTON_COLOR = new Color(255, 149, 149);
	private static final Color BORDER_COLOR = new Color(255, 170, 170);
	private static final Font FONT = new Font("Segoe UI Semibold", Font.PLAIN, 12);
	private static final EmptyBorder BUTTON_BORDER = new EmptyBorder(5,16,5,16);
	
	protected void setup() {
		UIManager.put("ToolTip.background", SECONDARY_BACKGROUND_COLOR);
		UIManager.put("ToolTip.border", false);
		
		UIManager.put("OptionPane.background", PRIMARY_BACKGROUND_COLOR);
		UIManager.put("OptionPane.messageFont", FONT);
		UIManager.put("OptionPane.warningIcon", new ImageIcon(getClass().getResource("/unina/delivery/resources/warning.png")));
		UIManager.put("OptionPane.errorIcon", new ImageIcon(getClass().getResource("/unina/delivery/resources/error.png")));
		
		UIManager.put("Panel.background", PRIMARY_BACKGROUND_COLOR);
		
		UIManager.put("Button.font", FONT);
		UIManager.put("Button.background", BUTTON_COLOR);
		UIManager.put("Button.border", BUTTON_BORDER);
		UIManager.put("Button.focus", BUTTON_COLOR);
		UIManager.put("Button.select", PRESSED_COLOR);
		
		UIManager.put("Label.font", FONT);
		
		UIManager.put("TextField.border", new LineBorder(BORDER_COLOR, 2));
		UIManager.put("TextField.selectionBackground", SECONDARY_BACKGROUND_COLOR);

		UIManager.put("PasswordField.border", new LineBorder(BORDER_COLOR, 2));
		UIManager.put("PasswordField.selectionBackground", SECONDARY_BACKGROUND_COLOR);
		
		UIManager.put("ComboBox.background", Color.white);
		UIManager.put("ComboBox.font", FONT);
		UIManager.put("ComboBox.selectionBackground", SECONDARY_BACKGROUND_COLOR);
		
		UIManager.put("Table.selectionBackground", SECONDARY_BACKGROUND_COLOR);
	
		UIManager.put("ScrollBar.border", Color.white);
		UIManager.put("ScrollBar.background", Color.white);
		
		System.out.println(UIManager.getDefaults());
	}
}
