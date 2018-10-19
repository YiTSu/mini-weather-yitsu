package com.yitsu.yitsuweather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class SelectCityActivity extends Activity {

    private ImageView imgTitleback;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);

        imgTitleback = findViewById(R.id.title_back);
        imgTitleback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("cityCode","101160101");
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }

}
