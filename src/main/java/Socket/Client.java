package Socket;

import Encryption.AES;
import Encryption.Crypto;
import Encryption.HybridSystem;
import Exception.*;
import Utils.IOStream;
import Utils.JSON;
import org.json.JSONObject;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;
import java.util.HashMap;

public class Client {

    private Socket socket;
    private IOStream io;
    private String host;
    private int port;

    public Client(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.socket = new Socket(host, port);
        this.io = new IOStream(this.socket);
    }

    public void send(String message) throws IOException {
        this.io.send(message);
    }

    public String receive() throws IOException {
        return this.io.receive();
    }

    public void send(String message, SecretKey secretKey) throws IOException, EncryptionException {
        io.send(Crypto.encrypt(Crypto.Cipher_AES, secretKey, message));
    }

    public String receive(SecretKey secretKey) throws IOException, DecryptionException {
        return Crypto.decrypt(Crypto.Cipher_AES, secretKey, io.receive());
    }

    public void close() throws IOException {
        if (this.io != null)
            this.io.close();
        if (this.socket != null)
            this.socket.close();
    }

    void run(HashMap<String, String> options) {

        AES aes = null;
        SecretKey secretKey = null;

        try {
            //Receive public key
            String pulbicKeyJson = receive();
            String pulbicKeyString = JSON.getString(pulbicKeyJson, "Public key");
            PublicKey publicKey = Crypto.toPublicKey(pulbicKeyString);

            //Send secret key
            aes = new AES();
            secretKey = aes.getSecretKey();
            String secretKeyJson = JSON.toJSON("Secret key", HybridSystem.encryptSecretKey(aes.getSecretKey(), publicKey));
            send(secretKeyJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String message = receive();
            System.out.println(message);
            if (message.equalsIgnoreCase("Exchanging session key successful")) {
                message = (new JSONObject(options)).toString();

                send(message, secretKey);

                message = receive(secretKey);
                System.out.println("Client receive: " + message);
            }
        } catch (EncryptionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DecryptionException e) {
            e.printStackTrace();
        }
    }

}
