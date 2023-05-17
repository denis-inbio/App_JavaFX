package ro.nq.metaexplorer.Utilities;

import ro.nq.metaexplorer.Applications.Terminal;
import ro.nq.metaexplorer.DataEncapsulations.ClassConstants;
import ro.nq.metaexplorer.DataEncapsulations.PeerSocket;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

public class Networking {
// ---- [class constants]
    public static final ClassConstants classConstants = new ClassConstants(
        Networking.class,
        "Networking",
        "",
        "",
        ""
    );
    public static final int serverSocketBacklog = 100;
// ---- [sub-system methods]
    public static boolean isLanAddress(InterfaceAddress interfaceAddress)
    {
        InetAddress address = interfaceAddress.getAddress();
            System.out.println("InetAddress: " + address + " | host address: " + address.getHostAddress() + " | host name: " + address.getHostName());

        int indexOctet0 = address.getHostAddress().indexOf("192");
        int indexOctet1 = address.getHostAddress().indexOf("168");

        if (indexOctet0 >= 0 && indexOctet1 >= 0 && indexOctet0 < indexOctet1) {
            return true;
        }
        else {
            return false;
        }
    }
    public static boolean startServer (Terminal application) {
        InetAddress serverLocalAddress = null;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = networkInterfaces.nextElement();
                        System.out.println("Network interface: " + networkInterface);
                    List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                        System.out.println("Interface addresses:");
                        for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                            System.out.println("\t" + interfaceAddress);
                                if (isLanAddress(interfaceAddress)) {
                                    serverLocalAddress = interfaceAddress.getAddress();
                                }
                        }
                }
        }
        catch (SocketException exception) {
            System.out.println("{Networking.startServer()} [SocketException] (NetworkInterface.getNetworkInterfaces()) - " + exception.getMessage() + "\t" + exception.getCause());
            return false;
        }
        System.out.println("Using interface: `" + serverLocalAddress + "` for local server");
            if (serverLocalAddress == null) { return false; }
        try {
                if (application.serverRunning.getValue() || application.serverSocket.getValue() != null || application.serverSocketThread.getValue() != null) {
                    Networking.stopServer(application);
                }
            ServerSocket serverSocket = new ServerSocket(0, serverSocketBacklog, serverLocalAddress);
            Thread serverThread = new Thread(application.serverSocketRunnable);
                application.serverRunning.setValue(true);
                application.serverSocket.setValue(serverSocket);
                application.serverSocketThread.setValue(serverThread);
            application.serverSocketThread.getValue().start();
                return true;
        }
        catch (IOException exception) {
            System.out.println("{Networking.startServer()} [IOException] (new ServerSocket()) - " + exception.getMessage() + "\t" + exception.getCause());
            return false;
        }
    }
    public static void stopServer (Terminal application) {
        application.serverRunning.setValue(false);
        try {
            application.serverSocket.getValue().close();
        }
        catch (IOException exception) {
            System.out.println("{Networking.stopServer()} [IOException] (Socket.close()) - " + exception.getMessage() + "\n" + exception.getCause());
        }
        application.serverSocket.setValue(null);

        try {
            application.serverSocketThread.getValue().interrupt();
            application.serverSocketThread.getValue().join();
            application.serverSocketThread.setValue(null);
        }
        catch (InterruptedException exception) {
            System.out.println("{Networking.stopServer()} [InterruptedException] (Thread.join()) - " + exception.getMessage() + "\n" + exception.getCause());
        }

        // <TODO> this should forcefully close the serverSocketThread by causing it an interrupt and terminating its while(running) loop
        // <TODO> close all active peers (?), and also interrupt and join their listening threads
        // <TODO> also close all queued file transfers (?)

        application.clearObservableActivePeers(true);
    }
    public static PeerSocket connectToPeer (String hostAddress, int portNumber) {
        try {
            Socket clientSocket = new Socket(hostAddress, portNumber);

            return new PeerSocket(clientSocket);

        } catch (IOException exception) {
            System.out.println("{Networking.connectToPeer()} [IOException] (new Socket()) - " + exception.getMessage() + "\t" + exception.getCause());
            return null;
        }
    }
    public static void disconnectFromPeer (PeerSocket peerSocket) {
        if (peerSocket.socket != null) {
            try {
                peerSocket.socket_lock.lock();
                peerSocket.running_lock.lock();
                peerSocket.incomingStreamThread_lock.lock();

                System.out.println("{Networking.disconnectFromPeer} - running -> false, socket -> null");
                peerSocket.running = false;
                peerSocket.socket.getValue().close();
                peerSocket.socket.setValue(null);

                System.out.println("{Networking.disconnectFromPeer} - thread.interrupt(), thread.join()");
                peerSocket.incomingStreamThread.interrupt();
                peerSocket.incomingStreamThread.join();

                System.out.println("{Networking.disconnectFromPeer} - thread -> null");
                peerSocket.incomingStreamThread = null;

                // <TODO> disconnect active peer (+) remove all of its queued files(*)
            }
            catch (IOException exception) {
                System.out.println("{Networking.disconnectFromPeer()} [IOException] (Socket.close()) - " + exception.getMessage() + "\n" + exception.getCause());
            }
            catch (InterruptedException exception) {
                System.out.println("{Networking.disconnectFromPeer()} [InterruptedException] (Thread.join()) - " + exception.getMessage() + "\n" + exception.getCause());
            }
            finally {
                peerSocket.incomingStreamThread_lock.unlock();
                peerSocket.running_lock.unlock();
                peerSocket.socket_lock.unlock();
            }
        }
    }
    public static boolean equalsSocketsByAddresses (Socket left, Socket right) {
        InetAddress inetLeft = left.getInetAddress();
            String canonicalLeft = inetLeft.getCanonicalHostName();
            String addressLeft = inetLeft.getHostAddress();
        InetAddress inetRight = right.getInetAddress();
            String canonicalRight = inetRight.getCanonicalHostName();
            String addressRight = inetRight.getHostAddress();

        System.out.println("Comparing sockets: `" + canonicalLeft + "` vs `" + canonicalRight + "`");
        return canonicalLeft.equals(canonicalRight);
    }
}
