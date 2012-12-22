/**
 * A class that provides positive (good) and negative (bad) smileys
 */
package eu.lavarde.pmtd;

import java.util.Random;

/**
 * @author Eric L.
 * 
 */
public class SmileysProvider {
	private static Random rnd = new Random();
	
	private static int good_smileys[] = {
		R.drawable.face_angel,
		R.drawable.face_devilish,
		R.drawable.face_glasses,
		R.drawable.face_grin,
		R.drawable.face_kiss,
		R.drawable.face_smile,
		R.drawable.face_smile_big,
		R.drawable.face_wink
	};
	public static int getGoodSmiley() {
		return good_smileys[rnd.nextInt(good_smileys.length)];
	}

	private static int bad_smileys[] = {
		R.drawable.face_crying,
		R.drawable.face_sad,
		R.drawable.face_surprise
	};
	public static int getBadSmiley() {
		return bad_smileys[rnd.nextInt(bad_smileys.length)];
	}

}
