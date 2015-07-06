package com.mobvoi.knowledgegraph.wearcontroltetris;

/**
 * Created by lili on 15-4-20.
 */

import java.util.ArrayList;

public class GravityYZFeature {
    public int n;
    public double baseValue;

    public int baseCount;
    public double baseSd;
    public ArrayList<Peek> peeks=new ArrayList<Peek>(2);

    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append("n: "+n+"\t"+"baseValue: "+baseValue+"\tbaseCount: "+baseCount+"\tbaseSd: "+baseSd+"\n");
        for(Peek peek:peeks){
            sb.append(peek.toString()+"\n");
        }

        return sb.toString();
    }
}

class Peek{
    public double max;
    public int startIdx;
    public int endIdx;
    public boolean downward;

    public String toString(){
        return (downward?"downward":"upward")+ "\tmax: "+max+"\tstartIdx: "+startIdx+"\tendIdx: "+endIdx;
    }
}
