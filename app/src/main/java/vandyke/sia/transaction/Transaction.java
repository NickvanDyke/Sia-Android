package vandyke.sia.transaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Transaction {

    private JSONObject json;
    private String transactionId;
    private int confirmationHeight;
    private long confirmationTimestamp; // max value of unsigned 64-bit integer if unconfirmed
    private ArrayList<TransactionInput> inputs;
    private ArrayList<TransactionOutput> outputs;

    public Transaction(JSONObject jsonObject) {
        try {
            json = jsonObject;
            transactionId = json.getString("transactionid");
            confirmationHeight = json.getInt("confirmationheight");
            confirmationTimestamp = json.getLong("confirmationtimestamp");
            inputs = new ArrayList<>();
            outputs = new ArrayList<>();
            JSONArray inputsJsonArray = json.getJSONArray("inputs");
            for (int i = 0; i < inputsJsonArray.length(); i++) {
                inputs.add(new TransactionInput(inputsJsonArray.getJSONObject(i)));
            }
            JSONArray outputsJsonArray = json.getJSONArray("outputs");
            for (int i = 0; i < outputsJsonArray.length(); i++)
                outputs.add(new TransactionOutput(outputsJsonArray.getJSONObject(i)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Transaction(String jsonString) throws JSONException {
        this(new JSONObject(jsonString));
    }

    /**
     *
     * @param json The JSONObject created from the string returned by the /wallet/transactions API request
     * @return ArrayList of transactions generated from the given json
     */
    public static ArrayList<Transaction> populateTransactions(JSONObject json) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        try {
            JSONArray confirmedTxs = json.getJSONArray("confirmedtransactions");
            for (int i = 0; i < confirmedTxs.length(); i++)
                transactions.add(new Transaction(confirmedTxs.getJSONObject(i)));
            if (!json.isNull("unconfirmedtransactions")) {
                JSONArray unconfirmedTxs = json.getJSONArray("unconfirmedtransactions");
                for (int i = 0; i < unconfirmedTxs.length(); i++)
                    transactions.add(new Transaction(unconfirmedTxs.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public JSONObject getJson() {
        return json;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getConfirmationHeight() {
        return confirmationHeight;
    }

    public long getConfirmationTimestamp() {
        return confirmationTimestamp;
    }

    public ArrayList<TransactionInput> getInputs() {
        return inputs;
    }

    public ArrayList<TransactionOutput> getOutputs() {
        return outputs;
    }

    public String toString() {
        return json.toString();
    }
}
