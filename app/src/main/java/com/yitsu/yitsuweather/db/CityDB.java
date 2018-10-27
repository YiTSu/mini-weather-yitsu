package com.yitsu.yitsuweather.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yitsu.yitsuweather.Bean.City;

import java.util.ArrayList;
import java.util.List;

/*
CityDB操作类，此类以数据库路径为构造参数，通过得到SQLiteDatabase的实例操作SQLite数据库，
可通过getAllCity方法得到数据库中的City列表
 */
public class CityDB {
    public static final String CITY_DB_NAME = "city.db";
    private static final String CITY_TABLE_NAME = "city";
    private SQLiteDatabase db;

    public CityDB(Context context, String path){
        db = context.openOrCreateDatabase(path,Context.MODE_PRIVATE,null);
    }

    public List<City> getAllCity(){  //通过SQLiteDatabase实例db操作SQLite数据库，逐项查询数据库中的内容，利用CityBean工具类逐个生成City对象并最终生成City列表
        List<City> list = new ArrayList<>();
        Cursor c = db.rawQuery("select * from "+ CITY_TABLE_NAME,null);
        while(c.moveToNext()){
            String province = c.getString(c.getColumnIndex("province"));
            String city = c.getString(c.getColumnIndex("city"));
            String number = c.getString(c.getColumnIndex("number"));
            String firstPY = c.getString(c.getColumnIndex("firstpy"));
            String allPY = c.getString(c.getColumnIndex("allpy"));
            String allFirstPY = c.getString(c.getColumnIndex("allfirstpy"));
            City item = new City(province,city,number,firstPY,allPY,allFirstPY);
            list.add(item);
        }
        return list;
    }
}
