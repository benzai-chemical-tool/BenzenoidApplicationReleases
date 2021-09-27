package utils;

import java.util.ArrayList;

public class SubMolecule {

	private int nbNodes, nbEdges, nbTotalNodes;
	private int [] vertices;
	private int [][] adjacencyMatrix;

	private ArrayList<ArrayList<Integer>> edgesMatrix;
	
	public SubMolecule(int nbNodes, int nbEdges, int nbTotalNodes, int [] vertices, int [][] adjacencyMatrix) {
		this.nbNodes = nbNodes;
		this.nbEdges = nbEdges;
		this.nbTotalNodes = nbTotalNodes;
		this.vertices = vertices;
		this.adjacencyMatrix = adjacencyMatrix;
		initEdgesMatrix();
	}

	public int getNbNodes() {
		return nbNodes;
	}

	public int getNbEdges() {
		return nbEdges;
	}
	
	public int getNbTotalNodes() {
		return nbTotalNodes;
	}
	
	public int[] getVertices() {
		return vertices;
	}

	public int[][] getAdjacencyMatrix() {
		return adjacencyMatrix;
	}
	
	public ArrayList<ArrayList<Integer>> getEdgesMatrix(){
		return edgesMatrix;
	}
	
	public void initEdgesMatrix() {
		
		edgesMatrix = new ArrayList<ArrayList<Integer>>();
		
		for (int i = 0 ; i < nbTotalNodes ; i++) {
			edgesMatrix.add(new ArrayList<Integer>());
		}
		
		int edgeIndex = 0;
		
		for (int i = 0 ; i < adjacencyMatrix.length ; i++) {
			for (int j = (i+1) ; j < adjacencyMatrix[i].length ; j++) {
				
				if (adjacencyMatrix[i][j] == 1) {
					edgesMatrix.get(i).add(edgeIndex);
					edgesMatrix.get(j).add(edgeIndex);
					edgeIndex ++;
				}
			}
		}
	}
}
