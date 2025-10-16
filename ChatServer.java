import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChatServer {
    public static final int PORT = 12345;
    // Map of nickname -> ClientHandler
    private final ConcurrentMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public void start() {
        System.out.println("Starting chat server on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                Thread t = new Thread(handler);
                t.start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Attempt to register a nickname. Returns true if successful (unique), false otherwise.
    public boolean registerClient(String nickname, ClientHandler handler) {
        // putIfAbsent returns null if there was no mapping
        return clients.putIfAbsent(nickname, handler) == null;
    }

    public void removeClient(String nickname) {
        if (nickname != null) {
            clients.remove(nickname);
            broadcastSystemMessage(nickname + " has left the chat.");
        }
    }

    // Broadcast a normal chat message
    public void broadcast(String from, String message) {
        String formatted = from + ": " + message;
        for (ClientHandler client : clients.values()) {
            client.send(formatted);
        }
    }

    // Broadcast system (server) messages like notifications
    public void broadcastSystemMessage(String message) {
        String formatted = "[Server] " + message;
        for (ClientHandler client : clients.values()) {
            client.send(formatted);
        }
    }

    // Send private message; returns true if target found
    public boolean privateMessage(String from, String targetNick, String message) {
        ClientHandler target = clients.get(targetNick);
        if (target != null) {
            target.send("(private) " + from + ": " + message);
            // Also send confirmation to sender if present
            ClientHandler sender = clients.get(from);
            if (sender != null && sender != target) {
                sender.send("(to " + targetNick + ") " + from + ": " + message);
            }
            return true;
        }
        return false;
    }

    // Change nickname; returns true if success
    public boolean changeNickname(String oldNick, String newNick, ClientHandler handler) {
        if (newNick == null || newNick.isEmpty()) return false;
        // ensure new nick not in use
        if (clients.containsKey(newNick)) return false;
        // atomically remove old and add new (simple implementation)
        clients.remove(oldNick);
        clients.put(newNick, handler);
        broadcastSystemMessage(oldNick + " changed name to " + newNick);
        return true;
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}
