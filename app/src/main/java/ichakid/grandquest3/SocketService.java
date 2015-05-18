package ichakid.grandquest3;

/**
 * Created by User on 4/24/2015.
 */
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

public class SocketService extends Service {
    public static String SERVERIP = "192.168.43.239";
    public static int SERVERPORT = 8000;
    public static final String BROADCAST = "android.intent.action.Broadcast";
    PrintWriter out;
    Socket socket;
    InputStream inputStream;
    ByteArrayOutputStream byteArrayOutputStream;
    InetAddress serverAddr;
    private String serverMessage;
    private boolean mRun = false;
    private OnMessageReceived mMessageListener = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        System.out.println("I am in Ibinder onBind method");
        return myBinder;
    }

    private final IBinder myBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SocketService getService() {
            System.out.println("I am in Localbinder ");
            return SocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("I am in on create");
    }

    public void IsBoundable(){
    }

    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
            System.out.println("in sendMessage "+ message);
        }
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        System.out.println("I am in on start");
        Runnable connect = new connectSocket();
        new Thread(connect).start();
        mMessageListener = new OnMessageReceived() {
            @Override
            //here the messageReceived method is implemented
            public void messageReceived(String message) {
                //this method calls the onProgressUpdate
                System.out.println("ini broadcast: " + message);
                Intent intent = new Intent(BROADCAST);
                intent.putExtra("serverMessage", message);
                sendBroadcast(intent);
                System.out.println("Broadcast finished");
            }
        };
        return START_STICKY;
    }

    class connectSocket implements Runnable {
        @Override
        public void run() {
            mRun = true;

            try {
                //computer's IP address.
                serverAddr = InetAddress.getByName(SERVERIP);
                Log.e("TCP Client", "C: Connecting...");
                //create a socket to make the connection with the server
                SocketAddress sockaddr = new InetSocketAddress(serverAddr, SERVERPORT);
                socket = new Socket();
                socket.connect(sockaddr, 3000);

                try {
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    byteArrayOutputStream = new ByteArrayOutputStream(4096);
                    inputStream = socket.getInputStream();
                    while(mRun){
                        //Receive message
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        try {
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                byteArrayOutputStream.reset();
                                byteArrayOutputStream.write(buffer, 0, bytesRead);
                                serverMessage = byteArrayOutputStream.toString("UTF-8");
                                mMessageListener.messageReceived(serverMessage);
                            }
                            serverMessage = "";
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
                } catch (Exception e) {
                    Log.e("TCP", "S: Error", e);
                }
            } catch (SocketTimeoutException e) {
                try {
                    socket.close();
                    mRun = false;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.out.println("Connection lost");
                Intent i=new Intent(getApplicationContext(), LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } catch (Exception e) {
                Log.e("TCP", "C: Error", e);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
            mRun = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        socket = null;
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}