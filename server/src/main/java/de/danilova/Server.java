package de.danilova;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

;

public class Server {
    public static void main(String[] args) {
        new Server().run();
    }

    public void run(){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workedGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup,workedGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            System.out.println("client connected");
                            channel.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(3*1024*1024, ClassResolvers.cacheDisabled(null)),
                                    new ClientHandler()
                            );
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(8189).sync();
            //при старте программы подкоючатся к базе данных
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workedGroup.shutdownGracefully();
        }
    }

}
