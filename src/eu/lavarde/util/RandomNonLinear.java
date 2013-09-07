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

	// ATTENTION: I stands for "included" meaning that also the max range is included
	public long nextLongI(long max) {

		if (max == 0) return 0; // deviation from nextInt which sends an exception
		if (max < Integer.MAX_VALUE) return nextInt((int)max+1);
		
		long res = nextLong();
		
		res %= max+1; // remainder of max
		if (res < 0) res += max+1; // % can give a negative result
		
		return res;
	}

	// ATTENTION: I stands for "included" meaning that also the max range is included
	public long nextLongGaussianI(double min, double mean, double max, double steepness) {
		double rnd;
		do {
			  rnd = super.nextGaussian();
		} while (rnd < -steepness || rnd > steepness); // in order to stay in the range
		if (rnd < 0) {
			rnd = mean + rnd * (mean - min) / steepness;
		} else if (rnd > 0) {
			rnd = mean + rnd * (max - mean) / steepness;
		} else {
			rnd = mean;
		}
		return Math.round(rnd);
	}
}
