package xyz.rtsvk.alfax.util.text;

import xyz.rtsvk.alfax.util.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class used to format strings
 * @author Jastrobaron
 */
public class FormattedString {

	/** Logger */
	private static final Logger logger = new Logger(FormattedString.class);

	/** Argument map */
	private final Map<String, Object> data;

	/** The format */
	private String format;

	/**
	 * Creates a new instance of the FormattedString object
	 * @return the FormattedString object
	 */
	public static FormattedString create() {
		return new FormattedString();
	}

	/**
	 * Creates a new instance of the FormattedString object
	 * @param format the format
	 * @return the FormattedString object
	 */
	public static FormattedString create(String format) {
		return FormattedString.create().setFormat(format);
	}

	/**
	 * Creates a new instance of the FormattedString object
	 * @param format the format
	 * @param params the parameter map
	 * @return the FormattedString object
	 */
	public static FormattedString create(String format, Map<String, Object> params) {
		return FormattedString.create(format).addParams(params);
	}

	/**
	 * Adds an argument to the formatter
	 * @param key argument name
	 * @param value argument value
	 * @return the FormattedString object
	 */
	public FormattedString addParam(String key, Object value) {
		this.data.put(key, value);
		return this;
	}

	/**
	 * Copies the parameters from a hashmap into the formatted string
	 * @param params the parameter map
	 * @return the FormattedString object
	 */
	public FormattedString addParams(Map<String, Object> params) {
		this.data.putAll(params);
		return this;
	}

	/**
	 * Builds the formatted string.
	 * @return the final output string
	 */
	public String build() {
		String output = this.format;
		for (Map.Entry<String, Object> entry : this.data.entrySet()) {
			String before = output;
			output = output.replace("${" + entry.getKey() + "}", entry.getValue().toString());
			if (before.equals(output)) {
				logger.warn("Unused parameter: " + entry.getKey());
			}
		}
		return output.trim();
	}

	public FormattedString setFormat(String format) {
		this.format = format;
		return this;
	}

	@Override
	public String toString() {
		return this.build();
	}

	/**
	 * Hidden class constructor
	 */
	private FormattedString() {
		this.format = null;
		this.data = new LinkedHashMap<>();
	}
}
