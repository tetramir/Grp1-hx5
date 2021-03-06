package modele;

/**
 * Classe permettant la gestion des heures utilisees pour le calcul de tournee.
 *
 */
public class Heure {

    private int heure;
    private int minutes;
    private int secondes;

    /**
     * Constructeur par defaut. Cree une heure 00:00:00
     */
    public Heure() {
	this.heure = 0;
	this.minutes = 0;
	this.secondes = 0;
    }

    /**
     * Constructeur de copie
     * 
     * @param h
     *            Heure a copier
     */
    public Heure(Heure h) {
	this.heure = h.heure;
	this.minutes = h.minutes;
	this.secondes = h.secondes;
    }

    /**
     * Cree une heure a partir d'un temps en secondes Precondition : Heure
     * comprise entre 00:00:00 et 23:59:59
     * 
     * @param secondes
     */
    public Heure(int secondes) {
	int reste = secondes;
	this.secondes = reste % 60;
	reste -= this.secondes;
	reste = reste / 60;
	this.minutes = reste % 60;
	reste -= this.minutes;
	reste = reste / 60;
	reste = reste % 24;
	this.heure = reste;
    }

    /**
     * Cree une heure a partir d'une heure formatee
     * 
     * @param heureFormatee
     *            Heure au format "hh:mm:ss"
     */
    public Heure(String heureFormatee) throws IndexOutOfBoundsException {
	String[] splits = heureFormatee.split(":");
	this.heure = Integer.parseInt(splits[0]);
	this.minutes = Integer.parseInt(splits[1]);
	this.secondes = Integer.parseInt(splits[2]);
    }

    /**
     * Permet l'affichage de l'heure courante sous la forme "hh:mm"
     * 
     * @return String formatée
     */
    public String afficherHoraire() {
	int min = this.minutes;
	int heure = this.heure;
	if (this.secondes >= 30) {
	    min = min++;
	    if (min == 60) {
		min = 0;
		heure++;
	    }
	}
	return (heure >= 10 ? "" : "0") + heure + ":" + (this.minutes >= 10 ? "" : "0") + this.minutes;
    }

    /**
     * Ajoute une heure a l'heure actuelle
     * 
     * @param h
     *            Une heure a ajouter
     * @return L'heure modifiee
     */
    public Heure ajouterHeure(Heure h) {
	this.secondes += h.secondes;
	if (secondes >= 60) {
	    this.secondes -= 60;
	    this.minutes++;
	}
	this.minutes += h.minutes;
	if (minutes >= 60) {
	    this.minutes -= 60;
	    this.heure++;
	}
	this.heure += h.heure;
	this.heure = this.heure % 24;
	return this;
    }

    /**
     * Ajoute des secondes a l'heure actuelle
     * 
     * @param secondes
     *            Nombre de secondes a ajouter
     * @return L'heure modifiee
     */
    public Heure ajouterSecondes(int secondes) {
	this.ajouterHeure(new Heure(secondes));
	return this;
    }

    /**
     * Compare deux heures. Renvoit true si elles sont egales, et false dans
     * l'autre cas.
     */
    @Override
    public boolean equals(Object obj) {
	if (obj instanceof Heure) {
	    Heure objH = (Heure) obj;
	    return this.heure == objH.heure && this.minutes == objH.minutes && this.secondes == objH.secondes;
	}
	return false;
    }

    /**
     * Affiche une version en secondes de l'heure (00:00:00 donne 0)
     * 
     * @return L'heure en secondes depuis minuit
     */
    public int toSeconds() {
	return (((this.heure * 60) + this.minutes) * 60 + this.secondes);
    }

    /**
     * Affiche l'heure courante sous la forme "hh:mm:ss"
     */
    public String toString() {
	return (this.heure >= 10 ? "" : "0") + this.heure + ":" + (this.minutes >= 10 ? "" : "0") + this.minutes + ":"
		+ (this.secondes >= 10 ? "" : "0") + this.secondes;
    }
}
