package vue;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.security.auth.DestroyFailedException;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout.Constraints;

import modele.Heure;
import modele.Itineraire;
import modele.Livraison;
import modele.Plan;
import modele.Troncon;

/**Zone de texte où sont décrite les livraisons. Les livraisons présentent les 
 * même interactions que dans une VuePlan.
 * 
 * @author florent
 *
 */
public class ZoneDeTexte extends JPanel implements Observer {

	private static int VALEUR_INTERSECTION_VIDE = -1;
	private Plan plan;
	private Fenetre fenetre;
	private InformationTextuelle texte;
	private ArrayList<InformationTextuelle> listeInformation;
	private GridBagConstraints contraintes;
	private int livraisonSurligne;

	/**Constructeur
	 * 
	 * @param plan plan dont on affichera les livraisons si elles existent, doit etre observable
	 * @param fenetre la fenetre dans laquelle s'inscrit cette zone de texte
	 */
	public ZoneDeTexte(Plan plan, Fenetre fenetre) {
		super();
		this.fenetre = fenetre;
		this.plan = plan;

		plan.addObserver(this);
		listeInformation = new ArrayList<InformationTextuelle>();
		ajouterZoneInformation("", 0);
		texte = listeInformation.get(0);
		setLayout(new GridBagLayout());
		contraintes = new GridBagConstraints();
		contraintes.gridwidth = GridBagConstraints.REMAINDER;
		contraintes.anchor = GridBagConstraints.PAGE_START;
		contraintes.fill = GridBagConstraints.HORIZONTAL;
		contraintes.gridy = 0;
		setLivraisonSurligne(-1);
		afficherInformations();
	}

	/**Methode utilisé pour mettre une livraison en surbrillance
	 * 
	 * @param idLivraison la livraison a surligner dans la zone de texte
	 */
	protected void setLivraisonSurligne(int idLivraison) {
		if (livraisonSurligne != idLivraison && plan.getLivraisonParAdresse(idLivraison) != null) {
			livraisonSurligne = idLivraison;
			for (InformationTextuelle info : listeInformation) {
				if (info instanceof DescriptionLivraison) {
					info = (DescriptionLivraison) info;
					((DescriptionLivraison) info).setSurbrillance(info.getIndex() == idLivraison);
				}
			}
			// update(getGraphics());
		}
	}

	private void ajouterZoneInformation(String information, int index) {
		InformationTextuelle info = new InformationTextuelle(information, index, fenetre);
		listeInformation.add(info);
	}

	private void ajouterDescLivraison(String information, int index, boolean valide) {
		DescriptionLivraison description = new DescriptionLivraison(information, index, fenetre, valide);
		listeInformation.add(description);
	}

	private InformationTextuelle getTitre() {
		return texte;
	}

	/*
	 * Fonction qui génère et affiche les informations d'une demande de
	 * livraison
	 * 
	 */
	private void genererInformationLivraison(List<Livraison> livraisons) {
		if (livraisons != null) {
			ajouterZoneInformation("Feuille de route de la tournée", 0);
			ajouterZoneInformation("Départ de l'entrepôt à l'adresse " + plan.getEntrepot().getId()
					+ " prévu à " + plan.afficherHeureDepart(), plan.getEntrepot().getId());
			for (Livraison livraison : livraisons) {
				contraintes.gridy = listeInformation.size();
				String plage = "";
				if (livraison.possedePlage()) {
					plage = " de " + livraison.getDebutPlage().afficherHoraire() + " a "
							+ livraison.getFinPlage().afficherHoraire();
				}
				if (livraison.getHeureArrivee() != null) {
					plage += "\nHeure d'arrivée : " + livraison.getHeureArrivee().afficherHoraire();
					if (livraison.possedePlage()) {
						if (livraison.getTpsAttente().toSeconds() != 0) {
							plage += "\nTemps d'attente : "
									+ livraison.getTpsAttente().afficherHoraire();
						}
					}
					plage += "\nHeure de départ : " + livraison.getHeureDepart().afficherHoraire();
				}
				ajouterDescLivraison("Livraison à l'adresse " + livraison.getAdresse().getId() + plage,
						livraison.getAdresse().getId(), livraison.getRespectePlage());
			}
			ajouterZoneInformation("Retour à l'entrepôt à l'adresse " + plan.getEntrepot().getId()
					+ " prévu à " + plan.afficherHeureRetour(), plan.getEntrepot().getId());
		} else {
			ajouterZoneInformation("", 0);
		}
	}

	/**Methode qui met à jour l'affichage des informations
	 * Appelé lorsque le plan notify ses observers
	 * 
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
		viderListeInfos();
		genererInformationLivraison(plan.getListeLivraisons());
		afficherInformations();
	}

	private void viderListeInfos() {
		listeInformation.clear();
	}

	private void afficherInformations() {
		this.removeAll();
		int i = 0;
		for (InformationTextuelle info : listeInformation) {
			i++;
			contraintes.gridy = i;
			add(info, contraintes);
		}
		fenetre.invalidate();
		fenetre.validate();
		fenetre.repaint();
	}
}
