package com.huawei.audiodevicekit.bluetoothsample.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;

import com.huawei.audiodevicekit.R;

import java.util.ArrayList;
import java.util.List;

public class TblAdapter2 extends BaseAdapter {
    private List<Entry2> list;
    private LayoutInflater inflater;
    public List<TblAdapter2.ViewHolder> holderList = new ArrayList<TblAdapter2.ViewHolder>();

    public TblAdapter2(Context context, List<Entry2> list){
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

        Entry2 entry = (Entry2) this.getItem(position);

        TblAdapter2.ViewHolder viewHolder;

        if(convertView == null){
            viewHolder = new TblAdapter2.ViewHolder();
            convertView = inflater.inflate(R.layout.entries_2, null);
            viewHolder._angle1 = (EditText) convertView.findViewById(R.id.edit_angle_1_2);
            viewHolder._intensity1= (EditText) convertView.findViewById(R.id.edit_intensity_1);
            viewHolder._angle2 = (EditText) convertView.findViewById(R.id.edit_angle_2_2);
            viewHolder._intensity2= (EditText) convertView.findViewById(R.id.edit_intensity_2);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (TblAdapter2.ViewHolder) convertView.getTag();
        }
        viewHolder._angle1.setText(entry._angle1);
        viewHolder._intensity1.setText(entry._intensity1);
        viewHolder._angle2.setText(entry._angle2);
        viewHolder._intensity2.setText(entry._intensity2);
        return convertView;
    }

    public static class ViewHolder{
        public EditText _angle1;
        public EditText _intensity1;
        public EditText _angle2;
        public EditText _intensity2;
    }
}
