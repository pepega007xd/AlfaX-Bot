package xyz.rtsvk.alfax.util;

public class TextUtils {

	// create a function to generate a random string of length n
	public static String getRandomString(int n) {
		// chose a Character random from this String
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
				+ "0123456789"
				+ "abcdefghijklmnopqrstuvxyz";

		// create StringBuffer size of AlphaNumericString
		StringBuilder sb = new StringBuilder(n);

		for (int i = 0; i < n; i++) {
			// generate a random number between
			// 0 to alphabet  variable length
			int index = (int)(alphabet.length() * Math.random());

			// add Character one by one in end of sb
			sb.append(alphabet.charAt(index));
		}

		return sb.toString();
	}

}
