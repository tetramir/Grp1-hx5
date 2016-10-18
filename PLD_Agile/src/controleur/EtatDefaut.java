package controleur;

import vue.Fenetre;
import xml.DeserialiseurXML;
import xml.ExceptionXML;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import modele.Plan;

public abstract class EtatDefaut implements Etat {
	// Definition des comportements par defaut des methodes
	
	public void chargerPlan(Controleur controleur, Plan plan, Fenetre fenetre) {
		try {
			DeserialiseurXML.chargerPlan(plan);
		} catch (ParserConfigurationException 
				| SAXException | IOException 
				| ExceptionXML | NumberFormatException e) {
		}
		// fenetre.afficherMessage(e);
		controleur.setEtatCourant(controleur.EtatPlanCharge);
	}
	
	public void chargerDemandeLivraison(Controleur controleur, Plan plan, Fenetre fenetre){}

	public void calculerTournee(Controleur controleur, Plan plan, Fenetre fenetre){}

	public void quitter(){}

	//public void genererFeuilleDeRoute(Plan plan, Fenetre fenetre){}

	public void clicGauche(Plan plan, Fenetre fenetre){}
}
