package com.jagrosh.jmusicbot.utils;

import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public final class QueueUtil {
    private QueueUtil() {
    }

    public static void skipTrack(AudioHandler handler) {
        AudioTrack currentPlayingTrack = handler.getPlayer().getPlayingTrack();
        long currentPlayingTrackPosition = currentPlayingTrack.getPosition();
        handler.getQueue().addAt((int)currentPlayingTrackPosition, new QueuedTrack(currentPlayingTrack.makeClone(),
            currentPlayingTrack.getUserData(Long.class) == null ? 0L : currentPlayingTrack.getUserData(Long.class)));
        handler.getPlayer().stopTrack();
    }

    public static void addCurrentTrackLast(AudioHandler handler) {
        AudioTrack currentPlayingTrack = handler.getPlayer().getPlayingTrack();
        handler.getQueue().addAt(handler.getQueue().size(), new QueuedTrack(currentPlayingTrack.makeClone(),
            currentPlayingTrack.getUserData(Long.class) == null ? 0L : currentPlayingTrack.getUserData(Long.class)));
    }
}
