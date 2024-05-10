package xyz.rtsvk.alfax.reactions;

import discord4j.core.object.reaction.ReactionEmoji;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReactionCallbackRegister {

	private final List<IReactionCallback> callbacks;

	public ReactionCallbackRegister() {
		this.callbacks = new ArrayList<>();
	}

	public void addReactionCallback(IReactionCallback reactionCallback) {
		this.callbacks.add(reactionCallback);
	}

	public Optional<IReactionCallback> getReactionCallback(ReactionEmoji emoji) {
		return this.callbacks.stream().filter(e -> e.getEmoji().equals(emoji)).findFirst();
	}
}
