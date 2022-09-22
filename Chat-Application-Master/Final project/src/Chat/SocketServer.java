/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat;

import java.io.*;
import java.net.*;


class ServerThread extends Thread {

    public SocketServer server = null;
    public Socket socket = null;
    public int ID = -1;
    public String username = "";
    public ObjectInputStream streamIn = null;
    public ObjectOutputStream streamOut = null;
    public ServerForm ui;

    public ServerThread(SocketServer _server, Socket _socket) {
        super();
        server = _server;
        socket = _socket;
        ID = socket.getPort();
        ui = _server.ui;
    }

    public void send(Message msg) {
        try {
            streamOut.writeObject(msg);
            streamOut.flush();
        } catch (IOException ex) {
            System.out.println("Exception [SocketClient : send(...)]");
        }
    }

    public int getID() {
        return ID;
    }

    @SuppressWarnings("deprecation")
    public void run() {
        ui.display.append("\nServer Thread " + ID + " running.");
        while (true) {
            try {
                Message msg = (Message) streamIn.readObject();
                server.handle(ID, msg);
            } catch (Exception ioe) {
                System.out.println(ID + " ERROR reading: " + ioe.getMessage());
                server.remove(ID);
                stop();
            }
        }
    }

    public void open() throws IOException {
        streamOut = new ObjectOutputStream(socket.getOutputStream());
        streamOut.flush();
        streamIn = new ObjectInputStream(socket.getInputStream());
    }

    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (streamIn != null) {
            streamIn.close();
        }
        if (streamOut != null) {
            streamOut.close();
        }
    }
}

public class SocketServer implements Runnable {

    public ServerThread clients[];
    public ServerSocket server = null;
    public Thread thread = null;
    public int clientCount = 0, port = 2020;
    public ServerForm ui;
    public Database db;
    public ObjectInputStream In;
    public ObjectOutputStream Out;

    public SocketServer(ServerForm frame) {

        clients = new ServerThread[50];
        ui = frame;
        db = new Database(ui.filePath);
            try {
            server = new ServerSocket(port);
            port = server.getLocalPort();
            ui.display.append("Server started. IP address: " + InetAddress.getLocalHost() + ", port : " + server.getLocalPort());
            start();
        } catch (IOException ioe) {
            ui.display.append("Can not connect to port: " + port + "\nRetrying");
            ui.RetryStart(0);
        }
    }

    public SocketServer(ServerForm frame, int Port) {

        clients = new ServerThread[50];
        ui = frame;
        port = Port;
        db = new Database(ui.filePath);
        
        
        try {
            server = new ServerSocket(port);
            port = server.getLocalPort();
            ui.display.append("Server startet. IP : " + InetAddress.getLocalHost() + ", Port : " + server.getLocalPort());
            start();
            
        } catch (IOException ioe) {
            ui.display.append("\nCan not connect to port " + port + ": " + ioe.getMessage());
        }
    }

    public void run() {
        while (thread != null) {
            try {
                ui.display.append("\nWaiting for a client ...");
                addThread(server.accept());
            } catch (Exception ioe) {
                ui.display.append("\nServer accept error: \n");
                ui.RetryStart(0);
            }
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }

    private int findClient(int ID) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }

    public synchronized void handle(int ID, Message msg) {
        if (msg.content.equals(".bye")){
            Announce("signout", "Server", msg.sender);
            remove(ID); 
	}
	else{
            if(msg.type.equals("login")){
                if(findUserThread(msg.sender) == null){
                    if(db.checkLogin(msg.sender, msg.content)){
                        clients[findClient(ID)].username = msg.sender;
                        clients[findClient(ID)].send(new Message("login", "Server", "TRUE", msg.sender));
                        Announce("newuser", "Server", msg.sender);
                        SendUserList(msg.sender);
                    }
                    else{
                        clients[findClient(ID)].send(new Message("login", "Server", "FALSE", msg.sender));
                    } 
                }
                else{
                    clients[findClient(ID)].send(new Message("login", "Server", "FALSE", msg.sender));
                }
            }
            else if(msg.type.equals("message")){
                if(msg.receiver.equals("All")){
                    Announce("message", msg.sender, msg.content);
                }
                else{
                    findUserThread(msg.receiver).send(new Message(msg.type, msg.sender, msg.content, msg.receiver));
                    clients[findClient(ID)].send(new Message(msg.type, msg.sender, msg.content, msg.receiver));
                }
            }
            else if(msg.type.equals("test")){
                clients[findClient(ID)].send(new Message("test", "Server", "OK", msg.sender));
            }
            else if(msg.type.equals("signup")){
                if(findUserThread(msg.sender) == null){
                    if(!db.userExists(msg.sender)){
                        db.addUser(msg.sender, msg.content);
                        clients[findClient(ID)].username = msg.sender;
                        clients[findClient(ID)].send(new Message("signup", "Server", "TRUE", msg.sender));
                        clients[findClient(ID)].send(new Message("login", "Server", "TRUE", msg.sender));
                        Announce("newuser", "Server", msg.sender);
                        SendUserList(msg.sender);
                    }
                    else{
                        clients[findClient(ID)].send(new Message("signup", "Server", "FALSE", msg.sender));
                    }
                }
                else{
                    clients[findClient(ID)].send(new Message("signup", "Server", "FALSE", msg.sender));
                }
            }
            else if(msg.type.equals("nilai")){
                 if(msg.receiver.equals("All")){
                    Announce("nilai", msg.sender, msg.content);
                }
                else{
                    findUserThread(msg.receiver).send(new Message(msg.type, msg.sender, msg.content, msg.receiver));
                    clients[findClient(ID)].send(new Message(msg.type, msg.sender, msg.content, msg.receiver));
                }
            }
        }
    }
    
public void Announce(String type, String sender, String content){
        Message msg = new Message(type, sender, content, "All");
        for(int i = 0; i < clientCount; i++){
            clients[i].send(msg);
        }
    }
    
    public void SendUserList(String toWhom){
        for(int i = 0; i < clientCount; i++){
            findUserThread(toWhom).send(new Message("newuser", "Server", clients[i].username, toWhom));
        }
    }
    
    public ServerThread findUserThread(String usr){
        for(int i = 0; i < clientCount; i++){
            if(clients[i].username.equals(usr)){
                return clients[i];
            }
        }
        return null;
    }
	
    @SuppressWarnings("deprecation")
    public synchronized void remove(int ID){  
    int pos = findClient(ID);
        if (pos >= 0){  
            ServerThread toTerminate = clients[pos];
            ui.display.append("\nRemoving client thread " + ID + " at " + pos);
	    if (pos < clientCount-1){
                for (int i = pos+1; i < clientCount; i++){
                    clients[i-1] = clients[i];
	        }
	    }
	    clientCount--;
	    try{  
	      	toTerminate.close(); 
	    }
	    catch(IOException ioe){  
	      	ui.display.append("\nError closing thread: " + ioe); 
	    }
	    toTerminate.stop(); 
	}
    }
    private void addThread(Socket socket) {
        if (clientCount < clients.length) {
            ui.display.append("\nClient accepted: " + socket);
            clients[clientCount] = new ServerThread(this, socket);
            try {
                clients[clientCount].open();
                clients[clientCount].start();
                clientCount++;
            } catch (IOException ioe) {
                ui.display.append("\nError opening thread: " + ioe);
            }
        } else {
            ui.display.append("\nClient refused: maximum " + clients.length + " reached.");
        }
    }
    public void send(Message msg) {
        try {
            Out.writeObject(msg);
            Out.flush();
            System.out.println("Outgoing : " + msg.toString());
        } catch (IOException ex) {
            System.out.println("Exception SocketClient send()");
        }
    }
}
