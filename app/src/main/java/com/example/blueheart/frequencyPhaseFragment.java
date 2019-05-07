package com.example.blueheart;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class frequencyPhaseFragment extends Fragment implements View.OnClickListener, parameterSend {


    Button plusbp;
    Button minusbp;
    TextView fftsizep;
    View vp;

    int pp=2;
    int expp=8;
    private parameterSend sendFFTSizep;

    public frequencyPhaseFragment(){

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        vp=inflater.inflate(R.layout.fragment_frequency_phase, container, false);
        plusbp=vp.findViewById(R.id.plus_button);
        minusbp=vp.findViewById(R.id.minus_button);
        minusbp.setOnClickListener(this);
        plusbp.setOnClickListener(this);
        fftsizep=vp.findViewById(R.id.size_fft);
        fftsizep.setText(String.valueOf(256));

        return vp;
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.plus_button:
                expp++;
                int res = (int) Math.pow(pp, expp);
                fftsizep.setText(String.valueOf(res));
                sendFFTSizep.fftSizePhase(res);
                break;
            case R.id.minus_button:
                expp--;
                int res1 = (int) Math.pow(pp, expp);
                if (res1<=128){
                    res1=128;
                    expp=7;
                }
                fftsizep.setText(String.valueOf(res1));
                sendFFTSizep.fftSizePhase(res1);
                break;
            default:
                break;
        }
    }

    public void changePhaTextView(String num){
        TextView textlag = (TextView) getView().findViewById(R.id.size_fft);
        textlag.setText(num);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v("xxxx","onResume called");
        realTimeAnalysisActivity r;
        r=(realTimeAnalysisActivity)getActivity();
        sendFFTSizep.fftSizePhase(r.getSize());

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            sendFFTSizep = (parameterSend) context;

        } catch (ClassCastException ignored) {

        }
    }

    @Override
    public void fftSize(int number) {

    }

    @Override
    public void timeSize(int number) {

    }

    @Override
    public void timeSize2(int number) {

    }

    @Override
    public void fftSizePhase(int number) {

    }

    @Override
    public void selectLag(int number) {

    }
}