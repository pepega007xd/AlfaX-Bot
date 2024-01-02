package xyz.rtsvk.alfax.commands.implementations;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatGPTCommand implements Command {

	private final Logger logger;
	private final Config config;

	public ChatGPTCommand(Config config) {
		this.logger = new Logger(this.getClass());
		this.config = config;
	}

	@Override
	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {

		StringBuilder message = new StringBuilder();
		for (int i = 0; i < args.size(); i++) {
			message.append(args.get(i));
			if (i != args.size() - 1) message.append(" ");
		}

		Message msg = channel.createMessage("Hmmm... :thinking:\n").block();
		if (msg == null) {
			this.logger.error("Failed to send message!");
			return;
		}

		StringBuilder sb = new StringBuilder();
		OpenAiService service = new OpenAiService(this.config.getString("openai-key"));
		ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
				.messages(List.of(new ChatMessage("user", message.toString())))
				.model(this.config.getString("openai-model"))
				.build();
		service.createChatCompletion(completionRequest).getChoices()
				.forEach(e -> sb.append(e.getMessage().getContent()));

		msg.edit(spec -> spec.setContent(sb.toString())).block();
	}

	@Override
	public String getName() {
		return "chatgpt";
	}

	@Override
	public String getDescription() {
		return "Spytaj sa ChatGPT!";
	}

	@Override
	public String getUsage() {
		return "chatgpt <otazka>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("gpt");
	}
}
