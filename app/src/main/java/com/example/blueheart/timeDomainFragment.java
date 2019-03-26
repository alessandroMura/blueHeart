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


public class timeDomainFragment extends Fragment implements View.OnClickListener, parameterSend  {

    View v;
    Button plusb;
    Button minusb;
    TextView samples_number;
    parameterSend time_size;


    int p=2;
    int exp=2;



    public timeDomainFragment(){

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_time_domain, container, false);
        plusb=v.findViewById(R.id.plus_button_time);
        minusb=v.findViewById(R.id.minus_button_time);
        plusb.setOnClickListener(this);
        minusb.setOnClickListener(this);
        samples_number=v.findViewById(R.id.time_samples);




        return v;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.plus_button_time:
                exp++;
                int res = (int) Math.pow(p, exp);
                samples_number.setText(String.valueOf(res));
                time_size.timeSize(res);


                break;
            case R.id.minus_button_time:
                exp--;
                int res1 = (int) Math.pow(p, exp);
                samples_number.setText(String.valueOf(res1));
                time_size.timeSize(res1);

                break;
            default:
                break;


        }

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            time_size = (parameterSend) context;

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
    public String sendString() {
        return null;
    }


}
