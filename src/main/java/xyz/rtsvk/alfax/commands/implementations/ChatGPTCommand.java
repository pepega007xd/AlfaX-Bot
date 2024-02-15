package xyz.rtsvk.alfax.commands.implementations;

import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.rtsvk.alfax.commands.Command;
import xyz.rtsvk.alfax.util.Config;

import java.time.Duration;
import java.util.List;

public class ChatGPTCommand implements Command {

	private final Config config;

	public ChatGPTCommand(Config config) {
		this.config = config;
	}

	@Override
	public void handle(User user, Snowflake messageId, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) throws Exception {
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

		StringBuilder output = new StringBuilder();
		OpenAiService service = new OpenAiService(
				this.config.getString("openai-api-key"),
				Duration.ofSeconds(this.config.getInt("openai-timeout")));
		ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
				.messages(List.of(new ChatMessage(ChatMessageRole.USER.value(), messageContent)))
				.model(this.config.getString("openai-model"))
				.build();
		List<ChatCompletionChoice> choices = service.createChatCompletion(completionRequest).getChoices();
		choices.forEach(e -> {
			String text = e.getMessage().getContent();
			output.append(text);
		});

		String response = output.toString()
				.replace("@", "@\u200D");   // Prevent mentions
		String[] chunks = splitToChunks(response, 2000);
		for (String chunk : chunks) {
			channel.createMessage(chunk).withMessageReference(messageId).block();
		}
	}

	private String[] splitToChunks(String response, int chunkSize) {
		int length = response.length();
		int chunkCount = (length + chunkSize - 1) / chunkSize;
		String[] chunks = new String[chunkCount];
		for (int i = 0; i < chunkCount; i++) {
			int start = i * chunkSize;
			int end = Math.min(length, start + chunkSize);
			chunks[i] = response.substring(start, end);
		}
		return chunks;
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
