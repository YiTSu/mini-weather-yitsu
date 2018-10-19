package com.yitsu.yitsuweather.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.yitsu.yitsuweather.Bean.City;
import com.yitsu.yitsuweather.db.CityDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    private static final String TAG = "MyApp";
    private static MyApplication mApplication;
    private CityDB mCityDB;
    private List<City> mCityList;
    @Override
    public void onCreate(){
        super.onCreate();
        mApplication = this;

        mCityDB = openCityDB();
        initCityList();
    }

    private void initCityList(){
        mCityList = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareCityList();
            }
        }).start();
    }
    private boolean prepareCityList(){
        mCityList = mCityDB.getAllCity();
        for (City city : mCityList){
            Log.d("CityList",city.getCity() + ":" + city.getNumber());
        }
        return true;
    }
    public List<City> getCityList(){
        return mCityList;
    }
    private CityDB openCityDB(){
        String path = "/data" +
                Environment.getDataDirectory().getAbsolutePath() +
                File.separator + getPackageName() +
                File.separator + "yitsuDatabase" +
                File.separator + CityDB.CITY_DB_NAME;
        File db = new File(path);
        if(!db.exists()){
            String pathfolder = "/data" +
                    Environment.getDataDirectory().getAbsolutePath() +
                    File.separator + getPackageName() +
                    File.separator + "yitsuDatabase" +
                    File.separator;
            File dirFirstFolder = new File(pathfolder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();
            }
            try{
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                byte[] buffer = new byte[1024];
                int len = -1;
                while((len = is.read(buffer)) != -1){
                    fos.write(buffer,0,len);
                    fos.flush();
                }
                fos.close();
                is.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new CityDB(this,path);
    }
    public static MyApplication getInstance(){
        return mApplication;
    }


}
