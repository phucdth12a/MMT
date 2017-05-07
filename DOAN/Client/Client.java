import java.io.*;
import java.net.*;
import java.util.*;

public class Client implements Runnable {

    // The Client socket
    private static Socket clientSocket = null;
    // The output stream
    private static DataOutputStream os = null;
    // The input stream
    private static DataInputStream is = null;

    private static BufferedReader inputLine = null;
    private static boolean closed = false;
    
    public static void main(String[] args) {
        
        // The default port.
        int portNumber = 2222;
        // The default host.
        String host = "localhost";
        boolean connect = true;

        String username = "";
        String password = "";
        
        Scanner input = new Scanner(System.in);
        DataInputStream stdIn = new DataInputStream(System.in);
        
        while (connect) {
            
            System.out.println("Nhap thong tin ket noi den Server: ");
            System.out.print("IP: ");
            host = input.nextLine();
            
            System.out.print("Port: ");
            portNumber = input.nextInt();
            input.nextLine();
            
            try {
                clientSocket = new Socket(host, portNumber);
                
                inputLine = new BufferedReader(new InputStreamReader(System.in));
                os = new DataOutputStream(clientSocket.getOutputStream());
                is = new DataInputStream(clientSocket.getInputStream());

                connect = false;
                
                System.out.println("----> Ket noi server thanh cong <----");

                boolean login = true;

                while (login) {

                    System.out.println("Vui long chon chuc nang: ");
                    System.out.println("1. Dang nhap");
                    System.out.println("2. Dang ky");

                    int choise = input.nextInt(); 
                    input.nextLine();
                    switch (choise) {
                        case 1: 
                            System.out.println("######## Dang nhap vao he thong ########");
                            System.out.print("Username: ");
                            username = input.nextLine();
                            
                            System.out.print("Password: ");
                            password = input.nextLine();

                            os.writeByte(1);
                            os.writeUTF(username);
                            os.writeUTF(password);
                            os.flush();

                            int resutl = is.read();

                            switch (resutl) {
                                case 1:
                                    System.out.println(is.readUTF());
                                    login = false;

                                    // send file name to server 
                                    sendFileNameToServer();
                                    


                                    break;
                                case 2:
                                    System.out.println(is.readUTF());
                                    login = true;
                                    break;
                                default:
                                    System.out.println(is.readUTF());
                                    break;
                            }

                            break;
                        case 2: 
                            System.out.println("######## Dang ky vao he thong ########");
                            System.out.print("Username: ");
                            username = input.nextLine();

                            System.out.print("Password: ");
                            password = input.nextLine();

                            os.writeByte(2);
                            os.writeUTF(username);
                            os.writeUTF(password);
                            os.flush();
                            System.out.println(is.readUTF());
                            login = true;

                            break;
                        default: 
                            System.out.println("Lua chon khong dung! Vui long chon lai");
                            login = true;
                            break;
                    }

                }
                
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host " + host);
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to the host " + host);            }
            
        }


        if (clientSocket != null && os != null && is != null) {
            try {
                String userInput;

                /*while ((userInput = stdIn.readLine()) != null) {
                    os.writeBytes(userInput);
                    os.writeByte('\n');
                    System.out.println("echo: " + is.readLine());
                } */

                new Thread(new Client()).start();
                while (!closed) {
                    os.writeUTF(inputLine.readLine().trim());
                }

                os.close();
                is.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("I/O failed on the connection to: taranis");
            }
        }
        
        
    }

    public static void sendFileNameToServer() throws IOException {

        File folder = new File("Upload");
        File[] listOfFiles = folder.listFiles();

        os.writeByte(3);

        InetAddress localhost = InetAddress.getLocalHost();
        // this code assumes IPv4 is used
        byte[] ip = localhost.getAddress();

        InetAddress address = InetAddress.getByAddress(ip);

        System.out.println(address);

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                
                os.writeUTF(listOfFiles[i].getName());
            }
        }
        os.flush();

    }

    public static void chucnang() throws IOException {
        
    }

    public void run() {
        try {
            byte messageType = is.readByte();
            switch (messageType) {
                case 20:
                    System.out.println("123213123");
                default:
                    break;
            }
            closed = true;
        } catch (IOException e) {
            System.err.println("IOException: " + e);
        }
    }
}
