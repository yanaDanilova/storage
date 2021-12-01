package de.danilova;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //в хендлере оставить получение обьекта, а обработку можно вынести в отдельные классы и при необходимости создать новый поток(executor service) уже в этих классах..хотя в любом случае у нетти свой пул есть
        if(msg instanceof FileMessage){
            FileMessage fileMessage = (FileMessage)msg;
            String filename = ((FileMessage) msg).getFilename();
            Path path = Paths.get("serverStorage/" + filename);
            boolean append = true;
            if(fileMessage.getPartNumber() == 1){
                append = false;
            }
            System.out.println(fileMessage.getPartNumber() + " / " + fileMessage.getPartsCount());
            FileOutputStream fos = new FileOutputStream(String.valueOf(path),append);
            fos.write(fileMessage.getData());
            fos.close();
            ctx.channel().writeAndFlush(updateServerFileList());
        }
        if(msg instanceof RequestMessage){
            String filename = ((RequestMessage) msg).getFilename();
            new Thread(()->{
                File file = new File("serverStorage/"+ filename);
                int bufSize = 10;
                int partsCount = new Long(file.length() / bufSize).intValue();
                if(file.length()% bufSize != 0){
                    partsCount++;
                }
                FileMessage fileMessage = new FileMessage(filename,-1,partsCount,new byte[bufSize]);
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    for (int i = 0; i < partsCount; i++) {
                       int readByte = fileInputStream.read(fileMessage.getData());
                       fileMessage.setPartNumber(i+1);
                        ctx.channel().writeAndFlush(fileMessage);
                        System.out.println("Отправлена часть #" + (i + 1));
                    }
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        }

        if(msg instanceof CommandMessage){
             String cmd = ((CommandMessage) msg).getCmd();
             if(cmd.startsWith("/list")){
                 ctx.channel().writeAndFlush(updateServerFileList());

             }
             if(cmd.startsWith("/delete")){
                 Path paths = Paths.get("serverStorage/"+((CommandMessage) msg).getFilename());
                 Files.deleteIfExists(paths);
                 ctx.channel().writeAndFlush(updateServerFileList());
             }
             if(cmd.startsWith("/rename")){
                 Path path1 = Paths.get("serverStorage/"+((CommandMessage) msg).getFilename());
                 Path path2 = Paths.get("serverStorage/"+((CommandMessage) msg).getNewFileName());
                 Files.move(path1,path2, StandardCopyOption.REPLACE_EXISTING);
                 ctx.channel().writeAndFlush(updateServerFileList());
             }

        }
        super.channelRead(ctx, msg);
    }

    public UpdateListMessage updateServerFileList() throws IOException {
         UpdateListMessage updateListMessage = new UpdateListMessage();
         List<String> serverFileList = Files.list(Paths.get("serverStorage")).map(p->p.getFileName().toString()).collect(Collectors.toList());
         updateListMessage.setServerFileList(serverFileList);
         return updateListMessage;
    }
}

