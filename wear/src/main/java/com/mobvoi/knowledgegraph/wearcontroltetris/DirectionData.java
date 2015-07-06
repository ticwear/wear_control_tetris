package com.mobvoi.knowledgegraph.wearcontroltetris;

/**
 * Created by lili on 15-4-19.
 */
public class DirectionData {
    public int dir;
    public float[] xyz;
    public long ts;

    public DirectionData(int direction, float[] data, long timeStamp){
        dir=direction;
        xyz=data;
        ts=timeStamp;
    }
}
