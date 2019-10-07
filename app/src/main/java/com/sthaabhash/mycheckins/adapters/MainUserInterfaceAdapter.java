package com.sthaabhash.mycheckins.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sthaabhash.mycheckins.R;
import com.sthaabhash.mycheckins.fragments.LogNewRecordsFragment;
import com.sthaabhash.mycheckins.model.RecordsModel;

import java.util.ArrayList;

public class MainUserInterfaceAdapter extends RecyclerView.Adapter<MainUserInterfaceAdapter.MyListUiViewHolder> {
    private Context context;
    private ArrayList<RecordsModel> records;

    public MainUserInterfaceAdapter(Context context, ArrayList<RecordsModel> records) {
        this.context = context;
        this.records = records;
    }

    @NonNull
    @Override
    public MyListUiViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyListUiViewHolder(LayoutInflater.from(context).inflate(R.layout.item_record_list,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyListUiViewHolder myListUiViewHolder, int i) {
        RecordsModel recordsModel=records.get(i);
        myListUiViewHolder.title.setText(recordsModel.getTitle());
        myListUiViewHolder.date.setText(recordsModel.getDate());
        myListUiViewHolder.place.setText(recordsModel.getPlace());


    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class MyListUiViewHolder extends RecyclerView.ViewHolder{
        TextView title,date,place;

        public MyListUiViewHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.tv_title);
            date=itemView.findViewById(R.id.tv_date);
            place=itemView.findViewById(R.id.tv_place);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogNewRecordsFragment logNewRecordsFragment=new LogNewRecordsFragment();
                    Bundle args=new Bundle();
                    args.putString("key","delete");
                    args.putInt("position",getAdapterPosition());
                    args.putSerializable("data",records.get(getAdapterPosition()));
                    logNewRecordsFragment.setArguments(args);
                    FragmentTransaction ft = ((FragmentActivity) v.getContext()).getSupportFragmentManager().beginTransaction();

                    ft.replace(R.id.mainDisplayFrame,logNewRecordsFragment).addToBackStack(null).commit();


                }
            });
        }
    }
}
