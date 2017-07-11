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
import butterknife.BindView;
import butterknife.ButterKnife;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import vandyke.siamobile.R;

public class TransactionDetailsHolder extends ChildViewHolder {

    @BindView(R.id.transactionDetailsConfirmHeight) protected TextView confirmationHeight;
    @BindView(R.id.transactionInputsList) protected ListView inputs;
    @BindView(R.id.transactionOutputsList) protected ListView outputs;

    public TransactionDetailsHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
