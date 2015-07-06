package com.mobvoi.knowledgegraph.wearcontroltetris;

import android.util.Log;

import com.google.gson.Gson;
import com.mobvoi.android.common.api.MobvoiApiClient;
import com.mobvoi.android.common.api.ResultCallback;
import com.mobvoi.android.wearable.MessageApi;
import com.mobvoi.android.wearable.Wearable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.mobvoi.knowledgegraph.wearcontroltetris.Constants.GAME_DATA_PATH;

/**
 * Created by lili on 15-4-18.
 */
public class SendResultThread extends Thread{
    public static final String TAG="SendResultThread";
    private BlockingQueue<RecognizeResult> queue;
    private MobvoiApiClient mobvoiApiClient;
    public SendResultThread(BlockingQueue<RecognizeResult> queue, MobvoiApiClient mobvoiApiClient){
        this.queue=queue;
        this.mobvoiApiClient=mobvoiApiClient;
    }
    private volatile boolean bStop;
    public void stopMe(){
        bStop=true;
    }

    @Override
    public void run(){
        Gson gson=new Gson();
        Log.d(TAG, "running");
        while(!bStop){
            try {
                RecognizeResult pr=queue.poll(1, TimeUnit.SECONDS);
                if(pr==null) continue;
                Log.d(TAG, "send "+pr.direction);
                String s=gson.toJson(pr);
                byte[] data=Bytes.toBytes(s);
                Wearable.MessageApi.sendMessage(
                        mobvoiApiClient, "default_node", GAME_DATA_PATH, data).setResultCallback(
                        new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                if (!sendMessageResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "Failed to send message with status code: "
                                            + sendMessageResult.getStatus().getStatusCode());
                                    }
                            }
                        }
                );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "stop");
    }
}
