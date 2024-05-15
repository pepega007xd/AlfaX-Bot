package xyz.rtsvk.alfax.webserver.contentparsing;

import java.util.Map;

public interface IContent {
	Map<String, Object> parse(String s);
}
