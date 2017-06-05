package vandyke.sia.transaction;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class TransactionIOBase {

    private String fundType;
    private boolean walletAddress;
    private String relatedAddress;
    private BigDecimal value;

    public TransactionIOBase(JSONObject json) {
        try {
            fundType = json.getString("fundtype");
            walletAddress = json.getBoolean("walletaddress");
            relatedAddress = json.getString("relatedaddress");
            value = new BigDecimal(json.getString("value"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getFundType() {
        return fundType;
    }

    public boolean isWalletAddress() {
        return walletAddress;
    }

    public String getRelatedAddress() {
        return relatedAddress;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String toString() {
        return fundType + walletAddress + relatedAddress + value;
    }
}
