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

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

public class Prefs extends APrefs {

	private SharedPreferences prefs;
	
	public Prefs(Context ctx) {
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	/*=== Overall Preferences with static functions ===*/
	
	public static void setLang(Context ctx, String lang) {
		if (! getLang(ctx).equals(lang)) { // we don't want to change something that doesn't need
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
			editor.putString("prefs.overall.lang", lang);
			editor.commit();
		}
		
		// It's not enough to set the preference, it also needs to be actually updated
		Resources standardResources = ctx.getResources();
		Configuration config = new Configuration(standardResources.getConfiguration());
		if (lang.equals("default")) {
			config.locale = Locale.getDefault();
		} else {		
			config.locale = new Locale(lang);
		}
		standardResources.updateConfiguration(config, standardResources.getDisplayMetrics());
	}
	public static String getLang(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx).getString("prefs.overall.lang", "default");
	}
	
	public static void setMaxTries(Context ctx, int value) {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
		editor.putString("prefs.overall.max_tries", String.valueOf(value));
		editor.commit();
	}
	public static int getMaxTries(Context ctx) {
		int max = 5;
		try {
			max = Integer.parseInt(
					PreferenceManager.getDefaultSharedPreferences(ctx).getString("prefs.overall.max_tries", "5"));
		} catch (NumberFormatException e) { // something wrong with stored number, fix it
			setMaxTries(ctx, 5);
			return 5;
		}
		if (max >= 100 || max <= 0) { // we accept maximum 99 tries, and at least 1
			setMaxTries(ctx, 5);
			return 5;
		}
		return max;
	}

	public static boolean isTimerVisible(Context ctx) {
		return PreferenceManager.getDefaultSharedPreferences(ctx)
				.getBoolean("prefs.overall.timer_visible", false);
	}
	public static void setTimerVisible(Context ctx, boolean value) {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
		editor.putBoolean("prefs.overall.timer_visible", value);
		editor.commit();
	}
	
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#getOperation()
	 */
	@Override
	public int getOperation() {
		return prefs.getInt("prefs.overall.operation", INumberProvider.PLUS);
	}
	
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#setOperation(int)
	 */
	@Override
	public void setOperation(int value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt("prefs.overall.operation", value);
		editor.commit();
	}
	
	@Override
	public int getRounds() { return 1; }

	@Override
	public void setRounds(int value) {	} // anyway not used, as result is always 1

	/*=== Generic Operation Preferences ===*/
	
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#setMaxValue(int, int)
	 */
	@Override
	public void setMaxValue(int value) {
		SharedPreferences.Editor editor = prefs.edit();
		switch (getOperation()) {
		case INumberProvider.PLUS:
			editor.putString("prefs.plus.max_value", String.valueOf(value));
			break;
		case INumberProvider.MINUS:
			editor.putString("prefs.minus.max_value", String.valueOf(value));
			break;
		case INumberProvider.TIMES:
			editor.putString("prefs.times.max_value", String.valueOf(value));
			break;
		case INumberProvider.DIVIDE:
			editor.putString("prefs.divide.max_value", String.valueOf(value));
			break;
		}
		editor.commit();
	}
	
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#getMaxValue(int)
	 */
	@Override
	public int getMaxValue() {
		int ret = 0;
		try {
			switch (getOperation()) {
			case INumberProvider.PLUS:
				ret = Integer.parseInt(prefs.getString("prefs.plus.max_value", "10"));
				if (isSmallNumbersMax() && ret > Integer.MAX_VALUE/2) {
					ret = Integer.MAX_VALUE/2;
					setMaxValue(ret);
				}
				break;
			case INumberProvider.MINUS:
				ret = Integer.parseInt(prefs.getString("prefs.minus.max_value", "10"));
				if (isSmallNumbersMax() && ret > Integer.MAX_VALUE/2) {
					ret = Integer.MAX_VALUE/2;
					setMaxValue(ret);
				}
				break;
			case INumberProvider.TIMES:
				ret = Integer.parseInt(prefs.getString("prefs.times.max_value", "10"));
				if (isSmallNumbersMax() && ret > Math.sqrt(Integer.MAX_VALUE)) {
					ret = (int) Math.floor(Math.sqrt(Integer.MAX_VALUE));
					setMaxValue(ret);
				}
				break;
			case INumberProvider.DIVIDE:
				ret = Integer.parseInt(prefs.getString("prefs.divide.max_value", "10"));
				if (isSmallNumbersMax() && ret > Math.sqrt(Integer.MAX_VALUE)) {
					ret = (int) Math.floor(Math.sqrt(Integer.MAX_VALUE));
					setMaxValue(ret);
				}
				break;
			default:
				return 10;
			}
		} catch (NumberFormatException e) { // something wrong with stored number, fix it
			setMaxValue(10);
			return 10;
		}
		if (ret > Integer.MAX_VALUE) {
			setMaxValue(Integer.MAX_VALUE);
			return Integer.MAX_VALUE;
		}
		if (ret <= 0) { // stored number really too small or wrong, fix it
			setMaxValue(10);
			return 10;
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#setSmallNumbersMax(int, boolean)
	 */
	@Override
	public void setSmallNumbersMax(boolean value) {
		SharedPreferences.Editor editor = prefs.edit();
		switch (getOperation()) {
		case INumberProvider.PLUS:
			editor.putBoolean("prefs.plus.max_applies", value);
			break;
		case INumberProvider.MINUS:
			editor.putBoolean("prefs.minus.max_applies", value);
			break;
		case INumberProvider.TIMES:
			editor.putBoolean("prefs.times.max_applies", value);
			break;
		case INumberProvider.DIVIDE:
			editor.putBoolean("prefs.divide.max_applies", value);
			break;
		}
		editor.commit();
	}
	
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#isSmallNumbersMax(int)
	 */
	@Override
	public boolean isSmallNumbersMax() {
		switch (getOperation()) {
		case INumberProvider.PLUS:
			return prefs.getBoolean("prefs.plus.max_applies", true);
		case INumberProvider.MINUS:
			return prefs.getBoolean("prefs.minus.max_applies", true);
		case INumberProvider.TIMES:
			return prefs.getBoolean("prefs.times.max_applies", true);
		case INumberProvider.DIVIDE:
			return prefs.getBoolean("prefs.divide.max_applies", true);
		default:
			return true;
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#setDecimalPlaces(int, int)
	 */
	@Override
	public void setDecimalPlaces(int value) {
		SharedPreferences.Editor editor = prefs.edit();
		switch (getOperation()) {
		case INumberProvider.PLUS:
			editor.putString("prefs.plus.decimal_places", String.valueOf(value));
			break;
		case INumberProvider.MINUS:
			editor.putString("prefs.minus.decimal_places", String.valueOf(value));
			break;
		case INumberProvider.TIMES:
			editor.putString("prefs.times.decimal_places", String.valueOf(value));
			break;
		case INumberProvider.DIVIDE:
			editor.putString("prefs.divide.decimal_places", String.valueOf(value));
			break;
		}
		editor.commit();
	}

	// TODO: Remove - Transitional function to get rid of overall decimal places
	public static boolean fixDecimalPlaces(Context ctx) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String defString = prefs.getString("prefs.overall.decimal_places", "XXX");
		if (! defString.equals("XXX")) { // then old preference did still exist
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("prefs.plus.decimal_places", defString);
			editor.putString("prefs.minus.decimal_places", defString);
			editor.putString("prefs.times.decimal_places", defString);
			editor.putString("prefs.divide.decimal_places", defString);
			editor.remove("prefs.overall.decimal_places");
			editor.commit();
			return true;
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#getDecimalPlaces(int)
	 */
	@Override
	public int getDecimalPlaces() {
		int dec = 0;
		try {			
			switch (getOperation()) {
			case INumberProvider.PLUS:
				dec = Integer.parseInt(prefs.getString("prefs.plus.decimal_places", "0"));
				break;
			case INumberProvider.MINUS:
				dec = Integer.parseInt(prefs.getString("prefs.minus.decimal_places", "0"));
				break;
			case INumberProvider.TIMES:
				dec = Integer.parseInt(prefs.getString("prefs.times.decimal_places", "0"));
				break;
			case INumberProvider.DIVIDE:
				dec = Integer.parseInt(prefs.getString("prefs.divide.decimal_places", "0"));
				break;
			}
		} catch (NumberFormatException e) { // something wrong with stored number, fix it
			setDecimalPlaces(0);
			return 0;
		}
		// An integer can have 9 full digits in base 10, but only half can be used to cope
		// with multiplications/divisions
		if (dec > 4) { setDecimalPlaces(4); dec = 4; }
		if (dec < 0) { setDecimalPlaces(0); dec = 0; }

		return dec;
	}

	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#setTableTraining(int, int)
	 */
	@Override
	public void setTableTraining(int value) {
		SharedPreferences.Editor editor = prefs.edit();
		switch (getOperation()) {
		case INumberProvider.PLUS:
			editor.putString("prefs.plus.table_training", String.valueOf(value));
			break;
		case INumberProvider.TIMES:
			editor.putString("prefs.times.table_training", String.valueOf(value));
			break;
		}
		editor.commit();
	}

	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#getTableTraining(int)
	 */
	@Override
	public int getTableTraining() {
		int ret = 0;
		try {
			switch (getOperation()) {
			case INumberProvider.PLUS:
				ret = Integer.parseInt(prefs.getString("prefs.plus.table_training", "0"));
				if (ret > Integer.MAX_VALUE/2)
					setTableTraining(Integer.MAX_VALUE/2);
				break;
			case INumberProvider.TIMES:
				ret = Integer.parseInt(prefs.getString("prefs.times.table_training", "0"));
				if (ret > Math.sqrt(Integer.MAX_VALUE))
					setTableTraining((int) Math.floor(Math.sqrt(Integer.MAX_VALUE)));
				break;
			default:
				return 0;
			}
		} catch (NumberFormatException e) { // something wrong with stored number, fix it
			setTableTraining(0);
			return 0;
		}
		if (ret < 0) { // stored number really too small or wrong, fix it
			setTableTraining(0);
			return 0;
		}
		return ret;
	}
	
	/*=== Specific Operation Preferences ===*/
	
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#isPlusCarryAllowed()
	 */
	@Override
	public boolean isPlusCarryAllowed() { return prefs.getBoolean("prefs.plus.carry_allow", false);	}
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#setPlusCarryAllowed(boolean)
	 */
	@Override
	public void setPlusCarryAllowed(boolean value) { setBoolean("prefs.plus.carry_allow", value); }
	
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#isMinusBorrowAllowed()
	 */
	@Override
	public boolean isMinusBorrowAllowed() { return prefs.getBoolean("prefs.minus.borrow_allow", false);	}
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#setMinusBorrowAllowed(boolean)
	 */
	@Override
	public void setMinusBorrowAllowed(boolean value) { setBoolean("prefs.minus.borrow_allow", value); }

	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#isMinusNegativeAllowed()
	 */
	@Override
	public boolean isMinusNegativeAllowed() { return prefs.getBoolean("prefs.minus.negative_allow", false);	}
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#setMinusNegativeAllowed(boolean)
	 */
	@Override
	public void setMinusNegativeAllowed(boolean value) { setBoolean("prefs.minus.negative_allow", value); }

	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#isDivideRestAllowed()
	 */
	@Override
	public boolean isDivideRestAllowed() { return prefs.getBoolean("prefs.divide.rest_allow", false); }
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#setDivideRestAllowed(boolean)
	 */
	@Override
	public void setDivideRestAllowed(boolean value) { setBoolean("prefs.divide.rest_allow", value); }

	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#isDivideIntegers()
	 */
	@Override
	public boolean isDivideIntegers() {	return prefs.getBoolean("prefs.divide.integers", false); }
	/* (non-Javadoc)
	 * @see eu.lavarde.pmtd.IPrefs#setDivideIntegers(boolean)
	 */
	@Override
	public void setDivideIntegers(boolean value) { setBoolean("prefs.divide.integers", value); }

	private void setBoolean(String key, boolean value) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

}

