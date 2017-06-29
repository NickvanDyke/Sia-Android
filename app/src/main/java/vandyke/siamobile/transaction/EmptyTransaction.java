package vandyke.siamobile.transaction;

import java.util.Date;

public class EmptyTransaction extends Transaction {
    public EmptyTransaction() {
        transactionId = "No transactions; they will appear in the space below";
        netValueStringExact = "";
        netValueStringRounded = "";
        confirmationDate = new Date();
        confirmed = true;
    }
}
