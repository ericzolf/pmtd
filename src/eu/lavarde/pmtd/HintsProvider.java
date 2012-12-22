/**
 * A class that provides hints for calculation based on certain information
 */
package eu.lavarde.pmtd;

import java.util.Random;

/**
 * @author Eric L.
 * 
 */
public class HintsProvider {
	public static int getHint(int sign, long op1, long op2, int fac1, int fac2) {
		float fop1 = (float)op1/(float)fac1;
		float fop2 = (float)op2/(float)fac2;
		switch (sign) {
		case INumberProvider.PLUS:
			return getPlusHint(fop1, fop2);
		case INumberProvider.MINUS:
			return getMinusHint(fop1, fop2);
		case INumberProvider.TIMES:
			return getTimesHint(fop1, fop2);
		case INumberProvider.DIVIDE:
			return getDivideHint(fop1, fop2);
		default:
			return R.string.hint_none;
		}
	}
	private static Random rnd = new Random();
	
	private static int generic_plus[] = {
		R.string.hint_plus_commute,
		R.string.hint_plus_smaller
	};
	private static int getPlusHint(float op1, float op2) {
		// try first the specific hints
		if (op1 == 0 || op2 == 0) return R.string.hint_plus_zero;
		if (op1 == 9 || op2 == 9) return R.string.hint_plus_nine;
		// then return one of the generic hints (we assume there is at least one)
		return generic_plus[rnd.nextInt(generic_plus.length)];
	}

	private static int generic_minus[] = {
		R.string.hint_minus_addition,
		R.string.hint_minus_smaller
	};
	private static int getMinusHint(float op1, float op2) {
		// try first the specific hints
		if (op2 == 0) return R.string.hint_minus_zero;
		if (op2 == 9) return R.string.hint_minus_nine;
		if (op1 == op2) return R.string.hint_minus_equal;
		// then return one of the generic hints (we assume there is at least one)
		return generic_minus[rnd.nextInt(generic_minus.length)];
	}

	private static int generic_times[] = {
		R.string.hint_times_commute
	};
	private static int getTimesHint(float op1, float op2) {
		// try first the specific hints
		if (op1 == 0 || op2 == 0) return R.string.hint_times_zero;
		if (op1 == 1 || op2 == 1) return R.string.hint_times_one;
		if (op1 == 2 || op2 == 2) return R.string.hint_times_two;
		if (op1 == 5 || op2 == 5) return R.string.hint_times_five;
		if (op1 == 3 || op2 == 3) return R.string.hint_times_three;
		if (op1 == 9 || op2 == 9) {
			if (rnd.nextInt(3) == 2) { // first "9" hint is more useful
				return R.string.hint_times_nine2;
			} else {
				return R.string.hint_times_nine1;
			}
		}
		if (op1 == 10 || op2 == 10) {
			if ( (op1 % 1 == 0) && (op2 % 1 == 0) ) { // this makes only sense with integers
				return R.string.hint_times_ten_int;
			}
		}
		if (op1 == 3 || op2 == 3) return R.string.hint_times_three;
		// then return one of the generic hints (we assume there is at least one)
		return generic_times[rnd.nextInt(generic_times.length)];
	}

	private static int generic_divide[] = {
		R.string.hint_divide_multiplication
	};
	private static int getDivideHint(float op1, float op2) {
		if (op1 == 0) return R.string.hint_divide_zero;
		if (op2 == 1) return R.string.hint_divide_one;
		if (op1 == op2) return R.string.hint_divide_equal;
		// then return one of the generic hints (we assume there is at least one)
		return generic_divide[rnd.nextInt(generic_divide.length)];
	}
}
