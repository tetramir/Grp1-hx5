package vue;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;

/**
 * Cette classe réagit à la souris et est utilisé pour décrire et effectuer les actions sur 
 * une livraison
 */
public class DescriptionLivraison extends InformationTextuelle {

	private static Color COUEUR_HOVER = new Color(0xA2A5F1);
	private PopMenuLivraison popupMenuLivraison;
	private PopMenuLivraisonAjout popupMenuLivraisonAjout;
	private boolean valide;

	protected Color background;

	public DescriptionLivraison(String information, int index, Fenetre fenetre, boolean valide) {
		super(information, index, fenetre);
		this.valide = valide;

		background = COULEUR_DEFAUT;
		if (!valide) {
			background = COULEUR_ERREUR;
			zoneInformation.setBackground(background);
		}

		zoneInformation.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent arg0) {

			}

			@Override
			public void mousePressed(MouseEvent arg0) {

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				setFocusDescription(false);
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				setFocusDescription(true);
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (SwingUtilities.isLeftMouseButton(arg0)) {
					fenetre.clicGaucheLivraison(index);
				} else if (SwingUtilities.isRightMouseButton(arg0)) {
					fenetre.clicDroitLivraison(index);
				}
			}
		});
	}

	/**Cette methode permet définir si la case est en surbrillance ou non
	 * 
	 * @param surbrillance variable booleenne qui met la description en surbrillance si vrai, et normal sinon
	 */
	protected void setSurbrillance(boolean surbrillance) {
		if (surbrillance) {

			zoneInformation.setBackground(COUEUR_HOVER);
		} else {
			zoneInformation.setBackground(background);
		}
	}

	private void setFocusDescription(boolean focused) {
		if (focused) {
			fenetre.setLivraisonSurvol(index);
		} else {
			fenetre.setLivraisonSurvol(-1);
		}
	}
}
