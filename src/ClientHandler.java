import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

    private static int count ;
    private Socket socket;
    private DataInputStream reader;
    private DataOutputStream writer;
    private String clientUsername;
    public static List<ClientHandler> clientHandlers = new ArrayList<>();

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.reader = new DataInputStream(socket.getInputStream());
            this.writer = new DataOutputStream(socket.getOutputStream());
            int len = reader.readInt();
            if (len > 0) {
                byte[] username = new byte[len];
                reader.readFully(username, 0, len);
                this.clientUsername = new String(username, StandardCharsets.UTF_8);
                System.out.println(clientUsername + "*********");
            }
            clientHandlers.add(this);
        } catch (IOException e) {
            closeConnection();
        }
    }
    @Override
    public void run() {
        while (socket.isConnected()) {
            try {
                int len = reader.readInt();
                if (len > 0) {
                    byte[] message = new byte[len];
                    reader.readFully(message, 0, len);
                    broadcastMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void broadcastMessage(byte[] message) {
        for (ClientHandler client : clientHandlers) {
            if (client != this) {
                try {
                    byte[] username = this.clientUsername.getBytes(StandardCharsets.UTF_8);

                    byte[] fullMessage = new byte[username.length + message.length];

                    System.arraycopy(username, 0, fullMessage, 0, username.length);
                    System.arraycopy(message, 0, fullMessage, username.length, message.length);

                    client.writer.writeInt(fullMessage.length);
                    client.writer.write(fullMessage);
                } catch (IOException e) {
                    closeConnection();
                }
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
    }

    public void closeConnection() {
        removeClientHandler();
        try {
            if (socket != null) {
                socket.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}