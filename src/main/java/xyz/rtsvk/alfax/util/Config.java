package xyz.rtsvk.alfax.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Config extends LinkedHashMap<String, Object> {

	private static final String ARG_PREFIX = "--";
	private static final String ARG_ASSIGN = "=";
	private static final String COMMENT = "#";

	private static final String TAG = "Config";

	public Config() {
		super();
	}

	public static Config from(String[] args) {
		Config cfg = new Config();

		// parse input args
		cfg.putAll(parse(args, true));

		if (cfg.containsKey("config")) {
			String fileName = String.valueOf(cfg.remove("config"));
			File file = new File(fileName);

			if (!file.exists()) {
				Logger.warn(TAG, "Specified config file does not exist. Skipping...");
				return cfg;
			}

			try {
				Scanner reader = new Scanner(new FileInputStream(file));
				StringBuilder content = new StringBuilder();
				while (reader.hasNextLine())
					content.append(reader.nextLine()).append("\n");
				cfg.putAll(parse(content.toString().split("\n"), false));
			}
			catch (Exception e) {
				Logger.error(TAG, "Error loading the config file: " + e.getMessage());
			}
		}

		return cfg;
	}

	private static Config parse(String[] args, boolean checkPrefix) {
		Config cfg = new Config();

		for (String arg : args) {
			if (arg.isEmpty()) continue;

			if (checkPrefix) {
				if (!arg.startsWith(ARG_PREFIX)) continue;
				else arg = arg.substring(ARG_PREFIX.length());
			}
			else if (arg.startsWith(COMMENT)) continue;

			int commentIdx = arg.indexOf(COMMENT);
			commentIdx = commentIdx == -1 ? arg.length() : commentIdx;

			int eqIdx = arg.indexOf(ARG_ASSIGN);
			if (eqIdx == -1) {
				cfg.put(arg.substring(0, commentIdx), "");
			}
			else {
				String key = arg.substring(0, eqIdx);
				String value = arg.substring(eqIdx + 1, commentIdx);

				cfg.put(key, value);
			}
		}

		return cfg;
	}

	public static Config defaultConfig() {
		Config config = new Config();

		// General configuration
		config.put("token", "your_bot_token_here");
		config.put("prefix", "!");
		config.put("scheduler-enabled", "false");

		// Weather API config
		config.put("weather-api-key", "your_weather_api_key_here");
		config.put("weather-lang", "en");

		// MySQL config
		config.put("db-host", "localhost:3306");
		config.put("db-user", "root");
		config.put("db-password", "password");
		config.put("db-name", "discord");

		// OpenAI API config
		config.put("openai-api-key", "your_openai_api_key_here");
		config.put("openai-model", "gpt-3.5-turbo");

		// HTTP server config
		config.put("webserver-port", "8080");

		// MQTT client config
		config.put("mqtt-enabled", "false");
		config.put("mqtt-uri", "mqtt://127.0.0.1:1883/");
		config.put("mqtt-user", "user");
		config.put("mqtt-password", "password");

		return config;
	}

	public void write(String filename) throws IOException {
		FileWriter writer = new FileWriter(filename);
		for (Map.Entry<String, Object> entry : this.entrySet()) {
			writer.append(entry.getKey());
			writer.append("=");
			writer.append(entry.getValue().toString());
			writer.append("\n");
		}
		writer.close();
	}

	public String getStringOrDefault(String key, String def) {
		return String.valueOf(this.getOrDefault(key, def));
	}

	public String getString(String key) {
		return getStringOrDefault(key, null);
	}

	public int getIntOrDefault(String key, int n) {
		Object value = this.get(key);
		return value != null ? Integer.parseInt(value.toString()) : n;
	}

	public int getInt(String key) {
		return getIntOrDefault(key, 0);
	}

	public boolean getBooleanOrDefault(String key, boolean b) {
		Object value = this.get(key);
		return value != null ? Boolean.parseBoolean(value.toString()) : b;
	}

	public boolean getBoolean(String key) {
		return getBooleanOrDefault(key, false);
	}
}
