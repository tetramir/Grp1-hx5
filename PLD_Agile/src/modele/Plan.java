package modele;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import tsp.TSPPlages;

public class Plan extends Observable {
    private HashMap<Integer, Intersection> listeIntersections; // Liste des
							       // intersections
							       // du plan
							       // classees selon
							       // leur
							       // identifiant
    private HashMap<Integer, List<Troncon>> listeTroncons; // Liste des troncons
							   // du plan classes
							   // selon
							   // l'identifiant de
							   // leur origine
    private DemandeDeLivraison demandeDeLivraison;
    private Tournee tournee;
    private ArrayList<Integer> idSommets;
    private HashMap<Integer, Sommet> sommets;
    private boolean calculTourneeEnCours;
    private int tpsAttente = 1;

    /**
     * Cree un plan ne possedant aucune intersection et aucun troncon
     */
    public Plan() {
	this.listeIntersections = new HashMap<Integer, Intersection>();
	this.listeTroncons = new HashMap<Integer, List<Troncon>>();
	this.calculTourneeEnCours = false;
    }

    /**
     * @return Renvoie l'Heure de départ de la demande/tournée sous forme de
     *         String
     */
    public String afficherHeureDepart() {
	if (tournee != null)
	    return tournee.getHeureDebut().toString();
	if (demandeDeLivraison != null)
	    return demandeDeLivraison.getHeureDepart().toString();
	return "NC";
    }

    /**
     * @return Renvoie l'Heure de retour de la tournée sous forme de String
     */
    public String afficherHeureRetour() {
	if (tournee != null) {
	    return tournee.getHeureFin().toString();
	}
	return "NC";
    }

    /**
     * Arrête le calcul de la tournée en cours si il existe
     */
    public void arreterCalculTournee() {
	this.calculTourneeEnCours = false;
    }

    /**
     * Calcule la tournee (algo Dijkstra et TSP) si possible et la cree
     * 
     * @param tpsLimite
     *            Temps maximum en millisecondes pour le calcul du parcours
     *            optimal
     * @return true Si une tournee a ete trouvee, false si aucune tournee n'a
     *         ete trouvee
     * @throws ExceptionTournee
     */
    public boolean calculerTournee() throws ExceptionTournee {
	if (this.demandeDeLivraison == null) {
	    throw new ExceptionTournee("Aucune demande de livraison n'a été chargée");
	}
	// On recupere la liste des identifiants des sommets devant constituer
	// le graphe complet analyse par le TSP
	idSommets = completionTableauLivraison();
	// On constitue un graphe complet grace a l'algorithme de Dijkstra
	Object[] resultDijkstra = calculerDijkstra(idSommets);
	// On initialise les variables à fournir au TSP
	TSPPlages tsp = new TSPPlages();
	int[] durees = recupererDurees(idSommets);
	int[][] couts = (int[][]) resultDijkstra[0];

	int[] plageDepart = new int[idSommets.size()];
	int[] plageFin = new int[idSommets.size()];

	plageDepart[0] = 0;
	plageFin[0] = Integer.MAX_VALUE;

	for (int i = 1; i < idSommets.size(); i++) {

	    if (this.getHashMapLivraisonsDemande().get(idSommets.get(i)).possedePlage()) {
		plageDepart[i] = this.getHashMapLivraisonsDemande().get(idSommets.get(i)).getDebutPlage().toSeconds();
		plageFin[i] = this.getHashMapLivraisonsDemande().get(idSommets.get(i)).getFinPlage().toSeconds();
	    } else {
		plageDepart[i] = 0;
		plageFin[i] = Integer.MAX_VALUE;
	    }
	}

	// On verifie que tous les sommets sont atteignables à partir de
	// l'entrepot
	for (int i = 1; i < idSommets.size(); i++) {
	    if (couts[0][i] == Integer.MAX_VALUE) {
		throw new ExceptionTournee("L'intersection d'identifiant " + idSommets.get(i)
			+ "n'est pas atteignable à partir de l'entrepôt");
	    }
	}

	this.calculTourneeEnCours = true;

	// On lance le calcul de la tournee dans un nouveau thread
	Callable<Boolean> calculTournee = () -> {
	    // On cherche l'itineraire optimal via l'utilisation du TSP
	    tsp.chercheSolution(idSommets.size(), couts, durees, plageDepart, plageFin,
		    this.demandeDeLivraison.getHeureDepart().toSeconds());
	    return tsp.getCoutMeilleureSolution() != Integer.MAX_VALUE;
	};

	ExecutorService executorCalculTournee = Executors.newFixedThreadPool(2);

	Future<Boolean> futureCalculTournee = executorCalculTournee.submit(calculTournee);

	// On recupere la meilleure tournee calculee a intervalle de temps
	// regulier dans un autre thread
	tournee = new Tournee(demandeDeLivraison.getHeureDepart());
	Callable<Boolean> recuperationMeilleurResultat = () -> {
	    boolean tourneeTrouvee = false;
	    while (this.calculTourneeEnCours == true) {
		try {
		    TimeUnit.SECONDS.sleep(tpsAttente);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		boolean calculTermine = futureCalculTournee.isDone();
		tsp.lock();
		int dureeTournee = tsp.getCoutMeilleureSolution();
		if (this.tournee.getDuree() != dureeTournee) {
		    tourneeTrouvee = true;
		    int[] ordreTournee = new int[idSommets.size()];
		    for (int i = 0; i < idSommets.size(); i++) {
			ordreTournee[i] = tsp.getMeilleureSolution(i);
		    }
		    tsp.unlock();
		    Itineraire[][] trajets = (Itineraire[][]) resultDijkstra[1];
		    tournee.mettreAJourTournee(dureeTournee, ordreTournee, trajets,
			    this.demandeDeLivraison.getHashMapLivraisons(), idSommets);
		    setChanged();
		    notifyObservers(this.tournee);
		} else {
		    tsp.unlock();
		}
		// Si le calcul est termine, on arrête de chercher une
		// nouvelle tournee
		if (calculTermine) {
		    this.calculTourneeEnCours = false;
		    try {
			tourneeTrouvee = futureCalculTournee.get();
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    } catch (ExecutionException e) {
			e.printStackTrace();
		    }
		}
	    }
	    tsp.setCalculEnCours(false);
	    return tourneeTrouvee;
	};

	Future<Boolean> futureRecuperationMeilleurResultat = executorCalculTournee.submit(recuperationMeilleurResultat);

	executorCalculTournee.shutdown();
	boolean tourneeTrouvee = false;
	// On attend la fin de l'execution des deux thread lances
	try {
	    executorCalculTournee.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
	    tourneeTrouvee = futureRecuperationMeilleurResultat.get();
	    /*
	     * if (this.tournee.getDuree() == Integer.MAX_VALUE &&
	     * tpsLimiteAtteint == false) { throw new ExceptionTournee(
	     * "Aucune tournée n'a été trouvée. " +
	     * "Veuillez recommencer avec de nouvelles plages horaires"); }
	     */
	} catch (InterruptedException | ExecutionException e) {
	    e.printStackTrace();
	}
	return tourneeTrouvee;

    }

    /**
     * Renvoie l'ObjetGraphique positionné au coordonnées du point précisé
     * 
     * @param p
     *            Position de l'ObjetGraphique à rechercher
     * @param tolerance
     *            Intervalle de tolerance de la recherche
     * @return ObjetGraphique aux coordonnées p si il existe, null sinon
     */
    public ObjetGraphique cherche(Point p, int tolerance) {
	ObjetGraphique objGraph = null;
	// On teste le clic sur la liste d'intersections
	for (Intersection inter : listeIntersections.values()) {
	    if (inter.contient(p, tolerance)) {
		objGraph = inter;
		Livraison livAssociee = this.getLivraisonParAdresse(inter.getId());
		if (livAssociee != null)
		    objGraph = livAssociee;
	    }
	}

	return objGraph;
    }

    /**
     * Creation d'un tableau faisant correspondre l'identifiant de chaque
     * intersection avec sa place dans le tableau des couts et des itineraires
     * resultant des calculs de plus court chemin
     * 
     * @return Identifiants des intersections constituant la tournee finale
     */
    private ArrayList<Integer> completionTableauLivraison() {
	ArrayList<Integer> sommets = new ArrayList<>();
	sommets.add(demandeDeLivraison.getEntrepot().getId());
	Set<Integer> cles = this.getHashMapLivraisonsDemande().keySet();
	Iterator<Integer> it = cles.iterator();
	while (it.hasNext()) {
	    Integer cle = it.next();
	    sommets.add(cle);
	}
	return sommets;
    }

    /**
     * Cree et ajoute une demande de livraison au plan
     * 
     * @param heureDepart
     *            Heure de depart de l'entrepot
     * @param entrepot
     *            Identifiant de l'intersection correspondant a l'entrepot
     * @throws ModeleException
     *             Renvoie une exception si la demande ne peut pas être créée
     */
    public void creerDemandeDeLivraison(Heure heureDepart, int entrepot) throws ModeleException {
	if (listeIntersections.get(entrepot) == null)
	    throw new ModeleException("L'adresse de l'entrepôt ne correspond pas à une intersection.");
	this.demandeDeLivraison = new DemandeDeLivraison(heureDepart, this.listeIntersections.get(entrepot));
	if (this.tournee != null) {
	    this.tournee = null;
	}
	setChanged();
	notifyObservers(demandeDeLivraison);
    }

    /**
     * Cree et ajoute une intersection au plan courant
     * 
     * @param id
     *            Identifiant de l'intersection a ajouter
     * @param longitude
     *            Longitude de l'intersection a ajouter
     * @param latitude
     *            Latitude de l'intersection a ajouter
     * @throws ModeleException
     *             Envoie une exception si l'Intersection ne peut pas être
     *             ajoutée
     */
    public void creerIntersection(int id, int longitude, int latitude) throws ModeleException {
	Intersection nouvIntersection = new Intersection(id, longitude, latitude);
	if (longitude < 0 || latitude < 0) {
	    throw new ModeleException("Une intersection possède des coordonnées négatives");
	}
	if (!this.listeIntersections.containsKey(id)) {
	    this.listeIntersections.put(id, nouvIntersection);
	} else {
	    throw new ModeleException("Plusieurs intersections portent l'id " + id + ", seule l'intersection x("
		    + this.listeIntersections.get(id).getLongitude() + ") y("
		    + this.listeIntersections.get(id).getLongitude() + ") a été créée");
	}
	setChanged();
	notifyObservers();
    }

    /**
     * Cree et ajoute une livraison a la demande de livraison associee au Plan
     * 
     * @param adresse
     *            Identifiant de l'intersection correspondant a la livraison a
     *            effectuer
     * @param duree
     *            Duree de la livraison a effectuer
     * @throws ModeleException
     *             Renvoie une exception si la Livraison ne paut pas être créée
     */
    public void creerLivraisonDemande(int adresse, int duree) throws ModeleException {
	if (listeIntersections.get(adresse) == null)
	    throw new ModeleException(
		    "L'adresse " + adresse + " d'une livraison ne correspond pas à une intersection.");
	if (duree < 0)
	    throw new ModeleException("La durée de la livraison à l'adresse " + adresse + " est négative.");
	this.demandeDeLivraison.ajouterLivraison(duree, this.listeIntersections.get(adresse));
	setChanged();
	notifyObservers();
    }

    /**
     * Cree et ajoute une livraison possedant une plage horaire a la demande de
     * livraison associee au Plan
     * 
     * @param adresse
     *            Identifiant de l'intersection correspondant a la livraison a
     *            effectuer
     * @param duree
     *            Duree de la livraison a effectuer
     * @param debutPlage
     *            Debut de la plage horaire de la livraison a effectuer
     * @param finPlage
     *            Fin de la plage horaire de la livraison a effectuer
     * @throws ModeleException
     *             Renvoie une exception si la Livraison ne peut pas être créée
     */
    public void creerLivraisonDemande(int adresse, int duree, String debutPlage, String finPlage)
	    throws ModeleException {
	if (listeIntersections.get(adresse) == null)
	    throw new ModeleException(
		    "L'adresse " + adresse + " d'une livraison ne correspond pas à une intersection.");
	if (duree < 0)
	    throw new ModeleException("La durée de la livraison à l'adresse " + adresse + " est négative.");
	this.demandeDeLivraison.ajouterLivraison(duree, this.listeIntersections.get(adresse), debutPlage, finPlage);
	setChanged();
	notifyObservers();
    }

    /**
     * Cree et ajoute un troncon au Plan courant
     * 
     * @param nom
     *            Nom du troncon a ajouter
     * @param longueur
     *            Longueur (en decimetres) du troncon a ajouter
     * @param vitMoyenne
     *            Vitesse moyenne de circulation (en decimetres/seconde) du
     *            troncon a ajouter
     * @param origine
     *            Origine du troncon a ajouter
     * @param destination
     *            Destination du troncon a ajouter
     * @throws ModeleException
     *             Renvoie une exception si le Troncon ne peut pas être créé
     */
    public void creerTroncon(String nom, int longueur, int vitMoyenne, int origine, int destination)
	    throws ModeleException {
	if (listeIntersections.get(origine) == null || listeIntersections.get(destination) == null)
	    throw new ModeleException(
		    "L'origine ou la destination du tronçon " + nom + " ne correspond pas à une intersection.");
	if (vitMoyenne <= 0)
	    throw new ModeleException("La vitesse moyenne du tronçon " + nom + " est négative ou nulle.");
	if (longueur <= 0)
	    throw new ModeleException("La longueur du tronçon " + nom + " est négative ou nulle.");

	Troncon nouvTroncon = new Troncon(nom, this.listeIntersections.get(origine),
		this.listeIntersections.get(destination), longueur, vitMoyenne);
	// Si un troncon ayant la meme origine que le troncon à ajouter,
	// on les inserent dans la meme liste.
	if (this.listeTroncons.containsKey(origine)) {
	    this.listeTroncons.get(origine).add(nouvTroncon);
	} else {
	    // On cree une nouvelle liste pour le nouvel origine
	    List<Troncon> nouvListeTroncons = new ArrayList<Troncon>();
	    nouvListeTroncons.add(nouvTroncon);
	    this.listeTroncons.put(origine, nouvListeTroncons);
	}
	setChanged();
	notifyObservers();
    }

    /**
     * @return String formatée représentant la feuille de route à suivre pour la
     *         tournée
     */
    public String genererFeuilleRoute() {
	if (tournee != null) {
	    return tournee.genererFeuilleRoute();
	}
	return "";
    }

    /**
     * Retourne l'adresse de la livraison précédant la livraison donnée
     * 
     * @param adrLiv
     *            Adresse de la livraison dont on cherche la livraison
     *            précédente
     * @return Adresse de la livraison précédente ou -1 si l'adresse donnée n'a
     *         pas de livraison ou de livraison suivante
     */
    public int getAdresseLivraisonPrecedente(int adrLiv) {
	if (tournee != null) {
	    return tournee.getAdresseLivraisonPrecedente(adrLiv);
	}
	return -1;
    }

    /**
     * Retourne l'adresse de la livraison suivant la livraison donnée
     * 
     * @param adrLiv
     *            Adresse de la livraison dont on cherche la livraison suivante
     * @return Adresse de la livraison suivante ou -1 si l'adresse donnée n'a
     *         pas de livraison ou de livraison suivante
     */
    public int getAdresseLivraisonSuivante(int adrLiv) {
	if (tournee != null) {
	    return tournee.getAdresseLivraisonSuivante(adrLiv);
	}
	return -1;
    }

    /**
     * @return Retourne le boolean indiquant si un calcul de tournée est en
     *         cours
     */
    public boolean getCalculTourneeEnCours() {
	return this.calculTourneeEnCours;
    }

    /**
     * METHODE DE TEST
     */
    public Integer getDureeTournee() {
	if (tournee != null) {
	    return tournee.getDuree();
	} else {
	    return null;
	}

    }

    /**
     * @return Retourne l'intersection où est situé l'entrepôt
     */
    public Intersection getEntrepot() {
	if (demandeDeLivraison != null) {
	    return demandeDeLivraison.getEntrepot();
	} else {
	    return null;
	}
    }

    /**
     * @return HashMap des livraisons de la demande
     */
    public HashMap<Integer, Livraison> getHashMapLivraisonsDemande() {
	if (demandeDeLivraison != null) {
	    return demandeDeLivraison.getHashMapLivraisons();
	} else {
	    return null;
	}
    }

    /**
     * @param id
     *            Id de l'Intersection à retourner
     * @return Retourne l'Intersection correspondante à l'id donné ou null si
     *         aucune intersection n'existe
     */
    public Intersection getIntersection(int id) {
	return this.listeIntersections.get(id);
    }

    /**
     * @return Renvoie la liste des Itineraires de la Tournee. Si la Tournee est
     *         null, renvoie null
     */
    public List<Itineraire> getItineraires() {
	if (tournee != null) {
	    return tournee.getItineraires();
	} else {
	    return null;
	}
    }

    /**
     * METHODE DE TEST
     * 
     * @return
     */
    public HashMap<Integer, Intersection> getListeIntersections() {
	return this.listeIntersections;
    }

    /**
     * @return La liste de livraisons de la tournée si celle ci existe, sinon la
     *         liste de livraisons de la demande. Si la demande et la tournée
     *         sont null, renvoie null
     */
    public List<Livraison> getListeLivraisons() {
	if (tournee != null) {
	    return tournee.getListeLivraisons();
	} else {
	    if (demandeDeLivraison != null) {
		return new ArrayList<Livraison>(demandeDeLivraison.getHashMapLivraisons().values());
	    } else {
		return null;
	    }
	}
    }

    /**
     * @return Liste des Troncons du Plan
     */
    public List<Troncon> getListeTroncons() {
	List<Troncon> listeNonOrdonneeTroncons = new ArrayList<Troncon>();
	Set<Integer> cles = this.listeTroncons.keySet();
	Iterator<Integer> it = cles.iterator();
	while (it.hasNext()) {
	    Integer cle = it.next();
	    listeNonOrdonneeTroncons.addAll(this.listeTroncons.get(cle));
	}
	return listeNonOrdonneeTroncons;
    }

    /**
     * METHODE DE TEST
     * 
     * @return
     */
    public HashMap<Integer, List<Troncon>> getListeTronconsTriee() {
	return this.listeTroncons;
    }

    /**
     * Renvoie la livraison associée à l'intersection à l'adresse donnée en
     * cherchant en priorité dans la tournée si elle existe ou dans la demande
     * de livraison sinon
     * 
     * @param adresse
     *            Id de l'intersection adresse de la livraison
     * @return La livraison associée
     */
    public Livraison getLivraisonParAdresse(int adresse) {
	if (tournee != null) {
	    return tournee.getLivraison(adresse);
	}
	if (demandeDeLivraison != null) {
	    return demandeDeLivraison.getLivraison(adresse);
	}
	return null;
    }

    /**
     * Retourne le point contenant les coordonnées maximales du Plan
     * 
     * @return Le point de coordonnées maximales contenues dans le Plan
     */
    public Point getPointBasDroite() {
	int maxBas = Integer.MIN_VALUE;
	int maxDroite = Integer.MIN_VALUE;
	for (Intersection i : listeIntersections.values()) {
	    if (i.getLongitude() >= maxDroite) {
		maxDroite = i.getLongitude();
	    }
	    if (i.getLatitude() >= maxBas) {
		maxBas = i.getLatitude();
	    }
	}
	return new Point(maxDroite, maxBas);
    }

    /**
     * METHODE DE TEST
     * 
     * @param idIntersection
     * @return
     */
    public List<Troncon> getTronconsParIntersection(int idIntersection) {
	return this.listeTroncons.get(idIntersection);
    }

    /**
     * Insère la livraison entre les livraisons de la tournée aux adresses
     * données
     * 
     * @param adresse
     *            L'adresse associée à la livraison
     * @param duree
     *            Durée de la livraison
     * @param debutPlage
     *            Heure de début de la plage
     * @param finPlage
     *            Heure de fin de la plage
     * @param adrPrec
     *            Adresse précédente de la livraison à ajouter
     * @param adrSuiv
     *            Adresse suivante de la livraison à ajouter
     */
    public void insererLivraisonTournee(int adresse, int duree, String debutPlage, String finPlage, int adrPrec,
	    int adrSuiv) {
	Intersection interAdresse = this.getIntersection(adresse);
	try {
	    Livraison liv = new Livraison(duree, interAdresse, debutPlage, finPlage);
	    tournee.insererLivraison(liv, adrPrec, adrSuiv, this);
	} catch (Exception e) {
	}
	setChanged();
	notifyObservers();
    }

    /**
     * Modifie la livraison de la tournée à l'adresse donnée avec les plages
     * horaires spécifiées
     * 
     * @param adrLivraison
     *            Adresse de la livraison à modifier
     * @param nvPlage
     *            True si la livraison doit avoir une plage, false sinon
     * @param nvDebut
     *            Nouvelle Heure de début de la plage
     * @param nvFin
     *            Nouvelle Heure de la fin de la plage
     */
    public void modifierPlageLivraison(int adrLivraison, boolean nvPlage, Heure nvDebut, Heure nvFin) {
	if (tournee != null) {
	    tournee.modifierPlageLivraison(adrLivraison, nvPlage, nvDebut, nvFin);
	}
	setChanged();
	notifyObservers();
    }

    @Override
    public void notifyObservers() {
	setChanged();
	super.notifyObservers();
    }

    /**
     * Recupere les durees des livraisons correspondant aux intersections
     * donnees en parametres
     * 
     * @param idSommets
     *            Liste des sommets dont il faut les durees
     * @return Tableau des durees ordonnees selon l'ordre des sommets en entree
     */
    private int[] recupererDurees(List<Integer> idSommets) {
	int[] durees = new int[idSommets.size()];
	durees[0] = 0; // temps a passer a l'entrepot
	for (int i = 1; i < idSommets.size(); i++) {
	    durees[i] = demandeDeLivraison.getLivraison(idSommets.get(i)).getDuree();
	}
	return durees;
    }

    /**
     * Retire la livraison indiquee de la tournee
     * 
     * @param adresse
     *            Identifiant de l'intersection correspondante a la livraison a
     *            retirer
     * @return Livraison retiree de la tournee
     */
    public Livraison retirerLivraisonTournee(int adresse) {
	// On recupere les ids du nouvel itineraire a creer
	Livraison livRetiree = this.tournee.supprimerLivraison(adresse, this);
	setChanged();
	notifyObservers();

	// On renvoie la livraison supprimee
	return livRetiree;
    }

    /**
     * Supprime la Tournee actuellement liée au Plan
     */
    public void supprimerTournee() {
	this.tournee = null;
	setChanged();
	notifyObservers();
    }

    /**
     * Vide le Plan, remet a zero les listes d'intersection, de troncon et
     * nullifie la demande de livraison et la tournee
     */
    public void viderPlan() {
	this.listeIntersections = new HashMap<Integer, Intersection>();
	this.listeTroncons = new HashMap<Integer, List<Troncon>>();
	this.demandeDeLivraison = null;
	this.tournee = null;
	setChanged();
	notifyObservers();
    }
    
    /**
     * Enumeration servant au calcul de plus court chemin selon l'algorithme de
     * Dijkstra
     * 
     * @author utilisateur
     *
     */
    private enum Etat {
	GRIS, NOIR, BLANC
    }

    private class Sommet implements Comparable<Sommet> {

	private int id;
	private int position;
	private int cout;
	private Etat etat;
	private Troncon antecedent;

	/**
	 * Cree un sommet a partir des informations de l'intersection
	 * correspondante
	 * 
	 * @param id
	 *            Id du sommet
	 * @param position
	 *            Position dans les tableaux de cout et d'itineraires
	 *            servant au calcul de la tournee finale
	 * @param cout
	 *            Cout intial du sommet
	 * @param etat
	 *            Etat initial du sommet (Gris, Noir ou Blanc)
	 */
	public Sommet(int id, int position, int cout, Etat etat) {
	    this.id = id;
	    this.position = position;
	    this.cout = cout;
	    this.etat = etat;
	}

	@Override
	public int compareTo(Sommet autre) {
	    // TODO Auto-generated method stub
	    int coutCompare = this.cout - autre.cout;
	    if (coutCompare == 0) {
		coutCompare = this.id - autre.id;
	    }
	    return coutCompare;
	}

	public Troncon getAntecedent() {
	    return this.antecedent;
	}

	public int getCout() {
	    return this.cout;
	}

	public Etat getEtat() {
	    return this.etat;
	}

	public int getId() {
	    return this.id;
	}

	public int getPosition() {
	    return this.position;
	}

	public void setAntecedent(Troncon nouvelAntecedent) {
	    this.antecedent = nouvelAntecedent;
	}

	public void setCout(int nouveauCout) {
	    this.cout = nouveauCout;
	}

	public void setEtat(Etat nouvelEtat) {
	    this.etat = nouvelEtat;
	}

    }

    /**
     * Initialise la liste des sommets selon la liste des intersections
     */
    public void initialiserSommets() {
	sommets = new HashMap<Integer, Sommet>();
	int position = 0;
	// On initialise l'ensemble des sommets a parcourir par l'algorithme,
	// correspondant a la liste des intersections du plan
	for (int id : listeIntersections.keySet()) {
	    Sommet nouveauSommet = new Sommet(id, position, Integer.MAX_VALUE, Etat.BLANC);
	    sommets.put(id, nouveauSommet);
	    position++;
	}
    }

    /**
     * Calcule les plus courts chemins entre les sommets indiques selon
     * l'algorithme de Dijkstra
     * 
     * @param idSommets
     *            Identifiants des intersections depuis lesquels il faut
     *            calculer les plus courts chemins
     * @return Deux tableaux contenant l'ensemble des couts et des itineraires
     *         optimaux resultant des calculs de plus court chemin effectues
     */
    public Object[] calculerDijkstra(ArrayList<Integer> idSommets) {
	int nbrSommets = idSommets.size();
	int[][] couts = new int[nbrSommets][nbrSommets];
	@SuppressWarnings("unchecked")
	Itineraire[][] trajets = new Itineraire[nbrSommets][nbrSommets];
	int position = 0;
	// On calcule les plus courts chemins en utilisant l'algorithme de
	// Dijkstra avec chacune des intersections passees
	// en parametre comme point de depart
	for (Integer i : idSommets) {
	    Object[] resultDijkstra = calculerDijkstra(i, idSommets);
	    int[] cout = (int[]) resultDijkstra[0];
	    Itineraire[] trajetsUnit = (Itineraire[]) resultDijkstra[1];
	    // On complete les tableaux de cout et d'itineraires permettant de
	    // de calculer la tournee
	    couts[position] = cout;
	    trajets[position] = trajetsUnit;
	    position++;
	}
	return new Object[] { couts, trajets };
    }

    /**
     * Calcule le plus court chemin a partir d'un sommet defini selon
     * l'algorithme de Dijkstra
     * 
     * @param sourceId
     *            Identifiant du sommet de depart
     * @param idSommets
     *            Identifiants des intersections vers lesquelles il faut trouver
     *            les plus courts chemins
     * @return Deux tableaux contenant l'ensemble des couts et des itineraires
     *         optimaux resultant des calculs de plus court chemin partant d'un
     *         unique sommet
     */
    public Object[] calculerDijkstra(int sourceId, ArrayList<Integer> idSommets) {
	// On initialise les etats, antecedents et couts des sommets
	initialiserSommets();

	int coutsSommets[] = new int[idSommets.size()];
	Itineraire[] tableauPiTrie= new Itineraire[idSommets.size()];
	if(!this.listeIntersections.containsKey((Integer) sourceId)) {
		return new Object[] { coutsSommets, tableauPiTrie };
	}
	NavigableSet<Sommet> sommetsGris = new TreeSet<>();

	// On initialise la liste des sommets gris en y mettant le sommet source
	Sommet som = this.sommets.get(sourceId);
	som.etat = Etat.GRIS;
	som.cout = 0;
	sommetsGris.add(som);
	while (!sommetsGris.isEmpty()) {
	    Sommet premierSommet = sommetsGris.first();
	    if (this.listeTroncons.get(premierSommet.getId()) != null) {
		for (Troncon t : this.listeTroncons.get(premierSommet.getId())) {
		    Sommet destination = sommets.get(t.getDestination().getId());
		    Etat etat = destination.getEtat();
		    if (etat != Etat.NOIR) {
			// On enleve du tas binaire le sommet pret a etre
			// relache
			// avant sa modification afin de ne pas dissocier
			// l'objet recupere
			// et celui contenu dans la liste des sommets gris
			sommetsGris.remove(destination);
			relacher(premierSommet, destination, t);
			if (etat == Etat.BLANC) {
			    destination.setEtat(Etat.GRIS);
			}
			sommetsGris.add(destination);
		    }
		}
	    }
	    sommetsGris.remove(premierSommet);
	    premierSommet.setEtat(Etat.NOIR);
	}

	// On recupere seulement les couts des sommets devant etre
	// presents dans la tournee
	int position = 0;
	for (int id : idSommets) {
	    coutsSommets[position] = sommets.get(id).getCout();
	    position++;
	}
 
	// On transforme la liste des sommets avec leur antecedent en un tableau
	// d'itineraires
	tableauPiTrie = convertirTableauItineraires(idSommets, sommets, sourceId);
	return new Object[] { coutsSommets, tableauPiTrie };
    }

    /**
     * Relachement de l'arc (origine, destination)
     * 
     * @param origine
     *            Sommet d'origine de l'arc
     * @param destination
     *            Sommet de destination de l'arc
     * @param antecedent
     *            Arc relache
     */
    private void relacher(Sommet origine, Sommet destination, Troncon antecedent) {
	int nouveauCout = origine.getCout() + antecedent.getTpsParcours();
	if (destination.getCout() > nouveauCout) {
	    destination.setCout(nouveauCout);
	    destination.setAntecedent(antecedent);
	}
    }

    /**
     * Mise en place de la liste des itineraires correspondant aux plus courts
     * chemins calcules selon l'algorithme de Dijkstra a partir d'un unique
     * sommet defini
     * 
     * @param idSommets
     *            Identifiants des intersections constituant la tournee finale
     * @param listeSommets
     *            Liste des sommets parcourus par l'algorithme de Dijkstra
     * @param sourceId
     *            Identifiant du sommet de depart
     * @return Tableau d'itineraires correpondant aux plus courts chemins entre
     *         le sommet de depart et les sommets constituant la tournee finale
     */
    private Itineraire[] convertirTableauItineraires(ArrayList<Integer> idSommets,
	    HashMap<Integer, Sommet> listeSommets, int sourceId) {
	@SuppressWarnings("unchecked")
	int nbrSommets = idSommets.size();
	Itineraire[] trajetsUnit = new Itineraire[nbrSommets];
	int position = 0;
	// On cree les itineraires correspondant aux plus courts chemins
	// calcules a partir d'un unique sommet de depart, et possedant pour
	// arrivee un sommet composant la tournee finale
	int idSommetCourant;
	for (Integer id : idSommets) {
	    List<Troncon> trajet = new ArrayList<Troncon>();
	    Troncon antecedent = listeSommets.get(id).antecedent;
	    idSommetCourant = id;
	    while (antecedent != null) {
		trajet.add(0, antecedent);
		idSommetCourant = antecedent.getOrigine().getId();
		antecedent = listeSommets.get(antecedent.getOrigine().getId()).getAntecedent();
	    }
	    // Si il n'y a pas de chemin entre le sommet de depart et le sommet
	    // courant,
	    // on considere que l'itineraire correspondant est vide
	    if (idSommetCourant != sourceId) {
		trajet.clear();
	    }
	    Itineraire iti = new Itineraire(this.listeIntersections.get(sourceId), this.listeIntersections.get(id),
		    trajet);
	    trajetsUnit[position] = iti;
	    position++;
	}
	return trajetsUnit;
    }
}
