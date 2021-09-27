package classifier;

public class Irregularity {

	private int [] groups;
	private double XI;
	
	public Irregularity(int [] groups, double XI) {
		this.groups = groups;
		this.XI = XI;
	}
	
	public int getGroup(int group) {
		return groups[group];
	}
	
	public double getXI() {
		return XI;
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0 ; i < groups.length ; i++)
			builder.append("N_" + (i + 1) + " = " + groups[i] + "\n");
		
		builder.append("XI = " + XI);
		
		return builder.toString();
	}
}
