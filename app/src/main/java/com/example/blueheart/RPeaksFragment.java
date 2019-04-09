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


public class RPeaksFragment extends Fragment implements View.OnClickListener, parameterSend  {

    View v2;
    Button plusb2;
    Button minusb2;
    TextView samples_number2;
    parameterSend time_size2;


    int p2=2;
    int exp2=2;



    public RPeaksFragment(){

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v2= inflater.inflate(R.layout.fragment_rpeaks, container, false);
        plusb2=v2.findViewById(R.id.plus_button_time);
        minusb2=v2.findViewById(R.id.minus_button_time);
        plusb2.setOnClickListener(this);
        minusb2.setOnClickListener(this);
        samples_number2=v2.findViewById(R.id.time_samples);
        return v2;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.plus_button_time:
                exp2++;
                int res = (int) Math.pow(p2, exp2);
                samples_number2.setText(String.valueOf(res));
                time_size2.timeSize2(res);


                break;
            case R.id.minus_button_time:
                exp2--;
                int res1 = (int) Math.pow(p2, exp2);
                samples_number2.setText(String.valueOf(res1));
                time_size2.timeSize2(res1);

                break;
            default:
                break;


        }

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            time_size2 = (parameterSend) context;

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
    public String sendString() {
        return null;
    }


}
