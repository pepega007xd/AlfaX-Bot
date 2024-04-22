package xyz.rtsvk.alfax.webserver.contentparsing;

import java.util.Map;

public interface Content {
	Map<String, Object> parse(String s);
}
