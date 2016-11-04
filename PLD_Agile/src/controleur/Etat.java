package controleur;

import vue.Fenetre;
import java.awt.Point;
import modele.Plan;

public interface Etat {
	
	/**
	 * Methode appelee par controleur apres un clic sur le bouton "Charger demande livraison"
	 * @param controleur 
	 * @param fenetre
	 */
	public void chargerDemandeLivraison(Controleur controleur, Plan plan, Fenetre fenetre);
	
	/**
	 * Methode appelee par controleur apres un clic sur le bouton "Calculer Tournee"
	 * @param controleur 
	 * @param plan
	 * @param fenetre
	 */
	public void calculerTournee(Controleur controleur, Plan plan, Fenetre fenetre, int tempsLimite);
	
	/**
	 * Methode appelee par controleur apres un clic sur le bouton "Quitter"
	 */
	public void quitter();
	
	
	/**
	 * Methode appelee par controleur apres un clic sur le bouton "Generer feuille de route"
	 * @param plan
	 * @param fenetre
	 */
	//public void genererFeuilleDeRoute(Plan plan, Fenetre fenetre);
	
	/**
	 * Methode appelee par controleur apres un clic sur le bouton "Charger un plan"
	 * @param plan
	 * @param fenetre
	 */
	public void chargerPlan(Controleur controleur, Plan plan, Fenetre fenetre);
	
	
	/**
	 * Methode appelee par controleur apres un clic gauche sur un point de la vue graphique
	 * @param fenetre
	 * @param plan
	 * @param p
	 */
	/*
	public void clicGauche( Plan plan, Fenetre fenetre, Point p);
	*/
	
	
	/**
	 * Methode appelee par controleur apres un clic sur le bouton "Undo"
	 * @param listeDeCdes
	 */
	public void undo(ListeDeCdes listeDeCdes);
	
	/**
	 * Methode appelee par controleur apres un clic sur le bouton "Redo"
	 * @param listeDeCdes
	 */
	public void redo(ListeDeCdes listeDeCdes);
	
	
	/**
	 * Methode appelee par controleur lorsque l'utilisateur clique sur le bouton 
	 * d'arret du calcul de tournee
	 * @param controleur 
	 * @param plan
	 * @param fenetre
	 */
	public void arreterCalcul(Controleur controleur, Plan plan, Fenetre fenetre);
	
	/**
	 * Methode appelee par controleur lorsque l'utilisateur fait un clique droit
	 * sur un point du plan lorsque la tournee est deja calculee.
	 * @param plan
	 * @param fenetre
	 * @param point
	 */
	public void clicDroitPlan(Plan plan, Fenetre fenetre, ListeDeCdes listeDeCdes, Point point);
}
