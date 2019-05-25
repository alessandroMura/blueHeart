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


public class frequencyMagnitudeFragment extends Fragment implements View.OnClickListener, parameterSend {

    Button plusb;
    Button minusb;
    TextView fftsize;
    View v;
    int p=2;
    int exp=8;
    private parameterSend sendFFTSize;

    public frequencyMagnitudeFragment(){

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_frequency_magnitude, container, false);
        plusb=v.findViewById(R.id.plus_button);
        minusb=v.findViewById(R.id.minus_button);
        minusb.setOnClickListener(this);
        plusb.setOnClickListener(this);
        fftsize=v.findViewById(R.id.size_fft);


        return v;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.plus_button:
                exp++;
                int res = (int) Math.pow(p, exp);
                fftsize.setText(String.valueOf(res));
                sendFFTSize.fftSize(res);
                break;
            case R.id.minus_button:
                exp--;
                int res1 = (int) Math.pow(p, exp);

                if (res1<=128){
                    res1=128;
                    exp=7;
                }
                fftsize.setText(String.valueOf(res1));
                sendFFTSize.fftSize(res1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            sendFFTSize = (parameterSend) context;

        } catch (ClassCastException ignored) {

        }
    }

    public void changeMagTextView(String num){
        TextView textlag = (TextView) getView().findViewById(R.id.size_fft);
        textlag.setText(num);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v("xxxx","onResume called");
        realTimeAnalysisActivity r;
        r=(realTimeAnalysisActivity)getActivity();
        sendFFTSize.fftSize(r.getSize());

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