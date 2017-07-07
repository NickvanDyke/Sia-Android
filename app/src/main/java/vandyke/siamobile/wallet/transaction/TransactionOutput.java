package vandyke.siamobile.wallet.transaction;

import org.json.JSONException;
import org.json.JSONObject;

public class TransactionOutput extends TransactionIOBase {

    private String id;
    private int maturityHeight;

    TransactionOutput(JSONObject json) {
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

    public String toString() {
//        if (!fundType.contains("miner"))
        return "ID: " + id +
                "\nMaturity height: " + maturityHeight +
                "\n" + super.toString();
//        else
//            return super.toString();
    }
}
