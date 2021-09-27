package solveur;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.BoolVar;
import utils.SubMolecule;

public class PerfectMatchingSolver {

	public static int computeNbPerfectMatching(SubMolecule subMolecule) {
		
		Model model = new Model("Kekule's structure");
		
		BoolVar [] edges = new BoolVar[subMolecule.getNbEdges()];
		
		for (int i = 0 ; i < subMolecule.getNbEdges() ; i++)
			edges[i] = model.boolVar("e_" + i);
		
		for (int i = 0 ; i < subMolecule.getNbTotalNodes() ; i++) {
			int degree = subMolecule.getEdgesMatrix().get(i).size();
			
			if (degree > 0) {
				BoolVar [] adjacentEdges = new BoolVar[degree];
				
				int index = 0;
				for (Integer edge : subMolecule.getEdgesMatrix().get(i)) {
					adjacentEdges[index] = edges[edge];
					index ++;
				}
				
				model.sum(adjacentEdges, "=", 1).post();
			}
		}
		
		model.getSolver().setSearch(new IntStrategy(edges, new FirstFail(model), new IntDomainMin()));
		Solver solver = model.getSolver();
		
		int nbSolutions = 0;
		
		while(solver.solve()) {
			Solution solution = new Solution(model);
			solution.record();
			nbSolutions ++;
		}
		
		return nbSolutions;
	}
}
