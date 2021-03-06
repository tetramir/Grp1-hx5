package vue;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;

/**Objet utilisé pour réaliser une selection d'heure avec des comboBoxs
 * 
 * @author florent
 *
 */
public class SelectionHeure extends JPanel {

	private JComboBox heure;
	private JComboBox minute;

	protected SelectionHeure() {
		super();
		heure = new JComboBox();
		minute = new JComboBox();

		for (int i = 0; i < 24; i++) {
			if (i < 10) {
				heure.addItem("0" + i);
			} else {
				heure.addItem(i + "");
			}
		}
		for (int i = 0; i < 12; i++) {
			if (i < 10) {
				minute.addItem("0" + i);
			} else {
				minute.addItem(i + "");
			}
		}
		setLayout(new GridBagLayout());
		GridBagConstraints cstr = new GridBagConstraints();
		cstr.ipadx = 10;
		add(heure);
		cstr.gridx = 1;
		add(minute);
	}

	/**
	 * 
	 * @return l'heure entrée au format hh:mm:00
	 */
	protected String getHeure() {
		return (String) heure.getSelectedItem() + ":" + (String) minute.getSelectedItem() + ":00";
	}

	/**Permet de bloquer l'edition de l'heure
	 * 
	 * @param editable
	 */
	protected void editable(boolean editable) {
		heure.setEnabled(editable);
		minute.setEnabled(editable);
	}

}
