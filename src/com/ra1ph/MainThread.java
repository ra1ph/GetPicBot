package com.ra1ph;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.filetransfer.*;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: ra1ph
 * Date: 18.06.13
 * Time: 17:20
 * To change this template use File | Settings | File Templates.
 */
public class MainThread implements Runnable, FileTransferListener, PacketListener {

    private static final int FAILED = -1;
    private static final int FAILED_NO_PHOTOS = -2;
    private static final int SUCCESS = 0;

    private static final String MYSQL_LOGIN = "root";
    private static final String MYSQL_PASS = "kh036Kh3Nb";
    private static final String MYSQL_HOSTNAME = "127.0.0.1";
    public static final String ACTION_TAG = "action";

    public static final String GET_PHOTO_TAG = "get_photo";
    private static final int FAILED_TRANSFER_FILE = -3;
    private static final int MAX_TRY_COUNT = 5;
    private static final int MAX_TRY_FILE_COUNT = 5;

    private static String HOSTNAME = "127.0.0.1";
    private static String user = "getpicbot";
    private static String pass = "k_lt45mm";
    private static String resource = "Smack";
    private static final long SLEEP_TIME = 300;
    private FileTransferManager fManager;

    private XMPPConnection connection;
    private boolean isActive = true;
    private Connection conn;
    private String FILE_INCOMING = "file_incoming";
    private String USER_COUNT = "user_count";

    private File dir;

    public static final String GET_XML = "jabber:iq:getpicture";


    @Override
    public void run() {
        //To change body of implemented methods use File | Settings | File Templates.

        dir = new File("cacheFiles");
        dir.mkdir();
        conn = mysqlConnect();
        connectionConfig(ProviderManager.getInstance());
        ConnectionConfiguration config = new ConnectionConfiguration(HOSTNAME, 5222, HOSTNAME);

        connection = new XMPPConnection(config);

        while (isActive) {
            if (!connection.isConnected()) try {
                connect(user, pass);
                if (connection != null) System.out.println("Connection and login to XMPP SUCCESS!!");
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                if (connection != null) System.out.println("Connection or login to XMPP FAIL!!");
            }
            if (!connection.isAuthenticated()) {
                try {
                    connection.login(user, pass);
                    System.out.println("Loign SUCCESS!!");
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    System.out.println("Login FAIL!!");
                }

            }
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }

    private void connect(String user, String pass) throws XMPPException {
        connection.connect();


        connection.login(user, pass, resource);

        fManager = new FileTransferManager(connection);

        connection.addPacketListener(this, null);

        fManager.addFileTransferListener(this);

    }

    private Connection mysqlConnect() {
        Properties connInfo = new Properties();

        connInfo.put("characterEncoding", "UTF8");
        connInfo.put("user", MYSQL_LOGIN);
        connInfo.put("password", MYSQL_PASS);

        while (isActive) {
            try {
                Connection connection = DriverManager.getConnection("jdbc:mysql://" + MYSQL_HOSTNAME + "/openfire", connInfo);
                if (connection != null) System.out.println("Connection to MySQL SUCCESS!!");
                return connection;
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                if (connection != null) System.out.println("Connection to MySQL FAIL!!");
            }
        }
        return null;
    }

    @Override
    public void processPacket(Packet packet) {
        //To change body of implemented methods use File | Settings | File Templates.

        if (packet instanceof Message) {
            if (((Message) packet).getExtension(GetPicEvent.NAMESPACE) != null) {
                GetPicEvent event = (GetPicEvent) ((Message) packet).getExtension(GetPicEvent.NAMESPACE);
                if (event.isGetPictureRequest()) {
                    sendSubmit(packet.getFrom());
                    String error = getFile(packet.getFrom());
                    if (error != null) {
                        System.out.println("Error: " + error);
                        Message msg = new Message();
                        msg.setTo(packet.getFrom());
                        GetPicEvent new_event = new GetPicEvent();
                        new_event.setError(error);
                        msg.addExtension(new_event);
                        connection.sendPacket(msg);
                    } else {
                        Message msg = new Message();
                        msg.setTo(packet.getFrom());
                        GetPicEvent new_event = new GetPicEvent();
                        new_event.setError("Success!");
                        msg.addExtension(new_event);
                        connection.sendPacket(msg);
                    }
                }
            } else if (((Message) packet).getBody() != null) {
                Message msg = (Message) packet;
                Message message = new Message();
                message.setFrom(msg.getTo());
                message.setTo(msg.getFrom());
                message.setBody(msg.getBody());
            }
        }
    }

    private void sendSubmit(String uid) {
        //To change body of created methods use File | Settings | File Templates.
        Message msg = new Message();
        GetPicEvent event = new GetPicEvent();
        event.setPictureSubmit(true);
        msg.addExtension(event);
        msg.setTo(uid);
        connection.sendPacket(msg);
    }

    @Override
    public void fileTransferRequest(final FileTransferRequest fileTransferRequest) {
        //To change body of implemented methods use File | Settings | File Templates.
        new Thread(new Runnable() {
            @Override
            public void run() {
                //To change body of implemented methods use File | Settings | File Templates.
                IncomingFileTransfer transfer = fileTransferRequest.accept();

                String filename = UUID.randomUUID().toString();
                File file = new File(dir, filename);
                try {
                    file.createNewFile();
                    transfer.recieveFile(file);
                    while (!transfer.isDone()) {
                        try {
                            Thread.sleep(SLEEP_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                    if ((transfer.getException() == null) && (transfer.getStatus().equals(FileTransfer.Status.complete))) {
                        addDBFile(fileTransferRequest.getRequestor(), filename);
                        System.out.println(file.getAbsolutePath());
                    }
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (XMPPException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }).start();
    }

    private void addDBFile(String user, String filename) {
        //To change body of created methods use File | Settings | File Templates.
        try {
            java.sql.Statement createTable = conn.createStatement();
            createTable.execute("CREATE TABLE IF NOT EXISTS " + FILE_INCOMING + " (user_id Varchar(255), filename Varchar(255), UNIQUE (filename))");

            java.sql.Statement insertFile = conn.createStatement();
            insertFile.execute("INSERT " + FILE_INCOMING + " VALUES('" + user + "','" + filename + "')");
            insertFile.close();

            createTable = conn.createStatement();
            createTable.execute("CREATE TABLE IF NOT EXISTS " + USER_COUNT + " (user_id Varchar(255), count INTEGER, UNIQUE(user_id))");
            createTable.close();

            Statement incrementCount = conn.createStatement();
            incrementCount.execute("INSERT INTO " + USER_COUNT + " VALUES('" + user + "',1) ON DUPLICATE KEY UPDATE count=count+1");
            incrementCount.close();

        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private String getFile(String user_id) {
        try {
            Statement getCount = conn.createStatement();
            ResultSet userCounts = getCount.executeQuery("SELECT * FROM " + USER_COUNT + " WHERE user_id='" + user_id + "'");
            int count = 0;
            if (userCounts.next()) count = userCounts.getInt("count");
            if (count > 0) {
                ResultSet files = getCount.executeQuery("SELECT * FROM " + FILE_INCOMING + " WHERE user_id<>'" + user_id + "' ORDER BY RAND() LIMIT 1");
                String filename = "";
                if (files.next()) {
                    filename = files.getString("filename");
                    String userFrom = files.getString("user_id");
                    int try_count = 0;
                    while (true) {
                        if (transferFile(new File(dir, filename), user_id, userFrom)) return null;
                        else {
                            try_count++;
                            if (try_count > MAX_TRY_FILE_COUNT) return Integer.toString(FAILED_TRANSFER_FILE);
                        }
                    }
                } else return Integer.toString(FAILED);

            } else return Integer.toString(FAILED_NO_PHOTOS);

        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return Integer.toString(FAILED);
        }
    }

    private boolean transferFile(File file, String user_id, String userFrom) {
        OutgoingFileTransfer transfer = fManager.createOutgoingFileTransfer(user_id);
        int try_count = 0;
        double progress = 0;
        try {
            transfer.sendFile(file, userFrom);
        } catch (XMPPException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        while (!transfer.isDone()) {
            if ((transfer.getException() == null) && (transfer.getStatus().equals(FileTransfer.Status.complete)))
                return true;
            if (progress == transfer.getProgress()) {
                try_count++;
                if (try_count > MAX_TRY_COUNT){
                    transfer.cancel();
                    return false;
                }
            } else {
                progress = transfer.getProgress();
                try_count = 0;
            }
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }
        if ((transfer.getException() == null) && (transfer.getStatus().equals(FileTransfer.Status.complete)))
            return true;
        return false;
    }

    private void connectionConfig(ProviderManager pm) {
        pm.addExtensionProvider("x", "jabber:x:getpic", new GetpicExtensionProvider());
    }
}
