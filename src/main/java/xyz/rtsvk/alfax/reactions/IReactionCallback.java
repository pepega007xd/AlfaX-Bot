package xyz.rtsvk.alfax.reactions;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import xyz.rtsvk.alfax.util.text.MessageManager;

public interface IReactionCallback {
	void handle(Message message, User whoClicked, MessageManager lang, int reactionsCount) throws Exception;
	ReactionEmoji getEmoji();
}
