package Socket;

import Encryption.Crypto;
import Encryption.HybridSystem;
import Encryption.RSA;
import TimeTable.Service;
import TimeTable.TimeTable;
import Exception.DecryptionException;
import Exception.EncryptionException;
import Utils.IOStream;
import Utils.JSON;
import org.json.JSONObject;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class ServerThread extends Thread {

    private Socket socket;
    private IOStream io;
    private int clientNo;

    ServerThread(Socket inSocket, int counter) throws IOException {
        this.socket = inSocket;
        this.io = new IOStream(inSocket);
        this.clientNo = counter;
    }

    public void send(String message, SecretKey secretKey) throws IOException, EncryptionException {
        io.send(Crypto.encrpy(Crypto.Cipher_AES, secretKey, message));
    }

    public void send(String message) throws IOException {
        this.io.send(message);
    }

    public String receive(SecretKey secretKey) throws IOException, DecryptionException {
        return Crypto.decrpy(Crypto.Cipher_AES, secretKey, io.receive());
    }

    public String receive() throws IOException {
        return this.io.receive();
    }

    public void close() throws IOException {
        if (this.io != null)
            this.io.close();
        if (this.socket != null)
            this.socket.close();
    }

    public void run() {
        RSA rsa = null;
        SecretKey secretKey = null;
        String message = "Exchanging session key successful";

        try {
            //Send public key
            rsa = new RSA();
            String publicKeyJson = JSON.toJSON("Public key", rsa.getPublicKey());
            send(publicKeyJson);

            //Receive secret key
            String secretKeyJson = receive();
            String secretKeyString = JSON.getString(secretKeyJson, "Secret key");
            secretKey = HybridSystem.decrpySecretKey(secretKeyString, rsa.getPrivateKey());
        } catch (Exception e) {
            message = "Exchanging session key failed";
        }

        try {
            System.out.println(message + " Client No: " + clientNo);
            send(message);
            if (message.equalsIgnoreCase("Exchanging session key successful")) {

                message = receive(secretKey);
                System.out.println("Server receive from client No " + clientNo + ": " + message);

                JSONObject options = new JSONObject(message);
                if (options.has("subjects")) {
                    List<TimeTable> timeTables = new ArrayList<TimeTable>();
                    String errorMessage = null;
                    try {
                        timeTables = Service.schedule(Service.callAPI(JSON.getString(message, "subjects")));
                    } catch (Exception e) {
                        errorMessage = e.getMessage();
                    }
                    if (errorMessage == null)
                        message = JSON.toJSON(Service.filter(timeTables, new JSONObject(message)), errorMessage).toString();
                    else
                        message = JSON.toJSON(timeTables, errorMessage).toString();
                } else
                    message = "Subjects is required";

                send(message, secretKey);
                System.out.println("Server send to client No " + clientNo + ": " + message);
            }
            close();
        } catch (EncryptionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DecryptionException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Client No: " + clientNo + " exit!!! ");
        }

    }
}