package de.danilova;

public class CommandMessage extends AbstractMessage{
     private String cmd;
     private String filename;
     private String newFileName;

    public String getNewFileName() {
        return newFileName;
    }

    public CommandMessage(String cmd) {
        this.cmd = cmd;
    }

    public String getFilename() {
        return filename;
    }

    public CommandMessage(String cmd, String filename){
        this.cmd = cmd;
        this.filename = filename;
    }

    public CommandMessage(String cmd, String filename, String newFileName){
        this.cmd = cmd;
        this.filename = filename;
        this.newFileName = newFileName;
    }

    public String getCmd() {
        return cmd;
    }
}
