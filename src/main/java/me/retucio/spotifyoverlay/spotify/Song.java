package me.retucio.spotifyoverlay.spotify;

import java.util.List;

public class Song {

    private final String name;
    private final List<String> artists;
    private final int duration;
    private final boolean empty;

    public Song(String name, List<String> artists, int duration, boolean empty) {
        this.name = name;
        this.artists = artists;
        this.duration = duration;
        this.empty = empty;
    }

    public static Song empty() {
        return new Song("", List.of(), -1, true);
    }

    public String name() {
        return name;
    }

    public List<String> artists() {
        return artists;
    }

    public int duration() {
        return duration;
    }

    public boolean isEmpty() {
        return empty;
    }
}