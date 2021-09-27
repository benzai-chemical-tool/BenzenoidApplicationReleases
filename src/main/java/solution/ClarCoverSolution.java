package solution;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClarCoverSolution extends BenzenoidSolution {
	private ArrayList<Integer> ronds = new ArrayList<Integer>();
	private ArrayList<Integer> doublesLiaisons = new ArrayList<Integer>(); // bof
	private ArrayList<Integer> celibataires = new ArrayList<Integer>(); // bof

	public ClarCoverSolution(BenzenoidSolution benzenoidSolution, String rondsDoublesLiaisonsEtCelibataires,
			int[] hexagonsCorrespondances) {
		super(benzenoidSolution, hexagonsCorrespondances);

		Pattern patternRond = Pattern.compile("rond(\\d+)=(\\d)");
		Matcher m = patternRond.matcher(rondsDoublesLiaisonsEtCelibataires);
		while (m.find()) {
			// System.out.println(m.group() + "," + m.group(1) + "," + m.group(2));
			if (m.group(2).charAt(0) == '1') {
				ronds.add(Integer.parseInt(m.group(1)));
				// System.out.println(m.group(1));
			}
		}

		Pattern patternLiaison = Pattern.compile("liaison(\\d+):(\\d)=(\\d)");
		m = patternLiaison.matcher(rondsDoublesLiaisonsEtCelibataires);
		while (m.find()) {
			if (m.group(3).charAt(0) == '1') {
				doublesLiaisons.add(Integer.parseInt(m.group(1)) * 6 + Integer.parseInt(m.group(2))); // bof
				// System.out.println(m.group(1));
			}
		}

		Pattern patternCelibataire = Pattern.compile("celibataire(\\d+):(\\d)=(\\d)");
		m = patternCelibataire.matcher(rondsDoublesLiaisonsEtCelibataires);
		while (m.find()) {
			if (m.group(3).charAt(0) == '1') {
				celibataires.add(Integer.parseInt(m.group(1)) * 6 + Integer.parseInt(m.group(2))); // bof
				// System.out.println(m.group(1));
			}
		}

	}

	public ArrayList<Integer> getRonds() {
		return ronds;
	}

	public int getRond(int i) {
		return ronds.get(i);
	}

	public ArrayList<Integer> getDoublesLiaisons() {
		return doublesLiaisons;
	}

	public int getDoubleLiaison(int i, int j) {
		return doublesLiaisons.get(i * 6 + j);
	}

	public ArrayList<Integer> getCelibataires() {
		return celibataires;
	}

	public int getCelibataire(int i, int j) {
		return celibataires.get(i * 6 + j);
	}

}
