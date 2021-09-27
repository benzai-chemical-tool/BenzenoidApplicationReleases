package modules;

import java.util.ArrayList;

import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import generator.GeneralModel;
import utils.Utils;
import view.generator.GeneratorCriterion;
import view.generator.GeneratorCriterion.Operator;
import view.generator.GeneratorCriterion.Subject;

public class CoronoidModule2 extends Module {

	private ArrayList<GeneratorCriterion> criterions;

	private final UndirectedGraphVar holes;
	private BoolVar[] holesVertices;
	private BoolVar[][] holesEdges;

	private IntVar nbConnectedComponents;

	private int nbMaxHoles;

	public CoronoidModule2(GeneralModel generalModel, ArrayList<GeneratorCriterion> criterions) {

		super(generalModel);

		this.criterions = criterions;
		holes = holesGraphVar("holes");

	}

	@Override
	public void setPriority() {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildVariables() {

		for (GeneratorCriterion criterion : criterions) {

			Operator operator = criterion.getOperator();

			if (criterion.getSubject() == Subject.NB_HOLES
					&& (operator == Operator.EQ || operator == Operator.LEQ || operator == Operator.LT)) {

				int nbHoles = Integer.parseInt(criterion.getValue());
				if (nbHoles > nbMaxHoles)
					nbMaxHoles = nbHoles;
			}
		}

		if (nbMaxHoles == 0)
			nbMaxHoles = generalModel.getNbMaxHexagons() / 2;

		holesVertices = new BoolVar[generalModel.getDiameter() * generalModel.getDiameter() + 1];
		for (int i = 0; i < generalModel.getDiameter() * generalModel.getDiameter() + 1; i++)
			holesVertices[i] = generalModel.getProblem().boolVar("nodes[" + i + "]");

		holesEdges = new BoolVar[generalModel.getDiameter() * generalModel.getDiameter()
				+ 1][generalModel.getDiameter() * generalModel.getDiameter() + 1];
		for (int i = 0; i < generalModel.getDiameter() * generalModel.getDiameter() + 1; i++)
			for (int j = 0; j < generalModel.getDiameter() * generalModel.getDiameter() + 1; j++)
				holesEdges[i][j] = generalModel.getProblem().boolVar("edges[" + i + "][" + j + "]");

		nbConnectedComponents = generalModel.getProblem().intVar("nb_connected_components", 1, nbMaxHoles + 1);
	}

	@Override
	public void postConstraints() {

		generalModel.getProblem().nodesChanneling(holes, holesVertices).post();
		generalModel.getProblem().neighborsChanneling(holes, holesEdges).post();

		generalModel.getProblem().minDegree(holes, 1).post();
		postBenzenoidXORHoles();

		generalModel.getProblem().nbConnectedComponents(holes, nbConnectedComponents).post();

		generalModel.getProblem().arithm(nbConnectedComponents, ">", 1).post();

		for (GeneratorCriterion criterion : criterions) {

			if (criterion.getSubject() == Subject.NB_HOLES) {
				String operator = criterion.getOperatorString();
				int value = Integer.parseInt(criterion.getValue());

				generalModel.getProblem().arithm(nbConnectedComponents, operator, value + 1).post();

			}
		}

		postHolesExternalFaceConnection();

	}

	@Override
	public void addWatchedVariables() {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeSolvingStrategy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void changeWatchedGraphVertices() {
		// TODO Auto-generated method stub

	}

	private UndirectedGraphVar holesGraphVar(String name) {

		int largeur = generalModel.getDiameter();
		int nbCouronnes = generalModel.getNbCrowns();
		int faceExterne = largeur * largeur;

		UndirectedGraph GUB = new UndirectedGraph(generalModel.getProblem(), largeur * largeur + 1, SetType.BITSET,
				false);
		UndirectedGraph GLB = new UndirectedGraph(generalModel.getProblem(), largeur * largeur + 1, SetType.BITSET,
				false);

		int i, j;
		for (j = 0; j < nbCouronnes; j++) {
			for (i = 0; i < nbCouronnes; i++) {
				GUB.addNode(Utils.getHexagonID(i, j, generalModel.getDiameter()));
				GUB.addNode(Utils.getHexagonID(nbCouronnes - 1 + i, nbCouronnes - 1 + j, generalModel.getDiameter()));
			}
		}
		for (j = 0; j < nbCouronnes - 2; j++) {
			for (i = 0; i < j + 1; i++) {
				GUB.addNode(Utils.getHexagonID(nbCouronnes - 1 + i + 1, j + 1, generalModel.getDiameter()));
			}
		}
		for (j = 0; j < nbCouronnes - 1; j++) {
			for (i = j; i < nbCouronnes - 2; i++) {
				GUB.addNode(Utils.getHexagonID(i + 1, nbCouronnes - 1 + j + 1, generalModel.getDiameter()));
			}
		}

		// Sommet de la face externe
		GUB.addNode(faceExterne);
		GLB.addNode(faceExterne);

		// Ajout des arêtes du coronénoïde englobant
//		System.out.println(GUB.getNodes());
		for (j = 0; j < nbCouronnes - 1; j++) {
			for (i = 0; i < nbCouronnes - 1; i++) {
				GUB.addEdge(Utils.getHexagonID(i, j, generalModel.getDiameter()),
						Utils.getHexagonID(i + 1, j, generalModel.getDiameter()));
				GUB.addEdge(Utils.getHexagonID(i, j, generalModel.getDiameter()),
						Utils.getHexagonID(i, j + 1, generalModel.getDiameter()));
				GUB.addEdge(Utils.getHexagonID(i, j, generalModel.getDiameter()),
						Utils.getHexagonID(i + 1, j + 1, generalModel.getDiameter()));
				GUB.addEdge(Utils.getHexagonID(nbCouronnes - 1 + i, nbCouronnes - 1 + j, generalModel.getDiameter()),
						Utils.getHexagonID(nbCouronnes - 1 + i + 1, nbCouronnes - 1 + j, generalModel.getDiameter()));
				GUB.addEdge(Utils.getHexagonID(nbCouronnes - 1 + i, nbCouronnes - 1 + j, generalModel.getDiameter()),
						Utils.getHexagonID(nbCouronnes - 1 + i, nbCouronnes - 1 + j + 1, generalModel.getDiameter()));
				GUB.addEdge(Utils.getHexagonID(nbCouronnes - 1 + i, nbCouronnes - 1 + j, generalModel.getDiameter()),
						Utils.getHexagonID(nbCouronnes - 1 + i + 1, nbCouronnes - 1 + j + 1,
								generalModel.getDiameter()));
			}
		}
		for (j = 0; j < nbCouronnes - 1; j++) {
			for (i = 0; i < j + 1; i++) {
				GUB.addEdge(Utils.getHexagonID(nbCouronnes - 1 + i, j, generalModel.getDiameter()),
						Utils.getHexagonID(nbCouronnes - 1 + i, j + 1, generalModel.getDiameter()));
				GUB.addEdge(Utils.getHexagonID(nbCouronnes - 1 + i, j, generalModel.getDiameter()),
						Utils.getHexagonID(nbCouronnes - 1 + i + 1, j + 1, generalModel.getDiameter()));
				GUB.addEdge(Utils.getHexagonID(nbCouronnes - 1 + i, j + 1, generalModel.getDiameter()),
						Utils.getHexagonID(nbCouronnes - 1 + i + 1, j + 1, generalModel.getDiameter()));
			}
		}
		for (j = 0; j < nbCouronnes - 1; j++) {
			for (i = j; i < nbCouronnes - 1; i++) {
				GUB.addEdge(Utils.getHexagonID(i, nbCouronnes - 1 + j, generalModel.getDiameter()),
						Utils.getHexagonID(i + 1, nbCouronnes - 1 + j, generalModel.getDiameter()));
				GUB.addEdge(Utils.getHexagonID(i, nbCouronnes - 1 + j, generalModel.getDiameter()),
						Utils.getHexagonID(i + 1, nbCouronnes - 1 + j + 1, generalModel.getDiameter()));
				GUB.addEdge(Utils.getHexagonID(i + 1, nbCouronnes - 1 + j, generalModel.getDiameter()),
						Utils.getHexagonID(i + 1, nbCouronnes - 1 + j + 1, generalModel.getDiameter()));
			}
		}
		for (j = 0; j < nbCouronnes - 1; j++) {
			GUB.addEdge(Utils.getHexagonID(largeur - 1, nbCouronnes - 1 + j, generalModel.getDiameter()),
					Utils.getHexagonID(largeur - 1, nbCouronnes + j, generalModel.getDiameter()));
			GUB.addEdge(Utils.getHexagonID(nbCouronnes - 1 + j, largeur - 1, generalModel.getDiameter()),
					Utils.getHexagonID(nbCouronnes + j, largeur - 1, generalModel.getDiameter()));
		}

		// arêtes avec la face externe
		for (i = 0; i < nbCouronnes - 1; i++) // haut
			GUB.addEdge(Utils.getHexagonID(i, 0, generalModel.getDiameter()), faceExterne);
		for (i = 0; i < nbCouronnes - 1; i++) // haut droit
			GUB.addEdge(Utils.getHexagonID(i + nbCouronnes - 1, i, generalModel.getDiameter()), faceExterne);
		for (i = 0; i < nbCouronnes - 1; i++) // bas droit
			GUB.addEdge(Utils.getHexagonID(largeur - 1, nbCouronnes - 1 + i, generalModel.getDiameter()), faceExterne);
		for (i = 0; i < nbCouronnes - 1; i++) // bas
			GUB.addEdge(Utils.getHexagonID(nbCouronnes + i, largeur - 1, generalModel.getDiameter()), faceExterne);
		for (i = 0; i < nbCouronnes - 1; i++) // bas gauche
			GUB.addEdge(Utils.getHexagonID(1 + i, nbCouronnes + i, generalModel.getDiameter()), faceExterne);
		for (i = 0; i < nbCouronnes - 1; i++) // haut gauche
			GUB.addEdge(Utils.getHexagonID(0, 1 + i, generalModel.getDiameter()), faceExterne);

		return generalModel.getProblem().nodeInducedGraphVar(name, GLB, GUB);

	}

	private void postBenzenoidXORHoles() {
		BoolVar[] benzenoidVertices = generalModel.getWatchedGraphVertices();
		for (int i = 0; i < benzenoidVertices.length; i++)
			if (inCoronenoid(i)) {
				generalModel.getProblem().addClauses(LogOp.xor(benzenoidVertices[i], holesVertices[i]));
			}
	}

	public boolean inCoronenoid(int x, int y) {
		int largeur = generalModel.getDiameter();
		int nbCouronnes = generalModel.getNbCrowns();
		if (x < 0 || y < 0 || x >= largeur || y >= largeur)
			return false;
		if (y < nbCouronnes)
			return x < nbCouronnes + y;
		else
			return x > y - nbCouronnes;
	}

	public boolean inCoronenoid(int i) {
		int largeur = generalModel.getDiameter();
		return inCoronenoid(i % largeur, i / largeur);
	}

	private void postHolesExternalFaceConnection() {

		int largeur = generalModel.getDiameter();
		int nbCouronnes = generalModel.getNbCrowns();
		int faceExterne = largeur * largeur;
		int i;

		for (i = 0; i < nbCouronnes - 1; i++) // haut
			generalModel.getProblem().addClauses(LogOp.implies(holesVertices[Utils.getHexagonID(i, 0, largeur)],
					holesEdges[Utils.getHexagonID(i, 0, largeur)][faceExterne]));
		for (i = 0; i < nbCouronnes - 1; i++) // haut droit
			generalModel.getProblem()
					.addClauses(LogOp.implies(holesVertices[Utils.getHexagonID(i + nbCouronnes - 1, i, largeur)],
							holesEdges[Utils.getHexagonID(i + nbCouronnes - 1, i, largeur)][faceExterne]));
		for (i = 0; i < nbCouronnes - 1; i++) // bas droit
			generalModel.getProblem()
					.addClauses(LogOp.implies(
							holesVertices[Utils.getHexagonID(largeur - 1, nbCouronnes - 1 + i, largeur)],
							holesEdges[Utils.getHexagonID(largeur - 1, nbCouronnes - 1 + i, largeur)][faceExterne]));
		for (i = 0; i < nbCouronnes - 1; i++) // bas
			generalModel.getProblem()
					.addClauses(LogOp.implies(holesVertices[Utils.getHexagonID(nbCouronnes + i, largeur - 1, largeur)],
							holesEdges[Utils.getHexagonID(nbCouronnes + i, largeur - 1, largeur)][faceExterne]));
		for (i = 0; i < nbCouronnes - 1; i++) // bas gauche
			generalModel.getProblem()
					.addClauses(LogOp.implies(holesVertices[Utils.getHexagonID(1 + i, nbCouronnes + i, largeur)],
							holesEdges[Utils.getHexagonID(1 + i, nbCouronnes + i, largeur)][faceExterne]));
		for (i = 0; i < nbCouronnes - 1; i++) // haut gauche
			generalModel.getProblem().addClauses(LogOp.implies(holesVertices[Utils.getHexagonID(0, 1 + i, largeur)],
					holesEdges[Utils.getHexagonID(0, 1 + i, largeur)][faceExterne]));
	}
}
