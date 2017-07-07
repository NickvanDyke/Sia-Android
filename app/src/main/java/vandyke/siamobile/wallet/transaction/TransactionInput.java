/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.transaction;

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
