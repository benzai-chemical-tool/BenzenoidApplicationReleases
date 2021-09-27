package solveur;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import molecules.Node;
import molecules.NodeSameLine;
import parsers.GraphParser;
import solveur.Aromaticity.RIType;
import molecules.Molecule;
import utils.EdgeSet;
import utils.Interval;
import utils.SubMolecule;
import utils.Utils;
import org.chocosolver.solver.search.strategy.strategy.*;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;

public class Approximation {
	
	static BufferedWriter log = null;
	private static String path = null;
	private static String nautyDirectory;
	private static boolean symmetries;
	
	private static final int MAX_CYCLE_SIZE = 4;
	
	public static int [][][] energies = new int[127][11][4];
	public static int [][] circuits;		
	public static int [] circuitCount;
	
	public static ArrayList<Integer> getVerticalNeighborhood(Molecule molecule, int hexagon, int [][] edgesCorrespondances, boolean left) {
		
		ArrayList<Integer> edges = new ArrayList<Integer>();
		
		int [] hexagonVertices = molecule.getHexagons()[hexagon];
		
		int x, y1, y2;
		
		if (left) {
			edges.add(edgesCorrespondances[hexagonVertices[4]][hexagonVertices[5]]);
			//System.out.println("adding (" + hexagonVertices[4] + ", " + hexagonVertices[5] + ")(" + edgesCorrespondances[hexagonVertices[4]][hexagonVertices[5]] + ")");
			x = molecule.getNodesRefs()[hexagonVertices[4]].getX();
			y1 = molecule.getNodesRefs()[hexagonVertices[4]].getY();
			y2 = molecule.getNodesRefs()[hexagonVertices[5]].getY();
		}
		
		else {
			edges.add(edgesCorrespondances[hexagonVertices[1]][hexagonVertices[2]]);
			//System.out.println("adding (" + hexagonVertices[1] + ", " + hexagonVertices[2] + ")(" + edgesCorrespondances[hexagonVertices[1]][hexagonVertices[2]] + ")");
			x = molecule.getNodesRefs()[hexagonVertices[1]].getX();
			y1 = molecule.getNodesRefs()[hexagonVertices[1]].getY();
			y2 = molecule.getNodesRefs()[hexagonVertices[2]].getY();
		}
		
		
		
		for (int i = 0 ; i < molecule.getNbNodes() ; i++) {
			
			for (int j = (i + 1) ; j < molecule.getNbNodes() ; j++) {
				
				if (molecule.getAdjacencyMatrix()[i][j] == 1) {
					
					Node u = molecule.getNodesRefs()[i];
					Node v = molecule.getNodesRefs()[j];
					
					if (left) {
						if ((u.getX() == v.getX()) && (u.getX() < x) && 
								((u.getY() == y1 && v.getY() == y2) ||  (u.getY() == y2 && v.getY() == y1))) {
									
								edges.add(edgesCorrespondances[i][j]);
								//System.out.println("adding (" + i + ", " + j + ")(" + edgesCorrespondances[i][j] + ")");
							}
					}
					
					else {
						if ((u.getX() == v.getX()) && (u.getX() > x) && 
							((u.getY() == y1 && v.getY() == y2) ||  (u.getY() == y2 && v.getY() == y1))) {
								
							edges.add(edgesCorrespondances[i][j]);
							//System.out.println("adding (" + i + ", " + j + ")(" + edgesCorrespondances[i][j] + ")");
						}
					}
					
					
				}
			}
		}
		
		//System.out.println("");
		
		return edges;
	}
	
	public static void computeCyclesRelatedToOneHexagon(Molecule molecule, int hexagon) {
		
		int [] firstVertices = new int [molecule.getNbEdges()];
		int [] secondVertices = new int [molecule.getNbEdges()];
		
		Model model = new Model("Cycles");

		UndirectedGraph GLB = new UndirectedGraph(model, molecule.getNbNodes(), SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(model, molecule.getNbNodes(), SetType.BITSET, false);

		for (int i = 0; i < molecule.getNbNodes(); i++) {
			GUB.addNode(i);

			for (int j = (i + 1); j < molecule.getNbNodes(); j++) {
				if (molecule.getAdjacencyMatrix()[i][j] == 1) {
					GUB.addEdge(i, j);
				}
			}
		}

		UndirectedGraphVar g = model.graphVar("g", GLB, GUB);

		BoolVar[] boolEdges = new BoolVar[molecule.getNbEdges()];
		int [][] ME = new int [molecule.getNbNodes()][molecule.getNbNodes()];
			
		int index = 0;
		for (int i = 0 ; i < molecule.getNbNodes() ; i++) {
			for (int j = (i+1) ; j < molecule.getNbNodes() ; j++) {

				if (molecule.getAdjacencyMatrix()[i][j] == 1) {
					boolEdges[index] = model.boolVar("(" + i + "--" + j + ")");
					model.edgeChanneling(g, boolEdges[index], i, j).post();
					firstVertices[index] = i;
					secondVertices[index] = j;
					ME[i][j] = index;
					ME[j][i] = index;
					index ++;
				}
			}
		}
			
		ArrayList<Integer> leftVerticalEdges = getVerticalNeighborhood(molecule, hexagon, ME, true);
		ArrayList<Integer> rightVerticalEdges = getVerticalNeighborhood(molecule, hexagon, ME, false);
		
		BoolVar[] left = new BoolVar[leftVerticalEdges.size()];
		BoolVar[] right = new BoolVar[rightVerticalEdges.size()];
		
		for (int i = 0 ; i < left.length ; i++) {
			left[i] = boolEdges[leftVerticalEdges.get(i)];
		}
		
		for (int i = 0 ; i < right.length ; i++) {
			right[i] = boolEdges[rightVerticalEdges.get(i)];
		}
		
		//model.sum(left, "=", 1).post(); //>= avant
		//model.sum(right, "=", 1).post();
		
		//model.or(left).post();
		//model.or(right).post();
		
		//model.arithm(left[0], "=", 1).post();
		//model.arithm(right[0], "=", 1).post();
		
		/* Taille de la bande centrale */
		
		IntVar[] indexesLeft = new IntVar[left.length];
		IntVar[] indexesRight = new IntVar[right.length];
		
		int maxSize = left.length + right.length - 1;
		
		int [] domainLeft = new int [left.length];
		for (int i = 0 ; i < left.length ; i++)
			domainLeft[i] = i;
		
		int [] domainRight = new int [right.length];
		for (int i = 0 ; i < right.length ; i++) 
			domainRight[i] = left.length + i;
		
		for (int i = 0 ; i < indexesLeft.length ; i++) {
			indexesLeft[i] = model.intVar("ILeft_" + i, new int [] {0, domainLeft[i]});
		}
		
		for (int i = 0 ; i < indexesRight.length ; i++) {
			indexesRight[i] = model.intVar("IRight_" + i, new int [] {0, domainRight[i]});
		}
		
		/*
		 * Fixing indexes variables values
		 */
		
		for (int i = 0 ; i < left.length ; i++) {
			
			model.ifThenElse(left[i], 
					         model.arithm(indexesLeft[i], "=", i), 
					         model.arithm(indexesLeft[i], "=", 0));
			
		}
		
		for (int i = 0 ; i < right.length ; i++) {
			
			model.ifThenElse(right[i], 
					         model.arithm(indexesRight[i], "=", left.length + i), 
					         model.arithm(indexesRight[i], "=", 0));
		}
		
		IntVar sumLeft = model.intVar("sumLeft", domainLeft);
		IntVar sumRight = model.intVar("sumRight", domainRight);
		
		model.sum(indexesLeft, "=", sumLeft).post();
		model.sum(indexesRight, "=", sumRight).post();
		
		int [] maxSizeDomain = new int [maxSize + 1];
		for (int i = 0 ; i <= maxSize ; i++) {
			maxSizeDomain[i] = i;
		}
		
		IntVar taille = model.intVar("sizeBand", maxSizeDomain);
			
		model.sum(new IntVar[] {taille, sumLeft}, "=", sumRight).post();
		model.arithm(taille, "<=", 5).post();
		model.arithm(taille, ">", 0).post();
		
		model.or(model.sum(left, "=", 1), model.arithm(left[0], "=", 1)).post();
		model.or(model.sum(right, "=", 1), model.arithm(right[0], "=", 1)).post();
		
		model.minDegree(g, 2).post();
		model.maxDegree(g, 2).post();
		
		model.connected(g).post();
			
		/*
		model.or(
			model.and(model.arithm(model.nbNodes(g), "=", 6), model.sum(boolEdges, "=", 6)),
			model.and(model.arithm(model.nbNodes(g), "=", 10), model.sum(boolEdges, "=", 10)),
			model.and(model.arithm(model.nbNodes(g), "=", 14), model.sum(boolEdges, "=", 14)),
			model.and(model.arithm(model.nbNodes(g), "=", 18), model.sum(boolEdges, "=", 18)),
			model.and(model.arithm(model.nbNodes(g), "=", 22), model.sum(boolEdges, "=", 22)),
			model.and(model.arithm(model.nbNodes(g), "=", 26), model.sum(boolEdges, "=", 26))
		).post();
		*/
			
		model.or(
				model.and(model.nbNodes(g, model.intVar(6)), model.sum(boolEdges, "=", 6)),
				model.and(model.nbNodes(g, model.intVar(10)), model.sum(boolEdges, "=", 10)),
				model.and(model.nbNodes(g, model.intVar(14)), model.sum(boolEdges, "=", 14)),
				model.and(model.nbNodes(g, model.intVar(18)), model.sum(boolEdges, "=", 18)),
				model.and(model.nbNodes(g, model.intVar(22)), model.sum(boolEdges, "=", 22)),
				model.and(model.nbNodes(g, model.intVar(26)), model.sum(boolEdges, "=", 26))
		).post();
		
		model.getSolver().setSearch(new IntStrategy(boolEdges, new FirstFail(model), new IntDomainMin()));
		Solver solver = model.getSolver();

		Solution solution;
			
		while(solver.solve()){
			solution = new Solution(model);
			solution.record();
					
			ArrayList<Integer> cycle = new ArrayList<Integer>();

			for (int i = 0 ; i < boolEdges.length ; i++) {
				if (solution.getIntVal(boolEdges[i]) == 1) {
					cycle.add(firstVertices[i]);
					cycle.add(secondVertices[i]);
				}
			}
			
			EdgeSet verticalEdges = computeStraightEdges(molecule, cycle);
			ArrayList<Interval> intervals = (ArrayList<Interval>) computeIntervals(molecule, cycle, verticalEdges);
			Collections.sort(intervals);
			int cycleConfiguration = Utils.identifyCycle(molecule, intervals);
			List<Integer> hexagons = getHexagons(molecule, cycle, intervals);
			
			if (cycleConfiguration != -1/* && hexagons.contains(hexagon)*/) {
				
				
				SubMolecule subMolecule = substractCycleAndInterior(molecule, cycle, intervals);
				int nbPerfectMatchings = PerfectMatchingSolver.computeNbPerfectMatching(subMolecule);
				
				int [][] energiesCycle = energies[cycleConfiguration];
				
				for (int idHexagon = 0 ; idHexagon < hexagons.size() ; idHexagon++) {
					
					int hexagonTreated = hexagons.get(idHexagon);
					if (hexagonTreated == hexagon) {
						for (int size = 0 ; size < 4 ; size ++) {
						
							if (energiesCycle[idHexagon][size] != 0)
								circuits[hexagon][size] += energiesCycle[idHexagon][size] * nbPerfectMatchings;			
						}
					}
				}
				
				//System.out.println(hexagons);
			}
			
			
			//treatCycle(molecule, cycle);
		}
		
		//displayResults();
	}
	
	public static void computeResonanceEnergy(Molecule molecule) throws IOException {

		energies = Utils.initEnergies();
		circuits = new int[molecule.getNbHexagons()][MAX_CYCLE_SIZE];		
		circuitCount = new int[energies.length];
		
		int [] firstVertices = new int [molecule.getNbEdges()];
		int [] secondVertices = new int [molecule.getNbEdges()];
		
		Model model = new Model("Cycles");

		UndirectedGraph GLB = new UndirectedGraph(model, molecule.getNbNodes(), SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(model, molecule.getNbNodes(), SetType.BITSET, false);

		for (int i = 0; i < molecule.getNbNodes(); i++) {
			GUB.addNode(i);

			for (int j = (i + 1); j < molecule.getNbNodes(); j++) {
				if (molecule.getAdjacencyMatrix()[i][j] == 1) {
					GUB.addEdge(i, j);
				}
			}
		}

		UndirectedGraphVar g = model.graphVar("g", GLB, GUB);

		BoolVar[] boolEdges = new BoolVar[molecule.getNbEdges()];
			
		int index = 0;
		for (int i = 0 ; i < molecule.getNbNodes() ; i++) {
			for (int j = (i+1) ; j < molecule.getNbNodes() ; j++) {

				if (molecule.getAdjacencyMatrix()[i][j] == 1) {
					boolEdges[index] = model.boolVar("(" + i + "--" + j + ")");
					model.edgeChanneling(g, boolEdges[index], i, j).post();
					firstVertices[index] = i;
					secondVertices[index] = j;
					index ++;
				}
			}
		}
			
		model.minDegree(g, 2).post();
		model.maxDegree(g, 2).post();
		model.connected(g).post();
			
		model.or(
				model.and(model.nbNodes(g, model.intVar(6)), model.sum(boolEdges, "=", 6)),
				model.and(model.nbNodes(g, model.intVar(10)), model.sum(boolEdges, "=", 10)),
				model.and(model.nbNodes(g, model.intVar(14)), model.sum(boolEdges, "=", 14)),
				model.and(model.nbNodes(g, model.intVar(18)), model.sum(boolEdges, "=", 18)),
				model.and(model.nbNodes(g, model.intVar(22)), model.sum(boolEdges, "=", 22)),
				model.and(model.nbNodes(g, model.intVar(26)), model.sum(boolEdges, "=", 26))
		).post();
			
		model.getSolver().setSearch(new IntStrategy(boolEdges, new FirstFail(model), new IntDomainMin()));
		Solver solver = model.getSolver();

		Solution solution;
			
		while(solver.solve()){
			solution = new Solution(model);
			solution.record();
					
			ArrayList<Integer> cycle = new ArrayList<Integer>();

			for (int i = 0 ; i < boolEdges.length ; i++) {
				if (solution.getIntVal(boolEdges[i]) == 1) {
					cycle.add(firstVertices[i]);
					cycle.add(secondVertices[i]);
				}
			}
			
			treatCycle(molecule, cycle);
		}
	}
	
	public static void displayResults() throws IOException{
		
		System.out.println("");
		System.out.println("LOCAL ENERGY");
		
		log.write(path);
		
		if (symmetries)
			log.write(" symmetries\n");
		else 
			log.write("\n");
		
		log.write("LOCAL ENERGY" + "\n");
		
		int [] globalEnergy = new int[MAX_CYCLE_SIZE];
		
		for (int i = 0 ; i < circuits.length ; i++) {
			System.out.print("H" + i + " : ");
			log.write("H" + i + " : ");
			
			for (int j = 0 ; j < MAX_CYCLE_SIZE ; j++) {
				
				System.out.print(circuits[i][j] + " ");
				log.write(circuits[i][j] + " ");
				globalEnergy[j] += circuits[i][j];
			}
			
			System.out.println("");
			log.write("\n");
		}
		
		System.out.println("");
		System.out.print("GLOBAL ENERGY : ");
		
		log.write("\n");
		log.write("GLOBAL ENERGY : \n");
		
		for (int i = 0 ; i < globalEnergy.length ; i++) {
			System.out.print(globalEnergy[i] + " ");
			log.write(globalEnergy[i] + " ");
		}
		System.out.println("");
		System.out.println("");
		
		log.write("\n");
		
		//log.close();
		
	}
	
	public static EdgeSet computeStraightEdges(Molecule molecule, ArrayList<Integer> cycle) {
		
		List<Node> firstVertices = new ArrayList<Node>();
		List<Node> secondVertices = new ArrayList<Node>();
		
		for (int i = 0 ; i < cycle.size() - 1 ; i += 2) {
			int uIndex = cycle.get(i);
			int vIndex = cycle.get(i + 1);
			
			Node u = molecule.getNodesRefs()[uIndex];
			Node v = molecule.getNodesRefs()[vIndex];
			
			if (u.getX() == v.getX()) {
				firstVertices.add(u);
				secondVertices.add(v);
			}
		}
		
		return new EdgeSet(firstVertices, secondVertices);
	}
	
	public static List<Interval> computeIntervals(Molecule molecule, ArrayList<Integer> cycle, EdgeSet edges){
		
		List<Interval> intervals = new ArrayList<Interval>();
	
		int [] edgesOK = new int [edges.size()];
		
		for (int i = 0 ; i < edges.size() ; i ++) {
			if (edgesOK[i] == 0) {
				edgesOK[i] = 1;
				Node u1 = edges.getFirstVertices().get(i);
				Node v1 = edges.getSecondVertices().get(i);
				
				int y1 = Math.min(u1.getY(), v1.getY());
				int y2 = Math.max(u1.getY(), v1.getY());
				
				List<NodeSameLine> sameLineNodes = new ArrayList<NodeSameLine>();
				
				for (int j = (i+1) ; j < edges.size() ; j++) {
					if (edgesOK[j] == 0) {
						Node u2 = edges.getFirstVertices().get(j);
						Node v2 = edges.getSecondVertices().get(j);
						
						int y3 = Math.min(u2.getY(), v2.getY());
						int y4 = Math.max(u2.getY(), v2.getY());
						
						if (y1 == y3 && y2 == y4) {
							edgesOK[j] = 1;
							sameLineNodes.add(new NodeSameLine(j, u2.getX()));
						}
					}
				}
				
				sameLineNodes.add(new NodeSameLine(i, u1.getX()));
				Collections.sort(sameLineNodes);
									
				for (int j = 0 ; j < sameLineNodes.size() ; j += 2) {
						
					NodeSameLine nsl1 = sameLineNodes.get(j);
					NodeSameLine nsl2 = sameLineNodes.get(j+1);
						
					Node n1 = edges.getFirstVertices().get(nsl1.getIndex());
					Node n2 = edges.getSecondVertices().get(nsl1.getIndex());
					Node n3 = edges.getFirstVertices().get(nsl2.getIndex());
					Node n4 = edges.getSecondVertices().get(nsl2.getIndex());
						
					intervals.add(new Interval(n1, n2, n3, n4));
				}
				
			}
		}
		
		return intervals;
	}
	
	public static boolean hasEdge(Molecule molecule, int [] vertices, int vertex) {
		
		for (int u = 0 ; u < molecule.getNbNodes() ; u++) {
			if (molecule.getAdjacencyMatrix()[vertex][u] == 1 && vertices[u] == 0)
				return true;
		}
		
		return false;
	}
	
	public static SubMolecule substractCycleAndInterior(Molecule molecule, ArrayList<Integer> cycle, ArrayList<Interval> intervals) {
		
		int [][] newGraph = new int [molecule.getNbNodes()][molecule.getNbNodes()];
		int [] vertices = new int [molecule.getNbNodes()];
		int [] subGraphVertices = new int[molecule.getNbNodes()];
		
		List<Integer> hexagons = getHexagons(molecule, cycle, intervals);
		
		for (Integer hexagon : hexagons) {
			int [] nodes = molecule.getHexagons()[hexagon];
			
			for (int i = 0 ; i < nodes.length ; i++)
				vertices[nodes[i]] = 1;
		}
		
		int subGraphNbNodes = 0;
		
		int nbEdges = 0;
		
		for (int u = 0 ; u < molecule.getNbNodes() ; u++) {
			if (vertices[u] == 0) {
				for (int v = (u+1) ; v < molecule.getNbNodes() ; v++) {
					if (vertices[v] == 0) {
						newGraph[u][v] = molecule.getAdjacencyMatrix()[u][v];
						newGraph[v][u] = molecule.getAdjacencyMatrix()[v][u];
						
						if (molecule.getAdjacencyMatrix()[u][v] == 1)
							nbEdges ++;
						
						if (subGraphVertices[u] == 0) {
								subGraphVertices[u] = 1;
								subGraphNbNodes ++;
						}
						
						if (subGraphVertices[v] == 0) {
								subGraphVertices[v] = 1;
								subGraphNbNodes ++;
						}
					}
				}
			}
		}
		
		return new SubMolecule(subGraphNbNodes, nbEdges, molecule.getNbNodes(), subGraphVertices, newGraph);
	}
	
	public static boolean intervalsOnSameLine(Interval i1, Interval i2) {
		return (i1.y1() == i2.y1() && i1.y2() == i2.y2());
	}

	public static List<Integer> getHexagons(Molecule molecule, ArrayList<Integer> cycle, ArrayList<Interval> intervals){
		List<Integer> hexagons = new ArrayList<Integer>();

		for (Interval interval : intervals){

			int [] hexagonsCount = new int [molecule.getNbHexagons()];

			for (int x = interval.x1() ; x <= interval.x2() ; x += 2){
				int u1 = molecule.getCoords().get(x, interval.y1());
				int u2 = molecule.getCoords().get(x, interval.y2());

				for (Integer hexagon : molecule.getHexagonsVertices().get(u1)) {
					hexagonsCount[hexagon] ++;
					if (hexagonsCount[hexagon] == 4)
						hexagons.add(hexagon);
				}

				for (Integer hexagon : molecule.getHexagonsVertices().get(u2)){
					hexagonsCount[hexagon] ++;
					if (hexagonsCount[hexagon] == 4)
						hexagons.add(hexagon);
				}
			}
		}

		return hexagons;
	}

	
	public static String displayCycle(ArrayList<Integer> cycle) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (Integer i : cycle) {
			if (!list.contains(i)) list.add(i);
		}
		Collections.sort(list);
		return list.toString();
	}

	public static void treatCycle(Molecule molecule, ArrayList<Integer> cycle) {
		
		EdgeSet verticalEdges = computeStraightEdges(molecule, cycle);
		ArrayList<Interval> intervals = (ArrayList<Interval>) computeIntervals(molecule, cycle, verticalEdges);
		Collections.sort(intervals);
		int cycleConfiguration = Utils.identifyCycle(molecule, intervals);
		
		if (cycleConfiguration != -1) {
			
			circuitCount[cycleConfiguration] ++;
			
			ArrayList<Integer> hexagons = (ArrayList<Integer>) getHexagons(molecule, cycle, intervals);
			SubMolecule subMolecule = substractCycleAndInterior(molecule, cycle, intervals);
			int nbPerfectMatchings = PerfectMatchingSolver.computeNbPerfectMatching(subMolecule);
			
			int [][] energiesCycle = energies[cycleConfiguration];
			
			if (hexagons.contains(0))
				System.out.print("");
			
			for (int idHexagon = 0 ; idHexagon < hexagons.size() ; idHexagon++) {
				
				int hexagon = hexagons.get(idHexagon);
				for (int size = 0 ; size < 4 ; size ++) {
					
					if (energiesCycle[idHexagon][size] != 0)
						circuits[hexagon][size] += energiesCycle[idHexagon][size] * nbPerfectMatchings;			
				}
			}
			
		}
	}
	 
	public static void computeResonanceEnergyWithSymmetries(Molecule molecule) throws IOException{
		
		energies = Utils.initEnergies();
		circuits = new int[molecule.getNbHexagons()][MAX_CYCLE_SIZE];		
		circuitCount = new int[energies.length];
		
		ArrayList<ArrayList<Integer>> orbits = molecule.getOrbits(nautyDirectory);
		
		for (ArrayList<Integer> orbit : orbits) {
			int hexagon = orbit.get(0);
			computeCyclesRelatedToOneHexagon(molecule, hexagon);
			int [] result = circuits[hexagon];
			
			for (int i = 1 ; i < orbit.size() ; i++) {
				int symmetricHexagon = orbit.get(i);
				circuits[symmetricHexagon] = result;
			}
		}
		
		displayResults();
	}
	
	public static Aromaticity solve(Molecule molecule) throws IOException {
		computeResonanceEnergy(molecule);
		return new Aromaticity(molecule, circuits, RIType.OPTIMIZED);
	}
	
	public static void main(String[] args) throws IOException {
		
		symmetries = false;;
		
		if (args.length < 1) {
			System.err.println("ERROR: invalid argument(s)");
			System.err.println("USAGE: java -jar Approximation.jar ${input_file_name} [-s | --symm]");
			System.exit(1);
		}
		
		path = args[0];
		
		if (args.length > 1 && (args[1].equals("-s") || args[1].equals("--symm")))
			symmetries = true;
			
		nautyDirectory = null;
		
		if (symmetries) {
			
			if (args.length > 2)
				nautyDirectory = args[2];
			else
				nautyDirectory = ".";
				
		}
			
		
		StringBuilder b = new StringBuilder();
		String [] splittedFilename = path.split(Pattern.quote("."));
		
		for (int i = 0 ; i < splittedFilename.length - 1 ; i++) {
			b.append(splittedFilename[i] + ".");
		}
		
		if (symmetries)
			b.append("log_symm");
		else
			b.append("log");
		
		String outputFileName = b.toString();
		
		log = new BufferedWriter(new FileWriter(new File(outputFileName)));
		
		System.out.println("writing on : " + outputFileName);
		
		System.out.println("computing " + path + "\n");
		
		Molecule molecule = GraphParser.parseUndirectedGraph(path, null, false);

		if (!symmetries) {
			long begin = System.currentTimeMillis();
			computeResonanceEnergy(molecule); 
			long end = System.currentTimeMillis();
			long time = end - begin;
			displayResults();
			System.out.println("time : " + time + " ms.");
			log.write("time : " + time + " ms." + "\n");
		}
		
		else {
			long begin = System.currentTimeMillis();
			computeResonanceEnergyWithSymmetries(molecule);
			long end = System.currentTimeMillis();
			long time = end - begin;
			System.out.println("time : " + time + " ms.");
			log.write("time : " + time + " ms." + "\n");
		}
			
		
		log.close();
		
	}
}
