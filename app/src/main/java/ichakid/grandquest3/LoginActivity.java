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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;

public class LoginActivity extends Activity implements View.OnClickListener {
    public static String token;
    private EditText username;
    private EditText password;
    private Button login;
    SocketService mBoundService = null;
    boolean mIsBound = false;
    String serverMessage = "";
    String msg = "";
    private ProgressDialog pd = null;
    boolean cek = false;
    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                serverMessage = "";
                serverMessage = bundle.getString("serverMessage");
                System.out.println("fullscreen: " + serverMessage);
                try {
                    JSONObject json = new JSONObject(serverMessage);
                    System.out.println(json.toString());
                    if (json.getString("status").equals("ok")){         //If it is login
                            if (json.has("token")) {
                                token = json.getString("token");
                                Intent mapIntent = new Intent(LoginActivity.this, MapActivity.class);
                                mapIntent.putExtra("login", serverMessage);
                                cek = true;
                                context.startActivity(mapIntent);
                            } else {        //if it is signup
                                if (LoginActivity.this.pd != null) {
                                    LoginActivity.this.pd.dismiss();
                                }
                                msg = "Signup success";
                                LoginActivity.this.showDialog(0);
                            }
                    } else if (json.getString("status").equals("fail")){
                        if (LoginActivity.this.pd != null) {
                            LoginActivity.this.pd.dismiss();
                        }
                        msg = json.getString("description");
                        LoginActivity.this.showDialog(0);
                    } else {
                        if (LoginActivity.this.pd != null) {
                            LoginActivity.this.pd.dismiss();
                        }
                        msg = "Error";
                        LoginActivity.this.showDialog(0);
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
        setContentView(R.layout.activity_login);
        showDialog(1);
        setupVariables();
    }

    public void authenticateLogin(View view) {
        if (mBoundService != null) {
            JSONObject json = new JSONObject();
            try {
                json.put("method", "login");
                json.put("username", username.getText().toString());
                json.put("password", password.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mBoundService.sendMessage(json.toString());
        }
    }

    private void setupVariables() {
        Typeface font = Typeface.createFromAsset(getAssets(), "OneDirection.ttf");
        TextView title = (TextView) findViewById(R.id.Title);
        title.setTypeface(font);
        username = (EditText) findViewById(R.id.usernameET);
        password = (EditText) findViewById(R.id.passwordET);
        login = (Button) findViewById(R.id.loginBtn);
        final Button registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);
        final Button exitButton = (Button) findViewById(R.id.exitButton);
        exitButton.setOnClickListener(this);
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

    private ServiceConnection mConnection = new ServiceConnection() {
        //EDITED PART
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
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
                builder.setMessage(msg);
                builder.setCancelable(false);
                builder.setPositiveButton("Ok", new OkOnClickListener());
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
            case 1:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Connect to server");
                alert.setMessage("Please write the server ip address and port number");
                final EditText serverip = new EditText(this);
                serverip.setHint("Server IP Address");
                final EditText port = new EditText(this);
                port.setHint("Port Number");
                port.setText("8000");
                serverip.setText("192.168.43.239");
                LinearLayout v = new LinearLayout(this);
                v.setOrientation(LinearLayout.VERTICAL);
                v.setDividerPadding(5);
                v.addView(serverip);
                v.addView(port);

                v.setPadding(20, 10, 20, 20);
                alert.setView(v);
                alert.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        SocketService.SERVERIP = serverip.getText().toString();
                        SocketService.SERVERPORT = Integer.parseInt(port.getText().toString());
                        startService(new Intent(LoginActivity.this, SocketService.class));
                        doBindService();
                    }
                });
                alert.show();
                break;
        }
        return super.onCreateDialog(id);
    }

    private final class OkOnClickListener implements
            DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.registerButton:
                doRegister();
                break;
            case R.id.exitButton:
                System.exit(0);
                break;
        }
    }

    private void doRegister() {
        if (mBoundService != null) {
            JSONObject json = new JSONObject();
            try {
                json.put("method", "signup");
                json.put("username", username.getText().toString());
                json.put("password", password.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mBoundService.sendMessage(json.toString());
        }

    }
}


