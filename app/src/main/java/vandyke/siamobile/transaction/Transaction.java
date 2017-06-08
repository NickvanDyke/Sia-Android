package vandyke.siamobile.transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

public class Transaction {

    private String transactionId;
    private int confirmationHeight;
    private Date confirmationDate; // max value of unsigned 64-bit integer if unconfirmed
    private ArrayList<TransactionInput> inputs;
    private ArrayList<TransactionOutput> outputs;
    private BigDecimal netValue; // this is relevant to the wallet

    public Transaction(JSONObject json) {
        try {
            transactionId = json.getString("transactionid");
            confirmationHeight = json.getInt("confirmationheight");
            long confirmationTimestamp = json.getLong("confirmationtimestamp");
            confirmationDate = confirmationTimestamp == 18446744073709551616D ? null : new Date(confirmationTimestamp * 1000);
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
//            System.out.println("input: " + Wallet.hastingsToSC(transactionInput.getValue()) + "  walletAddr: " + transactionInput.isWalletAddress());
        }
        for (TransactionOutput transactionOutput : outputs) {
            if (transactionOutput.isWalletAddress())
                netValue = netValue.add(transactionOutput.getValue());
//            System.out.println("output: " + Wallet.hastingsToSC(transactionOutput.getValue()) + "  walletAddr: " + transactionOutput.isWalletAddress());
        }
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
            JSONArray confirmedTxs = json.getJSONArray("confirmedtransactions");
            for (int i = 0; i < confirmedTxs.length(); i++)
                transactions.add(0, new Transaction(confirmedTxs.getJSONObject(i))); // TODO: more optimal way of making the list most-to-least-recent
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

    public ArrayList<TransactionInput> getInputs() {
        return inputs;
    }

    public ArrayList<TransactionOutput> getOutputs() {
        return outputs;
    }

    public BigDecimal getNetValue() {
        return netValue;
    }
}
