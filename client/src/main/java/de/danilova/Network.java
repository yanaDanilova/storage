package de.danilova;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Network {

    private static Socket socket;
    private static ObjectEncoderOutputStream outputStream;
    private static ObjectDecoderInputStream inputStream;


    public static void start(){
        try {
            socket = new Socket("localhost", 8189);
            outputStream = new ObjectEncoderOutputStream(socket.getOutputStream());
            inputStream = new ObjectDecoderInputStream(socket.getInputStream(), 3 * 1024 * 1024);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean sendMessage(AbstractMessage ms){
        try {
            outputStream.writeObject(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }


    public static AbstractMessage readObject() throws ClassNotFoundException, IOException {
        Object obj = inputStream.readObject();
        return (AbstractMessage) obj;
    }
}
