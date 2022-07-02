package com.huawei.audiodevicekit.bluetoothsample.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MyBluetooth {
    private BluetoothHeadset bluetoothHeadset;

    private BluetoothDevice btDevice;

    BluetoothProfile.ServiceListener blueHeadsetListener = new BluetoothProfile.ServiceListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {

            if(profile==BluetoothProfile.HEADSET){
                bluetoothHeadset=(BluetoothHeadset) proxy;
            }
            if (bluetoothHeadset != null) {
                @SuppressLint("MissingPermission") List<BluetoothDevice> devices = bluetoothHeadset.getConnectedDevices();
                System.out.println("蓝牙已连接...数量"+devices.size());
                for (int i = 0; i < devices.size(); i++) {
                    System.out.println("名称： "+devices.get(i).getName());
                    btDevice = devices.get(i);
                }
                //这里启动蓝牙语音输入
                start();
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if(profile==BluetoothProfile.HEADSET){
                bluetoothHeadset=null;
            }
        }
    };

    public void initBlueToothHeadset(Context context,Activity activity){
        BluetoothAdapter adapter;
        //android4.3之前直接用BluetoothAdapter.getDefaultAdapter()就能得到BluetoothAdapter
        if(android.os.Build.VERSION.SDK_INT<android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
            adapter=BluetoothAdapter.getDefaultAdapter();
        }else {
            BluetoothManager bm=(BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            adapter=bm.getAdapter();
        }
        adapter.getProfileProxy(context, blueHeadsetListener, BluetoothProfile.HEADSET);
    }

    /**
     * 蓝牙开始录入语音...
     */
    @SuppressLint("MissingPermission")
    public void start() {
        if (btDevice != null) {
            System.out.println(btDevice.getName()+" 蓝牙开始录入语音...");
            bluetoothHeadset.startVoiceRecognition(btDevice);
        }
    }

    /**
     * 蓝牙停止录入语音...
     */
    @SuppressLint("MissingPermission")
    public void stop() {
        if (btDevice != null) {
            System.out.println(btDevice.getName()+" 蓝牙停止录入语音...");
            bluetoothHeadset.stopVoiceRecognition(btDevice);
        }
    }
}