package de.danilova;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //в хендлере оставить получение обьекта, а обработку можно вынести в отдельные классы и при необходимости создать новый поток(executor service) уже в этих классах..хотя в любом случае у нетти свой пул есть
        if(msg instanceof FileMessage){
            String filename = ((FileMessage) msg).getFilename();
            byte[] file = ((FileMessage) msg).getFile();
            Files.write(Paths.get("serverStorage/" + filename), file);
            UpdateListMessage updateListMessage = new UpdateListMessage();
            updateListMessage.setServerFileList(updateServerFileList());
            ctx.channel().writeAndFlush(updateListMessage);

        }
        if(msg instanceof RequestMessage){
            String filename = ((RequestMessage) msg).getFilename();
            FileMessage fileMessage = new FileMessage(Paths.get("serverStorage/" + filename));
            ctx.channel().writeAndFlush(fileMessage);


        }

        if(msg instanceof CommandMessage){
             String cmd = ((CommandMessage) msg).getCmd();
             if(cmd.startsWith("/list")){
                 UpdateListMessage updateListMessage = new UpdateListMessage();
                 updateListMessage.setServerFileList(updateServerFileList());
                 ctx.channel().writeAndFlush(updateListMessage);
             }

        }
        super.channelRead(ctx, msg);
    }

    public List<String> updateServerFileList() throws IOException {
          List<String> serverFileList = Files.list(Paths.get("serverStorage")).map(p->p.getFileName().toString()).collect(Collectors.toList());
          return serverFileList;
    }
}

