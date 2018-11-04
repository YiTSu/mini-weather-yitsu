package com.yitsu.yitsuweather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yitsu.yitsuweather.Bean.TodayWeather;
import com.yitsu.yitsuweather.Util.NetUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity {
    SharedPreferences sharedPreferences;
    public static final int UPDATE_TODAY_WEATHER = 1;
    private ImageView imgUpdate;
    private ImageView imgCitySelect;
    private ProgressBar titleUpdateProgress;
    private TextView cityTv,timeTv,humidityTv,pmDataTv,pmQualityTv,weekTv,temperatureTv,climateTv,windTv,titleNameTv;
    /*
    通过Handler处理非主线程的传递过来的消息，通过Message得到消息内容，从而进行天气更新操作
     */
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather)msg.obj);
                    titleUpdateProgress.setVisibility(View.GONE);
                    imgUpdate.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        imgUpdate = findViewById(R.id.title_update);
        imgCitySelect = findViewById(R.id.title_city);
        titleUpdateProgress = findViewById(R.id.title_update_progress);
        sharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
        /*
        检查网络状态，通过Toast反馈当前的网络是否可用
         */
        if(NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE){
            Toast.makeText(MainActivity.this,"网络可用",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(MainActivity.this,"网络不可用",Toast.LENGTH_SHORT).show();
        }
        /*
        响应右上角的天气更新按钮进行天气的更新，我们通过将选择城市界面得到的cityCode存入SharedPreferences中，
        当用户点击右上角的天气更新按钮时，从SharedPreferences得到上次存下来的cityCode，进行对应城市天气状况的更新
        若SharedPreferences为空，则更新默认城市（北京）的天气
         */
        imgUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityCode = sharedPreferences.getString("main_city_code","101010100");
                imgUpdate.setVisibility(View.INVISIBLE);
                titleUpdateProgress.setVisibility(View.VISIBLE);
                queryWeatherCode(cityCode);
                Toast.makeText(MainActivity.this,"正在刷新",Toast.LENGTH_LONG).show();
            }
        });
        imgCitySelect.setOnClickListener(new View.OnClickListener() { //打开新的Activity选择新的城市
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SelectCityActivity.class);
                startActivityForResult(intent,1);

            }
        });
        initView();
    }

    /*
    通过选择城市界面返回得到的cityCode，进行对应城市天气状况的更新，另外将选择城市界面得到的cityCode存入
    SharedPreferences中，确保当用户点击右上角更新按钮时，更新对应城市的天气状况而不是默认城市(北京)的天气
     */
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode == 1 && resultCode == RESULT_OK){
            String newCityCode = data.getStringExtra("cityCode");
            queryWeatherCode(newCityCode);
            Toast.makeText(MainActivity.this,"正在刷新",Toast.LENGTH_LONG).show();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("main_city_code",newCityCode).apply();
             /*
            检查网络状态，通过Toast反馈当前的网络是否可用
            */
            if(NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE){
                Toast.makeText(MainActivity.this,"网络可用",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(MainActivity.this,"网络不可用",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
    初始化UI界面，将其全部置为N/A
     */
    void initView(){
        cityTv = findViewById(R.id.city);
        timeTv = findViewById(R.id.time);
        humidityTv = findViewById(R.id.humidity);
        pmDataTv = findViewById(R.id.pm_data);
        pmQualityTv = findViewById(R.id.pm2_5_quality);
        weekTv = findViewById(R.id.week_today);
        temperatureTv = findViewById(R.id.temperature);
        climateTv = findViewById(R.id.climate);
        windTv = findViewById(R.id.wind);
        titleNameTv = findViewById(R.id.title_name);
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
        titleNameTv.setText("N/A");
    }

    /*
    通过cityCode,利用查询天气的网络API在子线程查询天气状况，返回XML数据，利用XML解析函数解析得到todayWeather实例，
    并通过Message消息机制传递给主线程（由于本方法涉及到网络操作，所以要注意try、catch的异常处理函数块）
     */
    private void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey="+cityCode;
        //final String address = "http://www.baidu.com";
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                TodayWeather todayWeather = null;
                try{
                    URL url = new URL(address); //通过网络地址生成URL
                    /*
                    进行网络操作的连接及参数的设置
                     */
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    /*
                    基本的数据流操作
                     */
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder builder = new StringBuilder();
                    String str;
                    while((str = reader.readLine())!= null){
                        builder.append(str);
                    }
                    String responseStr = builder.toString();
                    todayWeather = parseXML(responseStr);
                    if(todayWeather != null){
                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }finally {
                    if(connection != null)
                    connection.disconnect();
                }


            }
        }).start();
    }

    /*
    利用XML解析工具一步一步解析XML数据，得到每一项的天气状况，通过TodayWeatherBean工具类的Setter函数存储每一项天气状况，
    最终返回包含各项天气信息的TodayWeather实例
     */
    private TodayWeather parseXML(String xmlData){

        TodayWeather todayWeather = null;
        int fengxiangCount=0;
        int fengliCount =0;
        int dateCount=0;
        int highCount =0;
        int lowCount=0;
        int typeCount =0;

        try{
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType = xmlPullParser.getEventType();
            while (eventType != xmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;

                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                                Log.d("myWeather", "city  " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                                Log.d("myWeather", "updatetime  " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                                Log.d("myWeather", "shidu:  " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                                Log.d("myWeather", "wendu:  " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                                Log.d("myWeather", "pm25: " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                                Log.d("myWeather", "quality: " + xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                Log.d("myWeather", "fengxiang: " + xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                Log.d("myWeather", "fengli: " + xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                Log.d("myWeather", "date: " + xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText());
                                Log.d("myWeather", "high: " + xmlPullParser.getText());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText());
                                Log.d("myWeather", "low: " + xmlPullParser.getText());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                Log.d("myWeather", "type: " + xmlPullParser.getText());
                                typeCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                eventType = xmlPullParser.next();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return todayWeather;
    }

    /*
    更新UI界面，利用TodayWeatherBean工具类简易地传递天气内容,通过Getter方法得到不同方面的天气状况并进行显示，更新当前的天气
     */
    void updateTodayWeather(TodayWeather todayWeather){
        titleNameTv.setText(todayWeather.getCity()+"天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime()+ "发布");
        humidityTv.setText("湿度："+todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh()+"~"+todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力:"+todayWeather.getFengli());
        Toast.makeText(MainActivity.this,"更新成功！",Toast.LENGTH_SHORT).show();
    }

}
