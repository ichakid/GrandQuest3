package ichakid.grandquest3;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.IBinder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class FindActivity extends Activity implements View.OnClickListener {
    GridView gridView;
    ArrayList<Item> itemGridArray = new ArrayList<Item>();
    InventoryGridViewAdapter itemGridAdapter;

    GridView gridView2;
    ArrayList<Item> itemGridArray2 = new ArrayList<Item>();
    InventoryGridViewAdapter itemGridAdapter2;
    public static int item = -1;
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
                if (FindActivity.this.pd != null) {
                    FindActivity.this.pd.dismiss();
                }
                try {
                    JSONObject json = new JSONObject(message);
                    if (json.getString("status").equals("ok")) {
                        if (item >= 0) {
                            Intent offerboxIntent = new Intent(FindActivity.this, OffersboxActivity.class);
                            offerboxIntent.putExtra("servermessage", message);
                            FindActivity.this.finish();
                            FindActivity.this.startActivity(offerboxIntent);
                        }
                    } else if (json.getString("status").equals("fail")) {
                        failDesc = json.getString("description");
                        FindActivity.this.showDialog(0);
                    } else {
                        failDesc = "Error";
                        FindActivity.this.showDialog(0);
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
        setContentView(R.layout.activity_find);

        TextView title = (TextView) findViewById(R.id.findTitle);
        Typeface font = Typeface.createFromAsset(getAssets(), "OneDirection.ttf");
        title.setTypeface(font);

        final ImageButton closeButton = (ImageButton) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(this);

        final Button findButton = (Button) findViewById(R.id.findButton);
        findButton.setOnClickListener(this);

        for (int i=0; i<10; i++){
            itemGridArray.add(new Item(Item.findBitmapById(this, i), Item.findNameById(i), true, false, i));
        }

        gridView = (GridView) findViewById(R.id.gridView1);
        itemGridAdapter = new InventoryGridViewAdapter(this, R.layout.inventory_grid_row, itemGridArray);
        gridView.setAdapter(itemGridAdapter);

        itemGridArray2.add(new Item(true, true));

        gridView2 = (GridView) findViewById(R.id.gridView2);
        itemGridAdapter2 = new InventoryGridViewAdapter(this, R.layout.inventory_grid_row, itemGridArray2);
        gridView2.setAdapter(itemGridAdapter2);
        doBindService();
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
//        if (receiver != null) {
//            unregisterReceiver(receiver);
//        }
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
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.closeButton:
                finish();
                break;
            case R.id.findButton:
                if ((item >= 0) && (mBoundService != null)) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("method", "sendfind");
                        json.put("token", LoginActivity.token);
                        json.put("item", item);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mBoundService.sendMessage(json.toString());
                    pd = ProgressDialog.show(this, "Working...", "Finding item...", true, false);
                }
                break;
        }
    }
}
