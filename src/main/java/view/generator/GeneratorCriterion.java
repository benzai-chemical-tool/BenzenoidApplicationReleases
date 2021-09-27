package view.generator;

import java.util.List;

public class GeneratorCriterion {

	public enum Subject {
		NB_HEXAGONS, NB_CARBONS, NB_HYDROGENS, DIAMETER, RECT_NB_LINES, RECT_NB_COLUMNS, CORONOID, CATACONDENSED,
		SYMM_MIRROR, SYMM_ROT_60, SYMM_ROT_120, SYMM_ROT_180, SYMM_VERTICAL, SYMM_ROT_120_V, SYMM_ROT_180_E, XI, N0, N1,
		N2, N3, N4, VIEW_IRREG, RECTANGLE, SINGLE_PATTERN, MULTIPLE_PATTERNS, FORBIDDEN_PATTERN, OCCURENCE_PATTERN,
		CORONOID_2, NB_HOLES, ROT_60_MIRROR, ROT_120_MIRROR_H, ROT_120_MIRROR_E, ROT_120_VERTEX_MIRROR,
		ROT_180_EDGE_MIRROR, ROT_180_MIRROR, MIRROR_H, MIRROR_E, RHOMBUS
	}

	public enum Operator {
		LEQ, LT, EQ, GT, GEQ, DIFF, NONE
	}

	private Subject subject;
	private Operator operator;
	private String value;

	public GeneratorCriterion(Subject subject, Operator operator, String value) {

		this.subject = subject;
		this.operator = operator;
		this.value = value;
	}

	public Subject getSubject() {
		return subject;
	}

	public Operator getOperator() {
		return operator;
	}

	public String getOperatorString() {
		switch (operator) {

		case LEQ:
			return "<=";

		case LT:
			return "<";

		case EQ:
			return "=";

		case GT:
			return ">";

		case GEQ:
			return ">=";

		case DIFF:
			return "!=";

		case NONE:
			return "";

		default:
			return null;
		}
	}

	public String getValue() {
		return value;
	}

	public static Operator getOperator(String operatorString) {

		if (operatorString.equals("<="))
			return Operator.LEQ;

		else if (operatorString.equals("<"))
			return Operator.LT;

		else if (operatorString.equals("="))
			return Operator.EQ;

		else if (operatorString.equals(">"))
			return Operator.GT;

		else if (operatorString.equals(">="))
			return Operator.GEQ;

		else if (operatorString.equals("!=") || operatorString.equals("<>"))
			return Operator.DIFF;

		else
			return null;
	}

	public static boolean containsSubject(List<GeneratorCriterion> criterions, Subject subject) {

		for (GeneratorCriterion criterion : criterions) {

			if (criterion.getSubject().equals(subject))
				return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return subject.toString() + " " + getOperatorString() + " " + value;
	}
}
