package com.huawei.audiodevicekit.bluetoothsample.view;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

import com.huawei.audiodevicekit.R;

public class TblAdapter1 extends BaseAdapter {

    private List<Entry1> list;
    private LayoutInflater inflater;

    public TblAdapter1(Context context, List<Entry1> list){
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        int ret = 0;
        if(list!=null){
            ret = list.size();
        }
        return ret;
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Entry1 entry = (Entry1) this.getItem(position);

        ViewHolder viewHolder;

        if(convertView == null){

            viewHolder = new ViewHolder();

            convertView = inflater.inflate(R.layout.entries_1, null);
            viewHolder._max = (EditText) convertView.findViewById(R.id.edit_max);
            viewHolder._angle1 = (EditText) convertView.findViewById(R.id.edit_angle1);
            viewHolder._min = (EditText) convertView.findViewById(R.id.edit_min);
            viewHolder._angle2 = (EditText) convertView.findViewById(R.id.edit_angle2);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder._max.setText(entry._max);
        viewHolder._max.setTextSize(13);
        viewHolder._angle1.setText(entry._angle1);
        viewHolder._angle1.setTextSize(13);
        viewHolder._min.setText(entry._min);
        viewHolder._min.setTextSize(13);
        viewHolder._angle2.setText(entry._angle2);
        viewHolder._angle2.setTextSize(13);

        return convertView;
    }

    public static class ViewHolder{
        public EditText _max;
        public EditText _angle1;
        public EditText _min;
        public EditText _angle2;
    }
}
