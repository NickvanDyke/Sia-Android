package vandyke.siamobile.transactionslist;

import android.view.View;
import android.widget.TextView;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;
import vandyke.siamobile.R;

public class TransactionHeaderHolder extends GroupViewHolder {

    protected TextView transactionStatus;
    protected TextView transactionValue;

    public TransactionHeaderHolder(View itemView) {
        super(itemView);
        transactionStatus = (TextView)itemView.findViewById(R.id.transactionStatus);
        transactionValue = (TextView)itemView.findViewById(R.id.transactionValue);
    }
}
