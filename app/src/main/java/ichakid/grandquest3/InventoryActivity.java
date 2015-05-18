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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Created by User on 4/23/2015.
 */
public class InventoryActivity extends Activity implements View.OnClickListener{
    //Inventory
    private GridView gridView;
    private ArrayList<Item> inventoryGridArray = new ArrayList<Item>();
    private InventoryGridViewAdapter inventoryGridAdapter;
    //Mixing item
    private GridView mixView;
    private ArrayList<Item> mixGridArray = new ArrayList<Item>();
    private InventoryGridViewAdapter mixGridAdapter;
    public static int item1 = -1;      //id of item to mix
    public static int item2 = -1;      //id of item to mix
    private int itemResult = -1;     //id of result mix item
    private boolean mixed = false;
    private ProgressDialog pd = null;
    //For service
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
                if (InventoryActivity.this.pd != null) {
                    InventoryActivity.this.pd.dismiss();
                }
                try {
                    JSONObject json = new JSONObject(message);
                    if (json.getString("status").equals("ok")){
                        itemResult = json.getInt("item");
                        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
                        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);
                        View resView = gridView.getChildAt(4);
                        ViewGroup container = (ViewGroup) resView.findViewById(R.id.container);
                        ((TextView) resView.findViewById(R.id.item_text)).setText("" + Item.findNameById(itemResult));
                        ImageView view = new ImageView(context);
                        view.setImageBitmap(Item.findBitmapById(context, itemResult));
                        container.addView(view);
                        GridView gridView1 = (GridView) rootView.findViewById(R.id.gridView1);
                        View resultView = gridView1.getChildAt(itemResult);
                        TextView num = (TextView) resultView.findViewById(R.id.item_text);
                        num.setText("" + (Integer.parseInt("" + num.getText()) + 1));
                        ((Button) rootView.findViewById(R.id.mixButton)).setText("Ok");
                        mixed = true;
                        InventoryActivity.item1 = -1; InventoryActivity.item2 = -1;
                    } else if (json.getString("status").equals("fail")){
                        failDesc = json.getString("description");
                        InventoryActivity.this.showDialog(0);
                    } else {
                        failDesc = "Error";
                        InventoryActivity.this.showDialog(0);
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
        setContentView(R.layout.inventory_grid);
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
            JSONArray invArray = json.getJSONArray("inventory");
            int[] array = new int[10];
            for (int i=0; i<10; i++){
                array[i] = invArray.getInt(i);
            }
            showInventory(array);
            doBindService();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void showInventory(int[] array){
        TextView title = (TextView) findViewById(R.id.inventoryTitle);
        Typeface font = Typeface.createFromAsset(getAssets(), "OneDirection.ttf");
        title.setTypeface(font);
        TextView title2 = (TextView) findViewById(R.id.mixTitle);
        title2.setTypeface(font);

        final ImageButton closeButton = (ImageButton) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(this);

        final Button mixButton = (Button) findViewById(R.id.mixButton);
        mixButton.setOnClickListener(this);
        for (int i=0; i<10; i++){
            inventoryGridArray.add(new Item(Item.findBitmapById(this, i), array[i], true, false, i));
        }

        gridView = (GridView) findViewById(R.id.gridView1);
        inventoryGridAdapter = new InventoryGridViewAdapter(this, R.layout.inventory_grid_row, inventoryGridArray);
        gridView.setAdapter(inventoryGridAdapter);

        //set mix grid view item
        Bitmap plusIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.plus);
        Bitmap equalIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.equal);

        mixGridArray.add(new Item(true, true));
        mixGridArray.add(new Item(plusIcon, false));
        mixGridArray.add(new Item(true, true));
        mixGridArray.add(new Item(equalIcon, false));
        mixGridArray.add(new Item(true, false));

        mixView = (GridView) findViewById(R.id.gridView);
        mixGridAdapter = new InventoryGridViewAdapter(this, R.layout.inventory_grid_row, mixGridArray);
        mixView.setAdapter(mixGridAdapter);

        doBindService();
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
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.closeButton:
                finish();
                break;
            case R.id.mixButton:
                if (!mixed) {
                    if ((item1 >= 0) && (item2 >= 0)) {
                        if (mBoundService != null) {
                            JSONObject json = new JSONObject();
                            try {
                                json.put("method", "mixitem");
                                json.put("token", LoginActivity.token);
                                json.put("item1", item1);
                                json.put("item2", item2);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mBoundService.sendMessage(json.toString());
                        }
                        this.pd = ProgressDialog.show(this, "Working...", "Mixing item...", true, false);
                    } else {
                        this.finish();
                    }
                } else {
                    finish();
                }
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
                AlertDialog dialog = builder.create();
                dialog.show();
        }
        return super.onCreateDialog(id);
    }

    private final class OkOnClickListener implements
            DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            InventoryActivity.this.finish();;
        }
    }

}
