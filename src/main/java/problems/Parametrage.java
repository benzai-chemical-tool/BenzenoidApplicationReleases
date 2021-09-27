package problems;

public class Parametrage {
	private int nbCouronnes = 0;
	private int nbHexagones = 0;
	private int diametre = 0;
	private String symmetry = "";
	private String shape = "";
	private String paramString = "";
	private String path = "";
	private int holes = -1;

	public Parametrage() {
		this(new String[0]);
	}

	public Parametrage(String[] args) {
		for(String param : args) {
/*			if(!param.startsWith("dir="))
				paramString = paramString + "_" + param;*/
			if(param.startsWith("dir="))
				path = param.substring(4);
			else if(param.startsWith("c="))
				nbCouronnes = Integer.parseInt(param.substring(2));
			else if(param.startsWith("h="))
				nbHexagones = Integer.parseInt(param.substring(2));
			else if(param.startsWith("diam="))
				diametre = Integer.parseInt(param.substring(5));
			else if(param.startsWith("sym="))
				symmetry = param.substring(4);
			else if(param.startsWith("shape="))
				shape = param.substring(6);
			else if(param.startsWith("holes="))
				setHoles(Integer.parseInt(param.substring(6)));
			else {
				System.out.println("Parameters : ");
				System.out.println("c=<int> -> coronenoid embedding size (c=1 for benzene, c=2 for coronene, etc)");
				System.out.println("h=<int> -> number of hexagons ");
				System.out.println("diam=<int> -> diameter of the hexagon graph");
				System.out.println("sym=<string> -> symmetry : 60[+mirror], 120[+mirrorH/mirrorE], 120vertex[+mirror], 180[edge][+mirror], mirrorH, mirrorE");
				System.out.println("   'edge' or 'vertex' specifies a rotation when not around an Hex");
				System.out.println("   'mirrorH' specifies an axial symmetry along a line of Hex, 'mirrorE' along an Edge");
				System.out.println("shape=<string> -> shape structure : tree for catacondensed, ...");
				System.out.println("dir=<string> -> directory where to store solutions");
				System.exit(0);
			}
			
		}
	}

	public void optimiseNbCouronnes() {
		// Nombre de couronnes
			if(symmetry.startsWith("60"))
				nbCouronnes = (nbHexagones + 10) / 6;
			else if(symmetry.startsWith("120"))
				nbCouronnes = (nbHexagones + 4) / 3;
			else if(holes > 0)
				nbCouronnes = nbHexagones > 4 * holes ? (nbHexagones + 2 - 4 * holes) / 2 : 1;
			else
				nbCouronnes = (nbHexagones + 2) / 2 < nbCouronnes || nbCouronnes == 0 ? (nbHexagones + 2) / 2 : nbCouronnes;
		
	}
	
	public int getNbCouronnes() {
		return nbCouronnes;
	}


	public int getNbHexagones() {
		return nbHexagones;
	}


	public int getDiametre() {
		return diametre;
	}


	public String getSymmetry() {
		return symmetry;
	}


	public String getShape() {
		return shape;
	}


	public String getParamString() {
		return paramString;
	}


	public String getPath() {
		return path;
	}
	

	public void setNbCouronnes(int newValue) {
		this.nbCouronnes =  newValue;
	}


	public void setNbHexagones(int nbHexagones) {
		this.nbHexagones = nbHexagones;
	}


	public void setDiametre(int diametre) {
		this.diametre = diametre;
	}


	public void setSymmetry(String symmetry) {
		this.symmetry = symmetry;
	}


	public void setShape(String shape) {
		this.shape = shape;
	}


	public int getHoles() {
		return holes;
	}

	public void setHoles(int holes) {
		this.holes = holes;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "benzenoid"
				+ (nbCouronnes == 0 ? "" : "_c=" + nbCouronnes)
				+ (nbHexagones == 0 ? "" : "_h=" + nbHexagones)
				+ (diametre == 0 ? "" : "_d=" + diametre)
				+ (symmetry.isEmpty() ? "" : "_sym=" + symmetry)
				+ (shape.isEmpty() ? "" : "_shape=" + shape)
				+ (holes == -1 ? "" : "_holes=" + holes);
	}


}
