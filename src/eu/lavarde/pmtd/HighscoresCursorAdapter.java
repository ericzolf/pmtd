package eu.lavarde.pmtd;

import java.text.SimpleDateFormat;
import eu.lavarde.db.HighscoresDbAdapter;
import android.content.Context;
import android.database.Cursor;
import java.text.DateFormat;
import java.util.Date;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class HighscoresCursorAdapter extends CursorAdapter {
	private final LayoutInflater mInflater;
	private final int rowLayout;
    private String[] from = new String[] { HighscoresDbAdapter.KEY_ID, HighscoresDbAdapter.KEY_CHALLENGE,
    		HighscoresDbAdapter.KEY_USER, HighscoresDbAdapter.KEY_SCORE, HighscoresDbAdapter.KEY_WHEN};
    private int[] to = new int[] { R.id.place_field, R.id.challenge_field, R.id.user_field,
    		R.id.score_field, R.id.when_field };
    
	
	public HighscoresCursorAdapter(Context context, Cursor c, int layout) {
		super(context, c, false);
	    mInflater = LayoutInflater.from(context);
	    rowLayout = layout;
		// TODO open other databases in order to get name of users and challenges
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView position = (TextView) view.findViewById(R.id.place_field);
		position.setText(String.valueOf(cursor.getPosition()+1)); // getPosition is zero-based, where we want to show 1-based
		TextView challenge = (TextView) view.findViewById(R.id.challenge_field);
		challenge.setText(cursor.getString(cursor.getColumnIndex(HighscoresDbAdapter.KEY_CHALLENGE))); // TODO replace ID through name
		TextView user = (TextView) view.findViewById(R.id.user_field);
		user.setText(cursor.getString(cursor.getColumnIndex(HighscoresDbAdapter.KEY_USER))); // TODO replace ID through name
		TextView score = (TextView) view.findViewById(R.id.score_field);
		score.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(HighscoresDbAdapter.KEY_SCORE))));
		TextView when = (TextView) view.findViewById(R.id.when_field);
		DateFormat df = SimpleDateFormat.getDateTimeInstance();
		when.setText(df.format(new Date(cursor.getInt(cursor.getColumnIndex(HighscoresDbAdapter.KEY_WHEN)))));
	}

	@Override
	public View newView(Context context, Cursor c, ViewGroup parent) {
		final View view = mInflater.inflate(rowLayout, parent, false);
		return view;
	}

}
