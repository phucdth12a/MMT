import java.io.*;
import java.net.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Server {

    // The server socket.
    private static ServerSocket serverSocket = null;
    // The client socket.
    private static Socket clientSocket = null;
    
    // This chat server can accept up to maxClientsCount clients' connections.
    private static final int maxClientsCount = 10;
    private static final clientThread[] threads = new clientThread[maxClientsCount];
    
    public static void main(String args[]) {
        
        // The default port number.
        int portNumber = 2222;
        if (args.length < 1) {
            System.out.println("Usage: java Server <portNumber>\n" + "Now using port number = " + portNumber);
        } else {
            portNumber = Integer.valueOf(args[0]).intValue();
        }
        
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }
        
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                int i = 0;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new clientThread(clientSocket, threads)).start();
                        break;
                    }
                }
                
                if (i == maxClientsCount) {
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    clientSocket.close();
                }
                
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        
    }
}

class clientThread extends Thread {
    
    private DataInputStream is = null;
    private DataOutputStream os = null;
    private Socket clientSocket = null;
    private final clientThread[] threads;
    private int maxClientsCount;
    private byte messageType;
    private String username = "";
    private String password = "";
    
    public clientThread(Socket clientSocket, clientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
    }
    
    public void run() {
        int maxClientsCount = this.maxClientsCount;
        clientThread[] threads = this.threads;

        try {

            is = new DataInputStream(clientSocket.getInputStream());
            os = new DataOutputStream(clientSocket.getOutputStream());

            boolean flag = true;
            while (flag) {
                
                messageType = is.readByte();

                switch (messageType) {
                    case 1:
                        username = is.readUTF();
                        password = is.readUTF();
                        
                        boolean kq = false;
                        
                        try {
                            File file = new File("user.txt");

                            BufferedReader br = new BufferedReader(new FileReader(file));
                            String line;
                            while ((line = br.readLine()) != null) {
                                MessageDigest md = MessageDigest.getInstance("MD5");
                                String[] content = line.split(",");
                                md.update(password.getBytes(), 0, password.length());
                                String pass = new BigInteger(1, md.digest()).toString(16);

                                if (username.equals(content[0]) && pass.equals(content[1])) {
                                    kq = true;
                                    break;
                                }
                            }
                        } catch (IOException | NoSuchAlgorithmException e) {
                            System.err.println("IOException: " + e);
                        }
                        
                        if (kq == true) {
                            os.write(1);
                            os.writeUTF("=> Dang nhap thanh cong.");
                        } else {
                            os.write(2);
                            os.writeUTF("=> Ten dang nhap hoac mat khau khong dung. Vui long nhap lai.");
                        }

                        break;
                    case 2:
                        username = is.readUTF();
                        password = is.readUTF();

                        try {
                            File file = new File("user.txt");
                            // if file doesnt exists, then create it
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
                            BufferedWriter bw = new BufferedWriter(fw);
                            bw.write(username);
                            bw.write(",");

                            MessageDigest md = MessageDigest.getInstance("MD5");
                            md.update(password.getBytes(), 0, password.length());
                            bw.write(new BigInteger(1, md.digest()).toString(16));
                            bw.write("\n");
                            bw.close();

                            os.writeUTF("=> Dang ky thanh cong.");

                        } catch (IOException | NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }

                        break;
                    case 3:
                        boolean close = false;
                        String fileName = is.readUTF();
                        while (fileName != null) {
                            System.out.println(fileName);
                            fileName = is.readUTF();
                        }
                        break;
                    default: 
                        System.out.println("sdsad");
                        flag = false;
                        break;
                } 

            }

            is.close();
            os.close();
            clientSocket.close();
        } catch (IOException e) {
            
        }
    }
}
