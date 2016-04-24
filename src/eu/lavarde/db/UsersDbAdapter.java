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

import eu.lavarde.pmtd.R;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Simple users database access helper class. Defines the basic CRUD operations
 * for the users database, and gives the ability to list all users as well as
 * retrieve, delete or modify a specific user.
 */
public class UsersDbAdapter {

    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";

    private PmtdDbHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_TABLE = "users";

    private final Context mCtx;


    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public UsersDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the users database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public UsersDbAdapter open() throws SQLException {
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


    /**
     * Create a new user using the name provided. If the user is
     * successfully created return the new rowId for that user, otherwise return
     * a -1 to indicate failure.
     * 
     * @param name the name of the user
     * @return rowId or -1 if failed
     */
    public long createUser(String name) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the user with the given rowId
     * 
     * @param rowId id of user to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteUser(long rowId) {
        if (mDb.delete(DATABASE_TABLE, KEY_ID + "=" + rowId, null) > 0) {
        	ChallengesDbAdapter.nullifyUser(mDb, rowId);
        	HighscoresDbAdapter.nullifyUser(mDb, rowId);
        	ScoreEvolutionDbAdapter.deleteUser(mDb, rowId);
        	return true;
        } else {
        	return false;
        }
    }

    /**
     * Return a Cursor over the list of all users in the database
     * 
     * @return Cursor over all users
     */
    public Cursor fetchAllUsers() {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_NAME},
        		null, null, null, null, KEY_NAME);
    }

    /**
     * Return a Cursor positioned at the user that matches the given rowId
     * 
     * @param rowId id of user to retrieve
     * @return Cursor positioned to matching user, if found
     * @throws SQLException if user could not be found/retrieved
     */
    public Cursor fetchUser(long rowId) throws SQLException {

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID, KEY_NAME},
            		KEY_ID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Return the name of the user that matches the given rowId
     * 
     * @param rowId id of user to retrieve
     * @return String of matching user, if found, else null
     * @throws SQLException if user could not be found/retrieved
     */
    public String fetchUserName(long rowId) throws SQLException {

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_NAME},
            		KEY_ID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null && mCursor.moveToFirst()) {
            return mCursor.getString(mCursor.getColumnIndex(ChallengesDbAdapter.KEY_NAME));
        }
        return mCtx.getString(R.string.sign_unknown);
    }
    
    /**
     * Update the user using the details provided. The user to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of user to update
     * @param title value to set user title to
     * @param body value to set user body to
     * @return true if the user was successfully updated, false otherwise
     */
    public boolean updateUser(long rowId, String name) {
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);

        return mDb.update(DATABASE_TABLE, args, KEY_ID + "=" + rowId, null) > 0;
    }
}
