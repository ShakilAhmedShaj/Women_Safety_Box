package com.womensafety.shajt3ch;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



public class Display extends Fragment {
    Cursor c;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        /*return inflater.inflate(R.layout.display, container, false);*/
        View view = inflater.inflate(R.layout.display, container, false);

        SQLiteDatabase db;
        db = getActivity().openOrCreateDatabase("NumberDB",Context.MODE_PRIVATE, null);

        c = db.rawQuery("SELECT * FROM details", null);
        if (c.getCount() == 0) {
            showMessage("Error", "No records found.");
            return inflater.inflate(R.layout.display, container, false);
        }
        StringBuffer buffer = new StringBuffer();
        while (c.moveToNext()) {
            buffer.append("Name: " + c.getString(0) + "\n");
            buffer.append("Number: " + c.getString(1) + "\n");
        }
        showMessage("Details", buffer.toString());
        /*Intent i_startservice=new Intent(Display.this,BgService.class);
        startService(i_startservice);
*/ return view;

    }



    public void showMessage(String title,String message)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }




    /*public void back(View v) {
        Intent i_back=new Intent(Display.this,MainActivity.class);
        startActivity(i_back);

    }
*/


}
