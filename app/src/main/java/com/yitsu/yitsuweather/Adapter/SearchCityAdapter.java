package com.yitsu.yitsuweather.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.yitsu.yitsuweather.Bean.City;
import com.yitsu.yitsuweather.R;

import java.util.ArrayList;
import java.util.List;

public class SearchCityAdapter extends BaseAdapter {

    private Context mContext;
    private List<City> mCityList;
    private List<City> mSearchCityList;
    private LayoutInflater mInflater;

    public SearchCityAdapter(Context context, List<City> cityList){
        mContext = context;
        mCityList = cityList;
        mSearchCityList = new ArrayList<City>();
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mSearchCityList.size();
    }

    @Override
    public Object getItem(int position) {
        return mSearchCityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.search_list_item,null);
        }
        TextView tvCityName = convertView.findViewById(R.id.tv_city_name);
        TextView tvCityCode = convertView.findViewById(R.id.tv_city_code);
        tvCityName.setText(mSearchCityList.get(position).getCity());
        tvCityCode.setText(mSearchCityList.get(position).getNumber());

        return convertView;
    }

    public Filter getFilter(){
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String str = constraint.toString().toUpperCase();
                FilterResults results = new FilterResults();
                List<City> filterList = new ArrayList<>();

                if(mCityList != null && mCityList.size() != 0){
                    for(City city : mCityList){
                        if(city.getAllPY().indexOf(str)>-1 || city.getCity().indexOf(str)>-1 || city.getAllFirstPY().indexOf(str)>-1){
                            filterList.add(city);
                        }
                    }
                }
                results.values = filterList;
                results.count = filterList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mSearchCityList = (List<City>) results.values;
                if(results.count > 0){
                    notifyDataSetChanged();
                }else{
                    notifyDataSetInvalidated();
                }

            }
        };
        return filter;
    }
}
