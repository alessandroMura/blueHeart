package com.example.blueheart;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class poincarePlotFragment extends Fragment {

    View v;
    public TextView pSD1,pSD2,pS;

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
}