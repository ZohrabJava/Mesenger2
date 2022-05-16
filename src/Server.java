import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;

    public Server(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execute() {
        try {
            int count=0;
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                if(count<2) {
                    System.out.println("A new client connected!");
                    ClientHandler clientHandler = new ClientHandler(socket);
                    Thread thread = new Thread(clientHandler);
                    thread.start();
                }
                count++;
            }

        } catch (IOException e) {
            closeServerConnection();
        }
    }

    public void closeServerConnection() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(5067);
        server.execute();
    }
}