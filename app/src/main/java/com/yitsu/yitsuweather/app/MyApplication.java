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

    private void initCityList(){ //为防止主线程阻塞，在子线程中进行CityList的初始化
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
    public List<City> getCityList(){ //通过调用此函数可得到CityList
        return mCityList;
    }

    /*
    此函数主要作用是打开CityDB，返回值为CityDB
    路径上如果数据库已经存在，则直接通过此路径创建CityDB操作对象，
    否则从资源文件中读取数据库内容，并在此路径上建立数据库，然后利用此路径创建CityDB操作对象
     */
    private CityDB openCityDB(){
        String path = "/data" +
                Environment.getDataDirectory().getAbsolutePath() +
                File.separator + getPackageName() +
                File.separator + "yitsuDatabase" +
                File.separator + CityDB.CITY_DB_NAME;
        File db = new File(path);
        if(!db.exists()){ //如果数据库不存在
            String pathfolder = "/data" +
                    Environment.getDataDirectory().getAbsolutePath() +
                    File.separator + getPackageName() +
                    File.separator + "yitsuDatabase" +
                    File.separator;
            File dirFirstFolder = new File(pathfolder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();
            }
            try{ //从资源文件中读取数据库内容并写到对应路径下
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
    public static MyApplication getInstance(){ //MyApplication的单例模式，通过此方法得到MyApplication类的实例
        return mApplication;
    }


}
