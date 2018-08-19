package com.womensafety.shajt3ch;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Verify extends Fragment implements View.OnClickListener {
    TextInputLayout source_no;
    private static EditText phone;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.selfregister, container, false);
        source_no = (TextInputLayout) view.findViewById(R.id.verify);
        phone = view.findViewById(R.id.editText1);
        Button submit = view.findViewById(R.id.button1);
        submit.setOnClickListener(this);
        return view;
    }




    @Override
    public void onClick(View view) {


        /*source_no.setHint("Your Phone No.");*/
        String str_source_no = phone.getText().toString();
        SQLiteDatabase db;
        db = getActivity().openOrCreateDatabase("NumDB", Context.MODE_PRIVATE, null);
        //	if(source_no.getText()!=null){

        db.execSQL("CREATE TABLE IF NOT EXISTS source(number VARCHAR);");
        db.execSQL("INSERT INTO source VALUES('" + str_source_no + "');");
        Toast.makeText(getActivity(), str_source_no + " Successfully Saved", Toast.LENGTH_SHORT).show();
        db.close();


    }
}
