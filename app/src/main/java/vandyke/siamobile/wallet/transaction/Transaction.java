package vandyke.siamobile.wallet.transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.api.Wallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

public class Transaction {

    protected String transactionId;
    protected boolean confirmed;
    protected int confirmationHeight;
    protected Date confirmationDate; // null if unconfirmed
    protected ArrayList<TransactionInput> inputs;
    protected ArrayList<TransactionOutput> outputs;
    protected BigDecimal netValue; // this is relevant to the wallet
    protected String netValueStringExact;
    protected String netValueStringRounded;
    protected boolean netZero;
    
    public Transaction() {
        
    }

    public Transaction(JSONObject json) {
        try {
            transactionId = json.getString("transactionid");
            confirmationHeight = json.getInt("confirmationheight");
            long confirmationTimestamp = json.getLong("confirmationtimestamp");
            confirmed = (confirmationTimestamp != 9223372036854775807D);
            confirmationDate = confirmed ? new Date(confirmationTimestamp * 1000) : new Date();
            inputs = new ArrayList<>();
            outputs = new ArrayList<>();
            JSONArray inputsJsonArray = json.getJSONArray("inputs");
            for (int i = 0; i < inputsJsonArray.length(); i++)
                inputs.add(new TransactionInput(inputsJsonArray.getJSONObject(i)));
            JSONArray outputsJsonArray = json.getJSONArray("outputs");
            for (int i = 0; i < outputsJsonArray.length(); i++)
                outputs.add(new TransactionOutput(outputsJsonArray.getJSONObject(i)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // calculate netValue
        netValue = new BigDecimal(0);
        for (TransactionInput transactionInput : inputs) {
            if (transactionInput.isWalletAddress())
                netValue = netValue.subtract(transactionInput.getValue());
        }
        for (TransactionOutput transactionOutput : outputs) {
            if (transactionOutput.isWalletAddress())
                netValue = netValue.add(transactionOutput.getValue());
        }
        netValueStringExact = netValue.toPlainString();
        netValueStringRounded = Wallet.round(Wallet.hastingsToSC(netValue));
        netZero = (netValue.compareTo(new BigDecimal(0)) == 0);
    }

    public Transaction(String jsonString) throws JSONException {
        this(new JSONObject(jsonString));
    }

    /**
     *
     * @param json The JSONObject created from the string returned by the /wallet/transactions API request
     * @return ArrayList of transactions generated from the given json. Note it goes from most-to-least-recent
     */
    public static ArrayList<Transaction> populateTransactions(JSONObject json) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        try {
            if (!json.isNull("confirmedtransactions")) {
                JSONArray confirmedTxs = json.getJSONArray("confirmedtransactions");
                for (int i = 0; i < confirmedTxs.length(); i++)
                    transactions.add(0, new Transaction(confirmedTxs.getJSONObject(i))); // TODO: more optimal way of making the list most-to-least-recent
            }
            if (!json.isNull("unconfirmedtransactions")) {
                JSONArray unconfirmedTxs = json.getJSONArray("unconfirmedtransactions");
                for (int i = 0; i < unconfirmedTxs.length(); i++)
                    transactions.add(0, new Transaction(unconfirmedTxs.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getConfirmationHeight() {
        return confirmationHeight;
    }

    public Date getConfirmationDate() {
        return confirmationDate;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public ArrayList<TransactionInput> getInputs() {
        return inputs;
    }

    public ArrayList<TransactionOutput> getOutputs() {
        return outputs;
    }

    public BigDecimal getNetValue() {
        return netValue;
    }

    public String getNetValueStringExact() {
        return netValueStringExact;
    }

    public String getNetValueStringRounded() {
        return netValueStringRounded;
    }

    public boolean isNetZero() {
        return netZero;
    }
}
