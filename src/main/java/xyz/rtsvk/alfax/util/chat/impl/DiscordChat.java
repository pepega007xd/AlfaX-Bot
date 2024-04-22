package xyz.rtsvk.alfax.util.chat.impl;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import xyz.rtsvk.alfax.util.chat.Chat;

import java.util.Optional;

public class DiscordChat implements Chat {

	private final MessageChannel discordChannel;
	private final Snowflake invokerMessageId;
	private Snowflake lastMessageId;
	private final String commandPrefix;

	public DiscordChat(MessageChannel discordChannel, Snowflake invokerMessageId, String commandPrefix) {
		this.discordChannel = discordChannel;
		this.invokerMessageId = invokerMessageId;
		this.commandPrefix = commandPrefix;
		this.lastMessageId = null;
	}

	@Override
	public Message sendMessage(String message) {
		return sendMessage(message, this.invokerMessageId);
	}

	@Override
	public Message sendMessage(String message, Snowflake reference) {
		Message msg = this.discordChannel.createMessage(message).withMessageReference(reference).block();
		if (msg != null) {
			this.lastMessageId = msg.getId();
		}
		return msg;
	}

	@Override
	public Message sendMessage(EmbedCreateSpec spec) {
		return this.discordChannel.createMessage(spec).withMessageReference(this.invokerMessageId).block();
	}

	@Override
	public MessageChannel getChannel() {
		return this.discordChannel;
	}

	@Override
	public Snowflake getLastMessageId() {
		return this.lastMessageId;
	}

	@Override
	public Snowflake getInvokerMessageId() {
		return this.invokerMessageId;
	}

	@Override
	public String getCommandPrefix() {
		return this.commandPrefix;
	}
}
