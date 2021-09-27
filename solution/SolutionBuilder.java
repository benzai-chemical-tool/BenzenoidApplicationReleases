package solution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import generator.GeneralModel;
import generator.ResultSolver;
import generator.fragments.Fragment;
import generator.fragments.FragmentOccurences;
import molecules.Molecule;
import molecules.Node;
import parsers.GraphParser;
import view.generator.GeneratorCriterion;
import view.generator.GeneratorCriterion.Operator;
import view.generator.GeneratorCriterion.Subject;

public class SolutionBuilder {

	public static Fragment buildFragment(Molecule molecule) {
		
		int nbHexagons = molecule.getNbHexagons();
		int [][] dualGraph = molecule.getDualGraph();
		
		/*
		 * Nodes
		 */
		
		Node [] nodes = new Node[nbHexagons];
		nodes[0] = new Node(0, 0, 0);
		
		int [] treatedHexagons = new int[nbHexagons];
		treatedHexagons[0] = 1;
		
		ArrayList<Integer> candidats = new ArrayList<>();
		candidats.add(0);
		
		while(candidats.size() > 0) {
			
			int candidat = candidats.get(0);
			
			for (int i = 0 ; i < 6 ; i++) {
				
				int neighbor = dualGraph[candidat][i];
				if (neighbor != -1 && treatedHexagons[neighbor] == 0) {
				
					treatedHexagons[neighbor] = 1;
					
					int x = nodes[candidat].getX();
					int y = nodes[candidat].getY();
					
					if (i == 0)
						nodes[neighbor] = new Node(x, y - 1, neighbor);
						
					else if (i == 1)
						nodes[neighbor] = new Node(x + 1, y, neighbor);
						
					else if (i == 2)
						nodes[neighbor] = new Node(x + 1, y + 1, neighbor);
						
					else if (i == 3)
						nodes[neighbor] = new Node(x, y + 1, neighbor);
						
					else if (i == 4)
						nodes[neighbor] = new Node(x - 1, y, neighbor);
					
					else 
						nodes[neighbor] = new Node(x - 1, y - 1, neighbor);
					
					candidats.add(neighbor);
					
				}
			}
			
			candidats.remove(candidats.get(0));
		}
		
		/*
		 * Matrix
		 */
		
		int [][] matrix = new int[nbHexagons][nbHexagons];
		
		for (int i = 0 ; i < nbHexagons ; i++) {
			for (int j = 0 ; j < 6 ; j ++) {
				
				int u = dualGraph[i][j];
				if (u != -1) {
					matrix[i][u] = 1;
					matrix[u][i] = 1;
				}
			}
		}
		
		/*
		 * Neighbor graph
		 */
		
		int [][] neighbors = dualGraph;
		
		/*
		 * Labels
		 */
		
		int [] labels = new int[nbHexagons];
		
		for (int i = 0 ; i < nbHexagons ; i++)
			labels[i] = 2;
		
		
		return new Fragment(matrix, labels, nodes, null, null, neighbors, 0);
		
	}
	
	private static GeneralModel buildModel(Molecule molecule) {
		
		Map<String, ArrayList<GeneratorCriterion>> map = new HashMap<>();

		map.put("hexagons", new ArrayList<>());
		map.put("carbons_hydrogens", new ArrayList<>());
		map.put("irregularity", new ArrayList<>());
		map.put("diameter", new ArrayList<>());
		map.put("rectangle", new ArrayList<>());
		map.put("rhombus", new ArrayList<>());
		map.put("coronoid", new ArrayList<>());
		map.put("coronoid2", new ArrayList<>());
		map.put("catacondensed", new ArrayList<>());
		map.put("symmetries", new ArrayList<>());
		map.put("patterns", new ArrayList<>());
		
		map.get("hexagons").add(new GeneratorCriterion(Subject.NB_HEXAGONS, Operator.EQ, Integer.toString(molecule.getNbHexagons())));
		
		return new GeneralModel(map.get("hexagons"), false);
	}
	
	public static BenzenoidSolution buildBenzenoidSolution(Molecule molecule) {
		
		Fragment fragment = buildFragment(molecule);
		ArrayList<Fragment> rotations = fragment.computeRotations();
		GeneralModel model = buildModel(molecule);
		
		FragmentOccurences occurence = null;
		for (Fragment fragmentRotation : rotations) {
			
			occurence = model.computeTranslationsBorders(fragmentRotation.getNodesRefs(), fragmentRotation.getNeighborGraph(), new FragmentOccurences(), true);
			if (occurence.size() > 0) 
				break;
		}
		
		Integer [] vertices = occurence.getOccurences().get(0);
		
		for (Integer u : vertices)
			model.getProblem().arithm(model.getWatchedGraphVertices()[u], "=", 1).post();
		
		ResultSolver result = model.solve();
		
		if (result.size() > 0) 
			return result.getSolutions().get(0);
		
		else	
			return null;
	}
	
	public static void main(String [] args) {
		
		Molecule molecule = GraphParser.parseUndirectedGraph("/home/adrien/Bureau/catacondensed_6/6_hexagons_1.graph_coord", null, false);
		BenzenoidSolution solution = buildBenzenoidSolution(molecule);
	}
}
