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
import android.os.CountDownTimer;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MapActivity extends Activity implements View.OnClickListener{
    public static boolean move = false;
    boolean collect = false;
    //For service
    public static SocketService mBoundService;
    private boolean mIsBound = false;
    boolean load = false;
    private String serverMessage = "";
    private Intent globalIntent;
    String msg = "Dummy";        //Message for dialog
    private ProgressDialog pd = null;       //Dialog for waiting
    private BroadcastReceiver receiver = new BroadcastReceiver(){   //BroadcastReceiver to receive intent with message from service
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                serverMessage = "";
                serverMessage = bundle.getString("serverMessage");
                System.out.println("fullscreen: " + serverMessage);
                JSONObject json = null;
                try {
                    json = new JSONObject(serverMessage);
                    if (json.getString("status").equals("ok")){         //If it is login
                        if ((globalIntent != null) && !move && !collect) {
                            if (load) {
                                globalIntent.putExtra("map", serverMessage);
                            } else {
                                globalIntent.putExtra("servermessage", serverMessage);
                            }
                            if (MapActivity.this.pd != null) {
                                MapActivity.this.pd.dismiss();
                            }
                            pooh = false;
                            MapActivity.this.startActivity(globalIntent);
                        } else if (move){
                            time = json.getLong("time");
                            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
                            String dateString = formatter.format(new Date(time * 1000L));
                            final TextView arrTime = (TextView) findViewById(R.id.timeArrText);
                            arrTime.setText("Arrive Time: " + dateString);
                            setTimer();
                            move = false;
                        } else if (collect){
                            int item = json.getInt("item");
                            msg = "You've got item " + Item.findNameById(item);
                            showDialog(0);
                            collect = false;
                        }
                    } else if (json.getString("status").equals("fail")){
                        if (MapActivity.this.pd != null) {
                            MapActivity.this.pd.dismiss();
                        }
                        msg = json.getString("description");
                        collect = false;
                        MapActivity.this.showDialog(0);
                    } else {
                        if (MapActivity.this.pd != null) {
                            MapActivity.this.pd.dismiss();
                        }
                        msg = "Error";
                        MapActivity.this.showDialog(0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private GridView gridView;      //GridView for map
    private ArrayList<Sqrchara> mapGridArray = new ArrayList<Sqrchara>();
    private MapGridViewAdapter mapGridAdapter;
    private static int row =5;    //Number of rows in map
    private static int col =5;    //Number of columns in map
    private static int poohX=0;  //Initial Pooh's position in map
    private static int poohY=0;  //Initial Pooh's position in map
    public static int moveX=0;  //Move Pooh's position in map
    public static int moveY=0;  //Move Pooh's position in map
    private static String name = "Load map first";
    private static long time;
    private TextView currTime;
    public static boolean pooh = false;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate (savedInstanceState);
        setContentView(R.layout.activity_map);
        String login = null, map = null;
        Boolean mapFound = false, loginFound = false;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                login = null; map = null;
            } else {
                if (extras.containsKey("login")) {
                    login = extras.getString("login");
                    loginFound = true;
                }
                if (extras.containsKey("map")) {
                    map = extras.getString("map");
                    mapFound =true;
                }
            }
        } else {
            if (savedInstanceState.containsKey("login")) {
                login = (String) savedInstanceState.getSerializable("login");
                loginFound = true;
            }
            if (savedInstanceState.containsKey("map")) {
                map = (String) savedInstanceState.getSerializable("map");
                mapFound =true;
            }
        }
        try {
            if (loginFound) {
                JSONObject jsonLogin = new JSONObject(login);
                poohX = jsonLogin.getInt("x");
                poohY = jsonLogin.getInt("y");
                time = jsonLogin.getLong("time");
            }
            if (mapFound) {
                JSONObject jsonMap = new JSONObject(map);
                name = jsonMap.getString("name");
                row = jsonMap.getInt("height");
                col = jsonMap.getInt("width");
            }
            showMap();
            doBindService();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setTime();
    }

    private void showMap(){
        final Button invButton = (Button) findViewById(R.id.invButton);
        invButton.setOnClickListener(this);

        final Button findButton = (Button) findViewById(R.id.findButton);
        findButton.setOnClickListener(this);

        final Button tBoxButton = (Button) findViewById(R.id.tBoxButton);
        tBoxButton.setOnClickListener(this);

        final Button collectButton = (Button) findViewById(R.id.collectButton);
        collectButton.setOnClickListener(this);

        final Button offerButton = (Button) findViewById(R.id.offerButton);
        offerButton.setOnClickListener(this);

        final Button logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(this);

        final Button mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(this);

        final TextView mapName = (TextView) findViewById(R.id.mapName);
        Typeface font = Typeface.createFromAsset(getAssets(), "OneDirection.ttf");
        mapName.setText(name);
        mapName.setTypeface(font);

        currTime = (TextView) findViewById(R.id.timeCurText);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        currTime.setText("Current Time:\r\n" + currentDateTimeString);

        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
        String dateString = formatter.format(new Date(time * 1000L));

        final TextView arrTime = (TextView) findViewById(R.id.timeArrText);
        arrTime.setText("Arrive Time: " + dateString);

        mapGridArray.clear();
        //to prepare the draw squares--as from server
        for (int i=0; i<row; i++) {
            for (int j=0; j<col; j++) {
                mapGridArray.add(new Sqrchara(Sqrchara.findBitmapById(this,1), true));
            }
        }
        gridView = (GridView) findViewById(R.id.gridMap);
        gridView.setNumColumns(col);
        mapGridAdapter = new MapGridViewAdapter(this, R.layout.activity_map_row, mapGridArray, poohY*col+poohX, col, row);
        gridView.setAdapter(mapGridAdapter);
    }

    private void setTimer(){
        final long timer = time * 1000;
        new CountDownTimer((timer - System.currentTimeMillis()), 1000){
            public void onTick(long millisUntilFinished){
                long cur = (timer - System.currentTimeMillis())/1000;
            }
            public void onFinish() {
                poohX = moveX;
                poohY = moveY;
                globalIntent = new Intent(MapActivity.this, MapActivity.class);
                MapActivity.this.finish();
                MapActivity.this.startActivity(globalIntent);
            }
        }.start();
    }

    private void setTime(){
        final long timer = time * 1000;
        new CountDownTimer(System.currentTimeMillis(), 1000){
            public void onTick(long millisUntilFinished){
                currTime = (TextView) findViewById(R.id.timeCurText);
                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
                String dateString = formatter.format(new Date(System.currentTimeMillis()));
                currTime.setText("Current Time: " + dateString);
                currTime.invalidate();
            }
            public void onFinish() {
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        collect = false;
        move = false;
        load = false;
        MapActivity.pooh = false;
        IntentFilter filter = new IntentFilter(SocketService.BROADCAST);
        registerReceiver(receiver, filter);
        doBindService();
        showMap();
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
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.logoutButton:
                Intent i=new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;

                //collectButton (to server)
            case R.id.collectButton:
                if (mBoundService != null){
                    JSONObject json = new JSONObject();
                    try {
                        json.put("method", "field");
                        json.put("token", LoginActivity.token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mBoundService.sendMessage(json.toString());
                    collect = true;
                }
               break;

                //invButton
            case R.id.invButton:
                if (mBoundService != null){
                    JSONObject json = new JSONObject();
                    try {
                        json.put("method", "inventory");
                        json.put("token", LoginActivity.token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mBoundService.sendMessage(json.toString());
                    //intent to inventory
                    globalIntent = new Intent(this, InventoryActivity.class);
                }
                break;

                //tBoxButton
            case R.id.tBoxButton:
                if (mBoundService != null){
                    JSONObject json = new JSONObject();
                    try {
                        json.put("method", "tradebox");
                        json.put("token", LoginActivity.token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mBoundService.sendMessage(json.toString());
                    //intent to tradebox
                    globalIntent = new Intent(this, TradeboxActivity.class);
                }
                break;

                //findButton
            case R.id.findButton:
                Intent findIntent = new Intent(this, FindActivity.class);
//                finish();
                startActivity(findIntent);
                break;

                //offerButton
            case R.id.offerButton:
                if (mBoundService != null){
                    JSONObject json = new JSONObject();
                    try {
                        json.put("method", "inventory");
                        json.put("token", LoginActivity.token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mBoundService.sendMessage(json.toString());
                    //intent to offer
                    globalIntent = new Intent(this, OfferActivity.class);
                }
                break;

            case R.id.mapButton:
                if (mBoundService != null){
                    JSONObject json = new JSONObject();
                    try {
                        json.put("method", "map");
                        json.put("token", LoginActivity.token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mBoundService.sendMessage(json.toString());
                    load = true;
                    //intent to inventory
                    MapActivity.pooh = false;
                    globalIntent = new Intent(this, MapActivity.class);
                }
                break;
        }
    }

    //ServiceConnection variable
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((SocketService.LocalBinder)service).getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }
    };

    //Bind activity to SocketService
    private void doBindService() {
        bindService(new Intent(this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        if(mBoundService!=null){
            mBoundService.IsBoundable();
        }
    }

    //Unbind activity from SocketService
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
                builder.setMessage(msg);
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
            dialog.dismiss();
        }
    }

}
