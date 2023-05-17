package ro.nq.metaexplorer.DataEncapsulations;

import ro.nq.metaexplorer.Utilities.Networking;

import java.io.IOException;
import java.net.Socket;

public class QueuedIncomingFile {
public Socket incomingSocket;
public FileMetadata fileMetadata;
public FileMetadataPacket fileMetadataPacket;
    public QueuedIncomingFile(Socket incomingSocket, FileMetadataPacket fileMetadataPacket) {
        this.incomingSocket = incomingSocket;
        this.fileMetadata = new FileMetadata(incomingSocket, fileMetadataPacket);
        this.fileMetadataPacket = fileMetadataPacket;
    }

    public static boolean equalsByFileMetadataPacketAndSocket (QueuedIncomingFile left, QueuedIncomingFile right) {
        boolean equalSocketAddresses = Networking.equalsSocketsByAddresses(left.incomingSocket, right.incomingSocket);
        boolean equalFileMetadataPackets = FileMetadataPacket.equalsByFileAttributes(left.fileMetadataPacket, right.fileMetadataPacket);
        return equalSocketAddresses && equalFileMetadataPackets;
    }

    public static boolean equalsByFileMetadata(FileMetadata left, FileMetadata right) {
        boolean equalSocketAddresses = Networking.equalsSocketsByAddresses(left.sourceSocket, right.sourceSocket);
        boolean equalFileMetadata = FileMetadata.equalsByFileAttributes(left, right);
        return equalSocketAddresses && equalFileMetadata;
    }

    public void closeSocket () {
        if (this.fileMetadata.sourceSocket != null) {
            try {
                this.fileMetadata.sourceSocket.close();
                this.fileMetadata.sourceSocket = null;
            }
            catch (IOException exception) {
                System.out.println("{QueuedIncomingFile.closeSocket()} [IOException] (Socket.close()) - " + exception.getMessage() + "\n" + exception.getMessage());
            }
        }
    }
}
