package main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.FileManager;

import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentText;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentTitle;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Keyword;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Keywords;
/**
 * Classe permettant de gerer les proprietes d'une page Web
 * @author utilisateur
 *
 */
public class Page {
	
	private final String url;
	private final int classement; //Classement selon la recherche google custom search
	private String titre;
	private String texteExtrait;
	private List<String> motscles;
	private Cluster cluster;
	private Model model; //Modele RDF de la page
	private boolean sportPage; // Indique si la page parle de sport
	
	/**
	 * Construit une page a partir de son url et de son classement
	 * @param url URL de la page
	 * @param classement Classement de la page
	 */
	public Page(String url, int classement){
		this.url = url;
		this.classement = classement;
	}
	

	/**
	 * Trouve les mots cles et le titre de la page.
	 */
	public void alchemyAPIKeywordPOO() {
		AlchemyLanguage service = new AlchemyLanguage();
	    service.setApiKey("a340a4d57f3fe543684011b1f7c6c1f6ea070336");
	    
	    Map<String,Object> params = new HashMap<String, Object>();
	    String url = this.getUrl();
	    List<String> listKeywords = new ArrayList<String>();
	    
		try {
		    URL urlAlchemy = new URL(url);
		    params.put(AlchemyLanguage.URL, urlAlchemy);
		} catch (MalformedURLException e) {
		    System.out.println("Url marlformed");
		}
		
		
		DocumentTitle texteExtraitTitle = null;
		try {
		    texteExtraitTitle = service.getTitle(params).execute();
		} catch (RuntimeException e) {
		    System.out.println("Title not retrieved");
		}
		
		String texteExtraitTitre = "Not retrieved";
		if(texteExtraitTitle!=null){
		texteExtraitTitre = texteExtraitTitle.getTitle().toString();
		}
		this.setTitre(texteExtraitTitre);
		
		List<Keyword> texteExtraitKeywordList = null ;
		try {
		Keywords texteExtraitKeyWord = service.getKeywords(params).execute();
		texteExtraitKeywordList = texteExtraitKeyWord.getKeywords();
		} catch (Exception e) {
		    System.out.println("Keywords not retrieved");
		} finally {

		}


		String texteExtrait = "";
		
		if(texteExtraitKeywordList!=null){
		    // On fait un string de la concatenation de tous les mot cles
		    for(Keyword motcle : texteExtraitKeywordList)
		    {
			listKeywords.add(motcle.getText());
			texteExtrait += motcle.getText()+" ";
		    }
		}
		texteExtrait += texteExtraitTitre;

		this.setMotscles(listKeywords);
		this.setTexteExtrait(texteExtrait);
		    
	}
	
	/**
	 * Trouve le texte extrait de la page.
	 */
	public void alchemyAPITextPOO() {
		AlchemyLanguage service = new AlchemyLanguage();
	    service.setApiKey("a340a4d57f3fe543684011b1f7c6c1f6ea070336");
	    
	    Map<String,Object> params = new HashMap<String, Object>();
	    String url = this.getUrl();
	    List<String> listKeywords = new ArrayList<String>();
	    
		try {
		    URL urlAlchemy = new URL(url);
		    params.put(AlchemyLanguage.URL, urlAlchemy);
		    DocumentText texteExtraitKeyWord = service.getText(params).execute();
			   
		    String texteExtrait = texteExtraitKeyWord.getText();
		    listKeywords.add(texteExtrait);
		    this.setMotscles(listKeywords);
		    this.setTexteExtrait(texteExtrait);
		} catch (Exception e) {
		    System.out.println("Text not retrieved");
		}

	}
	
	
	/**
	 * Trouve le titre de la page.
	 * @return Titre de la page
	 * @throws Exception
	 */
	public String alchemyAPITitrePage()throws Exception {

	    AlchemyLanguage service = new AlchemyLanguage();
	    service.setApiKey("a340a4d57f3fe543684011b1f7c6c1f6ea070336");
	    
	    Map<String,Object> params = new HashMap<String, Object>();
	    
	    URL urlAlchemy = new URL(url);
	    params.put(AlchemyLanguage.URL, urlAlchemy);
	    String titre;
	    try {
		DocumentTitle texteExtraitTexte = service.getTitle(params).execute();
	    	titre = texteExtraitTexte.getTitle();
	    } catch(Exception e) {
		System.out.println("Title not retrieved");
		titre = "Not retrieved";
	    }
	    return titre;
	}
	
	
	/**
	 * Trouve toutes les URI Dbpedia qu'il y a dans la page
	 * et en fait un modele RDF, ensuite on cherche si la page est une page de sport ou non.
	 */
	public void dbpediaSpotlightPOO() {

		/**
	     * ********************************************
		 * ********* RECUPERATION DES URI *************
		 * ********************************************
		 */
		
	    List<String> listeURI = new ArrayList<String>();
	    List<String> listeUrlRdf = new ArrayList<String>();
	
	    db c = new db ();  
	    c.configiration(0.0, 0, "non", "Default", "Default", "yes");  
	    try {
			c.evaluate(this.getTexteExtrait());
		} catch (Exception e) {
		    	System.out.println("URI list not retrieved");
		}
	    
	    listeURI = c.getResuFullURI();
	    

	    // **************************************************************************
	    
	    
	    
	    // On enlève les doublons de la liste
	    Set<String> set = new HashSet<String>() ;
	    set.addAll(listeURI) ;
	    ArrayList<String> listeURIDistinct = new ArrayList<String>(set) ;
	    double sizeListURI = listeURIDistinct.size();
        
	    System.out.println("nb URI page : " + sizeListURI);
	    
	    // On modifie les URI afin d'obtenir le lien des fichiers RDF
	    for(String uri : listeURIDistinct)
	    {
	    	String uriData = uri.replace("resource","data");
	    	listeUrlRdf.add(uriData+".rdf");
	    }
	    // On charge le modèle avec le fichier RDF
	    FileManager fManager = FileManager.get();
	    fManager.addLocatorURL();
	    
	    Model modelPage;
	    Model modelURI;
	    Model modelFinal = null;
	    if(sizeListURI>0){
	    	modelPage = fManager.loadModel(listeUrlRdf.get(0)); 	
	    }
	    else{
	    	modelPage = null;
	    }
	    if(sizeListURI>1){
	    	modelURI = fManager.loadModel(listeUrlRdf.get(1));
		    // On fait l'union des modèles de chaque mots, afin d'obtenir le modele de la page.
		    for(String urlRDF : listeUrlRdf)
		    {
			    	try {
			    	    modelURI = fManager.loadModel(urlRDF);
			    	    modelPage = modelPage.union(modelURI);
				} catch (Exception e) {
				    System.out.println("IRI malformed");
				}
		    }
	    }
	    
	    // Si le modele de la page n'est pas vide on regarde si c'est une page oriente sport ou non.
	    // Si c'est une page de sport on recentre le modele sur le sport. (on enleve les URI non sport)
	    if(modelPage != null) {
	    		try {
	    		    modelFinal = this.isASportPage(listeUrlRdf, modelPage);
			} catch (Exception e) {
			    System.out.println("Error during the evaluation of the page");
			}
	    }
	    System.out.println("La page parle de sport : " + this.isSportPage());
	    this.setModel(modelFinal);
	}
	
	
	
    // L'objectif etant d'avoir des modeles plus petits et plus interessants
    // on voudrait connaitre toutes les URI en liens avec le sport
    // On crée donc des requêtes SPARQL afin d'affiner le modèle
    
 
    
	
	/**
	 * On cherche si la page est une page de sport ou non, en creant des requetes SPARQL adaptees.
	 * @param listeUrlRdf Liste des fichiers rdf composant la page 
	 * @param model Modele de la page
	 * @throws Exception
	 */
	public Model isASportPage(List<String> listeUrlRdf, Model model)throws Exception {
		
	    List<String> listSportWord = new ArrayList<String>();
	    double sizeListURI = listeUrlRdf.size();
	    Model modelURI;
	    
	    // En premier lieu on regarde si la page contient l'URI d'un sportif, si oui c'est une page sport
	    
	    String sparqlQuery =
	    		"PREFIX db-owlr: <http://dbpedia.org/resource/>\n" +
	            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
	            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
	            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
	            "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
	            "\n" +
	            "SELECT distinct ?athlete  WHERE {\n" +
	            "  ?athlete rdf:type dbo:Athlete " + " .\n" +
	            "}";
	
	    String resultat = new String();
	    
	    // Création de la requête
	    Query query = QueryFactory.create(sparqlQuery) ;
	    try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
	    	
	      // Lancement de la requête
	      ResultSet results = qexec.execSelect() ;
	      
	      // Récupération des résultats
	      for ( ; results.hasNext() ; )
	      {
	        QuerySolution soln = results.nextSolution() ;
	        RDFNode athlete = soln.get("athlete") ;
	        String nomAthlete = athlete.toString();
	
	        resultat+= "athlete : " + nomAthlete + "\n";
	      }
	    } catch (Exception e) {
		
	    }
	    
	    if(!resultat.equals("")){
	    	this.setSportPage(true);
	    } 
	    else {
		
		 // Sinon, on lance une requete sur les commentaires avec les comparant avec des mots en rapport avec le sport
		 // fournis dans un fichier texte
		 // On compte le nombre d'uri sportive a l'interieur ou alors on compte le nombre de mots en lien
		 // avec le sport en faisant la concatenation de tous les commentaires.
		
	    	 String sparqlQuery2 =
	 	    		"PREFIX db-owlr: <http://dbpedia.org/resource/>\n" +
	 	            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
	 	            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
	 	            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
	 	            "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
	 	            "\n" +
	 	            "SELECT distinct ?uri ?comment  WHERE {\n" +
	 	            "  ?uri rdfs:comment ?comment " +" .\n" +
	 	            "FILTER (langMatches(lang (?comment),'EN'))" +" .\n" +
	 	            "}";
	 	
	 	    String allComments = new String();
	 	    double cptNbSportWord =0;
	 	    
	 	    // Création de la requête
	 	    Query query2 = QueryFactory.create(sparqlQuery2) ;
	 	    try (QueryExecution qexec = QueryExecutionFactory.create(query2, model)) {
	 	    	
	 		// Lancement de la requête
	 		ResultSet results2 = qexec.execSelect() ;
	 	      
	 		// Récupération des résultats
	 		for ( ; results2.hasNext() ; )
	 		{
	 		    QuerySolution soln = results2.nextSolution() ;
	 		    RDFNode uri = soln.get("uri") ;
	 		    RDFNode comment = soln.get("comment") ;
	 		    String nomComment = comment.toString();
	 	        
	 		    // Concatenation de tous les commentaires
	 		    allComments+= nomComment + " ";
	 		}
	 	      
	 		List<String> listOfWordsInComment = new ArrayList<String>(Arrays.asList(allComments.split(" ")));
	 	    
	 		// On recupere les mots de sports dans la liste
	 		listSportWord = addListWordsSport();
	 	    
	 		// On compte le nombre de mot de sport au total
	 		for(String wordSport : listSportWord )
	 		{
	 	    		// Non Sensitive
	 	    		boolean containsWordsNonSensitive = listOfWordsInComment.stream().filter(s -> s.equalsIgnoreCase(wordSport)).findFirst().isPresent();
	 	    		if(containsWordsNonSensitive){
	 	    		    cptNbSportWord++;
	 	    		}
	 	   
	 		}
	 	    
	 		// Si il y a plus d'un mot de sport par commentaire en moyenne, c'est une page sport
	 		// et on reduit le modele 
	 		if(cptNbSportWord/sizeListURI>1){
	 	    		this.setSportPage(true);
	 	    	
	 	    	
	 	    		FileManager fManager = FileManager.get();
	 	    		fManager.addLocatorURL();
	 		    
	 	    		// Pour chaque URI de la page on regarde si l'URI est sport ou non.
	 	    		// S'il l'est pas on enleve son modele du modele de la page.
	 	    		for(String urlRDF : listeUrlRdf)
	 	    		{
	 	    		    modelURI = fManager.loadModel(urlRDF);
	 	    		    if(!isASportURI(modelURI)){
			    		model = model.remove(modelURI);
	 	    		    }	
	 	    		}	 	    		 	    	
	 	   	} else {
	 	    	this.setSportPage(false);
	 	   	}
	 	    } catch (Exception e) {
	 		
	 	    }
	    }
	    
	    // Si ce n'est pas une page de sport on retourne le modele initial.
		return model;  
	}
	
	/**
	 * On cherche si l'URI fournie est une URI de sport ou non.
	 * @param modelURI URI a traiter
	 * @throws Exception
	 */
	public boolean isASportURI(Model modelURI)throws Exception {
		
	    List<String> listSportWord = new ArrayList<String>();
	    
	    String sparqlQuery =
	    		"PREFIX db-owlr: <http://dbpedia.org/resource/>\n" +
	            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
	            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
	            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
	            "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
	            "\n" +
	            "SELECT distinct ?athlete  WHERE {\n" +
	            "  ?athlete rdf:type dbo:Athlete " + " .\n" +
	            "}";
	
	    String resultat = new String();
	    
	    // Création de la requête
	    Query query = QueryFactory.create(sparqlQuery) ;
	    try (QueryExecution qexec = QueryExecutionFactory.create(query, modelURI)) {
	    	
	      // Lancement de la requête
	      ResultSet results = qexec.execSelect() ;
	      
	      // Récupération des résultats
	      for ( ; results.hasNext() ; )
	      {
	        QuerySolution soln = results.nextSolution() ;
	        RDFNode athlete = soln.get("athlete") ;
	        String nomAthlete = athlete.toString();
	
	        resultat+= "athlete : " + nomAthlete + "\n";
	      }
	    } catch (Exception e) {
		
	    }
	    
	    // Si il y a un athlete dedans, alors c'est une page de sport directement.
	    if(!resultat.equals("")){
	    	this.setSportPage(true);
	    } else {
	    	 String sparqlQuery2 =
	 	    		"PREFIX db-owlr: <http://dbpedia.org/resource/>\n" +
	 	            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
	 	            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
	 	            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
	 	            "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
	 	            "\n" +
	 	            "SELECT distinct ?uri ?comment  WHERE {\n" +
	 	            "  ?uri rdfs:comment ?comment " +" .\n" +
	 	            "FILTER (langMatches(lang (?comment),'EN'))" +" .\n" +
	 	            "}";
	 	
	 	    String allComments = new String();
	 	    double cptNbSportWord =0;
	 	    
	 	    // Création de la requête
	 	    Query query2 = QueryFactory.create(sparqlQuery2) ;
	 	    try (QueryExecution qexec = QueryExecutionFactory.create(query2, modelURI)) {
	 	    	
	 	      // Lancement de la requête
	 	      ResultSet results2 = qexec.execSelect() ;
	 	      
	 	      // Récupération des résultats
	 	      for ( ; results2.hasNext() ; )
	 	      {
	 	        QuerySolution soln = results2.nextSolution() ;
	 	        RDFNode uri = soln.get("uri") ;
	 	        RDFNode comment = soln.get("comment") ;
	 	        String nomComment = comment.toString();
	 	        
	 	        // Concatenation de tous les commentaires
	 	        allComments+= nomComment + " ";
	 	      }
	 	      
	 	      List<String> listOfWordsInComment = new ArrayList<String>(Arrays.asList(allComments.split(" ")));
	 	    
	 	      // On recupere les mots de sports dans la liste
	 	      listSportWord = addListWordsSport();
	 	    
	 	      // On compte le nombre de mot de sport au total
	 	      for(String wordSport : listSportWord ) {
	 	    	// Non Sensitive
	 	    	boolean containsWordsNonSensitive = listOfWordsInComment.stream().filter(s -> s.equalsIgnoreCase(wordSport)).findFirst().isPresent();
	 	    	if(containsWordsNonSensitive){
	 	    	    cptNbSportWord++;
	 	    	}	
	 	      }
	 	    
	 	      // Si il y a plus de deux mot de sport par commentaire, c'est une page sport
	 	      if(cptNbSportWord>2){
	 	    	return true;
	 	      } else {
	 	    	return false;
	 	      }
	 	    } catch (Exception e) {
	 		
	 	    }
	    	}
		return true;  
	}
	
	/**
	 * Lit le fichier texte de mots sportifs, et les ajoute a une liste de chaine de caracteres.
	 * @return listSportWord Liste composee des mots de sports.
	 */
	private List<String> addListWordsSport() {
		List<String> listSportWord = new ArrayList<String>();
	    String fichier = "server/src/listSports.txt";
	    try {
	    	InputStream ips = new FileInputStream(fichier);
	    	InputStreamReader ipsr = new InputStreamReader(ips);
	    	BufferedReader br = new BufferedReader(ipsr);
	    	String ligne;
	    	while ((ligne = br.readLine()) != null)
	    	{
	    		listSportWord.add(ligne);
	    	}
	    	br.close();
	    } catch (Exception e) {
	    	System.out.println("Error while comparing with list's words of sport");
	    }
	    return listSportWord;
	}
	
	
	// ***************************
	// ***** GETTER / SETTER ***** 
	// ***************************
	
	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
	
	public List<String> getMotscles() {
		return motscles;
	}


	public void setMotscles(List<String> motscles) {
		this.motscles = motscles;
	}


	public String getUrl() {
		return url;
	}


	public int getClassement() {
		return classement;
	}


	public String getTexteExtrait() {
		return texteExtrait;
	}


	public void setTexteExtrait(String texteExtrait) {
		this.texteExtrait = texteExtrait;
	}


	public Model getModel() {
		return model;
	}


	public void setModel(Model model) {
		this.model = model;
	}

	public boolean isSportPage() {
		return sportPage;
	}

	public void setSportPage(boolean sportPage) {
		this.sportPage = sportPage;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

}
