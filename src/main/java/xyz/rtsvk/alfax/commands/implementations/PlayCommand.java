package xyz.rtsvk.alfax.commands.implementations;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.voice.AudioProvider;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.lavaplayer.TrackScheduler;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.List;

public class PlayCommand implements ICommand {


	private final AudioPlayerManager playerManager;
	private final AudioPlayer player;
	private final AudioProvider provider;
	private final TrackScheduler scheduler;

	public PlayCommand(AudioPlayerManager playerManager, AudioPlayer player, AudioProvider provider, TrackScheduler scheduler) {
		this.playerManager = playerManager;
		this.player = player;
		this.provider = provider;
		this.scheduler = scheduler;
	}

	@Override
	public void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) throws Exception {
		if (args.isEmpty()) {
			chat.sendMessage("Please provide a link to a song.");
			return;
		}

		String link = args.get(0);
		if (!link.startsWith("http")) {
			link = "ytsearch:" + link;
		}

		playerManager.loadItem(link, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack audioTrack) {
				chat.sendMessage("Adding to queue: " + audioTrack.getInfo().title);
				scheduler.queue(audioTrack);
			}

			@Override
			public void playlistLoaded(AudioPlaylist audioPlaylist) {

			}

			@Override
			public void noMatches() {

			}

			@Override
			public void loadFailed(FriendlyException e) {

			}
		});
	}

	@Override
	public String getName() {
		return "play";
	}

	@Override
	public String getDescription() {
		return "Joins the voice channel you are in and plays the specified song.";
	}

	@Override
	public String getUsage() {
		return "play <link>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("p");
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
