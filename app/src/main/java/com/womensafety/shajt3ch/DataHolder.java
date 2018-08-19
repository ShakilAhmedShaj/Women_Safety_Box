package com.womensafety.shajt3ch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;



public class DataHolder extends Fragment {
    private RecyclerView mRecyclerView;

    private ArrayList<Model> mDatas;
    private static final String ARG_TITLE = "title";
    private String mTitle;
   /* Configure configure=new Configure();*/
   private final String[] First_Aid={"choking", "bleeding heavily" ,"burns" ,"a broken bone" ,"heart attack" ,"stroke" ,"seizures (epilepsy)"
           ,"diabetic emergency" ,"asthma attack"};
   private final String[] Self_Defence={"1","2","3"};

    public static DataHolder getInstance(String title) {
        DataHolder fra = new DataHolder();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TITLE, title);
        fra.setArguments(bundle);
        return fra;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mTitle = bundle.getString(ARG_TITLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_fragment, container, false);

        initData();
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.setAdapter(new RecyclerAdapter(mRecyclerView.getContext(), mDatas));

        return v;
    }

    /* private void initData() {
         mDatas = new ArrayList<>();
         for (int i = 'A'; i < 'z'; i++) {
             mDatas.add(mTitle + (char) i);
         }
     }*/
    private void initData() {
        mDatas = new ArrayList<>();
        if (mTitle.equals("First_Aid")) {
            for (int i = 0; i < First_Aid.length; i++) {

                mDatas.add(new Model(First_Aid[i]));
            }
        }

       else if (mTitle.equals("Self_defence")) {
            for (int i = 0; i < Self_Defence.length; i++) {

                mDatas.add(new Model(Self_Defence[i]));
            }
        }

    }
}

