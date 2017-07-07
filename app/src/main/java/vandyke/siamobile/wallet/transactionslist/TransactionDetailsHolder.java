/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.transactionslist;

import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import vandyke.siamobile.R;

public class TransactionDetailsHolder extends ChildViewHolder {

    protected TextView confirmationHeight;
    protected ListView inputs;
    protected ListView outputs;

    public TransactionDetailsHolder(View itemView) {
        super(itemView);
        confirmationHeight = (TextView)itemView.findViewById(R.id.transactionDetailsConfirmHeight);
        inputs = (ListView)itemView.findViewById(R.id.transactionInputsList);
        outputs = (ListView)itemView.findViewById(R.id.transactionOutputsList);
    }
}
