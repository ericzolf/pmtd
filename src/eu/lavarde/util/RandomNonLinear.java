package eu.lavarde.util;

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

import java.util.Random;

public class RandomNonLinear extends Random {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public long nextLong(long max) {

		if (max == 0) return 0; // deviation from nextInt which sends an exception
		if (max <= Integer.MAX_VALUE) return nextInt((int)max);
		
		long res = nextLong();
		
		res %= max; // remainder of max
		if (res < 0) res += max; // % can give a negative result
		
		return res;
	}

	/** A helper function to generate a random number according to the probabilities defined in different zones.
	 * @param limits array containing the limits of each zone; the length of the array must be one bigger than the weights.
	 * @param weights array containing the weight of each zone, defining the probability for a value in this zone. The sum of the weights must be equal to 1!
	 * @return a random number with a distribution according to the given parameters
	 */
	public long nextLong(long[] limits, double[] weights) {
/* for performance reasons, we assume that all weights sum up to exactly 1, but else we'd have to normalise them
		double fullWeight = 0;
		
		for(int i = 0; i < weights.length; i++) { // calculate the total weight
			fullWeight += weights[i];
		}
		for(int i = 0; i < weights.length; i++) { // and normalise the weights to a sum of 1
			weights[i] /= fullWeight;
		}
*/
		
		double rnd = nextDouble();
		double currWeight = 0;
		
		for(int i = 0; i < weights.length; i++) {
			if (currWeight + weights[i] > rnd) {
				return (long) ( (rnd - currWeight) * (limits[i+1] - limits[i]) / weights[i] + limits[i] );
			} else {
				currWeight += weights[i];
			}
		}

		return Long.MIN_VALUE; // should never be reached, just to keep Eclipse happy...
	}
}
