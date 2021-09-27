package solution.outpout;

import solution.BenzenoidSolution;
import solution.StatistiqueStructure;

public class StatCoverOutput implements SolutionOutput {

	private StatistiqueStructure stats;

	public StatCoverOutput(StatistiqueStructure stats) {
		super();
		this.stats = stats;
	}

	@Override
	public void output(BenzenoidSolution benzenoidSolution) {
		int taille = benzenoidSolution.getNbCouronnes() * 2 - 1;
		System.out.println(benzenoidSolution.getName());
		System.out.print("Hex (x, y) : ");
		stats.getSolution().getHexagonGraph().vertexSet().forEach((hex) -> System.out.print(hex + " (" + (hex % taille) + ", " + (hex / taille) + ") ; "));
		System.out.println();
		
		System.out.println(benzenoidSolution.getCarbonGraph());
		

		int nbClarCovers = stats.getNbClarCovers();
		System.out.println("# Structures de Clar : " + nbClarCovers);
		for(int hex : stats.getSolution().getHexagonGraph().vertexSet())
			if(stats.getCompteursRonds()[hex] > 0)
				System.out.print("Rond[" + hex + "]= " + (stats.getCompteursRonds()[hex] * 100 / nbClarCovers) + "% ; ");
		System.out.println();
			
		int nbKekuleCovers = stats.getNbKekuleCovers();
		System.out.println("# Structures de Kekule : " + nbKekuleCovers);

		for(int hex : stats.getSolution().getHexagonGraph().vertexSet())
			if(stats.getCompteursCycles()[hex] > 0)
				System.out.print("Cycle[" + hex + "]= " + (stats.getCompteursCycles()[hex] * 100 / nbKekuleCovers) + "% ; ");
		System.out.println();
		
		System.out.println("Clar matches Kekule : " + stats.kekuleMatchesClar());
		
		for(int hex : stats.getSolution().getHexagonGraph().vertexSet())
			for(int j = 0; j < 6; j++) 
				if(stats.getCompteursLiaisons()[hex][j] > 0)
					System.out.print("Liaison[" + hex + "][" + j + "] = " + (stats.getCompteursLiaisons()[hex][j]  * 100 / nbKekuleCovers) + "% ; ");
		System.out.println();

		for(int hex : stats.getSolution().getHexagonGraph().vertexSet())
			for(int j = 0; j < 6; j++) 
				if(stats.getCompteursCelibataires()[hex][j] > 0)
					System.out.print("Celibataire[" + hex + "][" + j + "] = " + (stats.getCompteursCelibataires()[hex][j]  * 100 / nbKekuleCovers) + "% ; ");
		System.out.println();
		
	}

}
