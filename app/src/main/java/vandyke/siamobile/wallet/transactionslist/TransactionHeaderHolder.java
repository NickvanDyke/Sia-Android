/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in Licensing section of the file 'README.md'
 * included in this source code package. All rights are reserved, with the exception of what is specified there.
 */

package vandyke.siamobile.wallet.transactionslist;

import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;
import vandyke.siamobile.R;

public class TransactionHeaderHolder extends GroupViewHolder {

    @BindView(R.id.transactionStatus) protected TextView transactionStatus;
    @BindView(R.id.transactionHeaderId) protected TextView transactionId;
    @BindView(R.id.transactionValue) protected TextView transactionValue;

    public TransactionHeaderHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
