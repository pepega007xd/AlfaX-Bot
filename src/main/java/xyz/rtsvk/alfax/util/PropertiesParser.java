package xyz.rtsvk.alfax.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PropertiesParser {

	private static final String ARG_ASSIGN = "=";
	private static final String COMMENT = "#";
	private final String entrySeparator;

	public PropertiesParser(String entrySeparator) {
		this.entrySeparator = entrySeparator;
	}

	public PropertiesParser() {
		this("\n");
	}

	public Map<String, Object> parse(String properties) {
		Map<String, Object> map = new HashMap<>();
		String[] lines = properties.split(this.entrySeparator);
		for (String line : lines) {
			parseLine(map, line);
		}
		return map;
	}

	public Map<String, Object> parse(File propertiesFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(propertiesFile));
		Map<String, Object> map = new HashMap<>();
		String line;
		while ((line = reader.readLine()) != null) {
			parseLine(map, line);
		}
		reader.close();
		return map;
	}

	private void parseLine(Map<String, Object> map, String line) {
		if (line.isEmpty()) return;
		if (line.startsWith(COMMENT)) return;

		int commentIdx = line.indexOf(COMMENT);
		commentIdx = commentIdx == -1 ? line.length() : commentIdx;

		int eqIdx = line.indexOf(ARG_ASSIGN);
		if (eqIdx == -1) {
			map.put(line.substring(0, commentIdx), "");
		}
		else {
			String key = line.substring(0, eqIdx);
			String value = line.substring(eqIdx + 1, commentIdx);
			map.put(key, value);
		}
	}
}
