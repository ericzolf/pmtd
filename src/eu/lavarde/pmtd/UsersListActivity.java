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

import eu.lavarde.db.PmtdDbHelper;
import eu.lavarde.db.UsersDbAdapter;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


/**
 * @author Eric L.
 *
 */
public class UsersListActivity extends ListActivity {
    private UsersDbAdapter mDbHelper;
    private Cursor mUsersCursor;
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_list);
        setTitle(getString(R.string.app_title, getString(R.string.select_user)));
        mDbHelper = new UsersDbAdapter(this);
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
		inflater.inflate(R.menu.users, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_user: {
			askUserName(0);
			break;
		}
		}
		
        return super.onOptionsItemSelected(item);
	}   

	/* --- Context Menu --- */

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.users_context, menu);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch(item.getItemId()) {
	    case R.id.select_user:
	    	selectChallenge(info.id);
	        break;
	    case R.id.rename_user:
	        askUserName(info.id);
	        break;
	    case R.id.delete_user:
	        mDbHelper.deleteUser(info.id);
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
		Cursor c = mUsersCursor;
		c.moveToPosition(position);
		selectChallenge(id);
	}

	/* --- Private Helper Functions --- */

	private void askUserName(final long userId) {
		// Taken from http://www.mkyong.com/android/android-prompt-user-input-dialog-example/
		
		// get user_dialog.xml view
		LayoutInflater li = LayoutInflater.from(this);
		View userView = li.inflate(R.layout.user_dialog, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set user_dialog.xml to the dialog builder
		alertDialogBuilder.setView(userView);

		final TextView label = (TextView) userView.findViewById(R.id.userLabel);
		final EditText name = (EditText) userView.findViewById(R.id.editUserName);

		// set dialog message
		alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok,
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
				// get user input and set it to result
				// edit text
			    	if (name.getText().length() > 0) {
			    		if (userId == 0) {
			    			mDbHelper.createUser(name.getText().toString());
			    		} else {
			    			mDbHelper.updateUser(userId, name.getText().toString());
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

		if (userId == 0) { // a new user
			alertDialogBuilder.setTitle(R.string.add_user);
			label.setText(R.string.give_user_name);
		} else {
			alertDialogBuilder.setTitle(R.string.rename_user);
			label.setText(R.string.give_new_user_name);
			Cursor c = mDbHelper.fetchUser(userId);
			name.setText(c.getString(c.getColumnIndex(UsersDbAdapter.KEY_NAME)));
		}

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}
	
	private void selectChallenge(long userId) {
		startActivity((new Intent(this, ChallengesListActivity.class)).putExtra(PmtdDbHelper.EXTRA_USERID, userId));
	}

	private void fillData() {
		// fetch all users from our database
        mUsersCursor = mDbHelper.fetchAllUsers();
        startManagingCursor(mUsersCursor);

        String[] from = new String[] { UsersDbAdapter.KEY_NAME };
        int[] to = new int[] { R.id.user_row_field };
        
        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter users =
            new SimpleCursorAdapter(this, R.layout.user_row, mUsersCursor, from, to);
        setListAdapter(users);		
	}

}
