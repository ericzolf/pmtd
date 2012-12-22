package eu.lavarde.pmtd;

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
            		+ ChallengesDbAdapter.KEY_USER + " integer REFERENCES users(_id) ON DELETE SET NULL, " // TODO: not sure it works automatically
            		+ ChallengesDbAdapter.KEY_ROUNDS + " integer not null, "
            		+ ChallengesDbAdapter.KEY_OPERATION + " integer not null, "
            		+ ChallengesDbAdapter.KEY_MAX + " integer not null, "
            		+ ChallengesDbAdapter.KEY_WHICHMAX + " boolean, "
            		+ ChallengesDbAdapter.KEY_PLACES + " integer not null, "
            		+ ChallengesDbAdapter.KEY_TABLE + " integer, "
            		+ ChallengesDbAdapter.KEY_BOOL1 + " boolean, "
            		+ ChallengesDbAdapter.KEY_BOOL2 + " boolean "
            		+ ");";

    private static final String DATABASE_NAME = "pmtd";
	private static final int DATABASE_VERSION = 2;

	// TODO: find a better place for these intents/extra constants
	public static final String EXTRA_USERID = "EXTRA_USERID";
	public static final String EXTRA_CHALLENGEID = "EXTRA_CHALLENGEID";

    PmtdDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_USERS);
        db.execSQL(DATABASE_CREATE_CHALLENGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	if (oldVersion < 2 && newVersion >= 2) {
            db.execSQL(DATABASE_CREATE_CHALLENGES); // challenges table was added with version 2
    	}
    }
    
}
