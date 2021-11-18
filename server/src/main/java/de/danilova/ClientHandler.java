package de.danilova;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //в хендлере оставить получение обьекта, а обработку можно вынести в отдельные классы и при необходимости создать новый поток(executor service) уже в этих классах..хотя в любом случае у нетти свой пул есть
        if(msg instanceof FileMessage){
            String filename = ((FileMessage) msg).getFilename();
            byte[] file = ((FileMessage) msg).getFile();
            Files.write(Paths.get("serverStorage/" + filename), file);
            //отправить новый список файлов (которые теперь хранятся на сервере)
        }
        if(msg instanceof RequestMessage){
            String filename = ((RequestMessage) msg).getFilename();
            FileMessage fileMessage = new FileMessage(Paths.get(filename));
            ctx.channel().writeAndFlush(fileMessage);


        }
        super.channelRead(ctx, msg);
    }
}

