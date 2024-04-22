package xyz.rtsvk.alfax.commands.sets;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import discord4j.common.util.Snowflake;
import discord4j.voice.AudioProvider;
import xyz.rtsvk.alfax.util.lavaplayer.LavaPlayerAudioProvider;
import xyz.rtsvk.alfax.util.lavaplayer.TrackScheduler;

import javax.sound.midi.Track;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class MusicPlayerCommandSet extends CommandSet {
	public MusicPlayerCommandSet() {
		super("music_player");

		final Map<Snowflake, Queue<Track>> queues = new HashMap<>();
		final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
		//playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
		AudioSourceManagers.registerRemoteSources(playerManager);
		final AudioPlayer player = playerManager.createPlayer();
		AudioProvider provider = new LavaPlayerAudioProvider(player);
		TrackScheduler trackScheduler = new TrackScheduler(player);
		player.addListener(trackScheduler);

		addCommand("play", (user, chat, args, guildId, bot, language) -> {

		});
	}
}
