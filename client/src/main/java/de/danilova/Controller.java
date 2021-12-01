package de.danilova;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {
    public ListView clientView;
    public ListView serverView;
    public TextField textField;

    private Path clientStorage = Paths.get("clientStorage");
    private String selectedFile;
    private String selectedStorage;
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
                    selectedStorage = "clientStorage";
                }
            });
            serverView.getItems().clear();
            Network.sendMessage(new CommandMessage("/list"));


            serverView.setOnMouseClicked(event -> {
                if(event.getClickCount() ==2 ){
                    selectedFile = String.valueOf(serverView.getSelectionModel().getSelectedItem());
                    selectedStorage = "serverStorage";
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
                        boolean append = true;
                        if(fileMessage.getPartNumber() == 1){
                            append = false;
                        }
                        System.out.println(fileMessage.getPartNumber() + " / " + fileMessage.getPartsCount());
                        FileOutputStream fos = new FileOutputStream(String.valueOf(path),append);
                        fos.write(fileMessage.getData());
                        fos.close();

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
        Path path = Paths.get("clientStorage/" + selectedFile);
        new Thread(()->{
            File file = new File(String.valueOf(path));
            int bufSize = 10;
            int partsCount = new Long(file.length() / bufSize).intValue();
            if(file.length()% bufSize != 0){
                partsCount++;
            }
            FileMessage fileMessage = new FileMessage(selectedFile,-1,partsCount,new byte[bufSize]);
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                for (int i = 0; i < partsCount; i++) {
                    int readByte = fileInputStream.read(fileMessage.getData());
                    fileMessage.setPartNumber(i+1);
                    Network.sendMessage(fileMessage);
                    System.out.println("Отправлена часть #" + (i + 1));
                }
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
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
        selectedFile = null;
    }

    public void deleteFile(ActionEvent actionEvent) throws IOException {
        if(selectedFile != null){
            if(selectedStorage.equals("clientStorage")){
                Path path = Paths.get(selectedStorage + "/" + selectedFile);
                Files.deleteIfExists(path);
                Platform.runLater(()->{
                    clientView.getItems().clear();
                    try {
                        clientView.getItems().addAll(updateClientsFileList(clientStorage));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            }else {
                Network.sendMessage(new CommandMessage("/delete", selectedFile));
            }
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR,"you must select a file!");
        }
    }



    public void renameFile(ActionEvent actionEvent) {
        if(selectedFile != null){
            String newFileName = textField.getText();
            if(selectedStorage.equals("clientStorage")){
                Path path1 = Paths.get(selectedStorage + "/" + selectedFile);
                Path path2 = Paths.get(selectedStorage + "/" + newFileName);
                try {
                    Files.move(path1,path2, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Platform.runLater(()->{
                    clientView.getItems().clear();
                    try {
                        clientView.getItems().addAll(updateClientsFileList(clientStorage));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            }else {
                Network.sendMessage(new CommandMessage("/rename", selectedFile, newFileName));
            }
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR,"you must select a file!");
        }
    }
}
