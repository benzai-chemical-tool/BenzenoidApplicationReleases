package solution;

import java.util.List;

import org.chocosolver.solver.Solution;

public class StatistiqueStructure {
	private BenzenoidSolution solution;
	private List<ClarCoverSolution> kekuleCoverSolutions;
	private List<ClarCoverSolution> clarCoverSolutions;
	private int nbKekuleCovers;
	private int nbClarCovers;

	private int [] compteursCycles;
	private int [][] compteursLiaisons;
	private int [][] compteursCelibataires;
	private int [] compteursRonds;
	private int [] compteursRBO;
	private boolean kekuleable;
	private boolean kekuleClarMatching;
	
	public StatistiqueStructure(BenzenoidSolution solution, List<ClarCoverSolution> kekuleCoverSolutions, List<ClarCoverSolution> clarCoverSolutions) {
		super();
		this.solution = solution;
		this.kekuleCoverSolutions = kekuleCoverSolutions;
		this.clarCoverSolutions = clarCoverSolutions;
		this.nbKekuleCovers = kekuleCoverSolutions.size();
		this.nbClarCovers = clarCoverSolutions.size();
		
		int nbCouronnes = solution.getNbCouronnes();
		int taille = (nbCouronnes * 2 - 1) * (nbCouronnes * 2 - 1);
		compteursCycles = new int[taille];
		compteursLiaisons = new int[taille][6];
		compteursCelibataires = new int[taille][6];
		compteursRonds = new int[taille];
		compteursRBO = new int[taille];
	}

	public void getStats() {
		int nbHex = solution.getHexagonGraph().vertexSet().size();
		
		for(int hex : solution.getHexagonGraph().vertexSet()) {
			compteursCycles[hex] = 0;
			compteursRonds[hex] = 0;
			compteursRBO[hex] = 0;
			for(int j = 0; j < 6 ; j++) {
				compteursLiaisons[hex][j] = 0;
				compteursCelibataires[hex][j] = 0;
			}
		}
		
		for(ClarCoverSolution cover : kekuleCoverSolutions) {
			for(int doubleLiaison : cover.getDoublesLiaisons()) {
				compteursLiaisons[doubleLiaison / 6][doubleLiaison % 6] ++;
				compteursRBO[doubleLiaison/6]++;
			}
			for(int celibataire : cover.getCelibataires())
				compteursCelibataires[celibataire / 6][celibataire % 6] ++;
			List<Integer> doublesLiaisons = cover.getDoublesLiaisons();
			for(int hex : solution.getHexagonGraph().vertexSet()) {
				if(doublesLiaisons.contains(6 * hex) && doublesLiaisons.contains(6 * hex + 2) && doublesLiaisons.contains(6 * hex + 4)
						|| doublesLiaisons.contains(6 * hex + 1) && doublesLiaisons.contains(6 * hex + 3) && doublesLiaisons.contains(6 * hex + 5))
					compteursCycles[hex] ++;
			}
		}
		
		for(ClarCoverSolution clarCover : clarCoverSolutions)
			for(int hex : clarCover.getRonds())
				compteursRonds[hex]++;
		
//		for(int hex : solution.getHexagonGraph().vertexSet()) {
//			System.out.println("cycle" + hex + ":" + compteursCycles[hex] + "/" + nbKekuleCovers);
//			for(int j = 0; j < 6 ; j++) {
//				System.out.println("liaison" + hex + "," + j + ":" + compteursLiaisons[hex][j] + " celib" + hex + "," + j + ":" + compteursCelibataires[hex][j]);
//			}
//		}
		
		kekuleable = computeKekuleable();
		kekuleClarMatching = computeKekuleClarMatching();
	}
	
	/***
	 * 
	 * @return
	 */
	private boolean computeKekuleable() {
		for(int hex : solution.getHexagonGraph().vertexSet())
			for(int j = 0; j < 6 ; j++)
				if(compteursCelibataires[hex][j] > 0)
					return false;
		return true;
	}

	/***
	 * 
	 * @return
	 */
	private boolean computeKekuleClarMatching() {
		boolean matching = true;
		for(int hex1 : solution.getHexagonGraph().vertexSet())
			for(int hex2 : solution.getHexagonGraph().vertexSet())
				if(hex1 < hex2 && (compteursCycles[hex1] - compteursCycles[hex2]) * (compteursRonds[hex1] - compteursRonds[hex2]) < 0)
					return false;
		return true;
	}		

	public BenzenoidSolution getSolution() {
		return solution;
	}

	public List<ClarCoverSolution> getKekuleCoverSolutions() {
		return kekuleCoverSolutions;
	}

	public int getNbKekuleCovers() {
		return nbKekuleCovers;
	}

	public int[] getCompteursCycles() {
		return compteursCycles;
	}

	public int[][] getCompteursLiaisons() {
		return compteursLiaisons;
	}

	public int[][] getCompteursCelibataires() {
		return compteursCelibataires;
	}

	public int[] getCompteursRonds() {
		return compteursRonds;
	}

	public int getNbClarCovers() {
		return nbClarCovers;
	}
	
	public int[] getCompteursRBO() {
		return compteursRBO;
	}

	public boolean kekuleMatchesClar() {
		return kekuleClarMatching;
	}

	public boolean isKekuleable() {
		return kekuleable;
	}

}
