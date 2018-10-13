package com.yitsu.yitsuweather.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtil {
    public static final int NETWORK_NONE = 0;
    public static final int NETWORK_WIFI = 1;
    public static final int NETWORK_MOBILE = 2;

    public static int getNetworkState(Context context){

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null){
            return NETWORK_NONE;
        }else{
            int netType = networkInfo.getType();
            if(netType == ConnectivityManager.TYPE_WIFI){
                return NETWORK_WIFI;
            }else if(netType == ConnectivityManager.TYPE_MOBILE){
                return NETWORK_MOBILE;
            }
        }
        return NETWORK_NONE;
    }
}
