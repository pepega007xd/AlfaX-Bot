package xyz.rtsvk.alfax.webserver.contentparsing;

import java.util.HashMap;
import java.util.Map;

public class FormContent implements Content {
	@Override
	public Map<String, Object> parse(String s) {
		Map<String, Object> map = new HashMap<>();
		String[] arr = s.split("&");


		// key1=value1&key2=value2....
		for (String line : arr)
			map.put(
					line.substring(0, line.indexOf("=")),
					line.substring(line.indexOf("=") + 1)
			);

		return map;
	}
}
