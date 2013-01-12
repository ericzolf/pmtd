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

import eu.lavarde.util.ChronoProvider;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
//import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.graphics.PorterDuff;

public abstract class PmtdRoundActivity extends Activity {
//	protected static final String TAG = PlusMinusTimesDivideActivity.class.getSimpleName();
	protected INumberProvider[] nums; // our "Model" object to keep data
	protected int round;
	protected IPrefs mPrefs;
	protected ChronoProvider chrono;
	protected boolean locked = false;
	
	// All GUI related variables
	protected Spinner operationSpinner;
	protected Button newExerciseButton;
	protected EditText proposedResultField;
	protected Button showSolutionButton;
	protected TextView infoLabel;
	protected TextView operand1Label;
	protected TextView operand2Label;
	protected TextView triesNumber;
	protected TextView triesMax;
	protected TextView hintText;
	protected TextView chronoText;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// Create the main view
        super.onCreate(savedInstanceState);
    	mPrefs = getPrefs();
        refreshLang(false);
        setContentView(R.layout.main);

        // Retrieve or initialise our number providers
        nums = new IntegerProvider[mPrefs.getRounds()];
        for(int i = 0; i < nums.length; i++) {
        	nums[i] = new IntegerProvider(mPrefs, i);
            nums[i].loadFromBundle(savedInstanceState);
        }
        Prefs.fixDecimalPlaces(this); // TODO: Remove - transitional fix
        
        // Prepare the spinner to get the operation choice (+/-/x/÷)
        operationSpinner = (Spinner) findViewById(R.id.OperationSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.signs_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operationSpinner.setAdapter(adapter);
        operationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				if (mPrefs.getOperation() != pos) { // necessary to avoid disturbing call at creation time
					mPrefs.setOperation(pos);
					nums[round].generateOperands();
					chrono.forceStart();
					initiateGUI();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Do nothing				
			}
        });
        operationSpinner.getBackground().setColorFilter(
        		R.color.tango_butter_1, PorterDuff.Mode.DST_IN);

        // Setup the new exercise button
        newExerciseButton = (Button) findViewById(R.id.NewExerciseButton);
        newExerciseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	nextRound();
            	initiateGUI();
           }
        });
//        newExerciseButton.getBackground().setColorFilter(
//        		R.color.tango_butter_1, PorterDuff.Mode.DST_IN);
        
        proposedResultField = (EditText) findViewById(R.id.ProposedResultField);
        proposedResultField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (!v.isEnabled()) return false;
				if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
					checkResult(v);
					return true;
				} else {
					return false;
				}
			}
        });
        
        // Setup the show solution button
        showSolutionButton = (Button) findViewById(R.id.ShowSolutionButton);
        showSolutionButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                checkResult(v);
            }
        });
//        showSolutionButton.getBackground().setColorFilter(
//        		R.color.tango_butter_1, PorterDuff.Mode.DST_IN);
  
        infoLabel = (TextView) findViewById(R.id.infoTextView);
        operand1Label = (TextView) findViewById(R.id.Operand1Label);
        operand2Label = (TextView) findViewById(R.id.Operand2Label);
		triesNumber = (TextView) findViewById(R.id.TriesNumber);
    	triesMax = (TextView) findViewById(R.id.TriesMax);
    	hintText = (TextView) findViewById(R.id.HintText);
    	chronoText = (TextView) findViewById(R.id.chronoText);

    	// now initialise the chronometre
    	if (Prefs.isTimerVisible(this)) {
    		chrono = new ChronoProvider(chronoText);
    		chronoText.setVisibility(TextView.VISIBLE);
    	} else {
    		chrono = new ChronoProvider();
    		chronoText.setVisibility(TextView.INVISIBLE);
    	}
    	chrono.loadFromBundle(savedInstanceState);
    	if (savedInstanceState != null) round = savedInstanceState.getInt("Pmtd_Round");
    	chrono.start();
    }

    protected abstract void nextRound();

	protected void renewRounds() {
        for(int i = 0; i < nums.length; i++) {
        	nums[i].generateOperands();
        }
        round = 0;
    	chrono.forceStart();
	}   

	protected abstract IPrefs getPrefs();

	/* (non-Javadoc)
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
    	super.onStart();
    	initiateGUI();
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		// After having pressed the Power button, onResume gets called when the screen is shut on,
		// not when the lock is gone
		KeyguardManager keymgr = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		if( keymgr.inKeyguardRestrictedInputMode()) { // screen is locked, wait for focus
			locked = true; // chrono will be resumed once focus is back (hence screen unlocked)
		} else { // screen is not locked, resume the chrono
			chrono.resume();
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
    	chrono.pause();
		locked = false; // shouldn't be necessary, but just in case...
		super.onPause();
	}
    
	/* (necessary because onResume is called before screen is actually unlocked)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus && locked) { // onResume was called while screen is locked
			chrono.resume();
			locked = false;
		}
	}

	protected void initiateGUI() {
        refreshLang(false);
        setTitle();
        
		// Prepare the GUI for user to be able to enter his answer
        operand1Label.setText(nums[round].getOperand1());
        operand2Label.setText(nums[round].getOperand2());
        operationSpinner.setSelection(mPrefs.getOperation());

    	proposedResultField.setText(nums[round].getAnswer());

    	setTriesGUI();                
    	
		setHintGUI();
		
        if (nums[round].isToBeFound() && nums[round].getTries() < Prefs.getMaxTries(this)) {
        	showSolutionButton.setEnabled(true);
        	proposedResultField.setEnabled(true);
        } else {
        	proposedResultField.setText(nums[round].getResult());
        	showSolutionButton.setEnabled(false);
        	proposedResultField.setEnabled(false);
        }
	}

	protected abstract void setTitle();

	protected void setTriesGUI() {
    	triesNumber.setText(String.format("%3d",nums[round].getTries()));
    	triesMax.setText(String.valueOf(Prefs.getMaxTries(this)));
	}
	
	protected void setHintGUI() {
		hintText.setMovementMethod(LinkMovementMethod.getInstance()); // needed to get click-able links
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
        nums[round].setAnswer(proposedResultField.getText().toString());
        for(int i = 0; i < nums.length; i++) nums[i].saveToBundle(outState);
		chrono.saveToBundle(outState);
		outState.putInt("Pmtd_Round", round);
		super.onSaveInstanceState(outState);
	}

	protected void refreshLang(boolean redraw) {
        Prefs.setLang(this, Prefs.getLang(this));	
        
        if (redraw) {
        	// force a redraw after the language has potentially changed, could be replaced by recreate() since API 11 (3.0)
        	 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        	 setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR); 
        }
	}

	protected abstract void checkResult(View v);

}
