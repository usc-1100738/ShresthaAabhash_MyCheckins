package com.sthaabhash.mycheckins.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sthaabhash.mycheckins.R;
import com.sthaabhash.mycheckins.activities.HelpActivity;
import com.sthaabhash.mycheckins.adapters.MainUserInterfaceAdapter;
import com.sthaabhash.mycheckins.database.DBHandler;
import com.sthaabhash.mycheckins.model.RecordsModel;

import java.util.ArrayList;

public class MainUserInterfaceFragment extends Fragment {
    private RecyclerView rv_itemList;
    private DBHandler dbHandler;
    private ArrayList<RecordsModel> recordsList = new ArrayList<>();
    private TextView tvRecordsStatus;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_user_interface, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvRecordsStatus = view.findViewById(R.id.tv_recordStatus);
        rv_itemList = view.findViewById(R.id.recyclerview_listUI);
        rv_itemList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        dbHandler = new DBHandler(getContext());
        recordsList = dbHandler.getRecords();

        if (recordsList.size() == 0) {
            tvRecordsStatus.setVisibility(View.VISIBLE);
        } else {
            rv_itemList.setLayoutManager(new LinearLayoutManager(getContext()));
            rv_itemList.setAdapter(new MainUserInterfaceAdapter(getContext(), recordsList));
            tvRecordsStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newRecord:
                FragmentTransaction newRecordFragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                newRecordFragmentTransaction.replace(R.id.mainDisplayFrame, new LogNewRecordsFragment()).addToBackStack(null).commit();
                break;

            case R.id.help:
                Intent intent = new Intent(getContext(), HelpActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
