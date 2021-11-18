package de.danilova;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileMessage extends AbstractMessage {

    private String filename;
    private byte[] file;

    public FileMessage(Path path) throws IOException {
        this.filename = path.getFileName().toString();
        this.file = Files.readAllBytes(path);
    }


    public String getFilename() {
        return filename;
    }

    public byte[] getFile() {
        return file;
    }
}



