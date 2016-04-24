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
 * Simple challenges database access helper class. Defines the basic CRUD operations
 * for the challenges database, and gives the ability to list all challenges as well as
 * retrieve, delete or modify a specific challenge.
 */
public class HighscoresDbAdapter {

	// TODO - should we put also the user name as to be able to keep highscores even if users deleted?
    public static final String KEY_ID = "_id"; // Long/Integer - a unique ID
    public static final String KEY_CHALLENGE = "challenge_id"; // Long/Integer - ID of challenge played
    public static final String KEY_USER = "user_id"; // Long/Integer - ID of user who played the challenge (null means unknown/deleted user)
    public static final String KEY_SCORE = "score"; // Integer - score reached
    public static final String KEY_WHENDONE = "whendone"; // TimeDate - simply integer
    
    public static final int MAX_HIGHSCORES = 50; // how many high-scores do we keep?

    private PmtdDbHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */

    private static final String DATABASE_TABLE = "highscores";

    private final Context mCtx;

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public HighscoresDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the highscores database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public HighscoresDbAdapter open() throws SQLException {
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

    /** This function inserts the given score into the table if it is indeed a high-score, and the table
     * is then trimmed if necessary to the maximum number of high-scores.
     * @param userId the ID of the user who has reached the given score
     * @param challengeId the ID of the challenge for which the score has been reached
     * @param score the score reached
     * @param when the time in seconds since the EPOCH when the score was reached
     * @return the 1-based rank of the score given in the high-scores (0 means that the score wasn't high enough to be inserted)
     */
    public int createHighscore(long userId, long challengeId, int score, int when) {
    	Cursor hsCursor = fetchChallengeHighscores(challengeId);
    	int cursorCount = hsCursor.getCount();
    	
        if (hsCursor.moveToLast()) { // we move to the lowest score

	        if (cursorCount >= MAX_HIGHSCORES && hsCursor.getInt(hsCursor.getColumnIndex(KEY_SCORE)) >= score) {
	        	return 0; // too many better high-scores already...
	        }
	        while (hsCursor.getPosition() >= 0 && hsCursor.getInt(hsCursor.getColumnIndex(KEY_SCORE)) < score) {
	        	hsCursor.moveToPrevious();
	        }
	        
        } // if the cursor is empty, moveToLast == false && getPosition == -1 i.e. the following works
        
        // getPosition is zero-based, whereas the rank returned is 1-based and the new high-score
        // will be inserted _after_ the current position, hence we need to add 2 to get the rank
        int pos = hsCursor.getPosition() + 2; // not sure but prefer to save the position before inserting

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_USER, userId);
        initialValues.put(KEY_CHALLENGE, challengeId);
        initialValues.put(KEY_SCORE, score);
        initialValues.put(KEY_WHENDONE, when);
        mDb.insert(DATABASE_TABLE, null, initialValues);

        // Trim the table to MAX_HIGHSCORES before returning
        hsCursor.moveToLast();
        while (cursorCount >= MAX_HIGHSCORES) {
        	deleteHighscore(hsCursor.getLong(hsCursor.getColumnIndex(KEY_ID)));
        	hsCursor.moveToPrevious();
        	cursorCount--;
        }
        
        return pos;
    }

    /**
     * Delete the high-score with the given rowId
     * 
     * @param rowId id of high-score to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteHighscore(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + rowId, null) > 0;
    }
    
    /**
     * Return a Cursor over the list of all high-scores in the database created for a specific challenge
     * @param challengeId the id of the challenge
     * @return Cursor over all high-scores for this challenge
     */
    public Cursor fetchChallengeHighscores(long challengeId) {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_USER, KEY_SCORE, KEY_WHENDONE},
        		KEY_CHALLENGE + "=" + challengeId, null, null, null, KEY_SCORE + " DESC");
    }
    
    /**
     * Return a Cursor over a limited list of high-scores in the database created for a specific challenge.
     * The result should be the same as for {@link #fetchChallengeHighscores(long)} but this foresees the
     * possibility to change {@link #MAX_HIGHSCORES} or even make it a preference at a later stage.
     * @param challengeId the id of the challenge
     * @return Cursor over {@link #MAX_HIGHSCORES} high-scores for this challenge
     */
   public Cursor fetchChallengeHighscoresLimited(long challengeId) {

       return mDb.query(DATABASE_TABLE, null, // new String[] {KEY_ID, KEY_USER, KEY_SCORE, KEY_WHEN},
       		KEY_CHALLENGE + "=" + challengeId, null, null, null, KEY_SCORE + " DESC LIMIT " + MAX_HIGHSCORES);
   }
    
   /**
     * Function to zero all high-scores of a specific user (assuming the user is being deleted
     * but we don't want to lose their high-scores). This function is static and meant to be used only
     * from an object that has access to the same DB.
     * 
     * @param localDb a pointer to the same database
     * @param userId id of the user to wipe out
     * @return true if some high-score was successfully updated, false otherwise
     */
    protected static boolean nullifyUser(SQLiteDatabase localDb, long userId) {
        ContentValues args = new ContentValues();
        args.put(KEY_USER, (Integer) null);

        return localDb.update(DATABASE_TABLE, args, KEY_USER + "=" + userId, null) > 0;
    }
    /**
     * Delete all the high-scores of a given challenge. This function is static and meant to be used only
     * from an object that has access to the same DB.
     * 
     * @param localDb a pointer to the same database
     * @param rowId id of high-score to delete
     * @return true if deleted, false otherwise
     */
    protected static boolean deleteChallenge(SQLiteDatabase localDb, long challengeId) {

        return localDb.delete(DATABASE_TABLE, KEY_CHALLENGE + "=" + challengeId, null) > 0;
    }
}
