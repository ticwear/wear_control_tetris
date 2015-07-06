package com.mobvoi.knowledgegraph.wearcontroltetris;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mobvoi.android.common.ConnectionResult;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.MessageEvent;
import com.mobvoi.android.wearable.Wearable;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.mobvoi.knowledgegraph.wearcontroltetris.Constants.GAME_START_PATH;
import static com.mobvoi.knowledgegraph.wearcontroltetris.Constants.GAME_START_RESP_PATH;
import static com.mobvoi.knowledgegraph.wearcontroltetris.Constants.GAME_STOP_PATH;

public class MainActivity extends Activity implements
        MessageApi.MessageListener, MobvoiApiClient.ConnectionCallbacks, MobvoiApiClient.OnConnectionFailedListener, SensorEventListener {
    private static final String TAG="MainActivity";

    private TextView mTextView;
    private MobvoiApiClient mApiClient;
    private SensorManager sensorManager;
    private Handler handler;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private boolean locked;
    private boolean isPlaying;
    private GestureRecoginizeThread recoginizeThread;
    private SendResultThread sendResultThread;
    private BlockingQueue<float[]> gravityDataQueue;

    private BlockingQueue<RecognizeResult> resultQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

        gravityDataQueue =new ArrayBlockingQueue<float[]>(100);
        resultQueue=new ArrayBlockingQueue<RecognizeResult>(100);

        mApiClient = new MobvoiApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        handler=new Handler();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mApiClient.connect();
    }

    @Override
    protected void onPause(){
        super.onPause();
        this.processStopMsg();
        mApiClient.disconnect();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(locked){
            wakeLock.release();
            locked=false;
        }

    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        Wearable.MessageApi.addListener(mApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    private Gson gson=new Gson();
    private GameSetting setting=new GameSetting();
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(GAME_START_PATH)) {
            Log.d(TAG, "start game");
            byte[] data=messageEvent.getData();
            try {
                setting = gson.fromJson(new String(data, "UTF8"), GameSetting.class);
            }catch(Exception e){
                Log.e(TAG, e.getMessage(), e);
            }
            Log.d(TAG, "hand: "+setting.watchInLeftWrist);
            Wearable.MessageApi.sendMessage(
                    mApiClient, "default_node", GAME_START_RESP_PATH, new byte[0]).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e(TAG, "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                                Toast.makeText(MainActivity.this, "发送启动响应消息失败", Toast.LENGTH_SHORT).show();
                            }else{
                                Log.d(TAG, "send start resp");
                            }
                        }
                    }
            );
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "WearControlTetris");
            wakeLock.acquire();
            locked=true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText("游戏进行中...");
                }
            });
            this.startGame();
        }else if(messageEvent.getPath().equals(GAME_STOP_PATH)){
            Log.d(TAG, "stop game");
            this.processStopMsg();


        }
    }

    private void stopGame(){
        if(!isPlaying) return;
        isPlaying =false;

        sensorManager.unregisterListener(this);
        if(recoginizeThread!=null){
            recoginizeThread.stopMe();
            recoginizeThread=null;
        }
        if(sendResultThread!=null){
            sendResultThread.stopMe();
            sendResultThread=null;
        }
        Log.d(TAG, "stopped");
    }

    private void processStopMsg(){
        this.stopGame();
        handler.post(new Runnable() {
            @Override
            public void run() {
                mTextView.setText("游戏已停止");
            }
        });
        if(locked){
            wakeLock.release();
            locked=false;
        }
    }

    private void startGame(){
        Sensor gravitySensor=sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_GAME);
        gravityDataQueue.clear();

        resultQueue.clear();
        recoginizeThread=new GestureRecoginizeThread(gravityDataQueue, resultQueue, setting.watchInLeftWrist);
        recoginizeThread.start();
        sendResultThread=new SendResultThread(resultQueue, mApiClient);
        sendResultThread.start();
        isPlaying=true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            gravityDataQueue.offer(event.values, 100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.w(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
