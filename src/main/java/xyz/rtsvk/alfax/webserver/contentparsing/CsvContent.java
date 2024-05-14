package xyz.rtsvk.alfax.webserver.contentparsing;

import xyz.rtsvk.alfax.util.PropertiesParser;

import java.util.HashMap;
import java.util.Map;

public class CsvContent implements Content {

	private static final PropertiesParser parser = new PropertiesParser("&");

	@Override
	public Map<String, Object> parse(String s) {
		int index = 0;
		while (index < s.length()) {
			char currChar = s.charAt(index);
			if (currChar == '%') {
				String hex = s.substring(index + 1, index + 3);
				char ch = (char) Integer.parseInt(hex, 16);
				s = s.substring(0, index) + ch + s.substring(index + 3);
			}
			else if (currChar == '+') {
				s = s.substring(0, index) + ' ' + s.substring(index + 1);
			}
		}
		return parser.parse(s);
	}
}
