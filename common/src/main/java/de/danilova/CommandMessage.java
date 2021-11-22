package de.danilova;

public class CommandMessage extends AbstractMessage{
     private String cmd;

    public CommandMessage(String cmd) {
        this.cmd = cmd;
    }


    public String getCmd() {
        return cmd;
    }
}
