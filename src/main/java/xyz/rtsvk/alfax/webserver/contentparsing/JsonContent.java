package xyz.rtsvk.alfax.webserver.contentparsing;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Map;

public class JsonContent implements Content {
	@Override
	public Map<String, Object> parse(String s) {
		try {
			JSONParser parser = new JSONParser();
			return (Map<String, Object>) parser.parse(s);
		}
		catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
