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

import eu.lavarde.db.ChallengesDbAdapter;
import eu.lavarde.db.PmtdDbHelper;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
//import android.text.InputFilter;
//import android.text.Spanned;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
//import android.widget.Toast;


/**
 * @author Eric L.
 *
 */
public class ChallengesListActivity extends ListActivity {
    private ChallengesDbAdapter mDbHelper;
    private Cursor mChallengesCursor;
	private long mUserId;
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.challenges_list);
        setTitle(getString(R.string.app_title, getString(R.string.select_challenge)));
        mUserId = this.getIntent().getExtras().getLong(PmtdDbHelper.EXTRA_USERID);
        mDbHelper = new ChallengesDbAdapter(this);
        mDbHelper.open();
        fillData();	
        registerForContextMenu(getListView());
	}

	@Override
	protected void onDestroy() {
		mDbHelper.close();
		super.onDestroy();
	}

	/* --- Options Menu --- */
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.challenges, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_challenge: {
			askChallengeName(0);
			break;
		}
		}
		
        return super.onOptionsItemSelected(item);
	}   

	/* --- Context Menu --- */

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.challenges_context, menu);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch(item.getItemId()) {
	    case R.id.select_challenge:
	    	launchChallenge(info.id);
	        break;
	    case R.id.rename_challenge:
	        askChallengeName(info.id);
	        break;
	    case R.id.delete_challenge:
	        mDbHelper.deleteChallenge(info.id);
	        break;
	    default:
		    return super.onContextItemSelected(item);
	    }
        fillData();
        return true;
	}
	
	/* --- On click --- */

	/* (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor c = mChallengesCursor;
		c.moveToPosition(position);
		launchChallenge(id);
	}

	/* --- Private Helper Functions --- */

	private void askChallengeName(final long challengeId) {
		// Taken from http://www.mkyong.com/android/android-prompt-challenge-input-dialog-example/
		
		// get challenge_dialog.xml view
		LayoutInflater li = LayoutInflater.from(this);
		View challengeView = li.inflate(R.layout.challenge_dialog, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set challenge_dialog.xml to the dialog builder
		alertDialogBuilder.setView(challengeView);

		final TextView label = (TextView) challengeView.findViewById(R.id.challengeLabel);
		final EditText name = (EditText) challengeView.findViewById(R.id.editChallengeName);
		
		final SeekBar rounds = (SeekBar) challengeView.findViewById(R.id.seekBarRounds);
		final TextView roundsView = (TextView) challengeView.findViewById(R.id.textViewRounds);
		
		final Spinner operation = (Spinner) challengeView.findViewById(R.id.challengeOperationSpinner);
		
		final SeekBar max = (SeekBar) challengeView.findViewById(R.id.seekBarMax);
		final TextView maxView = (TextView) challengeView.findViewById(R.id.textViewMax);

		final CheckBox maxWhich = (CheckBox) challengeView.findViewById(R.id.checkBoxMax);
		
		final SeekBar places = (SeekBar) challengeView.findViewById(R.id.seekBarPlaces);
		final TextView placesView = (TextView) challengeView.findViewById(R.id.textViewPlaces);
		
		final SeekBar table = (SeekBar) challengeView.findViewById(R.id.seekBarTable);
		final TextView tableView = (TextView) challengeView.findViewById(R.id.textViewTable);
		final LinearLayout tableLine = (LinearLayout) challengeView.findViewById(R.id.tableLine);
		
		final CheckBox bool1 = (CheckBox) challengeView.findViewById(R.id.checkBoxBool1);
		final CheckBox bool2 = (CheckBox) challengeView.findViewById(R.id.checkBoxBool2);

		// set dialog message
		alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok,
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
				// get challenge input and set it to result
				// edit text
			    	if (name.getText().length() > 0) {
			    		if (challengeId == 0) {
			    			mDbHelper.createChallenge(name.getText().toString(), mUserId, 
			    					getRoundsProgress(rounds),
			    					operation.getSelectedItemPosition(), 
			    					getMaxProgress(max),
			    					maxWhich.isChecked(), places.getProgress(), 
			    					table.getProgress(),
			    					bool1.isChecked(), bool2.isChecked());
			    		} else {
			    			mDbHelper.updateChallenge(challengeId, name.getText().toString());
			    		}
			    		fillData();
			    	}
			    }
			  })
			.setNegativeButton(android.R.string.cancel,
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
			    	dialog.cancel();
			    }
			  });

		if (challengeId == 0) { // a new challenge
			alertDialogBuilder.setTitle(R.string.add_challenge);
			label.setText(R.string.give_challenge_name);
	        operation.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view,
						int pos, long id) {
					bool2.setEnabled(true);
					switch (pos) {
					case INumberProvider.PLUS:
						table.setVisibility(View.VISIBLE);
						tableLine.setVisibility(View.VISIBLE);
						bool1.setText(R.string.prefs_plus_carry_allow_title);
						bool1.setVisibility(View.VISIBLE);
						bool2.setVisibility(View.GONE);
						break;
					case INumberProvider.MINUS:
						table.setVisibility(View.GONE);
						tableLine.setVisibility(View.GONE);
						bool1.setText(R.string.prefs_minus_borrow_allow_title);
						bool1.setVisibility(View.VISIBLE);
						bool2.setText(R.string.prefs_minus_negative_allow_title);
						bool2.setVisibility(View.VISIBLE);
						break;
					case INumberProvider.TIMES:
						table.setVisibility(View.VISIBLE);
						tableLine.setVisibility(View.VISIBLE);
						bool1.setVisibility(View.GONE);
						bool2.setVisibility(View.GONE);
						break;
					case INumberProvider.DIVIDE:
						table.setVisibility(View.GONE);
						tableLine.setVisibility(View.GONE);
						bool1.setText(R.string.prefs_divide_rest_allow_title);
						bool1.setVisibility(View.VISIBLE);
						bool2.setText(R.string.prefs_divide_integers_title);
						bool2.setVisibility(View.VISIBLE);
						bool2.setEnabled(bool1.isChecked());
						break;
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// Do nothing				
				}
	        });
	        bool1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if (operation.getSelectedItemPosition() == INumberProvider.DIVIDE)
						bool2.setEnabled(isChecked);
					else
						bool2.setEnabled(true);
				}
	        	
	        });
/*	        // TODO: make sure max and table stays within range. The following is probably the wrong
	        // approach, rather use onSomething method to change final result...
	        // setError http://blog.donnfelker.com/2011/11/23/android-validation-with-edittext/
	        // http://stackoverflow.com/questions/2763022/android-how-can-i-validate-edittext-input
	        max.setFilters(new InputFilter[] {new InputFilter() {

				@Override
				public CharSequence filter(CharSequence source, int start,
						int end, Spanned dest, int dstart, int dend) {
					// TODO Auto-generated method stub
					return null;
				}
	        	
	        }});
*/		} else { // Existing challenges can only be renamed, nothing else
			alertDialogBuilder.setTitle(R.string.rename_challenge);
			label.setText(R.string.give_new_challenge_name);
			// fetch the challenge from the DB and pre-populate the Dialog
			ChallengePrefs chlg = mDbHelper.fetchChallengePrefs(challengeId);
			name.setText(chlg.getName());
			setRoundsProgress(rounds, chlg.getRounds()); rounds.setVisibility(View.GONE);
			operation.setSelection(chlg.getOperation()); operation.setEnabled(false);
			setMaxProgress(max, chlg.getMaxValue()); max.setVisibility(View.GONE);
			maxWhich.setChecked(chlg.isSmallNumbersMax()); maxWhich.setEnabled(false);
			places.setProgress(chlg.getDecimalPlaces()); places.setVisibility(View.GONE);
			table.setProgress(chlg.getTableTraining()); table.setVisibility(View.GONE);
			bool1.setChecked(chlg.isBool1()); bool1.setEnabled(false);
			bool2.setChecked(chlg.isBool2()); bool2.setEnabled(false);
		}
		
		switch (operation.getSelectedItemPosition()) {
		case INumberProvider.PLUS:
			tableLine.setVisibility(View.VISIBLE);
			bool1.setText(R.string.prefs_plus_carry_allow_title);
			bool1.setVisibility(View.VISIBLE);
			bool2.setVisibility(View.GONE);
			break;
		case INumberProvider.MINUS:
			tableLine.setVisibility(View.GONE);
			bool1.setText(R.string.prefs_minus_borrow_allow_title);
			bool1.setVisibility(View.VISIBLE);
			bool2.setText(R.string.prefs_minus_negative_allow_title);
			bool2.setVisibility(View.VISIBLE);
			break;
		case INumberProvider.TIMES:
			tableLine.setVisibility(View.VISIBLE);
			bool1.setVisibility(View.GONE);
			bool2.setVisibility(View.GONE);
			break;
		case INumberProvider.DIVIDE:
			tableLine.setVisibility(View.GONE);
			bool1.setText(R.string.prefs_divide_rest_allow_title);
			bool1.setVisibility(View.VISIBLE);
			bool2.setText(R.string.prefs_divide_integers_title);
			bool2.setVisibility(View.VISIBLE);
			break;
		}
		
		// make sure that the values represented as text are same as seekbar values
    	roundsView.setText(String.valueOf(getRoundsProgress(rounds)));
    	maxView.setText(String.valueOf(getMaxProgress(max)));
    	placesView.setText(String.valueOf(places.getProgress()));
    	tableView.setText(String.valueOf(table.getProgress()));

	    places.setOnSeekBarChangeListener(new OnSeekBarChangeListener() { 

	        @Override 
	        public void onStopTrackingTouch(SeekBar seekBar) { 
	            // Toast.makeText(getBaseContext(), String.valueOf(places.getProgress()), Toast.LENGTH_SHORT).show(); 
	        } 

	        @Override 
	        public void onStartTrackingTouch(SeekBar seekBar) { 
	        } 

	        @Override 
	        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) { 
	        	placesView.setText(String.valueOf(seekBar.getProgress()));
	        } 
	    });
	    rounds.setOnSeekBarChangeListener(new OnSeekBarChangeListener() { 

	        @Override 
	        public void onStopTrackingTouch(SeekBar seekBar) { 
//	            Toast.makeText(getBaseContext(), String.valueOf(getRoundsProgress(rounds)), Toast.LENGTH_SHORT).show(); 
	        } 

	        @Override 
	        public void onStartTrackingTouch(SeekBar seekBar) { 
	        } 

	        @Override 
	        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) { 
	        	roundsView.setText(String.valueOf(getRoundsProgress(seekBar)));
	        } 
	    });
	    max.setOnSeekBarChangeListener(new OnSeekBarChangeListener() { 

	        @Override 
	        public void onStopTrackingTouch(SeekBar seekBar) { 
//	            Toast.makeText(getBaseContext(), String.valueOf(getMaxProgress(max)), Toast.LENGTH_SHORT).show(); 
	        } 

	        @Override 
	        public void onStartTrackingTouch(SeekBar seekBar) { 
	        } 

	        @Override 
	        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) { 
	        	maxView.setText(String.valueOf(getMaxProgress(seekBar)));
	        } 
	    });
	    table.setOnSeekBarChangeListener(new OnSeekBarChangeListener() { 

	        @Override 
	        public void onStopTrackingTouch(SeekBar seekBar) { 
//	            Toast.makeText(getBaseContext(), String.valueOf(getTableProgress(table)), Toast.LENGTH_SHORT).show(); 
	        } 

	        @Override 
	        public void onStartTrackingTouch(SeekBar seekBar) { 
	        } 

	        @Override 
	        public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) { 
	        	tableView.setText(String.valueOf(seekBar.getProgress()));
	        } 
	    });
	    
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}
	
	private void launchChallenge(long challengeId) {
		startActivity((new Intent(this, ChallengeRoundActivity.class))
				.putExtra(PmtdDbHelper.EXTRA_USERID, mUserId)
				.putExtra(PmtdDbHelper.EXTRA_CHALLENGEID, challengeId));
	}

	private void fillData() {
		// fetch all challenges from our database
        mChallengesCursor = mDbHelper.fetchAllChallenges();
        startManagingCursor(mChallengesCursor);

        String[] from = new String[] { ChallengesDbAdapter.KEY_NAME };
        int[] to = new int[] { R.id.challenge_row_field };
        
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter challenges =
            new SimpleCursorAdapter(this, R.layout.challenge_row, mChallengesCursor, from, to);
        setListAdapter(challenges);
	}

	private void setMaxProgress(SeekBar max, int maxValue) {
		if (maxValue > 10000)
			max.setProgress(4);
		else
			max.setProgress((int) Math.log10(maxValue) - 1);
	}

	private void setRoundsProgress(SeekBar rounds, int roundsValue) {
		if (roundsValue >= 99)
			rounds.setProgress(9);
		else
			rounds.setProgress((roundsValue/10)-1);
	}

	protected int getMaxProgress(SeekBar max) {
		int value = (int) Math.pow(10, max.getProgress()+1);
		if (value > 10000) 
			return 32768;
		else
			return value;
	}

	protected int getRoundsProgress(SeekBar rounds) {
		int value = 10*(rounds.getProgress()+1);
		if (value >= 100) 
			return 99;
		else
			return value;
	}

}
