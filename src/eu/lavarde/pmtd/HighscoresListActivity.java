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

import eu.lavarde.db.HighscoresDbAdapter;
import eu.lavarde.db.PmtdDbHelper;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;


/**
 * @author Eric L.
 *
 */
public class HighscoresListActivity extends ListActivity {
    private HighscoresDbAdapter mDbHelper;
    private Cursor mHighscoresCursor;
	private long mChallengeId;
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.highscores_list);
        setTitle(getString(R.string.app_title, getString(R.string.highscores)));
        mChallengeId = this.getIntent().getExtras().getLong(PmtdDbHelper.EXTRA_CHALLENGEID);
        mDbHelper = new HighscoresDbAdapter(this);
        mDbHelper.open();
        fillData();	
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}

	/* --- Private Helper Functions --- */

	private void fillData() {
		// fetch all challenges from our database
        mHighscoresCursor = mDbHelper.fetchChallengeHighscoresLimited(mChallengeId);
        startManagingCursor(mHighscoresCursor);

        String[] from = new String[] { HighscoresDbAdapter.KEY_ID, HighscoresDbAdapter.KEY_CHALLENGE,
        		HighscoresDbAdapter.KEY_USER, HighscoresDbAdapter.KEY_SCORE, HighscoresDbAdapter.KEY_WHEN};
        int[] to = new int[] { R.id.place_field, R.id.challenge_field, R.id.user_field,
        		R.id.score_field, R.id.when_field };
        
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter highscores =
            new SimpleCursorAdapter(this, R.layout.highscore_row, mHighscoresCursor, from, to);
        setListAdapter(highscores);
	}
}
