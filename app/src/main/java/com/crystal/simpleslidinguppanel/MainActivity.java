package com.crystal.simpleslidinguppanel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mLvMainContent;
    private ListView mLvList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag);

        /*mLvMainContent = (ListView) findViewById(R.id.lv_mainview);
        mLvList = (ListView) findViewById(R.id.lv_dragview);
        List<String> mainContentList = new ArrayList<String>();
        for(int i = 1; i <= 100; i++){
            mainContentList.add("crystal" + i);
        }
        mLvMainContent.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, mainContentList));
        mLvList.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, mainContentList));*/
    }
}
