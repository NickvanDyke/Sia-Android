package vandyke.siamobile.transactionslist;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;
import vandyke.siamobile.R;

public class TransactionHolder extends GroupViewHolder {

    protected TextView transactionStatus;
    protected TextView transactionId;
    protected TextView transactionValue;

    public TransactionHolder(View itemView) {
        super(itemView);
        transactionStatus = (TextView)itemView.findViewById(R.id.transactionStatus);
        transactionId = (TextView)itemView.findViewById(R.id.transactionHeaderId);
        transactionValue = (TextView)itemView.findViewById(R.id.transactionValue);
        itemView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String url = "http://explore.sia.tech/hash.html?hash=" + transactionId.getText().toString().replace("\\s*", "");
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                v.getContext().startActivity(i);
            }
        });
    }
}
