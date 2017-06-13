package vandyke.siamobile.transaction;

import org.json.JSONException;
import org.json.JSONObject;
import vandyke.siamobile.api.Wallet;

import java.math.BigDecimal;

public class TransactionIOBase {

    protected String fundType;
    protected boolean walletAddress;
    protected String relatedAddress;
    protected BigDecimal value;

    TransactionIOBase(JSONObject json) {
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
        return "Address: " + relatedAddress +
                "\nAddress from wallet: " + (walletAddress ? "yes" : "no") +
                "\nType: " + fundType +
                "\nAmount: " + Wallet.hastingsToSC(value).toPlainString();
    }
}
