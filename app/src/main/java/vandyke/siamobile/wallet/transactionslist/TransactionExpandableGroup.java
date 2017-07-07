/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.transactionslist;

import android.os.Parcel;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.Date;
import java.util.List;

public class TransactionExpandableGroup extends ExpandableGroup {

    private Date confirmationDate;

    public TransactionExpandableGroup(String value, Date confirmationDate, List items) {
        super(value, items);
        this.confirmationDate = confirmationDate;
    }

    public Date getConfirmationDate() {
        return confirmationDate;
    }

    protected TransactionExpandableGroup(Parcel in) {
        super(in);
    }
}
