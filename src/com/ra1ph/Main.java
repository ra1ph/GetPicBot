package com.ra1ph;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

public class Main{
    public static void main(String[] args) {
	// write your code here
       MainThread main = new MainThread();
        main.run();

    }
}
