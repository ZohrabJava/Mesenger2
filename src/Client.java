import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.math.BigInteger;

public class Client {
    final int n = 17;
    final int g = 3;
    int computes;
    int key = 0;
    volatile int number;
    private Socket socket;
    private String username;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public boolean isNum(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) > '9' || str.charAt(i) < '0') {
                return false;
            }
        }
        return true;
    }

    public String coding(String str) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            ret.append(Character.toChars(str.charAt(i) + key));
        }
        return ret.toString();
    }

    public String decoding(String str) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            ret.append(Character.toChars(str.charAt(i) - key));
        }
        return ret.toString();
    }

    public Client(String address, int port, String username) {
        try {
            this.socket = new Socket(address, port);
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.username = username;
        } catch (IOException e) {
            closeConnection();
        }
    }

    private static int calculatePower(BigInteger x, int y, BigInteger P) {

        BigInteger result = x.pow(y).mod(P);

        return Integer.parseInt(result.toString());

    }

    public void sendMessages() {
        new Thread(() -> {
            byte[] message;
            Scanner sc = new Scanner(System.in);
            try {
                byte[] name = this.username.getBytes(StandardCharsets.UTF_8);
                outputStream.writeInt(name.length);
                outputStream.write(name);
                System.out.println("Input  number for generate key ");
                number = sc.nextInt();
                System.out.println("Number is ok Wait for key");
                computes = (int) calculatePower(BigInteger.valueOf(g), number, BigInteger.valueOf(n));
                message = String.valueOf(computes).getBytes(StandardCharsets.UTF_8);
                outputStream.writeInt(message.length);
                outputStream.write(message);
                while (socket.isConnected()) {
                    String msg = sc.nextLine();
                    msg = coding(msg);
                    message = msg.getBytes(StandardCharsets.UTF_8);
                    outputStream.writeInt(message.length);
                    outputStream.write(message);
                }
            } catch (IOException e) {
                closeConnection();
            }
        }).start();
    }

    public void receiveMessages() {
        AtomicInteger count = new AtomicInteger();
        new Thread(() -> {
            try {
                while (number == 0) {
                    Thread.onSpinWait();
                }
                while (socket.isConnected()) {
                    int length = inputStream.readInt();

                    if (length > 0) {
                        byte[] message = new byte[length];
                        if (count.get() == 0) {
                            count.getAndIncrement();
                            inputStream.readFully(message, 0, length);
                            int com = Integer.parseInt((new String(message,
                                    StandardCharsets.UTF_8)).split(" ")[1]);
                            key = (int) calculatePower(BigInteger.valueOf(com),
                                    number, BigInteger.valueOf(n));
                            System.out.println("Your Key is " + key);
                        } else {
                            inputStream.readFully(message, 0, length);
                            String out = new String(message, StandardCharsets.UTF_8);
                            System.out.println("Message from " + out);
                            System.out.println("Decoding is " + decoding(out.substring(out.indexOf(" ") + 1)));
                        }
                    }
                }
            } catch (IOException e) {
                closeConnection();
            }
        }).start();
    }

    public void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter your name: ");
        String name = sc.nextLine();
        name += ": ";
        Client client = new Client("127.0.0.1", 5067, name);
        client.receiveMessages();
        client.sendMessages();

    }
}