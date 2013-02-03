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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import eu.lavarde.util.AboutDialog;


public class TrainingRoundActivity extends PmtdRoundActivity {
//	private static final String TAG = TrainingRoundActivity.class.getSimpleName();

	@Override
	protected IPrefs getPrefs() {
		return new Prefs(this);
	}

	@Override
	protected void setTitle() {
        setTitle(R.string.app_name);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.prefs: {
			startActivity(new Intent(this, PrefsActivity.class));
			refreshLang(true);
			break;
		}
		case R.id.about: {
			AboutDialog about = new AboutDialog(this);
			about.show();
			break;
		}
		case R.id.challenge: {
			startActivity(new Intent(this, UsersListActivity.class));
			break;
		}
		}
		
        return super.onOptionsItemSelected(item);
	}

	@Override
	protected void nextRound() {
    	renewRounds();
	}   
	
	protected void setHintGUI() {
		if (nums[round].isToBeFound() && nums[round].getTries() > 0) {
			hintText.setText(nums[round].getHint());
		} else {
			hintText.setText(R.string.hint_none);
		}
		super.setHintGUI();
	}

	protected void checkResult(View v) {
		// on click calculate and show the result
		int type = nums[round].getAnswerQuality(proposedResultField.getText().toString());
		
		initiateGUI();
		
		if (type == INumberProvider.CORRECT) { // answer is valid and correct
			chrono.stop();
			new AlertDialog.Builder(v.getContext())
			.setTitle(R.string.msg_answer_correct_title)
			.setIcon(getResources().getDrawable(SmileysProvider.getGoodSmiley()))
			.setMessage(getMsgWithResult(R.string.msg_answer_correct))
			.setPositiveButton(android.R.string.ok,null)
			.setNeutralButton(R.string.msg_again, new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						renewRounds();
						initiateGUI();					
					}
				})
			.show();
		} else if (nums[round].getTries() >= Prefs.getMaxTries(this)) { // too many tries and answer not correct
			chrono.stop();
			new AlertDialog.Builder(v.getContext())
			.setTitle(R.string.msg_answer_not_found_title)
			.setIcon(getResources().getDrawable(SmileysProvider.getBadSmiley()))
			.setMessage(getMsgWithResult(R.string.msg_answer_not_found))
			.setPositiveButton(android.R.string.ok,null)
			.setNeutralButton(R.string.msg_again, new OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						renewRounds();
						initiateGUI();					
					}
				})
			.show();
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
	
}
