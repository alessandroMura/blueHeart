package com.example.blueheart;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

public class utilities {

    public static void showToast(Context context,String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void setFragment(Fragment fragment,FragmentManager fragmentManager,int id) {
        FragmentTransaction fragtransaction = fragmentManager.beginTransaction();
        fragtransaction.replace(id, fragment);
        fragtransaction.commit();
    }

}
