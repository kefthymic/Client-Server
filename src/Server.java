import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private ServerSocket serverSocket;

    private static HashMap<Integer, Account> allAccounts;
    private static HashMap<Integer, Message> allMessages;

    private static int authToken;
    private static int messageId;

    public Server(String port){
        try{
            serverSocket=new ServerSocket(Integer.parseInt(port));
            allAccounts= new HashMap<>();
            allMessages= new HashMap<>();
            authToken=0;
            messageId=0;
        }catch (Exception e){
            System.err.println("Error: No connection");
            System.exit(1);
        }
    }

    public static void main(String[] args) throws IOException{
        Server server= new Server(args[0]);


        while (true){
            Socket clientSocket= server.serverSocket.accept();
            ServerThread serverThread= new ServerThread(clientSocket, allAccounts, allMessages);
            serverThread.start();
        }

    }

    public static int getAuthToken(){
        return ++authToken;
    }

    public static int getMessageId(){
        return ++messageId;
    }
}
