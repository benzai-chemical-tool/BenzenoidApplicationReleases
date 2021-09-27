package problems;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import molecules.Molecule;
import solution.BenzenoidSolution;

/* Définit le pb d'avoir les structures de Clar d'un benzénoïde donné
 * 
 */

public class ClarCoverProblem {
	private BenzenoidSolution benzenoid;
	private int nbCouronnes;
	private int largeur;
	private SimpleGraph<Integer, DefaultEdge> hexGraph;
	private Model model = new Model("Clar");
	private IntVar nbRonds;
	private IntVar nbCelibataires;
	//private IntVar objectif = model.intVar("objectif", -200, 999);

	public ClarCoverProblem(Molecule molecule) {
		
		super();
		
		nbCouronnes = (int) Math.floor((((double) ((double) molecule.getNbHexagons() + 1)) / 2.0) + 1.0);

		if (molecule.getNbHexagons() % 2 == 1)
			nbCouronnes --;
		
		largeur = nbCouronnes * 2 - 1;
		
		hexGraph = molecule.getHexagonGraph();
		
		initialize();
		
	}
	
	public ClarCoverProblem(BenzenoidSolution benzenoid) {
		super();
		this.benzenoid = benzenoid;
		nbCouronnes = benzenoid.getNbCouronnes();
		largeur = nbCouronnes * 2 - 1;
		hexGraph = benzenoid.getHexagonGraph();
		
		initialize();
		
	}
	
	private void initialize() {
		
		// Les ronds
				int i, j;
				int nbHex = hexGraph.vertexSet().size();
				BoolVar rien = model.boolVar(false);
				
				BoolVar[] ronds = new BoolVar[largeur * largeur];
				for(i = 0 ; i < ronds.length ; i++) {
					if(hexGraph.containsVertex(i)) {
						ronds[i] = model.boolVar("rond" + i);
						//System.out.println(ronds[i].getName());/////////////
					}
					else
						ronds[i] = rien;
				}
				
				// Les liaisons
				BoolVar[] liaisons = new BoolVar[largeur * largeur * 6];
				for(i = 0 ; i < liaisons.length ; i++) {
					if(hexGraph.containsVertex(i / 6)) {
						int hexVoisin = hexACote(i / 6, i % 6);
						liaisons[i] = model.boolVar("liaison" + i / 6 + ":" + i % 6);
					}
					else
						liaisons[i] = rien;

				}
				for(i = 0 ; i < liaisons.length ; i++) {
					if(hexGraph.containsVertex(i / 6)) {
						int hexVoisin = hexACote(i / 6, i % 6);
						if(hexGraph.containsVertex(hexVoisin) && hexVoisin > i / 6) { // C'est la même liaison mais vue depuis l'autre hexagone
							
							model.arithm(liaisons[i], "=", liaisons[hexVoisin * 6 + ((i % 6 + 3) % 6)]).post();
						}
					}
				}

				// Les célibataires
				BoolVar[] celibataires = new BoolVar[largeur * largeur * 6];
				for(i = 0 ; i < celibataires.length ; i++) {
					if(i == xyc2i((i / 6) % largeur, (i / 6) / largeur, i % 6, hexGraph)) {
						celibataires[i] = model.boolVar("celibataire" + i / 6 + ":" + i % 6);
					}
					else
						celibataires[i] = rien;
				}


				nbRonds = model.intVar("nbRonds", 0, nbHex);
				model.sum(ronds,  "=", nbRonds).post();
				nbCelibataires = model.intVar("nbCelibataires", 0, 2);
				model.sum(celibataires,  "=", nbCelibataires).post();
				
				for(i = -1 ; i <= largeur ; i++)
					for(j = 0 ; j <= largeur; j++) {
						if(hexGraph.containsVertex(xy2i(i, j)) || hexGraph.containsVertex(xy2i(i - 1, j - 1)) || hexGraph.containsVertex(xy2i(i, j - 1))) {
							BoolVar [] somme7 = new BoolVar[7];
							somme7[0] = hexGraph.containsVertex(xy2i(i, j)) ? ronds[xy2i(i, j)] : rien; // rond en bas
							somme7[1] = hexGraph.containsVertex(xy2i(i - 1, j - 1)) ? ronds[xy2i(i - 1, j - 1)] : rien; // rond en haut à gauche
							somme7[2] = hexGraph.containsVertex(xy2i(i, j - 1)) ? ronds[xy2i(i, j - 1)] : rien; // rond en haut à droite
							//liaison haut-gauche
							somme7[3] = (hexGraph.containsVertex(xy2i(i, j)) || hexGraph.containsVertex(xy2i(i - 1, j - 1))) ? liaisons[numLiaison(i, j, 5)] : rien;
							//liaison haut-droit
							somme7[4] = (hexGraph.containsVertex(xy2i(i, j)) || hexGraph.containsVertex(xy2i(i, j - 1))) ? liaisons[numLiaison(i, j, 0)] : rien;
							//liaison gauche-droite (haut)
							somme7[5] = (hexGraph.containsVertex(xy2i(i, j - 1)) || hexGraph.containsVertex(xy2i(i - 1, j - 1))) ? liaisons[numLiaison(i, j - 1, 4)] : rien;
							// celibataire
							somme7[6] = celibataires[xyc2i(i, j, 0, hexGraph)];
							model.sum(somme7, "=", 1).post();
									
						}
						if(hexGraph.containsVertex(xy2i(i, j)) || hexGraph.containsVertex(xy2i(i, j - 1)) || hexGraph.containsVertex(xy2i(i + 1, j))) {
							BoolVar [] somme7 = new BoolVar[7];				
							somme7[0] = hexGraph.containsVertex(xy2i(i, j - 1)) ? ronds[xy2i(i, j - 1)] : rien; // rond en haut
							somme7[1] = hexGraph.containsVertex(xy2i(i, j)) ? ronds[xy2i(i, j)] : rien; // rond en bas à gauche
							somme7[2] = hexGraph.containsVertex(xy2i(i + 1, j)) ? ronds[xy2i(i + 1, j)] : rien; // rond en bas à droite
							//liaison haut-gauche
							somme7[3] = (hexGraph.containsVertex(xy2i(i, j)) || hexGraph.containsVertex(xy2i(i, j - 1))) ? liaisons[numLiaison(i, j, 0)] : rien;
							//liaison haut-droit
							somme7[4] = (hexGraph.containsVertex(xy2i(i, j - 1)) || hexGraph.containsVertex(xy2i(i + 1, j))) ? liaisons[numLiaison(i + 1, j, 5)] : rien;
							//liaison gauche-droite (bas)
							somme7[5] = (hexGraph.containsVertex(xy2i(i, j)) || hexGraph.containsVertex(xy2i(i + 1, j))) ? liaisons[numLiaison(i, j, 1)] : rien;
							// celibataire
							somme7[6] = celibataires[xyc2i(i, j, 1, hexGraph)];
							model.sum(somme7, "=", 1).post();
						}
					}
		
	}
	
	private int numCote2DeltaNumHex(int cote) {
		switch(cote) {
		case 0 : return -largeur;
		case 1 : return 1;
		case 2 : return largeur + 1;
		case 3 : return largeur;
		case 4 : return -1;
		case 5 : return -largeur - 1;
		default : return 0;
		}
	}
	
	private int hexACote(int hex, int cote) {
		return hex + numCote2DeltaNumHex(cote);
	}
	
	private int numLiaison(int x, int y, int c) {
		int hex1, hex2;
		hex1 = xy2i(x, y);
		hex2 = hexACote(hex1, c);
		if(hexGraph.containsVertex(hex1))
			if(hexGraph.containsVertex(hex2))
				return hex1 < hex2 ? hex1 * 6 + c : hex2 * 6 + (c % 6 + 3) % 6;
			else
				return hex1 * 6 + c;
		else
			return hex2 * 6 + (c % 6 + 3) % 6;
	}
	/***
	 * 
	 * @param x
	 * @param y
	 * @return Conversion coordonnées (x,y) de l'hexagone en son indice
	 */
	public int xy2i(int x, int y) {
		return x + y * largeur;
	}

	/***
	 * 
	 * @param x
	 * @param y
	 * @param c
	 * @return Conversion coordonnées (x,y) et numéro côté c en un indice
	 */
	public int xyc2i(int x, int y, int c) {
		return xy2i(x, y) * 6 + c;
	}
	
	/***
	 * 
	 * @param x
	 * @param y
	 * @param c
	 * @param hexGraph
	 * @return Conversion coordonnées (x,y) et numéro côté c en un indice en fonction du graphe d'hexagones
	 */
	public int xyc2i(int x, int y, int c, SimpleGraph<Integer, DefaultEdge> hexGraph) {
		int i;
		switch(c) {
		case 0 :
			if(hexGraph.containsVertex(xy2i(x, y))) {
				i = xyc2i(x, y, c);
			}
			else if(hexGraph.containsVertex(xy2i(x - 1, y - 1))) {
				i = xyc2i(x - 1, y - 1, 2);
			}
			else if(hexGraph.containsVertex(xy2i(x, y - 1))) {
				i = xyc2i(x, y - 1, 4);
			}
			else i = -1;
			break;
		case 1 :
			if(hexGraph.containsVertex(xy2i(x, y))) {
				i = xyc2i(x, y, 1);
			}
			else if(hexGraph.containsVertex(xy2i(x, y - 1))) {
				i = xyc2i(x, y - 1, 3);
			}
			else if(hexGraph.containsVertex(xy2i(x + 1, y))) {
				i = xyc2i(x + 1, y, 5);
			}
			else
				i = -1;
			break;
		case 2 :
			if(hexGraph.containsVertex(xy2i(x + 1, y + 1))) {
				i = xyc2i(x + 1, y + 1, 0);
			}
			else if(hexGraph.containsVertex(xy2i(x, y))) {
				i = xyc2i(x, y, 2);
			}
			else if(hexGraph.containsVertex(xy2i(x + 1, y))) {
				i = xyc2i(x + 1, y, 4);
			}
			else
				i = -1;
			break;
		case 3 :
			if(hexGraph.containsVertex(xy2i(x, y + 1))) {
				i = xyc2i(x, y + 1, 1);
			}
			else if(hexGraph.containsVertex(xy2i(x, y))) {
				i = xyc2i(x, y, 3);
			}
			else if(hexGraph.containsVertex(xy2i(x + 1, y + 1))) {
				i = xyc2i(x + 1, y + 1, 5);
			}
			else
				i = -1;
			break;
		case 4 :
			if(hexGraph.containsVertex(xy2i(x, y + 1))) {
				i = xyc2i(x, y + 1, 0);
			}
			else if(hexGraph.containsVertex(xy2i(x - 1, y))) {
				i = xyc2i(x - 1, y, 2);
			}
			else if(hexGraph.containsVertex(xy2i(x, y))) {
				i = xyc2i(x, y, 4);
			}
			else
				i = -1;
			break;
		case 5 :
			if(hexGraph.containsVertex(xy2i(x - 1, y))) {
				i = xyc2i(x - 1, y , 1);
			}
			else if(hexGraph.containsVertex(xy2i(x - 1, y - 1))) {
				i = xyc2i(x - 1, y - 1, 3);
			}
			else if(hexGraph.containsVertex(xy2i(x, y))) {
				i = xyc2i(x, y, 5);
			}
			else
				i = -1;
			break;
		default:
			i = -1;
		}

		return i;
	}
	/***
	 * 
	 * @param x
	 * @param y
	 * @return si (x,y) est dans le coronenoide
	 */

	public boolean inCoronenoid(int x, int y) {
		if (x < 0 || y < 0 || x >= largeur || y >= largeur)
			return false;
		if (y < nbCouronnes)
			return x < nbCouronnes + y;
		else
			return x > y - nbCouronnes;
	}

	public boolean inCoronenoid(int i) {
		return inCoronenoid(i % largeur, i / largeur);
	}

	public Model getModel() {
		return model;
	}

	public IntVar getNbRonds() {
		return nbRonds;
	}

	public IntVar getNbCelibataires() {
		return nbCelibataires;
	}

/*	public IntVar getObjectif() {
		return objectif;
	}*/

}