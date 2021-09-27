package molecules;

import static java.lang.ProcessBuilder.Redirect.appendTo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import generator.GeneralModel;
import generator.ResultSolver;
import solution.BenzenoidSolution;
import solution.GraphConversion;
import utils.Couple;
import utils.Interval;
import utils.RelativeMatrix;
import view.generator.GeneratorCriterion;
import view.generator.GeneratorCriterion.Operator;
import view.generator.GeneratorCriterion.Subject;

public class Molecule {

	private RelativeMatrix nodesMem; // DEBUG

	private int nbNodes, nbEdges, nbHexagons, nbStraightEdges, maxIndex;
	private ArrayList<ArrayList<Integer>> edgeMatrix;
	private int[][] adjacencyMatrix;
	private ArrayList<String> edgesString;
	private ArrayList<String> hexagonsString;
	private Node[] nodesRefs;
	private RelativeMatrix coords;
	private int[][] hexagons;
	private int[][] dualGraph;
	private int[] degrees;

	private ArrayList<ArrayList<Integer>> hexagonsVertices;

	private int nbHydrogens;

	private String name;

	private String description;

	private ArrayList<Integer> verticesSolutions;

	private Couple<Integer, Integer>[] hexagonsCoords;

	/**
	 * Constructors
	 */

	public Molecule(int nbNodes, int nbEdges, int nbHexagons, int[][] hexagons, Node[] nodesRefs,
			int[][] adjacencyMatrix, RelativeMatrix coords) {

		this.nbNodes = nbNodes;
		this.nbEdges = nbEdges;
		this.nbHexagons = nbHexagons;
		this.hexagons = hexagons;
		this.nodesRefs = nodesRefs;
		this.adjacencyMatrix = adjacencyMatrix;
		this.coords = coords;

		hexagonsString = new ArrayList<>();

		for (int[] hexagon : hexagons) {

			StringBuilder builder = new StringBuilder();

			builder.append("h ");

			for (int u : hexagon) {
				Node node = nodesRefs[u];
				builder.append(node.getX() + "_" + node.getY() + " ");
			}

			hexagonsString.add(builder.toString());
		}

		initHexagons();
		computeDualGraph();
		computeDegrees();
		buildHexagonsCoords();
	}

	public Molecule(int nbNodes, int nbEdges, int nbHexagons, ArrayList<ArrayList<Integer>> edgeMatrix,
			int[][] adjacencyMatrix, ArrayList<String> edgesString, ArrayList<String> hexagonsString, Node[] nodesRefs,
			RelativeMatrix coords) {

		this.nbNodes = nbNodes;
		this.nbEdges = nbEdges;
		this.nbHexagons = nbHexagons;
		this.edgeMatrix = edgeMatrix;
		this.adjacencyMatrix = adjacencyMatrix;
		this.edgesString = edgesString;
		this.hexagonsString = hexagonsString;
		this.nodesRefs = nodesRefs;
		this.coords = coords;

		hexagons = new int[nbHexagons][6];
		initHexagons();

		computeDualGraph();
		computeDegrees();
		buildHexagonsCoords();
	}

	public Molecule(int nbNodes, int nbEdges, int nbHexagons, ArrayList<ArrayList<Integer>> edgeMatrix,
			int[][] adjacencyMatrix, ArrayList<String> edgesString, ArrayList<String> hexagonsString, Node[] nodesRefs,
			RelativeMatrix coords, RelativeMatrix nodesMem, int maxIndex) {

		this.nbNodes = nbNodes;
		this.nbEdges = nbEdges;
		this.nbHexagons = nbHexagons;
		this.edgeMatrix = edgeMatrix;
		this.adjacencyMatrix = adjacencyMatrix;
		this.edgesString = edgesString;
		this.hexagonsString = hexagonsString;
		this.nodesRefs = nodesRefs;
		this.coords = coords;
		this.nodesMem = nodesMem;
		this.maxIndex = maxIndex;

		hexagons = new int[nbHexagons][6];
		initHexagons();

		nbStraightEdges = 0;

		for (int i = 0; i < adjacencyMatrix.length; i++) {
			for (int j = (i + 1); j < adjacencyMatrix[i].length; j++) {
				if (adjacencyMatrix[i][j] == 1) {
					Node u1 = nodesRefs[i];
					Node u2 = nodesRefs[j];

					if (u1.getX() == u2.getX())
						nbStraightEdges++;
				}
			}
		}

		computeDualGraph();
		computeDegrees();
		buildHexagonsCoords();
	}

	/**
	 * Getters and setters
	 */

	public int[][] getDualGraph() {
		return dualGraph;
	}

	public int getNbNodes() {
		return nbNodes;
	}

	public int getNbEdges() {
		return nbEdges;
	}

	public int getNbHexagons() {
		return nbHexagons;
	}

	public int getMaxIndex() {
		return maxIndex;
	}

	public ArrayList<ArrayList<Integer>> getEdgeMatrix() {
		return edgeMatrix;
	}

	public int[][] getAdjacencyMatrix() {
		return adjacencyMatrix;
	}

	public ArrayList<String> getEdgesString() {
		return edgesString;
	}

	public ArrayList<String> getHexagonsString() {
		return hexagonsString;
	}

	public Node getNodeRef(int index) {
		return nodesRefs[index];
	}

	public RelativeMatrix getCoords() {
		return coords;
	}

	public Node[] getNodesRefs() {
		return nodesRefs;
	}

	public ArrayList<ArrayList<Integer>> getHexagonsVertices() {
		return hexagonsVertices;
	}

	public int getNbStraightEdges() {
		return nbStraightEdges;
	}

	public int[][] getHexagons() {
		return hexagons;
	}

	public int degree(int u) {
		return degrees[u];
	}

	/**
	 * Class's methods
	 */

	public SimpleGraph<Integer, DefaultEdge> getCarbonGraph() {
		return GraphConversion.buildCarbonGraph(this);
	}
	
	public SimpleGraph<Integer, DefaultEdge> getHexagonGraph() {
		return GraphConversion.buildHexagonGraph(this);
	}
	
	private void computeDegrees() {

		degrees = new int[nbNodes];

		for (int i = 0; i < nbNodes; i++) {

			int degree = 0;
			for (int j = 0; j < nbNodes; j++) {

				if (adjacencyMatrix[i][j] == 1)
					degree++;
			}

			degrees[i] = degree;
		}
	}

	private void computeDualGraph() {

		dualGraph = new int[nbHexagons][6];

		for (int i = 0; i < nbHexagons; i++)
			for (int j = 0; j < 6; j++)
				dualGraph[i][j] = -1;

		ArrayList<Integer> candidats = new ArrayList<Integer>();
		candidats.add(0);

		int index = 0;

		while (index < nbHexagons) {

			int candidat = candidats.get(index);
			int[] candidatHexagon = hexagons[candidat];

			for (int i = 0; i < candidatHexagon.length; i++) {

				int u = candidatHexagon[i];
				int v = candidatHexagon[(i + 1) % 6];

				System.out.print("");

				for (int j = 0; j < nbHexagons; j++) {
					if (j != candidat) { // j != i avant

						int contains = 0;
						for (int k = 0; k < 6; k++) {
							if (hexagons[j][k] == u || hexagons[j][k] == v)
								contains++;
						}

						if (contains == 2) {

							dualGraph[candidat][i] = j;

							if (!candidats.contains(j))
								candidats.add(j);

							break;
						}
					}
				}

			}
			index++;
		}
	}

	public void exportToGraphviz(String outputFileName) {
		// COMPIL: dot -Kfdp -n -Tpng -o test.png test
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(new File(outputFileName)));

			w.write("graph{" + "\n");

			for (int i = 1; i <= nodesRefs.length; i++) {
				w.write("\t" + i + " [pos=\"" + nodesRefs[(i - 1)].getX() + "," + nodesRefs[(i - 1)].getY() + "!\"]"
						+ "\n");
			}

			w.write("\n");

			for (int i = 0; i < adjacencyMatrix.length; i++) {
				for (int j = i + 1; j < adjacencyMatrix[i].length; j++) {

					if (adjacencyMatrix[i][j] != 0)
						w.write("\t" + (i) + " -- " + (j) + "\n");

				}
			}

			w.write("}");

			w.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RelativeMatrix getNodesMem() {
		return nodesMem;
	}

	private int findHexagon(int u, int v) {

		for (int i = 0; i < nbHexagons; i++) {
			int[] hexagon = hexagons[i];

			if (hexagon[4] == u && hexagon[5] == v)
				return i;
		}

		return -1;

	}

	private ArrayList<Integer> findHexagons(int hexagon, Interval interval) {

		ArrayList<Integer> hexagons = new ArrayList<Integer>();
		int size = interval.size() / 2;

		hexagons.add(hexagon);

		int newHexagon = hexagon;

		for (int i = 0; i < size; i++) {

			newHexagon = dualGraph[newHexagon][1];
			hexagons.add(newHexagon);
		}

		return hexagons;

	}

	public ArrayList<Integer> getAllHexagonsOfIntervals(ArrayList<Interval> intervals) {

		ArrayList<Integer> hexagons = new ArrayList<Integer>();

		for (Interval interval : intervals) {

			int hexagon = findHexagon(interval.x1(), interval.y1());
			hexagons.addAll(findHexagons(hexagon, interval));
		}

		return hexagons;
	}

	public void initHexagons() {

		hexagonsVertices = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < nbNodes; i++)
			hexagonsVertices.add(new ArrayList<Integer>());

		for (int i = 0; i < nbHexagons; i++) {
			String hexagon = hexagonsString.get(i);
			String[] sHexagon = hexagon.split(" ");

			for (int j = 1; j < sHexagon.length; j++) {
				String[] sVertex = sHexagon[j].split(Pattern.quote("_"));
				int x = Integer.parseInt(sVertex[0]);
				int y = Integer.parseInt(sVertex[1]);
				hexagons[i][j - 1] = coords.get(x, y);
				hexagonsVertices.get(coords.get(x, y)).add(i);
			}
		}
	}

	public int getNbHydrogens() {

		if (nbHydrogens == 0) {

			for (int i = 0; i < nbNodes; i++) {

				int degree = 0;
				for (int j = 0; j < nbNodes; j++) {

					if (adjacencyMatrix[i][j] == 1)
						degree++;
				}

				if (degree == 2)
					nbHydrogens++;
			}
		}

		return nbHydrogens;
	}

	public ArrayList<ArrayList<Integer>> getOrbits(String nautyDirectory) throws IOException {

		String tmpFilename = nautyDirectory + "/" + "tmp_nauty";
		String outputFilename = nautyDirectory + "/" + "output";

		System.out.println("tmpFilename : " + tmpFilename);
		System.out.println("outputFilename : " + outputFilename);
		System.out.println("");

		exportToNautyScript(tmpFilename);

		System.out.println("running : " + nautyDirectory + "/nauty26r12/dreadnaut");

		ProcessBuilder pb = new ProcessBuilder(nautyDirectory + "/nauty26r12/dreadnaut");
		pb.redirectInput(new File(tmpFilename));
		pb.redirectOutput(appendTo(new File(outputFilename)));
		Process p = pb.start();

		while (p.isAlive()) {
		}

		BufferedReader r = new BufferedReader(new FileReader(new File(outputFilename)));
		StringBuilder output = new StringBuilder();
		String line = null;
		boolean add = false;

		while ((line = r.readLine()) != null) {
			String[] splittedLine = line.split(" ");
			if (add)
				output.append(line);
			else if (splittedLine.length > 0 && splittedLine[0].equals("cpu"))
				add = true;
		}

		r.close();

		String orbitsStr = output.toString().trim().replaceAll(" +", " ");

		System.out.println("orbitsStr : " + orbitsStr);

		ProcessBuilder rm = new ProcessBuilder("rm", tmpFilename, outputFilename);
		Process prm = rm.start();

		while (prm.isAlive()) {
		}

		ArrayList<ArrayList<Integer>> orbits = new ArrayList<ArrayList<Integer>>();
		String[] splittedOrbits = orbitsStr.split(Pattern.quote("; "));

		for (int i = 0; i < splittedOrbits.length; i++) {

			String orbitStr;
			if (i < splittedOrbits.length - 1)
				orbitStr = splittedOrbits[i];
			else
				orbitStr = splittedOrbits[i].substring(0, splittedOrbits[i].length() - 1);

			String[] splittedOrbit = orbitStr.split(" ");

			ArrayList<Integer> orbit = new ArrayList<Integer>();

			if (splittedOrbit.length == 1)
				orbit.add(Integer.parseInt(splittedOrbit[0]));
			else {
				for (int j = 0; j < splittedOrbit.length - 1; j++) {
					String[] testSplit = splittedOrbit[j].split(Pattern.quote(":"));

					if (testSplit.length == 1) {
						orbit.add(Integer.parseInt(splittedOrbit[j]));
					}

					else {
						for (int k = Integer.parseInt(testSplit[0]); k <= Integer.parseInt(testSplit[1]); k++)
							orbit.add(k);
					}
				}
			}

			orbits.add(orbit);

		}

		return orbits;
	}

	public void exportToNautyScript(String outputFilename) throws IOException {

		BufferedWriter w = new BufferedWriter(new FileWriter(new File(outputFilename)));

		w.write("n = " + nbHexagons + "\n");
		w.write("g" + "\n");

		ArrayList<ArrayList<Integer>> neighbors = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < dualGraph.length; i++)
			neighbors.add(new ArrayList<Integer>());

		for (int i = 0; i < dualGraph.length; i++) {
			for (int j = 0; j < dualGraph[i].length; j++) {
				int v = dualGraph[i][j];
				if (v != -1) {
					if (!neighbors.get(v).contains(i))
						neighbors.get(i).add(v);
				}
			}
		}

		for (int i = 0; i < dualGraph.length; i++) {
			w.write(i + " : ");
			for (Integer u : neighbors.get(i)) {
				w.write(u + " ");
			}
			w.write(";\n");
		}

		w.write("x\n");
		w.write("o\n");

		w.close();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString() {

		if (name == null) {

			int nbCrowns = (int) Math.floor((((double) ((double) nbHexagons + 1)) / 2.0) + 1.0);

			if (nbHexagons % 2 == 1)
				nbCrowns--;

			int diameter = (2 * nbCrowns) - 1;

			int[][] coordsMatrix = new int[diameter][diameter];

			/*
			 * Building coords matrix
			 */

			for (int i = 0; i < diameter; i++) {
				for (int j = 0; j < diameter; j++) {
					coordsMatrix[i][j] = -1;
				}
			}

			for (int i = 0; i < diameter; i++)
				for (int j = 0; j < diameter; j++)
					coordsMatrix[i][j] = -1;

			int index = 0;
			int m = (diameter - 1) / 2;

			int shift = diameter - nbCrowns;

			for (int i = 0; i < m; i++) {

				for (int j = 0; j < diameter - shift; j++) {
					coordsMatrix[i][j] = index;
					index++;
				}

				for (int j = diameter - shift; j < diameter; j++)
					index++;

				shift--;
			}

			for (int j = 0; j < diameter; j++) {
				coordsMatrix[m][j] = index;
				index++;
			}

			shift = 1;

			for (int i = m + 1; i < diameter; i++) {

				for (int j = 0; j < shift; j++)
					index++;

				for (int j = shift; j < diameter; j++) {
					coordsMatrix[i][j] = index;
					index++;
				}

				shift++;
			}

			Couple<Integer, Integer>[] hexagons = new Couple[nbHexagons];

			ArrayList<Integer> candidats = new ArrayList<Integer>();
			candidats.add(0);

			hexagons[0] = new Couple<Integer, Integer>(0, 0);

			int[] checkedHexagons = new int[nbHexagons];
			checkedHexagons[0] = 1;

			while (candidats.size() > 0) {

				int candidat = candidats.get(0);

				int x1 = hexagons[candidat].getX();
				int y1 = hexagons[candidat].getY();

				for (int i = 0; i < 6; i++) {

					int neighbor = dualGraph[candidat][i];

					if (neighbor != -1) {
						if (checkedHexagons[neighbor] == 0) {

							int x2, y2;

							if (i == 0) {
								x2 = x1;
								y2 = y1 - 1;
							}

							else if (i == 1) {
								x2 = x1 + 1;
								y2 = y1;
							}

							else if (i == 2) {
								x2 = x1 + 1;
								y2 = y1 + 1;
							}

							else if (i == 3) {
								x2 = x1;
								y2 = y1 + 1;
							}

							else if (i == 4) {
								x2 = x1 - 1;
								y2 = y1;
							}

							else {
								x2 = x1 - 1;
								y2 = y1 - 1;
							}

							hexagons[neighbor] = new Couple<Integer, Integer>(x2, y2);
							candidats.add(neighbor);
							checkedHexagons[neighbor] = 1;
						}
					}
				}

				candidats.remove(candidats.get(0));
			}

			/*
			 * Trouver une mani�re de le faire rentrer dans le coron�no�de
			 */

			StringBuilder code = new StringBuilder();

			for (int i = 0; i < diameter; i++) {
				for (int j = 0; j < diameter; j++) {

					int h = coordsMatrix[i][j];

					if (h != -1) {

						boolean ok = true;

						Couple<Integer, Integer>[] newHexagons = new Couple[hexagons.length];

						for (int k = 0; k < hexagons.length; k++) {

							Couple<Integer, Integer> hexagon = hexagons[k];

							int xh = hexagon.getY() + i;
							int yh = hexagon.getX() + j;

							if (xh >= 0 && xh < diameter && yh >= 0 && yh < diameter && coordsMatrix[xh][yh] != -1) {
								newHexagons[k] = new Couple<>(xh, yh);
							}

							else {
								ok = false;
								break;
							}
						}

						if (ok) {

							for (int k = 0; k < newHexagons.length; k++) {
								code.append(coordsMatrix[newHexagons[k].getX()][newHexagons[k].getY()]);

								if (k < newHexagons.length - 1)
									code.append("-");
							}

							name = code.toString();
							return code.toString();

						}

					}
				}
			}

			name = null;
			return null;
		}

		else
			return name;
	}

	public void exportToCML(File file) {

	}

	public void exportToGraphFile(File file) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(file));

		writer.write("p DIMACS " + nbNodes + " " + nbEdges + " " + nbHexagons + "\n");

		for (int i = 0; i < nbNodes; i++) {
			for (int j = (i + 1); j < nbNodes; j++) {
				if (adjacencyMatrix[i][j] == 1) {

					Node u = nodesRefs[i];
					Node v = nodesRefs[j];

					writer.write("e " + u.getX() + "_" + u.getY() + " " + v.getX() + "_" + v.getY() + "\n");
				}
			}
		}

		for (int i = 0; i < nbHexagons; i++) {

			int[] hexagon = hexagons[i];
			writer.write("h ");

			for (int j = 0; j < 6; j++) {

				Node u = nodesRefs[hexagon[j]];
				writer.write(u.getX() + "_" + u.getY() + " ");
			}

			writer.write("\n");
		}

		writer.close();
	}

	public BenzenoidSolution buildBenzenoidSolution() {

		ArrayList<GeneratorCriterion> criterions = new ArrayList<>();
		criterions.add(new GeneratorCriterion(Subject.NB_HEXAGONS, Operator.EQ, Integer.toString(nbHexagons)));

		String name = toString();

		String[] split = name.split(Pattern.quote("-"));

		GeneralModel model = new GeneralModel(criterions, criterions, null);

		for (String s : split) {

			int u = Integer.parseInt(s);
			model.getProblem().arithm(model.getVG()[u], "=", 1).post();
		}

		ResultSolver result = model.solve();

		return result.getSolutions().get(0);
	}

	public void setVerticesSolutions(ArrayList<Integer> verticesSolutions) {
		this.verticesSolutions = verticesSolutions;
	}

	public ArrayList<Integer> getVerticesSolutions() {
		return verticesSolutions;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void buildHexagonsCoords() {

		hexagonsCoords = new Couple[nbHexagons];

		int[] checkedHexagons = new int[nbHexagons];
		checkedHexagons[0] = 1;

		ArrayList<Integer> candidats = new ArrayList<>();
		candidats.add(0);

		hexagonsCoords[0] = new Couple(0, 0);

		while (candidats.size() > 0) {

			int candidat = candidats.get(0);

			for (int i = 0; i < 6; i++) {
				int neighbor = dualGraph[candidat][i];
				if (neighbor != -1 && checkedHexagons[neighbor] == 0) {

					checkedHexagons[neighbor] = 1;

					int x = hexagonsCoords[candidat].getX();
					int y = hexagonsCoords[candidat].getY();

					int x2, y2;

					if (i == 0) {
						x2 = x;
						y2 = x - 1;
					}

					else if (i == 1) {
						x2 = x + 1;
						y2 = y;
					}

					else if (i == 2) {
						x2 = x + 1;
						y2 = y + 1;
					}

					else if (i == 3) {
						x2 = x;
						y2 = y + 1;
					}

					else if (i == 4) {
						x2 = x - 1;
						y2 = y;
					}

					else {
						x2 = x - 1;
						y2 = y - 1;
					}

					hexagonsCoords[neighbor] = new Couple<>(x2, y2);
					candidats.add(neighbor);
				}
			}

			candidats.remove(candidats.get(0));
		}
	}

	public Couple<Integer, Integer>[] getHexagonsCoords() {
		return hexagonsCoords;
	}
}
