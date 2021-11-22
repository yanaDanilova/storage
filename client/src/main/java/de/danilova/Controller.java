package de.danilova;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {
    public ListView clientView;
    public ListView serverView;
    public TextField textFieldSend;

    private Path clientStorage = Paths.get("clientStorage");
    private String selectedFile;
    private Path path;


    private List<String> serverFileList;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();

        try {
            if(!Files.exists(clientStorage)){
                Files.createDirectory(clientStorage);
            }
            clientView.getItems().clear();
            clientView.getItems().addAll(updateClientsFileList(clientStorage));
            clientView.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 ){
                    selectedFile = String.valueOf(clientView.getSelectionModel().getSelectedItem());
                }
            });
            serverView.getItems().clear();
            Network.sendMessage(new CommandMessage("/list"));


            serverView.setOnMouseClicked(event -> {
                if(event.getClickCount() ==2 ){
                    selectedFile = String.valueOf(serverView.getSelectionModel().getSelectedItem());
                }
            });


        }catch (IOException e){
            e.printStackTrace();
        }

        Thread thread = new Thread(()->{

            try {
                while (true){
                    AbstractMessage abstractMessage =  Network.readObject();
                    if(abstractMessage instanceof FileMessage){
                        FileMessage fileMessage = (FileMessage)abstractMessage;
                        String filename = fileMessage.getFilename();
                        Path path = Paths.get("clientStorage/" + filename);
                        Files.write(path ,fileMessage.getFile());
                        Platform.runLater(()->{
                            clientView.getItems().clear();
                            try {
                                clientView.getItems().addAll(updateClientsFileList(clientStorage));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                    }
                    if(abstractMessage instanceof UpdateListMessage){
                        serverFileList = ((UpdateListMessage) abstractMessage).getServerFileList();
                        updateServerFileList(serverFileList);
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        });
        thread.setDaemon(true);
        thread.start();

    }

    public void sendFile(ActionEvent actionEvent) {
        try {
            Path path = Paths.get("clientStorage/" + selectedFile);
            FileMessage fileMessage = new FileMessage(path);
            Network.sendMessage(fileMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> updateClientsFileList(Path path) throws IOException {
            return Files.list(clientStorage).map(p->p.getFileName().toString()).collect(Collectors.toList());
    }

    public void updateServerFileList(List<String> serverListFile){
        Platform.runLater(()->{
            serverView.getItems().clear();
            serverView.getItems().addAll(serverListFile);
        });
    }


    public void requireFile(ActionEvent actionEvent) {
        RequestMessage requestMessage = new RequestMessage(selectedFile);
        Network.sendMessage(requestMessage);
    }
}
