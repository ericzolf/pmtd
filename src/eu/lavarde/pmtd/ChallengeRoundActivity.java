package eu.lavarde.pmtd;

import eu.lavarde.db.HighscoresDbAdapter;
import eu.lavarde.db.PmtdDbHelper;
import eu.lavarde.db.ChallengesDbAdapter;
import eu.lavarde.db.ScoreEvolutionDbAdapter;
import eu.lavarde.db.UsersDbAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

//import android.database.Cursor;

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

public class ChallengeRoundActivity extends PmtdRoundActivity {
//	private static final String TAG = ChallengeRoundActivity.class.getSimpleName();
	private long mUserId;
	private String mUserName;
	private long mChallengeId;
	private String mChallengeName;
    private ChallengesDbAdapter mCDbAdapter;
    private UsersDbAdapter mUDbAdapter;
    private HighscoresDbAdapter mHDbAdapter;
    private ScoreEvolutionDbAdapter mSDbAdapter;
//    private Cursor mChallengesCursor;
    private int tries, failed, found;

    /* (non-Javadoc)
	 * @see android.app.Activity#onCreate()
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// Create the main view
        super.onCreate(savedInstanceState);
    	if (savedInstanceState != null) {
    		tries = savedInstanceState.getInt("Pmtd_Challenge_Tries");
    		failed = savedInstanceState.getInt("Pmtd_Challenge_Failed");
    		found = savedInstanceState.getInt("Pmtd_Challenge_Found");
    	}
    }
    
     /* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
        mCDbAdapter.close();
        mHDbAdapter.close();
        mSDbAdapter.close();
        mUDbAdapter.close();
		super.onDestroy();
	}

	@Override
	protected IPrefs getPrefs() {
        mUserId = this.getIntent().getExtras().getLong(PmtdDbHelper.EXTRA_USERID);
        mChallengeId = this.getIntent().getExtras().getLong(PmtdDbHelper.EXTRA_CHALLENGEID);

        mCDbAdapter = new ChallengesDbAdapter(this);
        mCDbAdapter.open();
        
        mUDbAdapter = new UsersDbAdapter(this);
        mUDbAdapter.open();
        mUserName = mUDbAdapter.fetchUserName(mUserId);
        
        mHDbAdapter = new HighscoresDbAdapter(this);
        mHDbAdapter.open();

        mSDbAdapter = new ScoreEvolutionDbAdapter(this);
        mSDbAdapter.open();

        ChallengePrefs prefs = mCDbAdapter.fetchChallengePrefs(mChallengeId);
        mChallengeName = prefs.getName();
        
        return prefs;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.challenge, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.challenge: {
			renewRounds();
			initiateGUI();					
			break;
		}
		}
		
        return super.onOptionsItemSelected(item);
	}

	@Override
	protected void setTitle() {
        setTitle(getString(R.string.app_title, getString(R.string.challenge)));
	}

	@Override
	protected void initiateGUI() {
		super.initiateGUI();
		operationSpinner.setEnabled(false);
		infoLabel.setText(getString(R.string.challenge_round, round + 1, nums.length, getScore()));
		newExerciseButton.setText(R.string.challenge_next_round);
	}

	@Override
	protected void nextRound() {
		int r = (round + 1) % nums.length;
		
		if ( (found+failed) == nums.length ) {
			round = r;
			return;
		}

		while (r != round) {
			if (nums[r].isToBeFound() && nums[r].getTries() < Prefs.getMaxTries(this)) {
				round = r;
				return;
			}
			r++;
		}
	}

	@Override
	protected void setHintGUI() {
		if ( (found+failed) < nums.length
			&& nums[round].isToBeFound() && nums[round].getTries() > 0 ) {
			hintText.setText(nums[round].getHint());
		} else {
			hintText.setText(getString(R.string.challenge_hint, 
					mChallengeName, mUserName, 
					tries, chrono.toString(),
					found, failed,
					getScore()));
		}
        super.setHintGUI();
	}

	private int getScore() {
		int elapsed = chrono.getElapsedSeconds();
		int score = (elapsed * tries) == 0 ? 
				0 : 
				found * 360360 * (found+failed) / (elapsed * tries);
					// 360360 is 7*8*9*5*11*13 and can be divided in multiple ways, and right size
		return score;
	}
	
	/**
	 * Finish the challenge: stop the clock, save the score evolution, and also a potential highscore
	 * and trigger a dialog to congratulate the user and ask if he wants to play again.
	 */
	private void finishChallenge() {
		chrono.stop(); initiateGUI();
		int score = getScore();
		mSDbAdapter.createScore(mUserId, mChallengeId, score, chrono.getStopSeconds(),
				chrono.getElapsedSeconds(), tries, found, failed);
		int rank = mHDbAdapter.createHighscore(mUserId, mChallengeId, score, chrono.getStopSeconds());
		String msg = getString(R.string.challenge_finished_message, getScore()) ;
		if (rank > 0) {// we've got a high-score!
			msg = msg + " " + getString(R.string.challenge_highscore_message, rank);
		}
		new AlertDialog.Builder(this)
		.setTitle(R.string.challenge_finished_title)
		.setIcon(R.drawable.trophy)
		.setMessage(msg)
		.setPositiveButton(android.R.string.ok, null)
		.setNeutralButton(R.string.challenge_new, new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						renewRounds();
						initiateGUI();					
					}
				})
		.setNegativeButton(R.string.challenge_other, new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						ChallengeRoundActivity.this.finish();
					}
				})
		.show();
	}

	@Override
	protected void renewRounds() {
		tries = failed = found = 0;
		super.renewRounds();
	}
	
	@Override
	protected void checkResult(View v) {
		// on click calculate and show the result
		int type = nums[round].getAnswerQuality(proposedResultField.getText().toString());
		tries++;
		
		initiateGUI();
		
		if (type == INumberProvider.CORRECT) { // answer is valid and correct
			found++;
			if ( (found+failed) == nums.length) {
				finishChallenge();
			} else {
				new AlertDialog.Builder(v.getContext())
				.setTitle(R.string.msg_answer_correct_title)
				.setIcon(getResources().getDrawable(SmileysProvider.getGoodSmiley()))
				.setMessage(getMsgWithResult(R.string.msg_answer_correct))
				.setPositiveButton(android.R.string.ok,new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						nextRound();
						initiateGUI();					
					}
				})
				.show();
			}
		} else if (nums[round].getTries() >= Prefs.getMaxTries(this)) { // too many tries and answer not correct
			failed++;
			if ( (found+failed) == nums.length) {
				finishChallenge();
			} else {
				new AlertDialog.Builder(v.getContext())
				.setTitle(R.string.msg_answer_not_found_title)
				.setIcon(getResources().getDrawable(SmileysProvider.getBadSmiley()))
				.setMessage(getMsgWithResult(R.string.msg_answer_not_found))
				.setPositiveButton(android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						nextRound();
						initiateGUI();					
					}
				})
				.show();
			}
		} else { // give (slightly) more detailed error message
		    if ((type & INumberProvider.TOOMANYDIGITS) != 0) { // too many digits
				new AlertDialog.Builder(v.getContext())
				.setTitle(R.string.msg_answer_not_valid_title)
				.setIcon(getResources().getDrawable(SmileysProvider.getBadSmiley()))
				.setMessage(getMsgWithAnswer(R.string.msg_answer_not_valid_too_many_digits))
				.setPositiveButton(android.R.string.ok,null)
				.show();
			} else if ((type & INumberProvider.INVALID) != 0) { // invalid result
				new AlertDialog.Builder(v.getContext())
				.setTitle(R.string.msg_answer_not_valid_title)
				.setIcon(getResources().getDrawable(SmileysProvider.getBadSmiley()))
				.setMessage(getMsgWithAnswer(R.string.msg_answer_not_valid))
				.setPositiveButton(android.R.string.ok,null)
				.show();
			} else if ((type & INumberProvider.INCORRECT) != 0) { // incorrect result
				new AlertDialog.Builder(v.getContext())
				.setTitle(R.string.msg_answer_incorrect_title)
				.setIcon(getResources().getDrawable(SmileysProvider.getBadSmiley()))
				.setMessage(getMsgWithAnswer(R.string.msg_answer_incorrect))
				.setPositiveButton(android.R.string.ok,null)
				.show();
			} 
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO: save the dialog state!
		outState.putInt("Pmtd_Challenge_Tries", tries);
		outState.putInt("Pmtd_Challenge_Failed", failed);
		outState.putInt("Pmtd_Challenge_Found", found);
		super.onSaveInstanceState(outState);
	}
}
