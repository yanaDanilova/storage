package de.danilova;

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


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();

        try {
            if(!Files.exists(clientStorage)){
                Files.createDirectory(clientStorage);
            }
            clientView.getItems().clear();
            clientView.getItems().addAll(getFile(clientStorage));
            clientView.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 ){
                    selectedFile = String.valueOf(clientView.getSelectionModel().getSelectedItem());
                }
            });
            serverView.getItems().clear();
            //как запросить у сервера какие файлы на нем хранятся
            serverView.getItems().addAll();
            clientView.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 ){
                    selectedFile = String.valueOf(clientView.getSelectionModel().getSelectedItem());
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

    public List<String> getFile(Path path) throws IOException {
        return Files.list(clientStorage).map(p->p.getFileName().toString()).collect(Collectors.toList());
    }


    public void requireFile(ActionEvent actionEvent) {
        RequestMessage requestMessage = new RequestMessage(selectedFile);
        Network.sendMessage(requestMessage);
    }
}
