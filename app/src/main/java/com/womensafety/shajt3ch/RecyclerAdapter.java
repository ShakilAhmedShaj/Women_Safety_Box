package com.womensafety.shajt3ch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;



public class RecyclerAdapter extends RecyclerView.Adapter<Holder> {
private Context mContext;
private List<Model> mDatas;

public RecyclerAdapter(Context context, List<Model> datas) {
        mContext = context;
        mDatas = datas;
        }

@Override
public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(
        mContext).inflate(R.layout.item_main, parent, false));
        }

@Override
public void onBindViewHolder(Holder holder, int position) {
        Model model=mDatas.get(position);
        holder.bindData(model);
        }

@Override
public int getItemCount() {
        return mDatas.size();
        }
        }
