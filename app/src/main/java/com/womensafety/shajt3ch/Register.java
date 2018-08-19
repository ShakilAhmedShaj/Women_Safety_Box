package com.womensafety.shajt3ch;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;


public class Register extends Fragment implements View.OnClickListener {
    TextInputLayout namelayout, numberlayout;
    private static EditText name, number;
    public SQLiteDatabase db;
    public static SQLiteDatabase db2;
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View view = inflater.inflate(R.layout.contacts_register, container, false);
        namelayout = view.findViewById(R.id.name);
        numberlayout = view.findViewById(R.id.mobile);
        name = view.findViewById(R.id.editText2);
        number = view.findViewById(R.id.editText3);
        Button save = view.findViewById(R.id.save);
        save.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(getActivity(), "save started", Toast.LENGTH_LONG).show();

       /* name.setHint("Person name");
        number.setHint("Mobile Number");*/
        String str_name = name.getText().toString();
        String str_number = number.getText().toString();
        PhoneNumber.phoneNumber = str_number;
        Log.d("Phone", PhoneNumber.phoneNumber);
        db = getActivity().openOrCreateDatabase("NumberDB", MODE_PRIVATE, null);
        //Toast.makeText(getApplicationContext(), "db created",Toast.LENGTH_LONG).show();
        //db2 = db;
        db.execSQL("CREATE TABLE IF NOT EXISTS details(Pname VARCHAR,number VARCHAR);");
        //Toast.makeText(getApplicationContext(), "table created",Toast.LENGTH_LONG).show();

        Cursor c = db.rawQuery("SELECT * FROM details", null);
        if (c.getCount() < 1) {
            db.execSQL("INSERT INTO details VALUES('" + str_name + "','" + str_number + "');");


            Toast.makeText(getActivity(), "Successfully Saved", Toast.LENGTH_SHORT).show();
        } else {

            db.execSQL("INSERT INTO details VALUES('" + str_name + "','" + str_number + "');");
            Toast.makeText(getActivity(), "Maximun Numbers limited reached. Previous numbers are replaced.", Toast.LENGTH_SHORT).show();
        }


        db.close();


    }


    public static String getNumber(SQLiteDatabase db2) {


        Cursor c = null;
        String phone_num = "";
        //db2 = openOrCreateDatabase("NumDB", MODE_PRIVATE, null);

        c = db2.rawQuery("SELECT * FROM details LIMIT 1", null);
        if (c.getCount() > 0) {
            //c.moveToFirst();

            while (c.moveToNext()) {

                phone_num += c.getString(1);
            }

        }

        return phone_num;


    }


}

