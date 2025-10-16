Project: Multithreaded Chat Application (Java Sockets + Multithreading)
Files included below:
- ChatServer.java
- ClientHandler.java
- ChatClient.java


How to compile & run (command-line):
1) Save each file into its own .java file (names above).
2) Compile:
javac ChatServer.java ClientHandler.java ChatClient.java
3) Start server (default port 12345):
java ChatServer
4) Start clients (in separate terminals):
java ChatClient


Notes:
- Clients first choose a nickname. Nicknames must be unique.
- Send messages and they will be broadcast to all connected clients.
- Private message: start the message with @nickname (example: @alex Hello)
- Commands: /quit to leave, /nick newname to change nickname.


This is a console (terminal) chat. You can run many clients to test.
