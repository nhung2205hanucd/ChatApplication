package Chat;

import java.io.*;
import java.net.*;

public class SocketClient implements Runnable {

    public int port;
    public String serverAddr;
    public Socket socket;
    public ClientForm ui;
    public ObjectInputStream In;
    public ObjectOutputStream Out;

    public SocketClient(ClientForm frame) throws IOException {
        ui = frame;
        this.serverAddr = ui.serverAddr;
        this.port = ui.port;
        socket = new Socket(InetAddress.getByName(serverAddr), port);

        Out = new ObjectOutputStream(socket.getOutputStream());
        Out.flush();
        In = new ObjectInputStream(socket.getInputStream());
    }

    
    @Override
    public void run() {
        boolean keepRunning = true;
        while (keepRunning) {
            try {
                Message msg = (Message) In.readObject();
                System.out.println("Incoming message: " + msg.toString());

                if (msg.type.equals("message")) {
                    if (msg.receiver.equals(ui.username)) {
                        ui.clientDisp.append("[" + msg.sender + " to Me] : " + msg.content + "\n");
                    } else {
                        ui.clientDisp.append("[" + msg.sender + " to " + msg.receiver + "] : " + msg.content + "\n");

                    }
                } else if (msg.type.equals("login")) {
                    if (msg.content.equals("TRUE")) {
                        ui.clientDisp.append("[Server to Me] : Login Successful\n");

                    } else {
                        ui.clientDisp.append("[Server to Me] : Login Failed\n");
                    }
                } else if (msg.type.equals("test")) {
                } else if (msg.type.equals("newuser")) {
                    if (!msg.content.equals(ui.username)) {
                        boolean exists = false;
                        for (int i = 0; i < ui.model.getSize(); i++) {
                            if (ui.model.getElementAt(i).equals(msg.content)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            ui.model.addElement(msg.content);
                        }
                    }
                } else if (msg.type.equals("signup")) {
                    if (msg.content.equals("TRUE")) {
                        ui.clientDisp.append("[Server to Me] : Singup Successful\n");

                    } else {
                        ui.clientDisp.append("[Server to Me] : Signup Failed\n");
                    }
                } else if (msg.type.equals("signout")) {
                    if (msg.content.equals(ui.username)) {
                        ui.clientDisp.append("[" + msg.sender + " to Me] : Bye\n");
                        for (int i = 1; i < ui.model.size(); i++) {
                            ui.model.removeElementAt(i);
                        }

                        ui.clientThread.stop();
                    } else {
                        ui.model.removeElement(msg.content);
                        ui.clientDisp.append("[" + msg.sender + " to All] : " + msg.content + " has signed out\n");
                    }
                } else {
                    ui.clientDisp.append("[Server to Me] : Unknown message type\n");
                }
            } catch (Exception ex) {
                keepRunning = false;
                ui.clientDisp.append("[Application to Me] : failed connection \n");
                ui.hostField.setEditable(true);
                ui.portField.setEditable(true);
                for (int i = 1; i < ui.model.size(); i++) {
                    ui.model.removeElementAt(i);
                }

                ui.clientThread.stop();

                System.out.println("Exception SocketClient run()");
                ex.printStackTrace();
            }
        }
    }

    public void send(Message msg) {
        try {
            Out.writeObject(msg);
            Out.flush();
            System.out.println("Outgoing message: " + msg.toString());
        } catch (IOException ex) {
            System.out.println("Exception SocketClient send()");
        }
    }

    public void closeThread(Thread t) {
        t = null;
    }
}
