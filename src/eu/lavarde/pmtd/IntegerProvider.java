package eu.lavarde.pmtd;

/* This file is part of the PlusMinusTimesDivide product, copyright Eric Lavarde <android@lavar.de>.
 * Trademarks (the product name, in English and translated, artwork like icons, and the domains
 * lavar.de and lavarde.eu - also reversed as package name) are properties of the copyright owner
 * and shall not be used by anyone else without explicit authorisation.

The code itself is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PlusMinusTimesDivide is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PlusMinusTimesDivide.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.regex.Pattern;
import eu.lavarde.util.RandomNonLinear;
import android.os.Bundle;
// import android.util.Log;

public class IntegerProvider implements INumberProvider {
	// private static final String TAG = IntegerProvider.class.getSimpleName();
	
	private long op1;
	private long op2;
	private IPrefs prefs;
	private int operation = -1; // position of the operation in the Spinner
	private int factor = 1;
	private int fac1, fac2, facr, facd; // factors of operands 1 and 2, result and difference between both
	private int tries = 0;
	private boolean tobefound = true;
	private static String dec = ".";
	private String answer = null;
	private static RandomNonLinear rnd = new RandomNonLinear();
	private int round;

	public IntegerProvider(IPrefs prefs) {
		this.prefs = prefs;
		generateOperands();
	}

	public IntegerProvider(IPrefs prefs, int round) {
		this.prefs = prefs;
		this.round = round;
		generateOperands();
	}

	public void setPrefs(IPrefs prefs) {
		this.prefs = prefs;
	}
	
	public boolean generateOperands() {
		operation = prefs.getOperation();
		factor = (int) Math.pow(10, prefs.getDecimalPlaces());
		long max = (long) prefs.getMaxValue() * factor;
		tobefound = true; answer = null; tries = 0;

		switch (operation) {
		case PLUS:
			generatePlusOperands(max);
			break;
		case MINUS:
			generateMinusOperands(max);
			break;
		case TIMES:
			generateTimesOperands(max);
			break;
		case DIVIDE:
			generateDivideOperands(max);
			break;
		}
		
		return true;
	}
	
	private void generatePlusOperands(long mMax) {
		int table = prefs.getTableTraining();
		if (table > 0) {
			op1 = table * factor;
		} else {
			op1 = rnd.nextLongI(mMax);
		}
		if (prefs.isSmallNumbersMax()) {
 			op2 = rnd.nextLongI(mMax);
		} else {
			op2 = rnd.nextLongI(mMax - op1);
		}

		if (! prefs.isPlusCarryAllowed()) {
			int rem1 = (int) (op1 % 10);
			int rem2 = (int) (op2 % 10);
			if (rem1 + rem2 >= 10) {
				rem1 = rnd.nextInt(10-rem1);
				op2 = op2 - rem2 + rem1;
			}
		}
		
		fac1 = fac2 = facr = factor; facd = 1;
	}

	private void generateMinusOperands(long mMax) {
		op1 = rnd.nextLongI(mMax);
		if (prefs.isSmallNumbersMax()) {
 			op2 = rnd.nextLongI(mMax);
		} else {
			op2 = rnd.nextLongI(mMax - op1);
		}
		op1 += op2;

		if (prefs.isMinusNegativeAllowed() && rnd.nextBoolean()) {
			long tmp = op1;
			op1 = op2;
			op2 = tmp;
		}
		
		if (! prefs.isMinusBorrowAllowed()) {
			int rem1 = (int) (op1 % 10);
			int rem2 = (int) (op2 % 10);
			if (rem1 < rem2) {
				rem1 = rnd.nextInt(rem1 + 1);
				op2 = op2 - rem2 + rem1;
			}
		}
		
		fac1 = fac2 = facr = factor; facd = 1;
	}

	private void generateTimesOperands(long mMax) {
		long bigMax = mMax * factor;

		int table = prefs.getTableTraining();
		if (table > 0) { // we train only a specific table
			op1 = table * factor;
		} else {
			if (prefs.isSmallNumbersMax()) {
				op1 = rnd.nextLongI(mMax);
			} else {
				op1 = rnd.nextLongGaussianI(0L, Math.sqrt(bigMax), mMax, 3.0);
			}
		}
		if (prefs.isSmallNumbersMax() || op1 == 0) {
 			op2 = rnd.nextLongI(mMax);
		} else {
			double localMax = (double) bigMax / op1;
			op2 = rnd.nextLongGaussianI(0, 2*localMax/3, localMax, 1.0);
		}
		fac1 = fac2 = factor;
		facr = factor * factor;
		facd = 1;
	}

	private void generateDivideOperands(long mMax) {
		long bigMax = mMax * factor;

		if (prefs.isDivideRestAllowed()) {
			if (prefs.isSmallNumbersMax()) {
				op2 = rnd.nextLongI(mMax-1)+1; // make sure op2 isn't equal 0
				op1 = rnd.nextLongGaussianI(0L, op2 * (2.0 * mMax + 1) / 3.0, op2 * mMax, 1.0); // the result of op1 / op2 will be between 0 and max
			} else {
				op1 = rnd.nextLongGaussianI(0L, (2.0 * bigMax) / 3.0, bigMax, 1.0);
				op2 = rnd.nextLongGaussianI(1L, Math.sqrt(op1), mMax, 3.0);
			}
			if (prefs.isDivideIntegers()) {
				op1 /= factor * factor;
				op2 /= factor;
				if (op2 == 0) op2 = 1; // could happen if op2 < factor!
			}
		} else {
			if (prefs.isSmallNumbersMax()) {
				op1 = rnd.nextLongI(mMax);
			} else {
				op1 = rnd.nextLongGaussianI(0L, Math.sqrt(bigMax), mMax, 3.0);
			}
			if (prefs.isSmallNumbersMax() || op1 == 0) {
	 			op2 = rnd.nextLongI(mMax-1)+1; // make sure op2 isn't equal 0
			} else {
				double localMax = (double) bigMax / op1;
				op2 = rnd.nextLongGaussianI(1, 2*localMax/3, localMax, 1.0);
			}
			op1 *= op2;
		}
		if (prefs.isDivideIntegers()) {
			facr = factor;
			fac1 = fac2 = 1;
			facd = factor;
		} else {
			facr = fac2 = factor;
			fac1 = factor * factor;
			facd = 1;
		}
	}

	public String getOperand1() {
		return toString(op1, fac1);
	}

	public String getOperand2() {
		return toString(op2, fac2);
	}

	public int getAnswerQuality(String answer) {
		long value = 0;
		int result = CORRECT;
		this.answer = answer;
		tries++;

		try {
			if (answer.matches("-?\\d+")) {
				value = Long.valueOf(answer) * facr;
			} else if (answer.matches("-?\\d+\\" + dec + "\\d+")) {
				String[] ans = answer.split( Pattern.quote( dec ) );
				value = Integer.valueOf(ans[1]);
				if (value != 0) { // do some padding to fit factor of result
					int powerdiff = (int) Math.log10(facr) - ans[1].length();
					if (powerdiff > 0) { // the answer has less digits than expected / allowed
						value *= Math.pow(10, powerdiff); // and that's OK, we pad with zeros...
					} else if (powerdiff < 0) { // the answer has more digits than expected / allowed
						if (value % Math.pow(10, -powerdiff) == 0) { //with only zeros at the end
							value /= Math.pow(10, -powerdiff); // that's OK
						} else {
							result |= (INVALID | TOOMANYDIGITS);
						}
					}
				}
				if (ans[0].startsWith("-")) {
					value = - value;
				}
				value += Long.valueOf(ans[0]) * facr;
			} else {
				result |= INVALID;
			}
		} catch (Exception e) {
			result |= INVALID;
		}


        if (result != CORRECT || (value != getResultValue())) {
        	result |= INCORRECT;
        } else {
        	tobefound = false;
        }
        return result;
	}

	public boolean isToBeFound() {
		return tobefound;
	}
	
	private long getResultValue() {
		switch (operation) {
		case PLUS:
			return (op1 + op2) * facd;
		case MINUS:
			return (op1 - op2) * facd;
		case TIMES:
			return (op1 * op2) * facd;
		case DIVIDE:
			return (op1 * facd / op2) ;
		default:
			return (Long) null;
		}
	}

	public String getResult() {
		return toString(getResultValue(), facr);
	}
	
	private String toString(long val, int fac) {
		if (fac == 1) {
			return String.valueOf(val);
		} else {
			long result = val / fac;
			long rest = Math.abs(val % fac);
			int digits = (int) Math.log10(fac);
			return String.format("%d" + dec + "%0" + digits + "d", result, rest);
		}
	}

	public int getOperation() {
		return operation;
	}

	public int getHint() {
		return HintsProvider.getHint(operation, op1, op2, fac1, fac2);
	}

	public void saveToBundle(Bundle stateSaver) {
		long[] long_array = {op1,op2};
		int[] int_array = {operation, factor, fac1, fac2, facr, facd, tries};
		stateSaver.putLongArray("IntegerProvider_longarray_" + round, long_array);
		stateSaver.putIntArray("IntegerProvider_intarray_" + round, int_array);
		stateSaver.putBoolean("IntegerProvider_tobefound_" + round, tobefound);
		stateSaver.putString("IntegerProvider_answer_" + round, answer);
	}

	public void loadFromBundle(Bundle stateSaver) {
		if (stateSaver == null) return;
		long[] long_array = stateSaver.getLongArray("IntegerProvider_longarray_" + round);
		int[] int_array = stateSaver.getIntArray("IntegerProvider_intarray_" + round);
		if (long_array != null && int_array != null) {
			op1 = long_array[0];
			op2 = long_array[1];
			operation = int_array[0];
			factor = int_array[1];
			fac1 = int_array[2];
			fac2 = int_array[3];
			facr = int_array[4];
			facd = int_array[5];
			tries = int_array[6];
			tobefound = stateSaver.getBoolean("IntegerProvider_tobefound_" + round);
			answer = stateSaver.getString("IntegerProvider_answer_" + round);
		}
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}
	
	public int getTries() {
		return tries;
	}
}
