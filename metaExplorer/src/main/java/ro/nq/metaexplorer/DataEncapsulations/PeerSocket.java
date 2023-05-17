package ro.nq.metaexplorer.DataEncapsulations;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PeerSocket {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        PeerSocket.class,
        "PeerSocket",
        "",
        "",
        ""
    );
    // <NOTE> before transmitting a serialized Object, send an int which maps to the underlying type the Object; the Object will be cast to the appropriate type
    // in source-code based on this mapping
    public static final Map<Object, Integer> mappingObjectTypeIdentification = Map.of(
        Object.class, 0,
        FileMetadataPacket.class, 1
    );
// ---- [state, ctor]
public Lock socket_lock;
public SimpleObjectProperty<Socket> socket;
public Lock customName_lock;
public SimpleStringProperty customName;
public Lock incomingStreamThread_lock;
public Thread incomingStreamThread;
public Lock running_lock;
public boolean running;
public Lock sendObject_lock;

    // <NOTE> this constructor is for when manually trying to connect to a peer
    public PeerSocket () {
        this.socket_lock = new ReentrantLock();
        this.socket = new SimpleObjectProperty<>(null);
        this.customName_lock = new ReentrantLock();
        this.customName = new SimpleStringProperty(null);
        this.incomingStreamThread_lock = new ReentrantLock();
        this.incomingStreamThread = null;
        this.running_lock = new ReentrantLock();
        this.running = false;
        this.sendObject_lock = new ReentrantLock();
    }
    // <NOTE> this constructor is for when the ServerSocket accepts a new client's socket, and the socket should begin an incoming traffic listening/polling thread
    public PeerSocket (Socket socket) {
        this.socket_lock = new ReentrantLock();
        this.socket = new SimpleObjectProperty<>(socket);
        this.customName_lock = new ReentrantLock();
        this.customName = new SimpleStringProperty(null);
        this.incomingStreamThread_lock = new ReentrantLock();
        this.incomingStreamThread = new Thread(this.peerSocketRunnable);
        this.running_lock = new ReentrantLock();
        this.running = true;
        this.sendObject_lock = new ReentrantLock();

        this.incomingStreamThread.start();
    }
    public static boolean equalsBySocketReference (PeerSocket left, PeerSocket right) {
        return left.socket == right.socket;
    }
    public static boolean equalsByEffectiveSocketAddress (PeerSocket left, PeerSocket right) {
        InetAddress leftAddress = left.socket.getValue().getInetAddress();
        InetAddress rightAddress = right.socket.getValue().getInetAddress();

        return Arrays.equals(leftAddress.getAddress(), rightAddress.getAddress());
    }
// ---- [runnable]
    public Runnable peerSocketRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getValue().getInputStream();
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

                    try {
                        while (running) {
                            Integer classType = (Integer) objectInputStream.readObject();
                            Object object = objectInputStream.readObject();

                            System.out.println("Class type: " + classType + "\n\tObject: " + object);

                            // <TODO> put it in a queue of received file, based on what classType it has
                            // if it's a FileMetadataMessage - queue it in bottom border
                            // if it's a FileCompleteMessage - I think it should be routed to the central queue of files, and be displayed
                            // if it's a ClientIdentification - it should be tested if it's the first (and should be the first input); based on it the connection is subsequently refused or persisted; on mismatch, the peer can be blacklisted
                        }
                    }
                    catch (IOException exception) {
                        System.out.println("{Runnable.run()} [IOException] (ObjectInputStream.readObject()) - " + exception.getMessage() + "\n" + exception.getCause());
                    }
                    catch (ClassNotFoundException exception) {
                        System.out.println("{Runnable.run()} [ClassNotFoundException] (ObjectInputStream.readObject()) - " + exception.getMessage() + "\n" + exception.getCause());
                    }
                }
                catch (IOException exception) {
                    System.out.println("{Runnable.run()} [IOException] (new ObjectInputStream()) - " + exception.getMessage() + "\n" + exception.getCause());
                }
            }
            catch (IOException exception) {
                System.out.println("{Runnable.run()} [IOException] (Socket.getInputStream()) - " + exception.getMessage() + "\n" + exception.getCause());
            }

        }
    };
// ---- [methods]
    public void beginTrafficThread (Socket socket) {
        if (socket != null) {
            try {
                this.socket_lock.lock();
                this.socket.setValue(socket);
            } finally {
                this.socket_lock.unlock();
            }

            try {
                this.incomingStreamThread_lock.lock();
                this.incomingStreamThread = new Thread(this.peerSocketRunnable);
            }
            finally {
                this.incomingStreamThread_lock.unlock();
            }

            try {
                this.running_lock.lock();
                this.running = true;
            }
            finally {
                this.running_lock.unlock();
            }

            this.incomingStreamThread.start();
        }
    }
    public static void sendObjectsToPeer (PeerSocket peerSocket, LinkedList<Object> objects) {
        try {
            peerSocket.sendObject_lock.lock();
            OutputStream outputStream = peerSocket.socket.getValue().getOutputStream();

            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

                try {
                    for (int index = 0; index < objects.size(); index++) {
                        Integer classType = PeerSocket.mappingObjectTypeIdentification.get(objects.get(index).getClass());
                            objectOutputStream.writeObject(classType);

                        objectOutputStream.writeObject(objects.get(index));
                    }
                }
                catch (IOException exception) {
                    System.out.println("{sendObjectToPeer()} [IOException] (ObjectOutputStream.writeObject()) - " + exception.getMessage() + "\n" + exception.getCause());
                }
            }
            catch (IOException exception) {
                System.out.println("{sendObjectToPeer()} [IOException] (new ObjectOutputStream()) - " + exception.getMessage() + "\n" + exception.getCause());
            }
        }
        catch (IOException exception) {
            System.out.println("{sendObjectToPeer()} [IOException] (Socket.getOutputStream()) - " + exception.getMessage() + "\n" + exception.getCause());
        }
        finally {
            peerSocket.sendObject_lock.unlock();
        }
    }
}
