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
import android.os.IBinder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class OfferActivity extends Activity implements View.OnClickListener {
    private int offered_item = -1;
    private int n1 = -1;
    private int demanded_item = -1;
    private int n2 = -1;

    GridView gridView;
    ArrayList<Item> inventoryGridArray = new ArrayList<Item>();
    InventoryGridViewAdapter itemGridAdapter;

    GridView gridView2;
    ArrayList<Item> itemGridArray2 = new ArrayList<Item>();
    InventoryGridViewAdapter itemGridAdapter2;
    AlertDialog dialog;
    private ProgressDialog pd = null;
    SocketService mBoundService = null;
    boolean mIsBound = false;
    private String failDesc = "";
    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String message = bundle.getString("serverMessage");
                System.out.println("mixitem" + message);
                if (OfferActivity.this.pd != null) {
                    OfferActivity.this.pd.dismiss();
                }
                try {
                    JSONObject json = new JSONObject(message);
                    if (json.getString("status").equals("ok")) {
                        failDesc = "Offer success";
                        OfferActivity.this.showDialog(0);
                    } else if (json.getString("status").equals("fail")) {
                        failDesc = json.getString("description");
                        OfferActivity.this.showDialog(0);
                    } else {
                        failDesc = "Error";
                        OfferActivity.this.showDialog(0);
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
        setContentView(R.layout.activity_offer);
        String response = null;
        Boolean resonse = false;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                response = null;
            } else {
                if (extras.containsKey("servermessage")) {
                    response = extras.getString("servermessage");
                    resonse = true;
                }
            }
        } else {
            if (savedInstanceState.containsKey("servermessage")) {
                response = (String) savedInstanceState.getSerializable("servermessage");
                resonse = true;
            }
        }
        try {
            if (resonse) {
                JSONObject json = new JSONObject(response);
                if (json.has("inventory")) {
                    JSONArray invArray = json.getJSONArray("inventory");
                    int[] array = new int[10];
                    for (int i = 0; i < 10; i++) {
                        array[i] = invArray.getInt(i);
                    }
                    showInventory(array);
                    doBindService();
                }
            } else {
                onDestroy();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private  void showInventory(int[] array){
        TextView title = (TextView) findViewById(R.id.findTitle);
        Typeface font = Typeface.createFromAsset(getAssets(), "OneDirection.ttf");
        title.setTypeface(font);

        final ImageButton closeButton = (ImageButton) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(this);

        final Button offerButton = (Button) findViewById(R.id.offeritemButton);
        offerButton.setOnClickListener(this);

        for (int i=0; i<10; i++){
            inventoryGridArray.add(new Item(Item.findBitmapById(this, i), array[i], true, false, i));
        }

        gridView = (GridView) findViewById(R.id.gridView1);
        itemGridAdapter = new InventoryGridViewAdapter(this, R.layout.inventory_grid_row, inventoryGridArray);
        gridView.setAdapter(itemGridAdapter);

        itemGridArray2.add(new Item(true, true));
        itemGridArray2.add(new Item(true, true));

        gridView2 = (GridView) findViewById(R.id.gridView2);
        itemGridAdapter2 = new InventoryGridViewAdapter(this, R.layout.inventory_grid_row, itemGridArray2);
        gridView2.setAdapter(itemGridAdapter2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(SocketService.BROADCAST);
        registerReceiver(receiver, filter);
        doBindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (receiver != null) {
//            unregisterReceiver(receiver);
//        }
        doUnbindService();
    }
    @Override
     public void onPause() {
        super.onPause();  // Always call the superclass method first
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_find, menu);
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
            case R.id.offeritemButton:
                ViewGroup itemstooffer = (ViewGroup) findViewById(R.id.itemstooffer);
                ViewGroup offer = (ViewGroup)((ViewGroup) itemstooffer.findViewById(R.id.gridView2)).getChildAt(0);
                offered_item = Integer.parseInt(((TextView) offer.findViewById(R.id.item_id)).getText().toString());
                NumberPicker np1 = (NumberPicker) findViewById(R.id.numberPicker);
                n1 = np1.getValue();
                ViewGroup demand = (ViewGroup)((ViewGroup) itemstooffer.findViewById(R.id.gridView2)).getChildAt(1);
                demanded_item = Integer.parseInt(((TextView) demand.findViewById(R.id.item_id)).getText().toString());
                NumberPicker np2 = (NumberPicker) findViewById(R.id.numberPicker2);
                n2 = np2.getValue();
                if ((offered_item >= 0) && (n1 >= 0) && (n2 >= 0) && (demanded_item >= 0) && (mBoundService != null)) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("method", "offer");
                        json.put("token", LoginActivity.token);
                        json.put("offered_item", offered_item);
                        json.put("demanded_item", demanded_item);
                        json.put("n1", n1);
                        json.put("n2", n2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mBoundService.sendMessage(json.toString());
                }
                this.pd = ProgressDialog.show(this, "Working...", "Downloading data...", true, false);
                break;
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
            // TODO Auto-generated method stub
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
                dialog = builder.create();
                dialog.show();
        }
        return super.onCreateDialog(id);
    }

    private final class OkOnClickListener implements
            DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            OfferActivity.this.finish();
        }
    }

}
