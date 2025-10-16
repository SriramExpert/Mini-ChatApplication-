import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles a single client connection.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private String nickname;
    private PrintWriter out;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);

            // 1) Ask for nickname
            out.println("Welcome! Please enter your nickname:");
            while (true) {
                String requested = in.readLine();
                if (requested == null) return; // client disconnected before choosing
                requested = requested.trim();
                if (requested.isEmpty()) {
                    out.println("Nickname cannot be empty. Enter nickname:");
                    continue;
                }
                if (server.registerClient(requested, this)) {
                    nickname = requested;
                    out.println("Nickname accepted. You can start chatting!");
                    server.broadcastSystemMessage(nickname + " has joined the chat.");
                    break;
                } else {
                    out.println("Nickname already in use. Try another:");
                }
            }

            // 2) Read messages from client
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Commands
                if (line.equalsIgnoreCase("/quit")) {
                    out.println("Goodbye!");
                    break;
                }

                if (line.startsWith("/nick ")) {
                    String newNick = line.substring(6).trim();
                    if (newNick.isEmpty()) {
                        out.println("New nickname cannot be empty.");
                    } else if (server.changeNickname(nickname, newNick, this)) {
                        // update local nickname only after server accepted change
                        nickname = newNick;
                        out.println("Nickname changed to: " + nickname);
                    } else {
                        out.println("Nickname change failed (already used or invalid).");
                    }
                    continue;
                }

                // Private message syntax: @target message
                if (line.startsWith("@")) {
                    int space = line.indexOf(' ');
                    if (space > 1) {
                        String target = line.substring(1, space);
                        String msg = line.substring(space + 1);
                        boolean ok = server.privateMessage(nickname, target, msg);
                        if (!ok) {
                            out.println("User '" + target + "' not found.");
                        }
                    } else {
                        out.println("Invalid private message format. Use: @nickname message");
                    }
                    continue;
                }

                // Otherwise broadcast
                server.broadcast(nickname, line);
            }

        } catch (IOException e) {
            System.err.println("Client handler exception for " + nickname + ": " + e.getMessage());
            // e.printStackTrace(); // optionally print stacktrace
        } finally {
            // cleanup
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
            server.removeClient(nickname);
        }
    }

    /**
     * Send a message to this client.
     */
    public void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}
