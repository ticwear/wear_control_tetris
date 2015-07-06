package com.mobvoi.knowledgegraph.wearcontroltetris;

public class GravityFeatureExtractor {
    public static GravityYZFeature extractYZFeature(double[] vec, double threshold){
        GravityYZFeature fea=new GravityYZFeature();
        //find baseValue
        double lastMean=0;
        for(double v:vec){
            lastMean+=v;
        }
        lastMean/=vec.length;

        for(int i=0;i<10;i++){
            double mean=0;
            int count=0;

            for(double v:vec){
                if(Math.abs(v-lastMean)<threshold){
                    mean+=v;
                    count++;
                }
            }

            if(count==0) break;
            mean/=count;
            if(Math.abs(lastMean - mean)<0.00001){
                break;
            }

        }
        fea.baseValue=lastMean;

        for(double v:vec){
            if(Math.abs(v-lastMean)<threshold){
                fea.baseCount++;
                fea.baseSd+=(v-lastMean)*(v-lastMean);
            }
            if(fea.baseCount>1){
                fea.baseSd/=(fea.baseCount-1);
            }
        }

        //find peek
        Peek peek=null;
        for(int i=0;i<vec.length;i++){
            if(vec[i]-lastMean>=threshold){//upward的peek
                if(peek==null){
                    peek=new Peek();
                    peek.downward=false;
                    peek.startIdx=i;
                    peek.endIdx=i;
                    peek.max=vec[i];
                }else{
                    if(peek.downward){//方向相反
                        //结束上一个
                        fea.peeks.add(peek);
                        peek=new Peek();
                        peek.downward=false;
                        peek.startIdx=i;
                        peek.endIdx=i;
                        peek.max=vec[i];
                    }else{//继续上一个peek
                        peek.endIdx=i;
                        peek.max=Math.max(peek.max, vec[i]);
                    }
                }
            }else if(vec[i]-lastMean<=-threshold){//downward的peek
                if(peek==null){
                    peek=new Peek();
                    peek.downward=true;
                    peek.startIdx=i;
                    peek.endIdx=i;
                    peek.max=vec[i];
                }else{
                    if(!peek.downward){//方向相反
                        //结束上一个
                        fea.peeks.add(peek);
                        peek=new Peek();
                        peek.downward=true;
                        peek.startIdx=i;
                        peek.endIdx=i;
                        peek.max=vec[i];
                    }else{//继续上一个peek
                        peek.endIdx=i;
                        peek.max=Math.min(peek.max, vec[i]);
                    }
                }
            }else{//baseline上的点
                if(peek!=null){
                    if(peek.downward){
                        if(vec[i]>=lastMean){
                            fea.peeks.add(peek);
                            peek=null;
                        }else{
                            peek.endIdx=i;
                        }
                    }else{
                        if(vec[i]<=lastMean){
                            fea.peeks.add(peek);
                            peek=null;
                        }else{
                            peek.endIdx=i;
                        }
                    }

                }
            }
        }




        fea.n=vec.length;
        return fea;
    }


}
