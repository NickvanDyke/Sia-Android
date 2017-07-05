package vandyke.siamobile.transactionslist;

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
