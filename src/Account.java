import java.util.ArrayList;
import java.util.List;

/**
 * Η κλάση Account χρησιμοποιείται για να αναπαραστήσει έναν χρήστη.
 * Οι μεταβλητές που χρησιμοποιούνται είναι:
 * @variable1 username το όνομα του χρήστη
 * @variable2 authToken, ένας μοναδικός κωδικός για κάθε χρήστη
 * @variable3 messageBox, που αποθηκεύονται όλα τα εισερχόμενα μηνύματα του χρήστη
 *
 * @author Efthymiadis Konstantinos
 */
public class Account {

    private String username;
    private int authToken;
    private List<Message> messageBox;

    public Account(String username, int authToken){
        this.username=username;
        this.authToken=authToken;
        messageBox= new ArrayList<Message>();
    }

    public String getUsername(){
        return username;
    }

    public int getAuthToken(){
        return authToken;
    }

    public List<Message> getMessageBox(){
        return messageBox;
    }

    /**
     * Συνάρτηση που προσθέτει ένα εισερχόμενο μήνυμα στο messageBox
     *
     * @param message το εισερχόμενο μήνυμα
     */
    public void addMessage(Message message){
        messageBox.add(message);
    }

}
