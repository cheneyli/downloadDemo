package com.cheney.filedownexample.utils;

/**
 * Created by cheney on 2017/5/20.
 */

public enum CacheDir {
    FILE("/.file/");

    private String dir;

    CacheDir(String dir) {
        this.dir = dir;
    }

    @Override
    public String toString() {
        return String.valueOf(this.dir);
    }

    public String getDir() {
        return dir;
    }
}
