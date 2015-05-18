package ichakid.grandquest3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class TradeboxActivity extends Activity implements View.OnClickListener {
    private ListView listView;
    private ArrayList<Row> listArray = new ArrayList<Row>();
    private ListViewAdapter listAdapter;
    private String offer_token;
    private boolean cancel = false;
    private boolean fetch = false;
    private ProgressDialog pd = null;
    private SocketService mBoundService;
    private boolean mIsBound = false;
    private String failDesc;
    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String message = bundle.getString("serverMessage");
                System.out.println("mixitem" + message);
                if (TradeboxActivity.this.pd != null) {
                    TradeboxActivity.this.pd.dismiss();
                }
                try {
                    JSONObject json = new JSONObject(message);
                    if (json.getString("status").equals("ok")){
                        TradeboxActivity.this.finish();
                    } else if (json.getString("status").equals("fail")){
                        failDesc = json.getString("description");
                        TradeboxActivity.this.showDialog(0);
                    } else {
                        failDesc = "Error";
                        TradeboxActivity.this.showDialog(0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tradebox);
        String response;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                response = null;
            } else {
                response = extras.getString("servermessage");
            }
        } else {
            response = (String) savedInstanceState.getSerializable("servermessage");
        }
        try {
            JSONObject json = new JSONObject(response);
            JSONArray offers = json.getJSONArray("offers");
            for(int i=0; i<offers.length(); i++){
                JSONArray row = offers.getJSONArray(i);
                listArray.add(new Row(row.getInt(0), row.getInt(1), row.getInt(2), row.getInt(3), row.getBoolean(4), row.getString(5)));
            }
            showTradebox();
            doBindService();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showTradebox(){
        TextView title = (TextView) findViewById(R.id.tradeboxTitle);
        Typeface font = Typeface.createFromAsset(getAssets(), "OneDirection.ttf");
        title.setTypeface(font);

        final ImageButton closeButton = (ImageButton) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.listView);
        listAdapter = new ListViewAdapter(this, R.layout.tradebox_row, listArray);
        listView.setAdapter(listAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(SocketService.BROADCAST);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tradebox, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.closeButton:
                finish();
                break;
        }
    }

    private class Row {
        public int offerId;
        public int nOffer;
        public int demandId;
        public int nDemand;
        public boolean availability;
        public String token;

        public Row(int offerId, int nOffer, int demandId, int nDemand, boolean availability, String token) {
            this.offerId = offerId;
            this.nOffer = nOffer;
            this.demandId = demandId;
            this.nDemand = nDemand;
            this.availability = availability;
            this.token = token;
        }
    }

    private class ListViewAdapter extends ArrayAdapter<Row>{
        private Context context;
        private int layoutResourceId;
        private ArrayList<Row> data = new ArrayList<>();

        public ListViewAdapter(Context context, int layoutResourceId, ArrayList<Row> data) {
            super(context, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            RowHolder holder = null;

            if (row == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);

                holder = new RowHolder();
                holder.grid = (GridView) row.findViewById(R.id.gridView3);
                holder.action = (Button) row.findViewById(R.id.actButton);
                holder.token = (TextView) row.findViewById(R.id.tokenRowTrbox);
                row.setTag(holder);
            } else {
                holder = (RowHolder) row.getTag();
            }
            final Row aRow = data.get(position);
            if (aRow.availability){
                holder.action.setText("Cancel");
                cancel = true;
                fetch = false;
                failDesc = "Are you sure you want to cancel this offer?";
            } else {
                holder.action.setText("Fetch");
                fetch = true;
                cancel = false;
                failDesc = "Are you sure you want to fetch this offer?";
            }
            holder.token.setText(aRow.token);
            holder.action.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    offer_token = aRow.token;
                    TradeboxActivity.this.showDialog(1);
                }
            });

            ArrayList<Item> gridArray = new ArrayList<Item>();
            InventoryGridViewAdapter gridAdapter;
            gridArray.add(new Item(Item.findBitmapById(context, aRow.offerId), aRow.nOffer, true, false, aRow.offerId));
            gridArray.add(new Item(Item.findBitmapById(context, aRow.demandId), aRow.nDemand, true, false, aRow.demandId));
            gridAdapter = new InventoryGridViewAdapter(context, R.layout.inventory_grid_row, gridArray);
            holder.grid.setAdapter(gridAdapter);

            return row;
        }

        private class RowHolder {
            public GridView grid;
            public Button action;
            public TextView token;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        //EDITED PART
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mBoundService = ((SocketService.LocalBinder)service).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }

    };

    private void doBindService() {
        bindService(new Intent(this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        if(mBoundService!=null){
            mBoundService.IsBoundable();
        }
    }


    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(failDesc);
                builder.setCancelable(false);
                builder.setPositiveButton("Ok", new OkOnClickListener());
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case 1:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(failDesc);
                builder1.setCancelable(false);
                builder1.setPositiveButton("Ok", new OkOnClickListener());
                builder1.setNegativeButton("Cancel", new CancelOnClickListener());
                AlertDialog dialog1 = builder1.create();
                dialog1.show();
                break;
        }
        return super.onCreateDialog(id);
    }

    private final class CancelOnClickListener implements
            DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            Toast.makeText(getApplicationContext(), "Cancel selected, activity continues",
                    Toast.LENGTH_LONG).show();
        }
    }

    private final class OkOnClickListener implements
            DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            if (fetch || cancel){
                if (mBoundService != null) {
                    JSONObject json = new JSONObject();
                    try {
                        if (fetch) {
                            json.put("method", "fetchitem");
                            fetch = false;
                        } else if (cancel){
                            json.put("method", "canceloffer");
                            cancel = false;
                        }
                        json.put("token", LoginActivity.token);
                        json.put("offer_token", offer_token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mBoundService.sendMessage(json.toString());
                }
            }
        }
    }
}
