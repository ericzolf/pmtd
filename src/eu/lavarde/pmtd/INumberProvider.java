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

import android.os.Bundle;

/**
 * An interface for number providers. It's not that the app could handle more than one provider but it helps having a
 * clean interface between the GUI and the number generator. It is to be noted as a general rule that numbers are only
 * exchanged as strings, this avoids interpretation issues and the need to differentiate between integers and floats.
 * @author Eric Lavarde
 *
 */
/**
 * @author ericl
 *
 */
public interface INumberProvider {
	public final static int CORRECT = 0;
	public final static int INVALID = 1;
	public final static int INCORRECT = 2;
	public final static int TOOMANYDIGITS = 4;
	
	public final static int PLUS   = 0;
	public final static int MINUS  = 1;
	public final static int TIMES  = 2;
	public final static int DIVIDE = 3;
	// TODO: add percentage operations and hours:minutes
	
	/** 
	 * Assigns a preference set to the number provider, so that the number provider can use it to pull parameters
	 * @param prefs the preference set to assign
	 */
	public void setPrefs(IPrefs prefs);
	/**
	 * Generates randomly the operands for the currently set operation; this will be done based on preferences of the app.
	 * @return Always true to be aligned with {@link #generateOperands(String)}
	 */
	public boolean generateOperands();
	/**
	 * @return The first operand as String representation.
	 */
	public String getOperand1();
	/**
	 * @return The second operand as String representation.
	 */
	public String getOperand2();
	/**
	 * @return The operation as a position (0 to 3) within an array assumed to be [PLUS, MINUS, TIMES, DIVIDE].
	 */
	public int getOperation();
	/**
	 * @return The result of <code>operand1 <i>operation</i> operand2</code> as String representation.
	 */
	public String getResult();
	/**
	 * Saves the parameter of the function as proposed solution and compares it with the result of 
	 * <code>operand1 <i>operation</i> operand2</code>, and returns the result of this comparison.
	 * @param answer The solution proposed for evaluation by the function.
	 * @return The result of the comparison, {@link #INVALID} if the result can't be interpreted as a number, possibly
	 * combined ("or-ed") with {@link #TOOMANYDIGITS} if the problem is a too long number. If the number recognised doesn't
	 * match the operation's result, {@link #INCORRECT} will be returned. And if everything is fine, {@link #CORRECT} 
	 * (actually {@value #CORRECT}) will be returned; in this last case, the result will also be marked as having been
	 * found (see {@link #isToBeFound()}).
	 */
	public int getAnswerQuality(String answer);
	/**
	 * Saves the parameter of the function as proposed solution by the user (no validity check is being made!).
	 * @param answer The solution proposed by the user.
	 */
	public void setAnswer(String answer);
	/**
	 * Returns the solution saved previously through {@link #getAnswerQuality(String)} or {@link #setAnswer(String)}.
	 * Remember that the string has not been validated and might be garbage.
	 * @return The proposed solution saved.
	 */
	public String getAnswer();
	/**
	 * Tells if the solution has not yet been successfully validated through {@link #getAnswerQuality(String)}.
	 * @return True if the solution has <i>not</i> yet been found, False otherwise.
	 */
	public boolean isToBeFound();
	
	/**
	 * Returns the number of tries till now.
	 * @return Number of tries (starting with 0).
	 */
	public int getTries();
	
	/**
	 * Function that provides the text ID of a hint related (or not) to the current operation.
	 * @return An integer value of type <code>R.string.hint_operation_...</code>, or <code>R.string.hint_none</code> if no hint is required, allowed or available.
	 */
	public int getHint();
	
	/**
	 * Use this function to save the state of the object (operands, operation, answer, found state).
	 * The context is not supposed to be saved (the application will have to set it itself though).
	 * @param stateSaver a {@link android.os.Bundle} object to which the object state can be saved.
	 */
	public void saveToBundle(Bundle stateSaver);
	/**
	 * Use this function to restore the state of the object (operands, operation, answer, found state).
	 * The context is not supposed to have been saved (the application will have to set it itself).
	 * @param stateSaver a {@link android.os.Bundle} object from which the object state can be loaded.
	 */
	public void loadFromBundle(Bundle stateSaver);
}
