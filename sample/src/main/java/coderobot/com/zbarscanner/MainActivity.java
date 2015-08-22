package coderobot.com.zbarscanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.coderobot.receiptscanner.ScannerActivity;
import com.coderobot.receiptscanner.model.Product;
import com.coderobot.receiptscanner.model.Receipt;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private ScanResultAdapter mAdapter;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listview);

        mAdapter = new ScanResultAdapter();

        listView.setAdapter(mAdapter);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            ArrayList<String> receipts = data.getExtras().getStringArrayList(ScannerActivity.KEY_RECEIPTS_JSON_STRING);

            assert receipts != null;

            for (String receipt : receipts) {
                log(receipt);
                mAdapter.add(new Gson().fromJson(receipt, Receipt.class));
            }
        }
    }

    @Override
    public void onClick(View v) {
        startActivityForResult(new Intent(this, ScannerActivity.class), 1000);
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }

    private class ScanResultAdapter extends BaseExpandableListAdapter {

        private ArrayList<Receipt> mReceipts = new ArrayList<>();

        public ScanResultAdapter() {
        }

        @Override
        public int getGroupCount() {
            return mReceipts.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mReceipts.get(groupPosition).details.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mReceipts.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mReceipts.get(groupPosition).details.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) convertView = getLayoutInflater().inflate(R.layout.layout_reciept, parent, false);

            Receipt receipt = mReceipts.get(groupPosition);

            TextView tvDate = (TextView) convertView.findViewById(R.id.date);
            TextView tvNum = (TextView) convertView.findViewById(R.id.num);
            TextView tvCost = (TextView) convertView.findViewById(R.id.cost);


            StringBuilder stringBuilder = new StringBuilder(receipt.invDate);
            stringBuilder.insert(4, "/").insert(7, "/");

            tvDate.setText(stringBuilder.toString());
            tvNum.setText("發票號碼 : " + receipt.invNum);
            tvCost.setText("總計 : " + receipt.invTotalCost);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            if (convertView == null) convertView = getLayoutInflater().inflate(R.layout.layout_product, parent, false);

            Product product = mReceipts.get(groupPosition).details.get(childPosition);

            TextView tvDescription = (TextView) convertView.findViewById(R.id.description);
            TextView tvPrice = (TextView) convertView.findViewById(R.id.unit_price);
            TextView tvQuantity = (TextView) convertView.findViewById(R.id.quantity);

            tvDescription.setText("品名 : " + product.description);
            tvPrice.setText("單價 : " + product.unitPrice);
            tvQuantity.setText("數量 : " + product.quantity);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        public void add(Receipt receipt) {
            mReceipts.add(receipt);
            notifyDataSetChanged();
        }
    }
}
