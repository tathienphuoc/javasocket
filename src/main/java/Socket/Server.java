package Socket;

import java.net.*;

public class Server {
    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(6000);
            int counter = 0;
            System.out.println("Server Started ....");
            while (true) {
                counter++;
                Socket serverClient = serverSocket.accept();  //server accept the client connection request
                System.out.println(" >> " + "Client No:" + counter + " started!!!");
                ServerThread sct = new ServerThread(serverClient, counter); //send  the request to a separate thread
                sct.start();
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            serverSocket.close();
        }

    }
}