package com.booyue.karaoke.audioPaly.bean;

/**
 * Created by Tianluhua on 2018\8\1 0001.
 */
public class AudioBean {

    private String name;
    private String path;
    private boolean isPlaying;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    @Override
    public String toString() {
        
        return "AudioBean{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", isPlaying=" + isPlaying +
                '}';
    }
}
