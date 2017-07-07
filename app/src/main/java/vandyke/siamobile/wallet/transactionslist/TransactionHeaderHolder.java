/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.transactionslist;

import android.view.View;
import android.widget.TextView;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;
import vandyke.siamobile.R;

public class TransactionHeaderHolder extends GroupViewHolder {

    protected TextView transactionStatus;
    protected TextView transactionId;
    protected TextView transactionValue;

    public TransactionHeaderHolder(View itemView) {
        super(itemView);
        transactionStatus = (TextView)itemView.findViewById(R.id.transactionStatus);
        transactionId = (TextView)itemView.findViewById(R.id.transactionHeaderId);
        transactionValue = (TextView)itemView.findViewById(R.id.transactionValue);
    }
}
