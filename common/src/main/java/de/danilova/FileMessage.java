package de.danilova;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileMessage extends AbstractMessage {

    private String filename;
    private int partNumber;
    private int partsCount;
    private byte[] data;

    public FileMessage(String filename, int partNumber, int partsCount, byte[] data) {
        this.filename = filename;
        this.partNumber = partNumber;
        this.partsCount = partsCount;
        this.data = data;
    }

    public String getFilename() {
        return filename;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public int getPartsCount() {
        return partsCount;
    }

    public byte[] getData() {
        return data;
    }


    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }






}



