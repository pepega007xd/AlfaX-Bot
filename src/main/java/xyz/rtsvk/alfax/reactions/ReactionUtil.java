package xyz.rtsvk.alfax.reactions;

import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.EmojiData;

public class ReactionUtil {
	public static ReactionEmoji getDefaultEmoji(String name) {
		return ReactionEmoji.of(EmojiData.builder().name(name).build());
	}
}
