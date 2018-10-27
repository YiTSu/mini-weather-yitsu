package com.yitsu.yitsuweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.yitsu.yitsuweather.Bean.City;
import com.yitsu.yitsuweather.app.MyApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectCityActivity extends Activity {

    private ImageView imgTitleback;
    private TextView titleName;
    private ListView listView;
    private List<City> mCityList;
    private MyApplication mApplication;
    private String currentCityName,currentCityCode;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        mApplication = MyApplication.getInstance();

        sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
        titleName = findViewById(R.id.title_name);
        listView = findViewById(R.id.list_view_select_city);
        titleName.setText("当前城市:"+sharedPreferences.getString("main_city_name","北京"));
        mCityList = mApplication.getCityList();
        imgTitleback = findViewById(R.id.title_back);
        imgTitleback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("cityCode",currentCityCode);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        List<Map<String,Object>> items = new ArrayList<>();
        for(int i = 0;i < mCityList.size();i++){
            Map<String,Object> listItems = new HashMap<>();
            listItems.put("cityName",mCityList.get(i).getCity());
            listItems.put("cityCode",mCityList.get(i).getNumber());
            items.add(listItems);
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(SelectCityActivity.this,items,R.layout.list_item,
                new String[]{"cityName","cityCode"},new int[]{R.id.tv_city_name,R.id.tv_city_code});
        listView.setAdapter(simpleAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentCityName = mCityList.get(position).getCity();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("main_city_name",currentCityName).apply();
                currentCityCode = mCityList.get(position).getNumber();
                titleName.setText("当前城市:"+currentCityName);
            }
        });
    }

}