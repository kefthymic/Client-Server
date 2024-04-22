import java.io.*;
import java.net.Socket;

public class Client {
    private Socket socket;
    private DataInputStream inputFromServer;
    private DataOutputStream outputToServer;

    public Client(String ip, String port){
        try{
            socket= new Socket(ip, Integer.parseInt(port));
            outputToServer= new DataOutputStream(socket.getOutputStream());
            inputFromServer= new DataInputStream(socket.getInputStream());
        }catch (Exception e){
            System.err.println("Error: No connection to server" + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String args[]) {
        Client client= new Client(args[0], args[1]);
        String functionId;

        functionId= args[2];

        switch (functionId) {
            case "1":
                System.out.println(client.createAccount(args[3]));
                break;
            case "2":
                System.out.println(client.showAccounts(args[3]));
                break;
            case "3":
                System.out.println(client.sendMessage(args[3], args[4], args[5]));
                break;
            case "4":
                System.out.println(client.showInbox(args[3]));
                break;
            case "5":
                System.out.println(client.readMessage(args[3], args[4]));
                break;
            case "6":
                System.out.println(client.deleteMessage(args[3], args[4]));
                break;
        }

        try{
            client.socket.close();
        }catch (IOException e){
            System.err.println("Error:" + e.getMessage());
        }


    }

    /**
     * Η συνάρτηση createAccount αποτελεί την πρώτη λειτουργία - functionID=1 -
     * Στον server στέλνεται ένα String της μορφής "1username" όπου το username δίνεται από τον client
     * Στη μεταβλητή result αποθηκεύεται η απάντηση του server
     *
     * @param username Το username που θέλει να χρησιμοποιήσει ο χρήστης
     *
     * @return ένα String ανάλογα με την τιμή result
     *         αν το result είναι ίσο με: -1, τότε το username υπάρχει ήδη και επιστρέφει ένα string με ένα κατάλληλο μήνυμα
     *                                    -2, τότε το username δεν είναι αποδεκτό, καθώς χρησιμοποιούνται μή αποδεκτοί χαρακτήρες
     *         σε άλλη περίπτωση ο λογαριασμός έχει δημιουργηθεί και στο result θα υπάρχει ένας θετικός αριθμός με το authToken του νέου λογαριασμού
     */
    private String createAccount(String username){
        String answer="";
        try{
            outputToServer.writeUTF("1"+username);
            int result= inputFromServer.readInt();

            if(result== -1){
                answer="Sorry, the user already exists";
            }
            else if(result==-2){
                answer="Invalid Username";
            }
            else{
                answer=result+"";
            }

        }catch (IOException e){
            System.err.println("Error");
            System.exit(1);
        }

        return answer;

    }

    /**
     * Η συνάρτηση showAccounts αποτελεί την δεύτερη λειτουργία - functionID=2 -
     * Στον server στέλνεται ένα String της μορφής "2key" που το key είναι μία μεταβλητή με το authToken ενός χρήστη
     *
     * @param key το authToken ενός χρήστη
     *
     * @return την απάντηση του server, που είτε θα εμφανίζει όλους τους λογαριασμούς του συστήματος, σε περίπτωση που το key αντιστοιχεί σε κάποιον χρήστη
     *                                      είτε θα εμφανίζει ένα κατάλληλο μήνυμα αν το key δεν είναι έγκυρο
     */
    private String showAccounts(String key){
        String result="";

        try{
            outputToServer.writeUTF("2"+key);
            result=inputFromServer.readUTF();

        }catch (IOException e){
            System.err.println("Error");
            System.exit(1);
        }


        return result;
    }

    /**
     * Η συνάρτηση sendMessage αποτελεί την τρίτη λειτουργία - functionID=3 -
     * Στον server στέλνεται ένα String της μορφής "3key^recipient.length()^recipient^message, που :
     *                              το key είναι το authToken ενός χρηστή
     *                              το recipient.length() είναι το μέγεθος του String του recipient
     *                              το recipient είναι το username του παραλήπτη
     *                              το message είναι το μήνυμα για αποστολή
     *                              ανάμεσα στις τρεις μεταβλητές χρησιμοποιείται ο χαρακτήρας '^' για να τις διαχωρίζει
     *                              Χρησιμοποιήθηκε αυτό το σύμβολο καθώς κανένα username δεν μπορεί να αποτελείται από αυτό, επομένως γίνεται εύκολος ο διαχωρισμός
     *
     * Στη μεταβλητή result αποθηκεύεται η απάντηση του server
     *
     * @param key το authToken του χρήστη
     * @param recipient το username του παραλήπτη
     * @param message το μήνυμα για αποστολή
     *
     * @return ένα String ανάλογα με την τιμή result
     *              Αν το result είναι ίσο με: 0, τότε το μήνυμα στάλθηκε με επιτυχία
     *                                         1, τότε ο recipient δεν αντιστοιχεί σε κάποιον χρήστη
     *                                         2, τότε το key δεν είναι έγκυρο
     */
    private String sendMessage(String key, String recipient, String message){
        String answer="";

        try {
            int result;

            outputToServer.writeUTF("3"+key+"^"+recipient.length()+"^"+recipient+"^"+message);
            result=inputFromServer.readInt();

            if(result==0){
                answer="OK";
            }
            else if(result==1){
                answer="User does not exist";
            }
            else if(result==2){
                answer="Invalid Auth Token";
            }

        }catch (IOException e){
            System.err.println("Error");
            System.exit(1);
        }


        return answer;
    }

    /**
     * Η συνάρτηση showInbox αποτελεί την τέταρτη λειτουργία - functionID=4 -
     * Στον Server στέλνεται ένα String της μορφής "4key", που το key είναι μία μεταβλητή με το authToken ενός χρήστη
     *
     *
     * @param key το authToken ενός χρήστη
     *
     * @return την απάντηση του Server, που είτε θα εμφανίζει όλα τα μηνύματα του χρήστη, σε περίπτωση που το key είναι έγκυρο
     *                                      είτε θα εμφανίζει ένα κατάλληλο μήνυμα αν το key είναι άκυρο
     */
    private String showInbox(String key){
        String answer="";

        try{
            outputToServer.writeUTF("4"+key);
            answer=inputFromServer.readUTF();

        }catch (IOException e){
            System.err.println("Error");
            System.exit(1);
        }

        return answer;
    }

    /**
     *
     * Η συνάρτηση readMessage αποτελεί την πέμπτη λειτουργία - functionID=5 -
     *
     * Στον Server στέλνεται ένα String της μορφής "5key^mesID" που:
     *                                      το key είναι μία μεταβλητή με το authToken ενός χρήστη
     *                                      το mesID είναι το ID του μηνύματος που ο χρήστης θέλει να διαβάσει
     *
     * Η απάντηση του server αποθηκεύεται στη μεταβλητή result.
     * Η απάντηση μπορεί να είναι της μορφής:
     *                      "0μήνυμα", αν υπάρχει το μήνυμα
     *                      "1", αν το messageID δεν υπάρχει στα εισερχόμενά του
     *                      "2", αν το key είναι άκυρο
     *                      Για αυτό στη μεταβλητή status αποθηκεύεται ο πρώτος; χαρακτήρας του String
     *
     *
     * @param key μεταβλητή με το authToken ενός χρήστη
     * @param mesID το ID του μηνύματος που ο χρήστης θέλει να διαβάσει
     *
     */
    private String readMessage(String key, String mesID){
        String answer="";

        try{
            String result;
            int status;

            outputToServer.writeUTF("5"+key+"^"+mesID);
            result=inputFromServer.readUTF();
            status=Integer.parseInt(result.charAt(0)+"");

            if(status==0){
                answer=result.substring(1);
            }
            else if (status==1){
                answer="Message ID does not exist";
            }
            else if (status==2){
                answer="Invalid Auth Token";
            }

        }catch (IOException e){
            System.err.println("Error");
            System.exit(1);
        }

        return answer;
    }

    /**
     * Η συνάρτηση deleteMessage αποτελεί την έκτη λειτουργία - functionID=6 -
     *
     * Στον server στέλνεται ένα μήνυμα της μορφής: "6key^mesID" που:
     *                                      το key είναι μία μεταβλητή με το authToken ενός χρήστη
     *                                      το mesID είναι μία μεταβλητή με το message ID που θέλει ο χρήστης να διαγράψει
     *
     * Στη μεταβλητή result αποθηκεύεται η απάντηση του server
     *
     * @param key μία μεταβλητή με το authToken ενός χρήστη
     * @param mesID μία μεταβλητή με το message ID που θέλει ο χρήστης να διαγράψει
     *
     * @return ένα String ανάλογα με την τιμή της result
     *              Αν result=0, τότε το μήνυμα διαγράφτηκε με επιτυχία
     *                 result=1, τότε το mesID δεν υπάρχει στα εισερχόμενα του χρήστη
     *                 result=2, τότε το key δεν είναι έγκυρο
     */
    private String deleteMessage(String key, String mesID){
        String answer="";

        try{
            int result;

            outputToServer.writeUTF("6"+key+"^"+mesID);
            result= inputFromServer.readInt();

            if(result==0){
                answer="OK";
            }
            else if(result==1){
                answer="Message does not exist";
            }
            else if(result==2){
                answer="Invalid Auth Token";
            }

        }catch (IOException e){
            System.err.println("Error");
            System.exit(1);
        }

        return answer;
    }


}
