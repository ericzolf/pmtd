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

import java.util.Locale;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
// import android.util.Log;
import android.widget.TextView;

public class ChronoProvider { // TODO: fix the chrono when the app is destroyed and re-created
	// private static final String TAG = ChronoProvider.class.getSimpleName();
	
	public enum ChronoState { UNSTARTED, STARTED, PAUSED, STOPPED; }
	
	private long startTime = 0, elapsedTime = 0, stopTime = 0;
	private ChronoState state = ChronoState.UNSTARTED;
	private TextView textView;

	public ChronoProvider(TextView textView) {
		this.textView = textView;
	}
	public ChronoProvider() {
		this.textView = null;
	}
	
	public boolean start() {
		if (state == ChronoState.UNSTARTED) {
			startTime = SystemClock.elapsedRealtime();
			state = ChronoState.STARTED;
			updateTextView();
			startHandler();
			return true;
		} else {
			updateTextView();
			return false;
		}
	}
	
	public boolean unstart() {
		stopHandler();
		state = ChronoState.UNSTARTED;
		startTime = elapsedTime = 0;
		updateTextView();
		return true;
	}

	public boolean forceStart() {
		unstart();
		return start();
	}

	public boolean pause() {
		if (state == ChronoState.STARTED) {
			stopHandler();
			elapsedTime = SystemClock.elapsedRealtime() - startTime;
			state = ChronoState.PAUSED;
			updateTextView();
			return true;
		} else {
			updateTextView();
			return false;
		}
	}

	public boolean resume() {
		if (state == ChronoState.PAUSED) {
			startTime = SystemClock.elapsedRealtime() - elapsedTime;
			state = ChronoState.STARTED;
			updateTextView();
			startHandler();
			return true;
		} else {
			updateTextView();
			return false;
		}
	}

	public boolean stop() {
		if (state == ChronoState.STARTED || state == ChronoState.PAUSED) {
			stopHandler();
			stopTime = System.currentTimeMillis();
			if (state == ChronoState.STARTED) elapsedTime = SystemClock.elapsedRealtime() - startTime;
			state = ChronoState.STOPPED;
			updateTextView();
			return true;
		} else {
			updateTextView();
			return false;
		}
	}

	private long getElapsedTime() {
		if (state == ChronoState.STARTED)
			return SystemClock.elapsedRealtime() - startTime;
		else
			return elapsedTime;
	}
	private void setElapsedTime(long elapsedTime) {
		if (state == ChronoState.STARTED)
			startTime = SystemClock.elapsedRealtime() - elapsedTime;
		else
			this.elapsedTime = elapsedTime;
	}

	public int getElapsedSeconds() {
		return (int) (getElapsedTime() / 1000L); // transform milliseconds in seconds
	}
	public int getStopSeconds() {
		return (int) (stopTime / 1000L);
	}
	public ChronoState getState() {
		return state;
	}
	
	/** Returns the elapsed time as a string.
	 * @return the elapsed time in the format MM:SS
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		int secs = getElapsedSeconds();
		int min  = secs / 60;
		secs %= 60;
		return String.format(Locale.getDefault(),"%02d:%02d", min, secs);
	}

	public void saveToBundle(Bundle stateSaver) {
		stateSaver.putLong("ChronoProvider_elapsedtime", getElapsedTime());
		stateSaver.putLong("ChronoProvider_starttime", startTime);
		stateSaver.putLong("ChronoProvider_stoptime", stopTime);
		stateSaver.putSerializable("ChronoProvider_state", state);
		stopHandler();
	}

	public void loadFromBundle(Bundle stateSaver) {
		if (stateSaver == null) return;
		state = (ChronoState) stateSaver.getSerializable("ChronoProvider_state");
		startTime = stateSaver.getLong("ChronoProvider_starttime");
		stopTime = stateSaver.getLong("ChronoProvider_stoptime");
		setElapsedTime(stateSaver.getLong("ChronoProvider_elapsedtime"));
		updateTextView();
		if (state == ChronoState.STARTED)
			startHandler();
	}
	
	private Handler handler = new Handler();
	private Runnable updateTimeTask = new Runnable() {
		public void run() {
			updateTextView();
			handler.postDelayed(this, 100L);
		}
	};
	private void startHandler() {
        handler.removeCallbacks(updateTimeTask);
		if (textView != null)
			handler.postDelayed(updateTimeTask, 100L);
	}
	private void stopHandler() {
        handler.removeCallbacks(updateTimeTask);
	}
	private void updateTextView() {
		if (textView != null)
			textView.setText(toString());
	}
}
