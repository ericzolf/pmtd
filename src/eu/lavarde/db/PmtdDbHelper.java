package eu.lavarde.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PmtdDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_CREATE_USERS =
			"create table users (" 
					+ UsersDbAdapter.KEY_ID   + " integer primary key autoincrement, "
					+ UsersDbAdapter.KEY_NAME + " text not null);";

    private static final String DATABASE_CREATE_CHALLENGES =
            "create table challenges (" 
            		+ ChallengesDbAdapter.KEY_ID   + " integer primary key autoincrement, "
            		+ ChallengesDbAdapter.KEY_NAME + " text not null, "
            		+ ChallengesDbAdapter.KEY_USER + " integer, "
            		+ ChallengesDbAdapter.KEY_ROUNDS + " integer not null, "
            		+ ChallengesDbAdapter.KEY_OPERATION + " integer not null, "
            		+ ChallengesDbAdapter.KEY_MAX + " integer not null, "
            		+ ChallengesDbAdapter.KEY_WHICHMAX + " boolean, "
            		+ ChallengesDbAdapter.KEY_PLACES + " integer not null, "
            		+ ChallengesDbAdapter.KEY_TABLE + " integer, "
            		+ ChallengesDbAdapter.KEY_BOOL1 + " boolean, "
            		+ ChallengesDbAdapter.KEY_BOOL2 + " boolean, "
            		+ "FOREIGN KEY(" + ChallengesDbAdapter.KEY_USER
        				+ ") REFERENCES users(" + UsersDbAdapter.KEY_ID
        				+ ") ON DELETE SET NULL " // TODO: not sure why it doesn't work automatically
            		+ ");";

    private static final String DATABASE_CREATE_HIGHSCORES =
            "create table highscores (" 
            		+ HighscoresDbAdapter.KEY_ID   + " integer primary key autoincrement, "
            		+ HighscoresDbAdapter.KEY_CHALLENGE + " integer not null, "
            		+ HighscoresDbAdapter.KEY_USER + " integer, "
            		+ HighscoresDbAdapter.KEY_SCORE + " integer not null, "
            		+ HighscoresDbAdapter.KEY_WHEN + " integer not null, "
            		+ "FOREIGN KEY(" + HighscoresDbAdapter.KEY_CHALLENGE 
        				+ ") REFERENCES challenges(" + ChallengesDbAdapter.KEY_ID
        				+ ") ON DELETE CASCADE, " // TODO: not sure why it doesn't work automatically
        			+ "FOREIGN KEY(" + HighscoresDbAdapter.KEY_USER
        				+ ") REFERENCES users(" + UsersDbAdapter.KEY_ID
        				+ ") ON DELETE SET NULL " // TODO: not sure why it doesn't work automatically
            		+ ");";

    private static final String DATABASE_NAME = "pmtd";
	private static final int DATABASE_VERSION = 4;

	// TODO: find a better place for these intents/extra constants
	public static final String EXTRA_USERID = "EXTRA_USERID";
	public static final String EXTRA_CHALLENGEID = "EXTRA_CHALLENGEID";

    PmtdDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Enable foreign key constraints
    	db.execSQL("PRAGMA foreign_keys = ON;");
    	
    	// Then create the databases
        db.execSQL(DATABASE_CREATE_USERS);
        db.execSQL(DATABASE_CREATE_CHALLENGES);
        db.execSQL(DATABASE_CREATE_HIGHSCORES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Enable foreign key constraints
    	db.execSQL("PRAGMA foreign_keys = ON;");

    	if (oldVersion < 2 && newVersion >= 2) {
            db.execSQL(DATABASE_CREATE_CHALLENGES); // challenges table was added with version 2
    	}
    	if (oldVersion < 3 && newVersion >= 3) {
            db.execSQL(DATABASE_CREATE_HIGHSCORES); // high-scores table was added with version 3
    	}
    	if (oldVersion < 4 && newVersion >= 4) { // FIX whendone was saved in milliseconds instead of seconds
            db.execSQL("update highscores set whendone = whendone / 1000 where whendone > 1000000000000;");
    	}
    }
}
