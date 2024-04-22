/**
 * Η κλάση Message χρησιμοποιείται για να αναπαραστήσει ένα μήνυμα που αποστέλλεται.
 * Χρησιμοποιούνται οι ακόλουθες μεταβλητές:
 *
 * @variable1 isRead δείχνει αν το μήνυμα έχει διαβαστεί
 * @variable2 sender, που αποθηκεύει το username του αποστολέα
 * @variable3 receiver, που αποθηκεύει το username του παραλήπτη
 * @variable4 body, που σε αυτήν είναι αποθηκευμένο το μήνυμα που στάλθηκε
 * @variable5 messageId, ένας μοναδικός κωδικός για κάθε ένα μήνυμα που στάλθηκε
 *
 * @author Efthymiadis Konstantinos
 */
public class Message {
    private boolean isRead;
    private String sender;
    private String receiver;
    private String body;
    int messageId;

    public Message( String sender, String receiver, String body, int messageId){
        this.isRead=false;
        this.sender=sender;
        this.receiver=receiver;
        this.body=body;
        this.messageId=messageId;
    }

    public boolean getIsRead(){
        return isRead;
    }

    public String getSender(){
        return sender;
    }

    public String getReceiver(){
        return receiver;
    }

    public String getBody(){
        return body;
    }

    public int getMessageId(){
        return messageId;
    }

    /**
     * Συνάρτηση που χρησιμοποιείται όταν διαβάζεται ένα μήνυμα και αλλάζει την τιμή της μεταβλητής isRead σε true
     */
    public void messageRead(){
        isRead=true;
    }


}
