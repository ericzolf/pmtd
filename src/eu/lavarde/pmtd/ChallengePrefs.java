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
package eu.lavarde.pmtd;

public class ChallengePrefs implements IPrefs {
	private long id;
	private String name;
	private long userId;
	private int rounds;
	private int operation;
	private int maxValue;
	private boolean maxIsSmall;
	private int places;
	private int table;
	private boolean[] bool = new boolean[2];

	// --- CONSTRUCTOR ---
	
	// --- Challenge specific functions ---
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getRounds() {
		return rounds;
	}

	public void setRounds(int rounds) {
		this.rounds = rounds;
	}
	
	public boolean isBool1() {	return bool[0];	}
	public void    setBool1(boolean bool) {	this.bool[0] = bool;	}
	public boolean isBool2() {	return bool[1];	}
	public void    setBool2(boolean bool) {	this.bool[1] = bool;	}

	// --- IPrefs interface functions ---

	@Override
	public int getOperation() {
		return operation;
	}

	@Override
	public void setOperation(int value) {
		operation = value;
	}

	@Override
	public void setMaxValue(int value) {
		maxValue = value;

	}

	@Override
	public int getMaxValue() {
		return maxValue;
	}

	@Override
	public void setSmallNumbersMax(boolean value) {
		maxIsSmall = value;
	}

	@Override
	public boolean isSmallNumbersMax() {
		return maxIsSmall;
	}

	@Override
	public void setDecimalPlaces(int value) {
		places = value;
	}

	@Override
	public int getDecimalPlaces() {
		return places;
	}

	@Override
	public void setTableTraining(int value) {
		table = value;
	}

	@Override
	public int getTableTraining() {
		return table;
	}

	@Override
	public boolean isPlusCarryAllowed() {
		return bool[0];
	}

	@Override
	public void setPlusCarryAllowed(boolean value) {
		bool[0] = value;
	}

	@Override
	public boolean isMinusBorrowAllowed() {
		return bool[0];
	}

	@Override
	public void setMinusBorrowAllowed(boolean value) {
		bool[0] = value;
	}

	@Override
	public boolean isMinusNegativeAllowed() {
		return bool[1];
	}

	@Override
	public void setMinusNegativeAllowed(boolean value) {
		bool[1] = value;
	}

	@Override
	public boolean isDivideRestAllowed() {
		return bool[0];
	}

	@Override
	public void setDivideRestAllowed(boolean value) {
		bool[0] = value;
	}

	@Override
	public boolean isDivideIntegers() {
		return bool[1];
	}

	@Override
	public void setDivideIntegers(boolean value) {
		bool[1] = value;
	}

}
