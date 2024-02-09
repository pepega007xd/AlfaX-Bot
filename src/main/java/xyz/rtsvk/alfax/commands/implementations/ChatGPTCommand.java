package xyz.rtsvk.alfax.commands.implementations;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Logger;

import java.util.List;

public class ChatGPTCommand implements Command {

	private final Logger logger;
	private final Config config;

	public ChatGPTCommand(Config config) {
		this.logger = new Logger(this.getClass());
		this.config = config;
	}

	@Override
	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {
		if (this.config.getBoolean("openai-disabled")) {
			channel.createMessage("Tento prikaz je zakazany administratorom!").block();
			return;
		}

		StringBuilder message = new StringBuilder();
		for (int i = 0; i < args.size(); i++) {
			message.append(args.get(i));
			if (i != args.size() - 1) message.append(" ");
		}

		String messageContent = message.toString();
		if (messageContent.isEmpty()) {
			channel.createMessage("Nic si sa nespytal bratm :skull:").block();
			return;
		}

		Message msg = channel.createMessage("Hmmm... :thinking:\n").block();
		if (msg == null) {
			this.logger.error("Failed to send message!");
			return;
		}

		StringBuilder output = new StringBuilder();
		OpenAiService service = new OpenAiService(this.config.getString("openai-api-key"));
		ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
				.messages(List.of(new ChatMessage(ChatMessageRole.USER.value(), messageContent)))
				.model(this.config.getString("openai-model"))
				.build();
		service.createChatCompletion(completionRequest).getChoices()
				.forEach(e -> {
					String text = e.getMessage().getContent();
					output.append(text);
				});

		String response = output.toString()
				.replace("@", "@ ");
		msg.edit(spec -> spec.setContent(response)).block();
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
