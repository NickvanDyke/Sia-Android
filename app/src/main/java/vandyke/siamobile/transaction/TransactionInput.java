package vandyke.siamobile.transaction;

import org.json.JSONException;
import org.json.JSONObject;

public class TransactionInput extends TransactionIOBase {

    private String parentId;

    TransactionInput(JSONObject json) {
        super(json);
        try {
            parentId = json.getString("parentid");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getParentId() {
        return parentId;
    }

    public String toString() {
        return "Parent ID: " + parentId +
                "\n" + super.toString();
    }
}
