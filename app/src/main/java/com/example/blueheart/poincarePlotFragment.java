package com.example.blueheart;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class poincarePlotFragment extends Fragment implements View.OnClickListener,parameterSend {

    View v;
    public TextView pSD1,pSD2,pS;
    Button plusb;
    Button minusb;
    TextView samples_number;
    parameterSend lag_size;
    int lag=1;

    public poincarePlotFragment(){

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_poincare_plot, container, false);
        pSD1=v.findViewById(R.id.pSD1);
        pSD2=v.findViewById(R.id.pSD2);
        pS=v.findViewById(R.id.pS);
        plusb=v.findViewById(R.id.plus_button);
        minusb=v.findViewById(R.id.minus_button);
        plusb.setOnClickListener(this);
        minusb.setOnClickListener(this);
        samples_number=v.findViewById(R.id.size_lag);
        samples_number.setText(String.valueOf(1));
        return v;
    }

    public void changeFragmentTextView(String sd1,String sd2,String s) {
        TextView textsd1 = (TextView) getView().findViewById(R.id.pSD1);
        textsd1.setText(sd1);
        TextView textsd2 = (TextView) getView().findViewById(R.id.pSD2);
        textsd2.setText(sd2);
        TextView texts = (TextView) getView().findViewById(R.id.pS);
        texts.setText(s);
    }

    public void changeLagTextView(String num){
        TextView textlag = (TextView) getView().findViewById(R.id.size_lag);
        textlag.setText(num);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.plus_button:
                lag++;
                int res=lag;
                samples_number.setText(String.valueOf(res));
                lag_size.selectLag(res);
                break;
            case R.id.minus_button:
                lag--;
                int res1 = lag;
                samples_number.setText(String.valueOf(res1));
                lag_size.selectLag(res1);
                break;
            default:
                break;
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            lag_size = (parameterSend) context;

        } catch (ClassCastException ignored) {

        }
    }

    @Override
    public void selectLag(int number) {
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

}