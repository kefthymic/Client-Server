import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class ServerThread extends Thread{
    private Socket clientSocket;
    private DataInputStream inputFromClient;
    private DataOutputStream outputToClient;
    private HashMap<Integer, Account> allAccounts;
    private HashMap<Integer, Message> allMessages;

    public ServerThread(Socket clientSocket, HashMap<Integer, Account> allAccounts, HashMap<Integer, Message> allMessages){
        try{
            this.clientSocket=clientSocket;
            this.allAccounts= allAccounts;
            this.allMessages= allMessages;
            inputFromClient= new DataInputStream(this.clientSocket.getInputStream());
            outputToClient= new DataOutputStream(this.clientSocket.getOutputStream());
        }catch (IOException e){
            System.err.println("Error: No Connection" +"\n" + e.getMessage());
        }
    }


    /**
     * Στη μεταβλητή dataFromClient αποθηκεύεται το αίτημα του client. Στον πρώτο χαρακτήρα είναι αποθηκευμένος ένας αριθμός από το 1 έως το 6, ο οποίος αντιστοιχεί σε κάποια λειτουργία που θέλει να εκτελέσει ο client.
     * Αυτός ο αριθμός αποθηκεύεται στη μεταβλητή functionID. Ανάλογα με την τιμή του functionID καλείται μία διαφορετική συνάρτηση, που η κάθε μία παίρνει ως όρισμα την υπόλοιπη συμβολοσειρά της dataFromClient
     * Τα αποτελέσματα των συναρτήσεων επιστρέφονται ως απάντηση στον client
     *
     */
    public void run(){
        try{
            String dataFromClient= inputFromClient.readUTF();
            String functionId= dataFromClient.charAt(0)+"";
            dataFromClient=dataFromClient.substring(1);


            if(functionId.equals("1")){
                outputToClient.writeInt(createAccount(dataFromClient));
            }
            else if(functionId.equals("2")){
                outputToClient.writeUTF(showAccounts(dataFromClient));
            }
            else if(functionId.equals("3")){
                outputToClient.writeInt(sendMessage(dataFromClient));
            }
            else if(functionId.equals("4")){
                outputToClient.writeUTF(showInbox(dataFromClient));
            }
            else if(functionId.equals("5")){
                outputToClient.writeUTF(readMessage(dataFromClient));
            }
            else if(functionId.equals("6")){
                outputToClient.writeInt(deleteMessage(dataFromClient));
            }


        }catch (IOException e){
            System.err.println("Error: " + e.getMessage());
        } finally {
            try{
                clientSocket.close();
            }catch (IOException e){
                System.err.println("Error" + e.getMessage());
            }
        }
    }

    /**
     * Η συνάρτηση createAccount αποτελεί την πρώτη λειτουργία - functionID=1 -
     * Σε αυτή την περίπτωση, στο String dataFromClient θα υπάρχει μόνο το username
     * Αρχικά, ελέγχεται αν το username ανήκει σε κάποιον άλλο χρήστη. Αν ανήκει, τότε η συνάρτηση επιστρέφει την τιμή -1
     * Έπειτα, ελέγχονται όλοι οι χαρακτήρες του username, ώστε να διαπιστωθεί αν υπάρχουν μη-έγκυροι χαρακτήρες. Αν υπάρχουν, τότε η συνάρτηση επιστρέφει την τιμή -2
     * Τέλος, δημιουργείται ένα νέο account και η συνάρτηση επιστρέφει το authToken του χρήστη
     */
    private int createAccount(String dataFromClient){
        String newUsername= dataFromClient;

        for(Account account: allAccounts.values()){
            if( account.getUsername().equals(newUsername))
                return -1;
        }

        char character;

        for(int i=0;i<newUsername.length();i++){
            character=newUsername.charAt(i);

            if(!( (character>='a' && character<='z') || (character>='A' && character<='Z') || (character>='0' && character<='9') || character=='_') )
                return -2;
        }


        int newAuthToken= Server.getAuthToken();
        Account account= new Account(newUsername, newAuthToken);
        allAccounts.put(newAuthToken, account);

        return newAuthToken;

    }

    /**
     * Η συνάρτηση showAccounts αποτελεί τη δεύτερη λειτουργία - functionID=2 -
     * Σε αυτή την περίπτωση, στο dataFromClient θα υπάρχει μόνο το authToken ενός χρήστη.
     * Αρχικά, ελέγχεται αν το authToken αντιστοιχεί σε κάποιον χρήστη.
     *              Αν ανήκει, τότε η συνάρτηση επιστρέφει όλα τα username των χρηστών
     *                         αλλιώς επιστρέφει ένα String που αναφέρει ότι το authToken δεν ανήκει σε κάποιον χρήστη
     **/
    private String showAccounts(String dataFromClient){
        int authToken= Integer.parseInt(dataFromClient);

        if(allAccounts.get(authToken)==null){
            return "Invalid Auth Token";
        }

        String temp="";
        int i=0;
        for(Account account: allAccounts.values()){
            temp+= ++i +". " + account.getUsername() + "\n";
        }

        return temp;
    }

    /**
     * Η συνάρτηση sendMessage αποτελεί την τρίτη λειτουργία - functionID=3 -
     * Σε αυτή την περίπτωση στο dataFromClient θα υπάρχει ένα String της μορφής: authToken^recipient.length()^recipient^messageBody
     *
     * Αρχικά, ελέγχεται αν το authToken αντιστοιχεί σε κάποιον χρήστη. Αν δεν υπάρχει, τότε η συνάρτηση επιστρέφει την τιμή: 2
     *                                                                  ενώ αν υπάρχει τότε στη μεταβλητή sender αποθηκεύεται ο συγκεκριμένος χρήστης
     * Έπειτα, ελέγχεται αν υπάρχει χρήστης με username ίδιο με αυτό της μεταβλητής recipient. Αν δεν υπάρχει η συνάρτηση επιστρέφει την τιμή: 1,
     * Αλλιώς δημιουργείται και στέλνεται το μήνυμα και η συνάρτηση επιστρέφει την τιμή 0
     */
    private int sendMessage(String dataFromClient){
        String tempAuthToken="";
        int authToken;
        String recipient="";
        String tempRecipient="";
        int tempRecipient2;
        String messageBody="";
        int i;

        for(i=0;dataFromClient.charAt(i)!='^';i++){
            tempAuthToken+=dataFromClient.charAt(i)+"";
        }
        authToken=Integer.parseInt(tempAuthToken);

        for(i++;dataFromClient.charAt(i)!='^';i++){
            tempRecipient+=dataFromClient.charAt(i);
        }

        tempRecipient2=Integer.parseInt(tempRecipient);

        for(i++;tempRecipient2>0;i++){
            recipient+=dataFromClient.charAt(i);
            tempRecipient2--;
        }
        messageBody=dataFromClient.substring(i+1);

        Account sender= allAccounts.get(authToken);

        if(sender==null)
            return 2;

        Account receiver=null;

        for(Account account : allAccounts.values()){
            if(account.getUsername().equals(recipient)){
                receiver= account;
            }
        }

        if(receiver==null){
            return 1;
        }


        int messId= Server.getMessageId();
        Message message= new Message(sender.getUsername(), receiver.getUsername(), messageBody, messId);
        allMessages.put(messId, message);

        receiver.addMessage(message);


        return 0;

    }

    /**
     * Η συνάρτηση showInbox αποτελεί την τέταρτη λειτουργία - functionID=4 -
     * Σε αυτή την περίπτωση στο dataFromClient θα υπάρχει μόνο το authToken ενός χρήστη
     * Αν αντιστοιχεί σε κάποιον χρήστη, τότε η συνάρτηση θα επιστρέφει ένα String με όλα τα εισερχόμενα μηνύματα του χρήστη
     *                           αλλιώς, θα επιστρέφει ένα String που να αναφέρει ότι το authToken δεν ανήκει σε κάποιον χρήστη
     */
    private String showInbox(String dataFromClient){

        int authToken= Integer.parseInt(dataFromClient);
        Account account= allAccounts.get(authToken);
        if(account==null){
            return "Invalid Auth Token";
        }

        String answer="";

        for(Message message: account.getMessageBox()){
            answer+= message.getMessageId() +". from: " + message.getSender();

            if(!message.getIsRead())
                answer+="*";

            answer+="\n";
        }

        return answer;
    }

    /**
     * Η συνάρτηση readMessage αποτελεί την πέμπτη λειτουργία - functionID=5 -
     * Σε αυτή την περίπτωση στο dataFromClient θα υπάρχει ένα String της μορφής: authToken^messageId
     * Αρχικά, ελέγχεται αν το authToken αντιστοιχίζεται σε κάποιον χρήστη
     *              Αν δεν υπάρχει, τότε η συνάρτηση επιστρέφει to String "2"
     * Αν υπάρχει, τότε ελέγχεται αν το messageId υπάρχει και αν ανήκει στα εισερχόμενά του.
     *              Αν όχι, τότε η συνάρτηση επιστρέφει το String "1"
     * Αλλιώς, χρησιμοποιείται η συνάρτηση messageRead, ώστε να ενημερωθεί η κατάσταση του μηνύματος
     * και στέλνεται στον client ένα String, που ο πρώτος χαρακτήρας είναι το '0', ακολουθεί το username του αποστολέα μέσα σε παρενθέσεις και έπειτα το μήνυμα
     */
    private String readMessage(String dataFromClient){
        String tempAuthToken="";
        int authToken;
        int messageId;
        int i;

        for(i=0;dataFromClient.charAt(i)!='^';i++){
            tempAuthToken+=dataFromClient.charAt(i);
        }
        authToken=Integer.parseInt(tempAuthToken);

        messageId= Integer.parseInt(dataFromClient.substring(i+1));

        Account account= allAccounts.get(authToken);
        if(account==null)
            return "2";
        Message message= allMessages.get(messageId);

        if(message==null || !message.getReceiver().equals(account.getUsername()))
            return "1";

        message.messageRead();

        return "0" + "(" + message.getSender() + ")" + message.getBody();

    }

    /**
     * Η συνάρτηση deleteMessage αποτελεί την έκτη λειτουργία - functionID=6 -
     * Σε αυτή την περίπτωση στο dataFromClient θα υπάρχει ένα String της μορφής: authToken^messageId
     * Αρχικά, ελέγχεται αν το authToken αντιστοιχίζεται σε κάποιον χρήστη
     *      Αν δεν υπάρχει, τότε η συνάρτηση επιστρέφει την τιμή 2
     * Αν υπάρχει, τότε ελέγχεται αν το messageId υπάρχει και αν ανήκει στα εισερχόμενά του.
     *      Αν όχι, τότε η συνάρτηση επιστρέφει την τιμή 1
     * Αλλιώς, το μήνυμα διαγράφεται και η συνάρτηση επιστρέφει την τιμή 0
     */
    private int deleteMessage(String dataFromClient){
        String tempAuthToken="";
        int authToken;
        int messageId;
        int i;

        for(i=0;dataFromClient.charAt(i)!='^';i++){
            tempAuthToken+=dataFromClient.charAt(i);
        }
        authToken=Integer.parseInt(tempAuthToken);

        messageId= Integer.parseInt(dataFromClient.substring(i+1));

        Account account= allAccounts.get(authToken);
        if(account==null)
            return 2;
        Message message= allMessages.get(messageId);

        if(message==null || !message.getReceiver().equals(account.getUsername()))
            return 1;

        allMessages.remove(messageId);

        for(i=0;i<account.getMessageBox().size();i++){
            if(account.getMessageBox().get(i).getMessageId()==messageId)
                account.getMessageBox().remove(i);
        }

        return 0;

    }




}
