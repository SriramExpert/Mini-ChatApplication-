import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ChatClient {
private String serverHost = "localhost";
private int serverPort = 12345;


public static void main(String[] args) {
ChatClient client = new ChatClient();
if (args.length >= 1) client.serverHost = args[0];
if (args.length >= 2) client.serverPort = Integer.parseInt(args[1]);
client.start();
}


public void start() {
try (Socket socket = new Socket(serverHost, serverPort);
BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in))) {


// Thread to read messages from server and print them
Thread readerThread = new Thread(() -> {
try {
String s;
while ((s = serverIn.readLine()) != null) {
System.out.println(s);
}
} catch (IOException e) {
// connection closed
}
});
readerThread.setDaemon(true);
readerThread.start();


// Read user input and send to server
String input;
while ((input = userIn.readLine()) != null) {
serverOut.println(input);
if (input.equalsIgnoreCase("/quit")) {
// allow server to reply and then exit
break;
}
}


System.out.println("Disconnected.");


} catch (IOException e) {
System.err.println("Client error: " + e.getMessage());
e.printStackTrace();
}
}
}