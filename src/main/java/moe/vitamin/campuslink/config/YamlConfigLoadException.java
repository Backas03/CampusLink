package moe.vitamin.campuslink.config;

import lombok.Getter;

import java.io.File;

@Getter
public class YamlConfigLoadException extends Exception {

    private final File file;

    public YamlConfigLoadException(File file, Exception e) {
        super(e);
        this.file = file;
    }
}
