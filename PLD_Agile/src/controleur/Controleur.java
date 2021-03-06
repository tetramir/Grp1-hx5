package controleur;

import vue.Fenetre;

import java.awt.Point;

import modele.Plan;

public class Controleur {

	private Plan plan;
	private Fenetre fenetre;
	private EtatDefaut etatCourant;
	private int tempsLimite;
	private ListeDeCdes listeDeCdes;

	// Instances associees a chaque etat possible du controleur
	protected final EtatInitial ETAT_INITIAL = new EtatInitial();
	protected final EtatPlanCharge ETAT_PLAN_CHARGE = new EtatPlanCharge();
	protected final EtatDemandeLivraisonCharge ETAT_DEMANDE_LIVRAISON_CHARGE = new EtatDemandeLivraisonCharge();
	protected final EtatCalculEnCours ETAT_CALCUL_EN_COURS = new EtatCalculEnCours();
	protected final EtatTourneeCalculee ETAT_TOURNEE_CALCULEE = new EtatTourneeCalculee();
	protected final EtatAjoutLivraison ETAT_AJOUT_LIVRAISON = new EtatAjoutLivraison();
	protected final EtatChoisirLivraisonEchange ETAT_ECHANGER_LIVRAISON = new EtatChoisirLivraisonEchange();

	/**
	 * Cree le controleur de l'application
	 * 
	 * @param plan
	 *                le plan
	 */
	public Controleur(Plan plan) {
		this.plan = plan;
		listeDeCdes = new ListeDeCdes();
		etatCourant = ETAT_INITIAL;
		String titre = "Optimod";
		tempsLimite = 10000;
		int longueur = 720;
		int largeur = 1024;
		fenetre = new Fenetre(titre, longueur, largeur, plan, this);
		ETAT_INITIAL.init(this, plan, fenetre);
		ETAT_PLAN_CHARGE.init(this, plan, fenetre);
		ETAT_DEMANDE_LIVRAISON_CHARGE.init(this, plan, fenetre);
		ETAT_CALCUL_EN_COURS.init(this, plan, fenetre);
		ETAT_TOURNEE_CALCULEE.init(this, plan, fenetre);
		ETAT_AJOUT_LIVRAISON.init(this, plan, fenetre);
		ETAT_ECHANGER_LIVRAISON.init(this, plan, fenetre);
	}

	/**
	 * Methode appelee par fenetre lorsque l'utilisateur clique sur
	 * "annuler" dans le mode d'ajout de livraison.
	 */
	public void annulerAjout() {
		etatCourant.annulerAction();
	}

	/**
	 * Methode appelee par fenetre apres un clic sur le bouton "Arreter"
	 * lors du calcul de la tournee
	 */
	public void arreterCalculTournee() {
		etatCourant.arreterCalcul();
	}

	/**
	 * Methode appelee par fenetre apres un clic sur le bouton "Calcul
	 * Tournee"
	 */
	public void calculTournee() {
		etatCourant.calculerTournee(tempsLimite);
	}

	/**
	 * Methode appelee par fenetre apres un clic sur le bouton "Charger
	 * demande de livraison"
	 */
	public void chargerDemandeLivraison() {
		if (!plan.getCalculTourneeEnCours())
			etatCourant.chargerDemandeLivraison();
		else
			fenetre.afficherMessage(
					"Veuillez arrêter le calcul de la tournée avant de recharger le plan ou la demande de livraison.");
	}

	// Methodes correspondant aux evenements utilisateur
	/**
	 * Methode appelee par fenetre apres un clic sur le bouton "Charger
	 * plan"
	 */
	public void chargerPlan() {
		if (!plan.getCalculTourneeEnCours())
			etatCourant.chargerPlan();
		else
			fenetre.afficherMessage(
					"Veuillez arrêter le calcul de la tournée avant de recharger le plan ou la demande de livraison.");
	}

	/**
	 * Methode appelee par fenetre lorsque l'utilisateur clique sur ajouter
	 * une nouvelle livraison sur le plan.
	 * 
	 * @param idIntersection
	 */
	public void clicAjouterLivraison(int idIntersection) {
		etatCourant.clicAjouterLivraison(idIntersection);
	}

	/**
	 *Methode appelee par fenetre apres un clic gauche sur un point de la vue
	 * graphique 
	 * @param idPrec
	 * @param idSuiv
	 * @param duree
	 */
	public void clicAjouterLivraisonPosition(int idPrec, int idSuiv, int duree) {
		etatCourant.clicAjouterLivraisonPosition(idPrec, idSuiv, duree);
	}


	/**
	 * Methode appelee par fenetre apres un clic gauche sur un point de la
	 * vue graphique
	 * @param idPrec
	 * @param idSuiv
	 * @param duree
	 * @param debutPlage
	 * @param finPlage
	 */
	public void clicAjouterLivraisonPosition(int idPrec, int idSuiv, int duree, String debutPlage,
			String finPlage) {
		etatCourant.clicAjouterLivraisonPosition(idPrec, idSuiv, duree, debutPlage, finPlage);
	}

	/**
	 * Methode appelee par fenetre apres un clic droit sur un point du plan une
	 * fois le calcul de la tournee termine.
	 * @param idIntersection
	 */
	public void clicDroitIntersection(int idIntersection) {
		etatCourant.clicDroitIntersection(idIntersection);
	}

	/**
	 * Methode appelee par fenetre apres un clic droit sur un point du plan
	 * une fois le calcul de la tournee termine.
	 * 
	 * @param idLivraison
	 *            l'id de la livraison cliquee par l'utilisateur
	 */
	public void clicDroitLivraison(int idLivraison) {
		etatCourant.clicDroitLivraison(idLivraison);
	}

	/**
	 * Methode appelee lorsque l'utilisateur clique sur le bouton echanger
	 * livraison
	 * @param idLivraison l'id de livraison
	 */
	public void clicEchangerLivraison(int idLivraison) {
		etatCourant.clicEchangerLivraisons(idLivraison);
	}
	
	/**
	 * Methode appelee par fenetre apres un clic gauche sur une livraison du plan une
	 * fois le calcul de la tournee termine.
	 * @param idLivraison
	 */
	public void clicGaucheLivraison(int idLivraison) {
		etatCourant.clicGaucheLivraison(idLivraison);
	}
	
	/**
	 * methode qui genere la feuille de route de la tournee calculee
	 */
	public void genererFeuilleDeRoute() {
		etatCourant.genererFeuilleDeRoute();
	}

	/**
	 * @return l'etat courant du controleur
	 */
	protected EtatDefaut getEtatCourant() {
		return etatCourant;
	}
	
	/**
	 * 
	 * @return la fenetre
	 */
	protected Fenetre getFenetre() {
		return fenetre;
	}
	
	/**
	 * 
	 * @return la liste des commandes
	 */
	protected ListeDeCdes getListeCde() {
		return listeDeCdes;
	}

	/**
	 * 
	 * @return le plan
	 */
	protected Plan getPlan() {
		return plan;
	}


	/**
	 * Methode appelee pour modifier une livraion
	 * @param adrLiv adresse de livraison
	 * @param possedePlage true si la livraison possede une plage horaire
	 * @param heureDebut heure de debut de la plage horaire
	 * @param heureFin heure de fin de la plage horaire
	 */
	public void modifierLivraison(int adrLiv, boolean possedePlage, String heureDebut, String heureFin) {
		etatCourant.modifierLivraison(adrLiv, possedePlage, heureDebut, heureFin);
	}

	/**
	 * Methode appelee par fenetre lorsque l'utilisateur clique sur Echanger
	 * apres avoir selectionner une 1ere livraison.
	 * 
	 * @param idLivraison
	 */
	public void passerEtatEchangerLivraison(int idLivraison) {
		etatCourant.clicEchangerLivraisons(idLivraison);
	}

	/**
	 * Methode appelee par fenetre apres un clic sur le bouton "Quitter"
	 */
	public void quitter() {
		etatCourant.quitter();
	}

	/**
	 * Methode appelee par fenetre apres un clic sur le bouton "Redo"
	 */
	public void redo() {
		etatCourant.redo();
	}

	/**
	 * Change l'etat courant du controleur
	 * 
	 * @param etat
	 *                le nouvel etat courant
	 */
	protected void setEtatCourant(EtatDefaut etat) {
		etatCourant = etat;
	}

	/**
	 * Methode appelee par fenetre lorsque l'utilisateur clique sur
	 * supprimer apres avoir fait un clic droit sur une livraison.
	 * 
	 * @param idLivraison
	 */
	public void supprimerLivraison(int idLivraison) {
		etatCourant.supprimerLivraison(idLivraison);
	}

	/**
	 * Methode appelee par fenetre lorsque l'utilisateur survole le plan une
	 * fois la tournée calculée.
	 * 
	 * @param point
	 * @param tolerance
	 */
	public void survolPlan(Point point, int tolerance) {
		etatCourant.survolPlan(point, tolerance);
	}

	/**
	 * Methode appelee par la fenetre quand l'utilisateur clique sur le
	 * bouton "Undo"
	 */
	public void undo() {
		etatCourant.annulerAction();
		etatCourant.undo();
	}
}
