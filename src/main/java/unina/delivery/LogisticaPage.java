package unina.delivery;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;

import javax.swing.JFrame;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JTable;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.awt.event.ActionEvent;

public class LogisticaPage extends JFrame {
	
	private Controller myController;
	private JPanel panel;
	private JTable vehiclesTable;
	private JTable shippersTable;
	private DatePicker datePicker;
	private TimePicker initialTimePicker;
	private JLabel initialTimePickerLabel;
	private JLabel finalTimePickerLabel;
	private TimePicker finalTimePicker;
	private JButton applyButton;
	private JScrollPane vehiclesScrollPane;
	private JScrollPane shippersScrollPane;
	
	public LogisticaPage(Controller controller) {
		myController = controller;
		
		setIconImage(Toolkit.getDefaultToolkit().getImage((LoginPage.class.getResource("/unina/delivery/resources/logo.png"))));
		setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));		
		setTitle("UninaDelivery");
		
		panel = new JPanel();
		setContentPane(panel);
		panel.setMinimumSize(new Dimension(640, 400));
		panel.setForeground(new Color(0, 0, 0));
		panel.setLayout(new MigLayout("", "[5px:10px,left][10px:200px,grow][5px:n][10px:200px,grow][5px:10px,right]", "[::60px,grow,top][][][grow][][][10px:n,bottom]"));
		
		JPanel filtersPanel = new JPanel();
		panel.add(filtersPanel, "cell 0 0 5 1,alignx center,aligny center");
		filtersPanel.setLayout(new MigLayout("", "[][][][][][][50px][]", "[][]"));
		
		JLabel dateLabel = new JLabel("Data programmata:");
		filtersPanel.add(dateLabel, "cell 0 0,alignx right,aligny center");
		
		datePicker = new DatePicker();
		datePicker.setPreferredSize(new Dimension(160, 19));
		filtersPanel.add(datePicker, "cell 1 0 2 1,alignx left,aligny center");
		
		applyButton = new JButton("Applica");
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applicaButtonPressed();
			}
		});
		filtersPanel.add(applyButton, "cell 7 0 1 2,alignx right,aligny center");
		
		initialTimePickerLabel = new JLabel("Orario di partenza:");
		filtersPanel.add(initialTimePickerLabel, "cell 0 1,alignx right,aligny center");
		
		initialTimePicker = new TimePicker();
		filtersPanel.add(initialTimePicker, "cell 1 1,alignx left,aligny top");
		
		finalTimePickerLabel = new JLabel("Orario di ritorno:");
		filtersPanel.add(finalTimePickerLabel, "cell 2 1,alignx right,aligny center");
		
		finalTimePicker = new TimePicker();
		filtersPanel.add(finalTimePicker, "cell 3 1,alignx left,aligny top");
		
		JLabel vehiclesLabel = new JLabel("Mezzi di trasporto disponibili");
		panel.add(vehiclesLabel, "cell 1 2");
		
		JLabel shippersLabel = new JLabel("Corrieri disponibili");
		panel.add(shippersLabel, "cell 3 2");
		
		vehiclesScrollPane = new JScrollPane();
		panel.add(vehiclesScrollPane, "cell 1 3,grow");
		
		TableModel vehiclesDataModel = new MezziDiTrasportoTableModel(myController);
		vehiclesTable = new JTable(vehiclesDataModel);
		vehiclesScrollPane.setViewportView(vehiclesTable);
		
		shippersScrollPane = new JScrollPane();
		panel.add(shippersScrollPane, "cell 3 3,grow");
		
		shippersTable = new JTable();
		shippersScrollPane.setViewportView(shippersTable);
		
		JButton backButton = new JButton("Indietro");
		panel.add(backButton, "cell 1 5,alignx left,aligny top");
		
		JButton saveButton = new JButton("Salva");
		panel.add(saveButton, "cell 3 5,alignx right,aligny top");
		
		
		//setBackground(new Color(255, 234, 234));
		setSize(new Dimension(640, 480));
		setLocationRelativeTo(null);				
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(640, 480));
	}
	

	private void applicaButtonPressed() {
		LocalDate date = datePicker.getDate();
		LocalTime initTime = initialTimePicker.getTime();
		LocalTime finalTime = finalTimePicker.getTime();
		myController.applicaButtonPressedLogisticaPage(date, initTime, finalTime);
		vehiclesTable.validate();
		vehiclesTable.repaint();
		/*if (date != null && initTime != null && finalTime != null)
		{
			
		}
		else
		{
			JOptionPane.showMessageDialog(this,
					"Si prega di validare data e orario di inizio e fine prima di applicare i filtri.",
					"Campi non validi",
					JOptionPane.WARNING_MESSAGE, null);
		}*/
	}
	
	class MezziDiTrasportoTableModel extends AbstractTableModel{
		private static final long serialVersionUID = 1L;
		private String columnNames[] = { "Tipo", "Capienza", "Corrieri disponibili"};
		private Controller myController;
		
		MezziDiTrasportoTableModel(Controller controller)
		{
			myController = controller;
		}
		
		@Override
		public String getColumnName(int index) {
		    return columnNames[index];
		}
		
		@Override
	      public Class<?> getColumnClass(int col) {
	        return String.class;
	    }
		
	    @Override
	      public boolean isCellEditable(int row, int col) {
	        return false;
	      }
		
	    @Override
	    public int getColumnCount() { return columnNames.length; }
	    
	    @Override
	    public int getRowCount() {return myController.countMezziDiTrasportoWithCorrieri();}
	    
	    @Override
	    public Object getValueAt(int row, int col) { 
	    	MezzoDiTrasporto riga = myController.getMezziDiTrasportoDisponibiliConCorriere().get(row);
	    	switch(col)
	    	{
	    	case 0:
	    		return riga.getTipoMezzo();
	    	case 1:
	    		return riga.getCapienza();
	    	case 2:
	    		return myController.getNumberOfCorrieriDisponibili(datePicker.getDate(), initialTimePicker.getTime(),
	    				finalTimePicker.getTime(), riga.getTarga());
	    	default:
	    		return "error";
	    	}
	    }
	}
}
