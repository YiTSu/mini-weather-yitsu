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
        /*
        初始化SelectCityActivity时，从SharedPreferences中取出当前城市名称用于标题城市名的设置，若不存在当前城市名，则为默认的城市——北京
         */
        titleName.setText("当前城市:"+sharedPreferences.getString("main_city_name","北京"));
        mCityList = mApplication.getCityList();
        imgTitleback = findViewById(R.id.title_back);
        /*
        响应SelectCityActivity的返回按钮，关闭本Activity并携带cityCode数据传递给MainActivity
         */
        imgTitleback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("cityCode",currentCityCode);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        /*
        为ListView的创建准备数据，将CityList中的cityName和cityCode取出并放入Map中，最后利用for循环组成Map的列表用于SimpleAdapter的构建
         */
        List<Map<String,Object>> items = new ArrayList<>();
        for(int i = 0;i < mCityList.size();i++){
            Map<String,Object> listItems = new HashMap<>();
            listItems.put("cityName",mCityList.get(i).getCity());
            listItems.put("cityCode",mCityList.get(i).getNumber());
            items.add(listItems);
        }

        /*
        利用上面准备的数据创建SimpleAdapter，并将此Adapter赋给ListView
        R.layout.list_item为ListView每个条目的布局文件
         */
        SimpleAdapter simpleAdapter = new SimpleAdapter(SelectCityActivity.this,items,R.layout.list_item,
                new String[]{"cityName","cityCode"},new int[]{R.id.tv_city_name,R.id.tv_city_code});
        listView.setAdapter(simpleAdapter);

        /*
        为ListView设置每个Item条目的点击监听事件，当某个条目按下时，通过position取得对应条目的cityName和cityCode,利用cityName实时更新标题的城市名称，
        并将cityName存储在SharedPreferences中，用于每次打开SelectCityActivity时标题城市名的设置。
        另外，将cityCode存储在此Activity的成员变量中，当用户点击此Activity的返回按钮时，可以将cityCode返回给MainActivity
         */
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