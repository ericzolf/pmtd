package eu.lavarde.util;

import org.apache.http.protocol.HTTP;
import eu.lavarde.pmtd.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Display;
import android.view.WindowManager;

public class AboutDialog {
	private Context ctx;
	private String app_name;
	private String app_version;
	private String android_version;
	private String screen_size;
	
	public AboutDialog(Context ctx) {
		this.ctx = ctx;
		try
		{
			app_name = ctx.getPackageName();
		    app_version = ctx.getPackageManager().getPackageInfo(app_name, 0).versionName;
		}
		catch (NameNotFoundException e)
		{
		    app_version = "NOT FOUND";
		}
		android_version = android.os.Build.VERSION.RELEASE + "(" + android.os.Build.VERSION.CODENAME + ")";
		Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		screen_size = display.getWidth() + "x" + display.getHeight();
	}
	
	public void show() {
		new AlertDialog.Builder(ctx)
		.setTitle(R.string.app_name)
		.setIcon(ctx.getResources().getDrawable(R.drawable.icon))
		.setMessage(ctx.getString(R.string.app_name) + " " + app_version + "\n"
				+ ctx.getString(R.string.about_text))
		.setPositiveButton(android.R.string.ok,null)
		.setNeutralButton(R.string.about_enhancement, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendEmailWithInfo(ctx.getString(R.string.about_subject_enhancement), ctx.getString(R.string.about_text_enhancement));
			}
		})
		.setNegativeButton(R.string.about_bug, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendEmailWithInfo(ctx.getString(R.string.about_subject_bug), ctx.getString(R.string.about_text_bug));
			}
		})
		.show();
	}

	private void sendEmailWithInfo(String subject, String text) {
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		// The intent does not have a URI, so declare the "text/plain" MIME type
		emailIntent.setType(HTTP.PLAIN_TEXT_TYPE);
		emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"android@lavar.de"}); // recipients
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, app_name + "/" + subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, text + "\n\n"
				+ "App version:     " + app_version + "\n"
				+ "Android version: " + android_version + "\n"
				+ "Screen WxH:      " + screen_size);
		// NOT NEEDED: emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://path/to/email/attachment"));
		// You can also attach multiple items by passing an ArrayList of Uris
		
		// Create and start the chooser
		ctx.startActivity( // start with chooser made of intent and title
				Intent.createChooser(emailIntent, ctx.getString(R.string.about_chooser))
				);	
	}
}
