package eu.lavarde.util;

import eu.lavarde.pmtd.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class WipDialog { // Work in Progress dialog
	private Context ctx;
	
	public WipDialog(Context ctx) {
		this.ctx = ctx;
	}
	
	public void show() {
		new AlertDialog.Builder(ctx)
		.setCancelable(true)
		.setMessage(R.string.temp_wip)
		.setTitle(R.string.temp_sorry)
		.setIcon(R.drawable.wip)
		.setNegativeButton(android.R.string.cancel,
		  new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog,int id) {
		    	dialog.cancel();
		    }
		  }).show();
	}

}
