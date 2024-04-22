package xyz.rtsvk.alfax.util.chat;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;

public interface Chat {
	Message sendMessage(String message);
	Message sendMessage(String message, Snowflake reference);
	Message sendMessage(EmbedCreateSpec spec);
	Snowflake getLastMessageId();
	Snowflake getInvokerMessageId();
	MessageChannel getChannel();
	String getCommandPrefix();
}
