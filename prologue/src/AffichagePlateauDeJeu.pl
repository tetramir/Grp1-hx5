﻿/*
 Affichage d'une case du plateau de jeu
 */
printCase(X, Y) :- 
	case(X,Y,obstacle),!,
	write('M').
	
printCase(X,Y):-
	case(X,Y,bonus),!,
	write('B').
	
printCase(X, Y) :- 
	case(X,Y,1),!,
	write('1').
	
printCase(X, Y) :- 
	case(X,Y,2),!,
	write('2').
	
printCase(X, Y) :-
	visites(X,Y),!,
	write('*').
	
printCase(_, _) :- 
	write(' ').
/*
	Ensemble de fonctions permettant un affichage de l'état du jeu sur le terminal
*/

/*
 Affichage d'une ligne du plateau de jeu
 */
printLine(Y) :- 
	dimensions(X,_), 
	ExtremeX is X - 1,!,
	write('|'), 
	repeat, 
		between(0,ExtremeX,Long) ,
		printCase(Long, Y),
		Long is ExtremeX,!,
	write('|'),
	writeln('').

/*
	Affichage d'une bordure
*/
printExtremeLine(X,_) :- 
	write(' '), 
	repeat, 
		between(0, X, Long), 
		write('-'), 
		Long is X, !,
	writeln('').
/*
	Affiche le plateau de jeu
*/
displayBoard:-
	dimensions(X, Y),
	ExtremeY is Y - 1,!,
	printExtremeLine(X,Y),
	repeat, 
		between(0, ExtremeY, Haut),
		printLine(Haut),
		Haut is ExtremeY,!, 
	printExtremeLine(X,Y).