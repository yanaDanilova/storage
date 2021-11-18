package de.danilova;

public class RequestMessage extends AbstractMessage {


    private String filename;

    public RequestMessage(String filename) {

        this.filename = filename;
    }



    public String getFilename() {
        return filename;
    }



    public void setFilename(String filename) {
        this.filename = filename;
    }
}
