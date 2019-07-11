package com.jagrosh.jmusicbot.commands.owner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

public class ExportCmd extends OwnerCommand {
    private final Bot bot;

    public ExportCmd(Bot bot) {
        this.bot = bot;
        this.name = "export";
        this.help = "exports the current playlist";
    }

    @Override
    public void execute(CommandEvent event) {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        List<String> info = new ArrayList<>();
        AudioPlayer player = handler.getPlayer();
        if (player.getPlayingTrack() != null) {
            info.add(String.valueOf(player.getPlayingTrack().getUserData()));
            info.add(player.getPlayingTrack().getInfo().uri);
        }
        handler.getQueue().getList().stream()
            .map(QueuedTrack::getTrack
            ).forEach(track -> {
            info.add(String.valueOf(track.getUserData()));
            info.add(track.getInfo().uri);
        });

        String list = String.join("\n", info);
        try {
            bot.getPlaylistLoader().writePlaylist("exported", list);
            event.replySuccess("Successfully exported playlist");
        } catch (IOException e) {
            LoggerFactory.getLogger("ExportCmd").error("Failed to export playlist", e);
        }
        /*
        String json = new Gson().toJson(tracks);
        TextUtil.postToPasteService(json)
            .thenApply(pasteUrl -> {
                if (pasteUrl.isPresent()) {
                    String url = pasteUrl.get() + ".fredboat";
                    return String.format("exportPlaylistResulted: %s", url);
                } else {
                    return "Failed to export playlist";
                }
            })
            .thenAccept(event::reply)
            .whenComplete((ignored, t) -> {
                if (t != null) {
                    LoggerFactory.getLogger("ExportCmd").error("Failed to export to any paste service", t);
                }
            });
         */
    }
}