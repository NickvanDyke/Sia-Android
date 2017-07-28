/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.backend.wallet.transaction;

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
        String result = "";
        if (!fundType.contains("miner"))
            result += "Address: " + relatedAddress +
                    "\nWallet address: " + (walletAddress ? "yes" : "no") + "\n";
        result += "Type: " + fundType +
                "\nAmount: " + Wallet.INSTANCE.hastingsToSC(value).toPlainString();
        return result;
    }
}
