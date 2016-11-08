package modele.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.junit.Before;
import org.junit.Test;

import modele.Intersection;
import modele.Itineraire;
import modele.Plan;
import modele.Troncon;
import tsp.TSPPlages;

public class TSPPlagesTest {
    Plan plan;
    TSPPlages tsp = new TSPPlages();
    int[][] coutCompComplet;
    Itineraire[][] trajetsUnitCompComplet;
    int[] duree;
    int[] plagesDebut;
    int[] plagesFin;
    
    @Before
    public void setUp(){
	
    }
    
    @Test
    public void testCalculerTourneeValide() {
	plan = new Plan();
	plan.ajouterIntersection(1, 412, 574);
	Intersection i1 = new Intersection(1, 412, 574);
	plan.ajouterIntersection(2, 217, 574);
	Intersection i2 = new Intersection(2, 217, 574);
	plan.ajouterIntersection(3, 325, 574);
	Intersection i3 = new Intersection(3, 325, 574);
	plan.ajouterIntersection(4, 412, 544);
	Intersection i4 = new Intersection(4, 412, 544);
	plan.ajouterIntersection(5, 742, 574);
	Intersection i5 = new Intersection(5, 742, 574);
	plan.ajouterIntersection(6, 451, 174);
	Intersection i6 = new Intersection(6, 418, 974);
	plan.ajouterIntersection(10, 418, 974);
	Intersection i10 = new Intersection(10, 418, 974);
	plan.ajouterTroncon("t_1_2", 5, 1, 1, 2);
	Troncon t_1_2 = new Troncon("t_1_2", i1, i2, 5, 1);
	plan.ajouterTroncon("t_2_1", 5, 1, 2, 1);
	Troncon t_2_1 = new Troncon("t_2_1", i2, i1, 5, 1);
	plan.ajouterTroncon("t_2_4", 25, 1, 2, 4);
	Troncon t_2_4 = new Troncon("t_2_4", i2, i4, 25, 1);
	plan.ajouterTroncon("t_4_2", 25, 1, 4, 2);
	Troncon t_4_2 = new Troncon("t_4_2", i4, i2, 25, 1);
	plan.ajouterTroncon("t_4_5", 3, 1, 4, 5);
	Troncon t_4_5 = new Troncon("t_4_5", i4, i5, 3, 1);
	plan.ajouterTroncon("t_5_4", 3, 1, 5, 4);
	Troncon t_5_4 = new Troncon("t_5_4", i5, i4, 3, 1);
	plan.ajouterTroncon("t_4_3", 8, 1, 4, 3);
	Troncon t_4_3 = new Troncon("t_4_3", i4, i3, 8, 1);
	plan.ajouterTroncon("t_3_4", 8, 1, 3, 4);
	Troncon t_3_4 = new Troncon("t_3_4", i3, i4, 8, 1);
	plan.ajouterTroncon("t_3_5", 1, 1, 3, 5);
	Troncon t_3_5 = new Troncon("t_3_5", i3, i5, 1, 1);
	plan.ajouterTroncon("t_5_3", 1, 1, 5, 3);
	Troncon t_5_3 = new Troncon("t_5_3", i5, i3, 1, 1);
	plan.ajouterTroncon("t_3_6", 6, 1, 3, 6);
	Troncon t_3_6 = new Troncon("t_3_6", i3, i6, 6, 1);
	plan.ajouterTroncon("t_6_3", 6, 1, 6, 3);
	Troncon t_6_3 = new Troncon("t_6_3", i6, i3, 6, 1);
	plan.ajouterTroncon("t_5_6", 10, 1, 5, 6);
	Troncon t_5_6 = new Troncon("t_5_6", i5, i6, 10, 1);
	plan.ajouterTroncon("t_6_5", 10, 1, 6, 5);
	Troncon t_6_5 = new Troncon("t_6_5", i6, i5, 10, 1);
	plan.ajouterTroncon("t_6_10", 11, 1, 6, 10);
	Troncon t_6_10 = new Troncon("t_6_10", i6, i10, 11, 1);
	plan.ajouterTroncon("t_10_6", 11, 1, 10, 6);
	Troncon t_10_6 = new Troncon("t_10_6", i10, i6, 11, 1);
	plan.ajouterTroncon("t_10_1", 6, 1, 10, 1);
	Troncon t_10_1 = new Troncon("t_10_1", i10, i1, 6, 1);
	plan.ajouterTroncon("t_1_10", 6, 1, 1, 10);
	Troncon t_1_10 = new Troncon("t_1_10", i1, i10, 6, 1);
	plan.ajouterTroncon("t_2_10", 1, 1, 2, 10);
	Troncon t_2_10 = new Troncon("t_2_10", i2, i10, 1, 1);
	plan.ajouterTroncon("t_10_2", 1, 1, 10, 2);
	Troncon t_10_2 = new Troncon("t_10_2", i10, i2, 1, 1);
	coutCompComplet = new int[3][3];
	trajetsUnitCompComplet = new Itineraire[3][3];
	coutCompComplet[0][0] = 0;
	coutCompComplet[0][1] = 23;
	coutCompComplet[0][2] = 6;
	coutCompComplet[1][0] = 23;
	coutCompComplet[1][1] = 0;
	coutCompComplet[1][2] = 17;
	coutCompComplet[2][0] = 6;
	coutCompComplet[2][1] = 17;
	coutCompComplet[2][2] = 0;
	List<Troncon> list1_1 = new ArrayList<>();
	Itineraire iti1_1 = new Itineraire(i1, i1, list1_1);
	List<Troncon> list1_3 = new ArrayList<>();
	list1_3.add(t_1_10);
	list1_3.add(t_10_6);
	list1_3.add(t_6_3);
	Itineraire iti1_3 = new Itineraire(i1, i3, list1_3);
	List<Troncon> list1_10 = new ArrayList<>();
	list1_10.add(t_1_10);
	Itineraire iti1_10 = new Itineraire(i1, i10, list1_10);
	trajetsUnitCompComplet[0][0] = iti1_1;
	trajetsUnitCompComplet[0][1] = iti1_3;
	trajetsUnitCompComplet[0][2] = iti1_10;
	List<Troncon> list3_1 = new ArrayList<>();
	list3_1.add(t_3_6);
	list3_1.add(t_6_10);
	list3_1.add(t_10_1);
	Itineraire iti3_1 = new Itineraire(i3, i1, list3_1);
	trajetsUnitCompComplet[1][0] = iti3_1;
	List<Troncon> list3_3 = new ArrayList<>();
	Itineraire iti3_3 = new Itineraire(i3, i3, list3_3);
	trajetsUnitCompComplet[1][1] = iti3_3;
	List<Troncon> list3_10 = new ArrayList<>();
	list3_10.add(t_3_6);
	list3_10.add(t_6_10);
	Itineraire iti3_10 = new Itineraire(i3, i10, list3_10);
	trajetsUnitCompComplet[1][2] = iti3_10;
	List<Troncon> list10_1 = new ArrayList<>();
	list10_1.add(t_10_1);
	Itineraire iti10_1 = new Itineraire(i10, i1, list10_1);
	trajetsUnitCompComplet[2][0] = iti10_1;
	List<Troncon> list10_3 = new ArrayList<>();
	list10_3.add(t_10_6);
	list10_3.add(t_6_3);
	Itineraire iti10_3 = new Itineraire(i10, i3, list10_3);
	trajetsUnitCompComplet[2][1] = iti10_3;
	List<Troncon> list10_10 = new ArrayList<>();
	Itineraire iti10_10 = new Itineraire(i10, i10, list10_10);
	trajetsUnitCompComplet[2][2] = iti10_10;
	duree = {0, 5, 15, 10};
	tsp.chercheSolution(500, 4, coutCompComplet, duree, horaireDebut, horaireFin, heureDepart);
    }

}
