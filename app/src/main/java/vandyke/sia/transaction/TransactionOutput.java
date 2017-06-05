package vandyke.sia.transaction;

import org.json.JSONException;
import org.json.JSONObject;

public class TransactionOutput extends TransactionIOBase {

    private String id;
    private int maturityHeight;

    public TransactionOutput(JSONObject json) {
        super(json);
        try {
            id = json.getString("id");
            maturityHeight = json.getInt("maturityheight");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public int getMaturityHeight() {
        return maturityHeight;
    }
}
