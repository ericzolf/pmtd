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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	/* === Methods needed to extend PreferenceActivity === */
	
	private Prefs mPrefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		findPreference("prefs.divide.integers").setEnabled(mPrefs.isDivideRestAllowed());
        setTitle(getString(R.string.app_title, getString(R.string.preferences)));
	}

	@Override
	public void onContentChanged() {
		if (mPrefs == null) mPrefs = new Prefs(this);
			// assumes onContentChanged is called at the very beginning of onCreate
		Prefs.setLang(this, Prefs.getLang(this));
        setTitle(getString(R.string.app_title, getString(R.string.preferences)));
		super.onContentChanged();
	}

	/* === Implement OnSharedPreferenceChangeListener === */

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals("prefs.divide.rest_allow")) {
			findPreference("prefs.divide.integers").setEnabled(mPrefs.isDivideRestAllowed());
		}
		else if (key.equals("prefs.overall.lang")) {
			Prefs.setLang(this, Prefs.getLang(this));
			// force a redraw after the language has potentially changed, could be replaced by recreate() since API 11 (3.0)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
	}

}

