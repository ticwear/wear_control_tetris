package com.mobvoi.knowledgegraph.wearcontroltetris;

/**
 * Created by lili on 15-4-18.
 */
public class RecognizeResult {
    public static final int DIRECTION_LEFT=0;
    public static final int DIRECTION_RIGHT=1;
    public static final int DIRECTION_UP=2;
    public static final int DIRECTION_DOWN=3;
    public static final int DIRECTION_DROP=4;

    public long timestamp;
    public int direction;

    public RecognizeResult(int dir, long ts){
        direction=dir;
        timestamp=ts;
    }
}
