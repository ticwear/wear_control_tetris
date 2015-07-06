package com.mobvoi.knowledgegraph.wearcontroltetris;

import android.util.Log;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by lili on 15-4-18.
 */
public class GestureRecoginizeThread extends Thread{
    public static final String TAG="GestureRecoginizeThread";
    private BlockingQueue<float[]> gravityAccQueue;
    private BlockingQueue<RecognizeResult> resultQueue;
    private int lastDir=-1;
    private int lastDirDuration=0;
    private boolean leftHand;
    public GestureRecoginizeThread(BlockingQueue<float[]> gravityAccQueue, BlockingQueue<RecognizeResult> resultQueue, boolean leftHand){
        this.gravityAccQueue =gravityAccQueue;
        this.resultQueue=resultQueue;
        this.leftHand=leftHand;
    }

    LinkedList<DirectionData> recentResults=new LinkedList<DirectionData>();
    int maxRecentCount=80;

    float threshold=0.8f;

    private int maxEqualSubSequenceLength(){
        //TODO optimize data structure
        DirectionData[] arr=recentResults.toArray(new DirectionData[0]);
        int maxLen=0;
        for(int i=0;i<arr.length-1;i++){
            DirectionData dd=arr[i];
            int j=i+1;
            for(;j<arr.length;j++){
                if(!isSimilar(dd, arr[j])){
                    break;
                }
            }
            maxLen=Math.max(maxLen, j-i+1);
        }

        return maxLen;
    }


    private boolean isSimilar(DirectionData rr1, DirectionData rr2){
        if(rr1.dir!=rr2.dir) return false;

        float dist=(rr1.xyz[0] - rr2.xyz[0]) * (rr1.xyz[0] - rr2.xyz[0])
                +(rr1.xyz[1] - rr2.xyz[1]) * (rr1.xyz[1] - rr2.xyz[1])
                +(rr1.xyz[2] - rr2.xyz[2]) * (rr1.xyz[2] - rr2.xyz[2]);
        return dist<threshold;
    }

    private void takeAction(DirectionData dd){
        recentResults.add(dd);
        if(recentResults.size()>maxRecentCount){
            recentResults.removeFirst();
        }

        int maxSamePoints=this.maxEqualSubSequenceLength();
        if(dd.dir==RecognizeResult.DIRECTION_LEFT|| dd.dir==RecognizeResult.DIRECTION_RIGHT){
            if(maxSamePoints>=10 && maxSamePoints<20 && lastDirDuration==0){//500ms - 1s
                for(int i=0;i<2;i++) {
                    RecognizeResult rr = new RecognizeResult(dd.dir, System.currentTimeMillis());
                    try {
                        resultQueue.offer(rr, 500, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        Log.w(TAG, e.getMessage(), e);
                    }
                }
                lastDir=dd.dir;
                lastDirDuration++;
                //recentResults.clear();
            }else if(maxSamePoints>=20 && maxSamePoints<30 && lastDirDuration==1){
                for(int i=0;i<2;i++) {
                    RecognizeResult rr = new RecognizeResult(dd.dir, System.currentTimeMillis());
                    try {
                        resultQueue.offer(rr, 500, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        Log.w(TAG, e.getMessage(), e);
                    }
                }
                lastDir=dd.dir;
                lastDirDuration++;
            }else if(maxSamePoints>=30 + (lastDirDuration-2)*5){
                try {
                    for(int i=0;i<2;i++) {
                        RecognizeResult rr = new RecognizeResult(dd.dir, System.currentTimeMillis());
                        resultQueue.offer(rr, 500, TimeUnit.MILLISECONDS);
                    }
                } catch (InterruptedException e) {
                    Log.w(TAG, e.getMessage(), e);
                }
                lastDirDuration++;
            }
        }else if(dd.dir==RecognizeResult.DIRECTION_DOWN){
            if(maxSamePoints>=10 && maxSamePoints<20 && lastDirDuration==0){//500ms - 1s
                for(int i=0;i<2;i++) {
                    RecognizeResult rr = new RecognizeResult(dd.dir, System.currentTimeMillis());
                    try {
                        resultQueue.offer(rr, 500, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        Log.w(TAG, e.getMessage(), e);
                    }
                }
                lastDir=dd.dir;
                lastDirDuration++;
                //recentResults.clear();
            }else if(maxSamePoints>=20 && lastDirDuration==1){

                RecognizeResult rr = new RecognizeResult(RecognizeResult.DIRECTION_DROP, System.currentTimeMillis());
                try {
                    resultQueue.offer(rr, 500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Log.w(TAG, e.getMessage(), e);
                }

                lastDir=-1;
                lastDirDuration=0;
            }
        }else if(dd.dir==RecognizeResult.DIRECTION_UP){
            if(maxSamePoints>=20 +(lastDirDuration-1)*20){//每500ms变化一次

                RecognizeResult rr = new RecognizeResult(dd.dir, System.currentTimeMillis());
                try {
                    resultQueue.offer(rr, 500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Log.w(TAG, e.getMessage(), e);
                }

                lastDir=dd.dir;
                lastDirDuration++;
                //recentResults.clear();
            }
        }
    }

    private void processLeftHand(float[] gravityData){
        DirectionData dd=null;
        if(gravityData[1] < -8){//回到中间位置
            if(lastDir==-1 && recentResults.size()>0){
                boolean allSameDir=true;
                int dir=-1;
                for(DirectionData data: recentResults){
                    if(dir==-1){
                        dir=data.dir;
                    }else if(dir!=data.dir){
                        allSameDir=false;
                        break;
                    }
                }
                if(allSameDir){
                    RecognizeResult rr=new RecognizeResult(dir, System.currentTimeMillis());
                    try {
                        resultQueue.offer(rr, 500, TimeUnit.MILLISECONDS);
                    }catch(InterruptedException e){
                        Log.w(TAG, e.getMessage(), e);
                    }
                }
            }
            lastDir=-1;
            lastDirDuration=0;
            recentResults.clear();
            return;
            //dd=new DirectionData(-1, gravityData, System.currentTimeMillis());
        }else if(gravityData[2] < -6 && gravityData[1] < -5 || gravityData[2] < -7){//left
            dd=new DirectionData(RecognizeResult.DIRECTION_LEFT, gravityData, System.currentTimeMillis());
        }else if(gravityData[2] > 6 && gravityData[1] < -5 || gravityData[2] > 7){
            dd=new DirectionData(RecognizeResult.DIRECTION_RIGHT, gravityData, System.currentTimeMillis());
        }else if(gravityData[1] < -6 && gravityData[0] > 5 || gravityData[0] > 7){
            dd=new DirectionData(RecognizeResult.DIRECTION_UP, gravityData, System.currentTimeMillis());
        }else if(gravityData[0] < -6 && gravityData[1] < -5 || gravityData[0] < -7){
            dd=new DirectionData(RecognizeResult.DIRECTION_DOWN, gravityData, System.currentTimeMillis());
        }
        if(dd==null) return;
        this.takeAction(dd);
    }

    private void processRightHand(float[] gravityData){
        //Log.d(TAG, "data: "+gravityData[0]+","+gravityData[1]+","+gravityData[2]);
        DirectionData dd=null;
        if(gravityData[1] < -8){//回到中间位置
            if(lastDir==-1 && recentResults.size()>0){
                boolean allSameDir=true;
                int dir=-1;
                for(DirectionData data: recentResults){
                    if(dir==-1){
                        dir=data.dir;
                    }else if(dir!=data.dir){
                        allSameDir=false;
                        break;
                    }
                }
                if(allSameDir){
                    RecognizeResult rr=new RecognizeResult(dir, System.currentTimeMillis());
                    try {
                        resultQueue.offer(rr, 500, TimeUnit.MILLISECONDS);
                    }catch(InterruptedException e){
                        Log.w(TAG, e.getMessage(), e);
                    }
                }
            }
            lastDir=-1;
            lastDirDuration=0;
            recentResults.clear();
            return;
            //dd=new DirectionData(-1, gravityData, System.currentTimeMillis());
        }else if(gravityData[2] > 7 && gravityData[1]< -3 || gravityData[2] > 8){//left
            dd=new DirectionData(RecognizeResult.DIRECTION_LEFT, gravityData, System.currentTimeMillis());
        }else if(gravityData[2] < -6 && gravityData[1] < -5 || gravityData[2] < -7){
            dd=new DirectionData(RecognizeResult.DIRECTION_RIGHT, gravityData, System.currentTimeMillis());
        }else if(gravityData[0] <-5 && gravityData[1] < -6 || gravityData[0] < -7){
            dd=new DirectionData(RecognizeResult.DIRECTION_UP, gravityData, System.currentTimeMillis());
        }else if(gravityData[0] > 5 && gravityData[1] < - 7 || gravityData[0] > 7){
            dd=new DirectionData(RecognizeResult.DIRECTION_DOWN, gravityData, System.currentTimeMillis());
        }
        if(dd==null) return;

        this.takeAction(dd);
    }

    @Override
    public void run(){
        Log.d(TAG, "running");

        while(!bStop){
            try {
                float[] gravityData= gravityAccQueue.poll(100, TimeUnit.MILLISECONDS);
                if(gravityData==null) continue;

                if(!leftHand){
                    this.processRightHand(gravityData);
                }else{
                    this.processLeftHand(gravityData);
                }


            } catch (InterruptedException e) {

            }
        }
        Log.d(TAG, "stop");
    }

    private volatile boolean bStop;
    public void stopMe(){
        bStop=true;
    }


}
