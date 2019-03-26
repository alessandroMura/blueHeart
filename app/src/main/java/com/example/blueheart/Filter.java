package com.example.blueheart;


import android.util.Log;


public class Filter {

    private static final String TAG = "Debugging TAG";
    private float [] currentECGReading;
    private float currentECGVal ;
    private int samplingRate = 250 ;
    private static final float[] lowPassCoeff = { 1.0000f,    2.0000f,     1.0000f,     1.0000f,     -1.4755f,    0.5869f} ;
    private static final float[] highPassCoeff = {  1.0000f,   -2.0000f,    1.0000f,    1.0000f,   -1.8227f,    0.8372f} ;
    private static final float[] diffCoeff = {-0.1250f,	-0.2500f,	0f,	0.2500f,	0.1250f} ;
    private static final int windowWidth = 80 ;
    private FixedQueue<Float> movingWindowQueue = new FixedQueue<>(windowWidth) ;
    private float movingWindowSum = 0f;

    private float[] tempArrayX = new float[diffCoeff.length] ;

    private float[] lowPass_temp = new float[lowPassCoeff.length] ;
    private float[] highPass_temp = new float[highPassCoeff.length] ;

    private int i,n=12;
    private float y0=0,y1=0,y2=0, x[] = new float[26];


    ///////
    private float highy0=0,highy1=0, highx[] = new float[66];
    private int Highn=32;




    public Filter(float[] ecg_vals) {
        currentECGReading = ecg_vals ;
    }

    public Filter() {
        System.arraycopy(diffCoeff, 0, tempArrayX, 1, diffCoeff.length-1);
    }



    public void setCurrentECGReading(float[] currentECGReading) {
        this.currentECGReading = currentECGReading;

    }

    public void staticFilter(){
        float tempVar ;
        for (int i = 0; i < currentECGReading.length; i++) {
            tempVar = 0 ;

            tempVar = lowPassNext(currentECGReading[i]) ;
            tempVar = highPassNext(tempVar) ;
            tempVar = diffFilterNext(tempVar) ;
            tempVar = squareNext(tempVar) ;

            //or//
            //tempVar = squareNext(diffFilterNext(highPassNext(lowPassNext(currentECGReading[i])))) ;
        }

    }

    public void movingWindowhandler(){


    }

    public void queueInit(){

    }

    public float lowPassNext(float upVal){
        float mod = 0 ;
        for (int i = 0; i < lowPassCoeff.length; i++) {
            mod = mod + upVal* lowPassCoeff[i] ;
        }
        return mod ;
    }

    public float lowPassNext(String upValString){

        float mod = 0, upVal ;
        upVal = Float.parseFloat(upValString) ;

        for (int i = 0; i < lowPassCoeff.length; i++) {
            mod = mod + upVal* lowPassCoeff[i] ;
        }
        return mod ;
    }


    float LowPassFilter(float val){

        x[n] = x[n + 13] = val;
        y0 = (y1*2) - y2 + x[n] - (x[n +6]*2) + x[n +12];
        y2 = y1;
        y1 = y0;
        y0 = y0/32;
        if(--n < 0)
            n = 12;

        return y0 ;

    }

    float HighPassFilter(float val){

        highx[Highn] = highx[Highn + 33] = val;
        highy0 = highy1 + highx[Highn] - highx[Highn + 32];
        highy1 = highy0;
        if(--Highn < 0)
            Highn = 32;

        return highx[Highn + 16] - (highy0/32);
    }

    public float highPassNext(float upVal){
        float mod = 0 ;

        for (int i = 0; i < highPassCoeff.length; i++) {
            mod = mod + upVal* highPassCoeff[i] ;
        }
        return mod ;
    }

    public float diffFilterNext(float upVal){
        tempArrayX[0] = upVal ;

        float mod = 0 ;
        for (int i = 0; i < diffCoeff.length; i++) {
            mod = mod + tempArrayX[i] * diffCoeff[i] ;
        }
        return mod ;
    }

    public float squareNext(float upVal){
        return (float)Math.pow(upVal, 2) ;
    }



    public float movingWindowNext(float upVal){
        float mod ;
        movingWindowQueue.add(upVal) ;
        movingWindowSum = 0 ;
        for (int i = 0; i < movingWindowQueue.size(); i++) {
            movingWindowSum = movingWindowSum + movingWindowQueue.get(i) ;
        }
        mod = movingWindowSum/movingWindowQueue.size() ;
        return mod;
    }



}
