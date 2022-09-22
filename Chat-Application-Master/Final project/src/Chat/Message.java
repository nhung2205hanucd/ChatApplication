/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat;

import java.io.Serializable;

/**
 *
 *
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    public String type, sender, content, receiver;

    public Message(String type, String sender, String content, String receiver) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.receiver = receiver;
    }
@Override
    public String toString() {
        return "{type:'" + type + "', sender:'" + sender + "', content:'" + content + "', receiver:'" + receiver + "'}";
    }

}
