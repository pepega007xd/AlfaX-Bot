package xyz.rtsvk.alfax.webserver.contentparsing;

import java.util.HashMap;
import java.util.Map;

public class FormContent implements Content {
	@Override
	public Map<String, Object> parse(String s) {
		Map<String, Object> map = new HashMap<>();

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

		// key1=value1&key2=value2....
		String[] arr = s.split("&");
		for (String entry : arr) {
			String key = entry.substring(0, entry.indexOf("="));
			String value = entry.substring(entry.indexOf("=") + 1);

			map.put(key, value);
		}
		return map;
	}
}
