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
/* Most of the code has been shamelessly stolen from the Notepad tutorial 
 * under http://developer.android.com/training/notepad/index.html
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package eu.lavarde.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Simple score evolutions database access helper class. Defines the basic CRUD operations
 * for the score evolution database, and gives the ability to list all scores over time and/or consolidate
 * them, and of course add new scores.
 */
public class ScoreEvolutionDbAdapter {

	public static final String KEY_ID = "_id"; // Long/Integer - a unique ID
	public static final String KEY_CHALLENGE = "challenge_id"; // Long/Integer - ID of challenge played
	public static final String KEY_USER = "user_id"; // Long/Integer - ID of user who played the challenge (null means unknown/deleted user)
	public static final String KEY_SCORE = "score"; // Integer - score reached
	public static final String KEY_WHENDONE = "whendone"; // TimeDate - simply integer seconds since EPOCH
	public static final String KEY_DURATION = "duration"; // Integer - how many seconds needed to answer
	public static final String KEY_TRIES = "tries"; // Integer - number of tries, the less the better
	public static final String KEY_FOUND = "found"; // Integer - how many solutions found
	public static final String KEY_FAILED = "failed"; // Integer - how many solutions not found
	public static final String KEY_QTY = "qty"; // Integer - number of challenges wrapped into this entry, used as weight
	public static final String KEY_TYPE = "type"; // Integer - 0 - single, 1 - day, 2 - week, 3 - month, 4 - year TODO!

	private PmtdDbHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final int YEAR_TYPE   = 4;
	private static final int MONTH_TYPE  = 3;
	private static final int WEEK_TYPE   = 2;
	private static final int DAY_TYPE    = 1;
	private static final int SINGLE_TYPE = 0;

	/**
	 * Database creation sql statement
	 */

	private static final String DATABASE_TABLE = "evoscores";

	private final Context mCtx;

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public ScoreEvolutionDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	/**
	 * Open the scores database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException if the database could be neither opened or created
	 */
	public ScoreEvolutionDbAdapter open() throws SQLException {
		mDbHelper = new PmtdDbHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();

		// Enable foreign key constraints
		if (!mDb.isReadOnly()) {
			mDb.execSQL("PRAGMA foreign_keys = ON;");
		}

		return this;
	}

	public void close() {
		mDbHelper.close();
	}
	/** This function inserts the given score into the table, and the table is then consolidated.
	 * @param userId the ID of the user who has reached the given score
	 * @param challengeId the ID of the challenge for which the score has been reached
	 * @param score the score reached
	 * @param whendone when was the score reached
	 * @param duration how much time did it take to finalize the challenge
	 * @param tries number of tries needed
	 * @param found number of results found
	 * @param failed number of results not found
	 * @return true if the insertion was successful, false otherwise
	 */
	public boolean createScore(long userId, long challengeId, int score, int whendone, int duration, int tries,
			int found, int failed) {

		boolean result = createScore(userId, challengeId, score, whendone, duration, tries, found, failed, 1, SINGLE_TYPE);

		// TODO aggregate table in an asynchronous manner or somewhere else, less critical/often?
		aggregateScores(userId, challengeId);

		return result;
	}

	/** This function inserts the given score into the table.
	 * @param userId the ID of the user who has reached the given score
	 * @param challengeId the ID of the challenge for which the score has been reached
	 * @param score the score reached
	 * @param whendone when was the score reached
	 * @param duration how much time did it take to finalize the challenge
	 * @param tries number of tries needed
	 * @param found number of results found
	 * @param failed number of results not found
	 * @param qty how many entries rolled-up into this entry, used as weight
	 * @param type 0 - single, 1 - day, 2 - week, 3 - month, 4 - year
	 * @return true if the insertion was successful, false otherwise
	 */
	private boolean createScore(long userId, long challengeId, int score, int whendone, int duration, int tries,
			int found, int failed, int qty, int type) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_USER, userId);
		initialValues.put(KEY_CHALLENGE, challengeId);
		initialValues.put(KEY_SCORE, score);
		initialValues.put(KEY_WHENDONE, whendone);
		initialValues.put(KEY_DURATION, duration);
		initialValues.put(KEY_TRIES, tries);
		initialValues.put(KEY_FOUND, found);
		initialValues.put(KEY_FAILED, failed);
		initialValues.put(KEY_QTY, qty);
		initialValues.put(KEY_TYPE, type);
		return mDb.insert(DATABASE_TABLE, null, initialValues) >= 0;
	}

	/** This function aggregate the scores according to their age to make sure there are not too many entries
	 * in the database.
	 * @param userId the ID of the user for which to aggregate scores
	 * @param challengeId the ID of the challenge for which to aggregate scores
	 * @return true if the aggregation was successful, false otherwise
	 */
	private boolean aggregateScores(long userId, long challengeId) {
		Boolean successful = true;
		// Replace now with a constant time value as to avoid time slippage
		String Now = String.valueOf((int) (System.currentTimeMillis()/1000L));
		/* UN*X shell command to output second limits compared to current time (useful for validating tests) 
		   now=$(date +%s); sqlite3 /dev/null "select
		   strftime('%s', '${now}', 'unixepoch', '-10 years', 'start of year') as '10 years',
		   strftime('%s', '${now}', 'unixepoch', '-1 year', 'start of year') as '1 year',
		   strftime('%s', '${now}', 'unixepoch', '-3 months', 'start of month') as '3 months',
		   strftime('%s', '${now}', 'unixepoch', '-1 month', 'start of month') as '1 month';"
		 */
		successful &= aggregateScoresinTimeRange(userId, challengeId, YEAR_TYPE,
				new String[] {Now, "-10 years", "start of year", String.valueOf(YEAR_TYPE)}, "%Y");
		successful &= aggregateScoresinTimeRange(userId, challengeId, MONTH_TYPE,
				new String[] {Now, "-1 year", "start of year", String.valueOf(MONTH_TYPE)}, "%Y%m");
		successful &= aggregateScoresinTimeRange(userId, challengeId, WEEK_TYPE,
				new String[] {Now, "-3 months", "start of month", String.valueOf(WEEK_TYPE)}, "%Y%W");
		successful &= aggregateScoresinTimeRange(userId, challengeId, DAY_TYPE,
				new String[] {Now, "-1 month", "start of month", String.valueOf(DAY_TYPE)}, "%Y%j");
		return successful;
	}

	/** This function aggregate the scores according to their age to make sure there are not too many entries
	 * in the database.
	 * @param userId the ID of the user for which to aggregate scores
	 * @param challengeId the ID of the challenge for which to aggregate scores
	 * @return true if the aggregation was successful, false otherwise
	 */
	private boolean aggregateScoresinTimeRange(long userId, long challengeId, int type, String[] whereArgs, String groupBy) {
		ContentValues lValues = new ContentValues();
		Boolean successful = true;
		// those two values won't change during the course of the aggregation
		lValues.put(KEY_USER, userId);
		lValues.put(KEY_CHALLENGE, challengeId);
		lValues.put(KEY_TYPE, type);

		mDb.beginTransaction();
		try {
			Cursor lCursor;

			// first make sure that there is at least one row to make an average of
			lCursor = mDb.query(DATABASE_TABLE,new String[] { "SUM("+KEY_QTY+")" },
					KEY_USER + "=" + userId + " AND " + KEY_CHALLENGE + "=" + challengeId + " AND "
							+ KEY_WHENDONE + " < strftime('%s', ?, 'unixepoch', ?, ?)" + " AND "
							+ KEY_TYPE + " < ?", whereArgs,
							null, null, null
					);
			if ( (! lCursor.moveToFirst()) || lCursor.isNull(0) || lCursor.getInt(0) == 0) {
				return successful; // sum(qty) is 0 hence nothing to aggregate, avoid division by zero
			}

			// then get all these averages (the quantity is summed not averaged)
			lCursor = mDb.query(DATABASE_TABLE,new String[] {
					wAvg(KEY_SCORE), wAvg(KEY_WHENDONE), wAvg(KEY_DURATION), wAvg(KEY_TRIES), wAvg(KEY_FOUND), wAvg(KEY_FAILED),
					"SUM("+KEY_QTY+")"},
					KEY_USER + "=" + userId + " AND " + KEY_CHALLENGE + "=" + challengeId + " AND "
							+ KEY_WHENDONE + " < strftime('%s', ?, 'unixepoch', ?, ?)" + " AND "
							+ KEY_TYPE + "< ?", whereArgs,
							"strftime('" + groupBy + "'," + KEY_WHENDONE + ",'unixepoch')", null, null
					); // TODO continue to implement aggregation queries possibly using pre-compiled query
			if (lCursor.moveToFirst()) {
				while (! lCursor.isAfterLast()) {
					lValues.put(KEY_SCORE, lCursor.getInt(0));
					lValues.put(KEY_WHENDONE, lCursor.getInt(1));
					lValues.put(KEY_DURATION, lCursor.getInt(2));
					lValues.put(KEY_TRIES, lCursor.getInt(3));
					lValues.put(KEY_FOUND, lCursor.getInt(4));
					lValues.put(KEY_FAILED, lCursor.getInt(5));
					lValues.put(KEY_QTY, lCursor.getInt(6));
					successful &= mDb.insert(DATABASE_TABLE, null, lValues) >= 0;
					lCursor.moveToNext();
				}
			}
			if (successful) { // if all new values have been properly saved, delete the old ones
				mDb.delete(DATABASE_TABLE,KEY_USER + "=" + userId + " AND " + KEY_CHALLENGE + "=" + challengeId + " AND "
						+ KEY_WHENDONE + " < strftime('%s', ?, 'unixepoch', ?, ?)" + " AND "
						+ KEY_TYPE + "< ?", whereArgs);
				mDb.setTransactionSuccessful();
			}
		} finally {
			mDb.endTransaction();
		}
		return successful;
	}

	/**
	 * Delete the score with the given rowId
	 * 
	 * @param rowId id of score to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteScore(long rowId) {

		return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all scores in the database created for a specific challenge
	 * @param challengeId the id of the challenge
	 * @return Cursor over all high-scores for this challenge
	 */
	public Cursor fetchScores(long userId, long challengeId) {

		return mDb.query(DATABASE_TABLE, null, // querying all columns
				KEY_CHALLENGE + "=" + challengeId + " AND " + KEY_USER + "=" + userId,
				null, null, null, KEY_SCORE + " DESC");
	}

	/**
	 * Delete all the scores of a specific user. This function is static and meant to be used only
	 * from an object that has access to the same DB.
	 * 
	 * @param localDb a pointer to the same database
	 * @param userId id of the user to delete
	 * @return true if deleted, false otherwise
	 */
	protected static boolean deleteUser(SQLiteDatabase localDb, long userId) {
		return localDb.delete(DATABASE_TABLE, KEY_USER + "=" + userId, null) > 0;
	}

	/**
	 * Delete all the scores of a given challenge. This function is static and meant to be used only
	 * from an object that has access to the same DB.
	 * 
	 * @param localDb a pointer to the same database
	 * @param rowId id of high-score to delete
	 * @return true if deleted, false otherwise
	 */
	protected static boolean deleteChallenge(SQLiteDatabase localDb, long challengeId) {

		return localDb.delete(DATABASE_TABLE, KEY_CHALLENGE + "=" + challengeId, null) > 0;
	}


	/**
	 * Helper function to create an SQL statement weighting the values of a column by KEY_QTY
	 *  
	 * @param valuecol name of the column which needs to be averaged
	 * @return the SQL string providing the weighted average
	 */
	private String wAvg(String valuecol) {
		return "sum(" + valuecol + "*" + KEY_QTY + ") / sum(" + KEY_QTY + ") AS avg_" + valuecol;
	}
}
