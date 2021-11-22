package de.danilova;

import java.util.List;

public class UpdateListMessage extends AbstractMessage{
    private List<String> serverFileList;




    public void setServerFileList(List<String> serverFileList) {
        this.serverFileList = serverFileList;
    }

    public List<String> getServerFileList() {
        return serverFileList;
    }
}
