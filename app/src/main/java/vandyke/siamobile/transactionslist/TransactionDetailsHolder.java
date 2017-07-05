package vandyke.siamobile.transactionslist;

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