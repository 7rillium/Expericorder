package com.huawei.audiodevicekit.bluetoothsample.presenter;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;

import androidx.core.app.ActivityCompat;

import com.huawei.audiobluetooth.api.AudioBluetoothApi;
import com.huawei.audiobluetooth.api.Cmd;
import com.huawei.audiobluetooth.api.data.SensorData;
import com.huawei.audiobluetooth.api.data.SensorDataHelper;
import com.huawei.audiobluetooth.constant.ConnectState;
import com.huawei.audiobluetooth.layer.protocol.mbb.DeviceInfo;
import com.huawei.audiobluetooth.utils.BluetoothUtils;
import com.huawei.audiobluetooth.utils.LogUtils;
import com.huawei.audiodevicekit.R;
import com.huawei.audiodevicekit.bluetoothsample.contract.SampleBtContract;
import com.huawei.audiodevicekit.bluetoothsample.model.SampleBtModel;
import com.huawei.audiodevicekit.bluetoothsample.model.SampleBtRepository;
import com.huawei.audiodevicekit.bluetoothsample.view.TblActivity2;
import com.huawei.audiodevicekit.mvp.impl.ABaseModelPresenter;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.resource.Resource;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import java.util.HashMap;
import java.util.Locale;
import android.app.Application;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SampleBtPresenter extends ABaseModelPresenter<SampleBtContract.View, SampleBtModel>
        implements SampleBtContract.Presenter, SampleBtModel.Callback {
    private static final String TAG = "SampleBtPresenter";

    private SpeechRecognizer speech;
    private TextToSpeech tts;//创建语音对象

    private int stepInd;
    private int labInd;
    /**
     * 位置权限
     */
    private String[] locationPermission = {"android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION"};

    private int LOCATION_PERMISSION_REQUEST_CODE = 188;

    @Override
    public SampleBtModel createModel() {
        return new SampleBtRepository(this);
    }

    @Override
    public void initBluetooth(Context context) {
        LogUtils.i(TAG, "initBluetooth");
        if (!isUiDestroy()) {
            getModel().initBluetooth(context);
        }

        tts=new TextToSpeech(getUi().getContext(), new TextToSpeech.OnInitListener() {//实例化自带语音对象
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){//设置语音
                    tts.setLanguage(Locale.CHINESE);//中文
                    tts.speak("欢迎使用",TextToSpeech.QUEUE_FLUSH,null);//播报“今天温度36.3℃”
                }
            }
        });

        labInd=0;
        stepInd=0;

        checkLocationPermission((Activity) context);
        startSearch();
    }

    @Override
    public void checkLocationPermission(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat
                    .requestPermissions(activity, locationPermission, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            LogUtils.i(TAG, "Already got LOCATION Permission");
            startSearch();
        }
    }

    @Override
    public void processLocationPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startSearch();
            } else {
                if (!isUiDestroy()) {
                    getUi().onError("缺少位置权限，扫描设备失败！");
                }
            }
        }
    }

    @Override
    public void deInit() {
        LogUtils.i(TAG, "deInit");
        AudioBluetoothApi.getInstance().deInit();
    }

    @Override
    public void startSearch() {
        LogUtils.i(TAG, "startSearch");
        if (!isUiDestroy()) {
            getUi().onStartSearch();
            getModel().startSearch();
        }
    }

    @Override
    public void connect(String mac) {
        LogUtils.i(TAG, "connect");
        if (!isUiDestroy()) {
            if (!BluetoothUtils.checkMac(mac)) {
                LogUtils.e(TAG, "Invalid MAC address.connect fail ! ");
                getUi().onError("Invalid MAC address.connect fail ! ");
                return;
            }
            getModel().connect(mac);
        }
    }

    @Override
    public boolean isConnected(String mac) {
        boolean connected = AudioBluetoothApi.getInstance().isConnected(mac);
        LogUtils.i(TAG, "isConnected connected = " + connected);
        return connected;
    }

    public static String parseIatResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }

    //回调结果：
    private void printResult(RecognizerResult results, EditText mResultText) {
        String text = parseIatResult(results.getResultString());
        // 自动填写地址
        mResultText.append(text);
    }

    //TODO 开始说话：
    public void btnVoice(EditText mResultText) {
        RecognizerDialog dialog = new RecognizerDialog(getUi().getContext(), null);
        // 设置参数
        dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        dialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        dialog.setParameter(SpeechConstant.VAD_BOS, "10000");
        dialog.setParameter(SpeechConstant.VAD_EOS, "10000");

        dialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                printResult(recognizerResult, mResultText);
            }
            @Override
            public void onError(SpeechError speechError) {
            }
        });
        dialog.show();
        Toast.makeText(getUi().getContext(), "请开始说话", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void disConnect(String mac) {
        LogUtils.i(TAG, "disConnect");
        if (!isUiDestroy()) {
            if (!BluetoothUtils.checkMac(mac)) {
                LogUtils.e(TAG, "Invalid MAC address.disConnect fail ! ");
                getUi().onError("Invalid MAC address.disConnect fail ! ");
                return;
            }
            getModel().disConnect(mac);
        }
    }

    public void speechStartStop(String[] guide){
        LogUtils.i(TAG, "speechStart"+guide.length);
        if(tts.isSpeaking()){
            speechStop();
        }
        else {
            speechStart(guide);
        }
    }

    public void speechStart(String[] guide){
        LogUtils.i(TAG, "speechStart"+guide.length);
        if (labInd<guide.length ){
            String[] steps = guide[labInd].split("###");
            if (stepInd<steps.length){
                tts.speak(steps[stepInd],TextToSpeech.QUEUE_ADD,null);
                getUi().onSendCmdSuccess(steps[stepInd]);
            }
        }
    }

    public void speechStop(){
        tts.stop();
        tts.speak("已暂停",TextToSpeech.QUEUE_FLUSH,null);
    }

    public void speechReplay(String[] guide){
        if(tts.isSpeaking()){
            tts.stop();
        }
        speechStart(guide);
    }

    public void nextStep(String[] guide){
        if(tts.isSpeaking()) {
            tts.stop();
        }
        String[] steps = guide[labInd].split("###");
        stepInd+=1;
        if(stepInd>=steps.length){
            nextLab(guide);
        }
        else {
            speechStart(guide);
        }
    }

    public void lastStep(String[] guide){
        if(tts.isSpeaking()) {
            tts.stop();
        }
        if(stepInd>0){
            stepInd-=1;
            speechStart(guide);
        }
        else {
            lastLab(guide);
        }
    }

    public void nextLab(String[] guide){
        if(tts.isSpeaking()) {
            tts.stop();
        }
        labInd+=1;
        stepInd=0;
        if(labInd>=guide.length){
            //              stateInfo = getUi().getContext().getResources().getString(R.string.data_channel_ready);
            //                registerListener(device.getAddress());
            //Intent intent= new Intent();
            //intent.setClass(mContext, InfoActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //mContext.startActivity(intent);

            tts.speak("您已完成所有实验步骤",TextToSpeech.QUEUE_FLUSH,null);
        }
        else {
            speechStart(guide);
        }
    }

    public void noteRecord(){
        Intent intent = new Intent();
        Context mContext = getUi().getContext();
        intent.setClass(mContext,TblActivity2.class);
        mContext.startActivity(intent);
    }

    public void lastLab(String[] guide){
        if(tts.isSpeaking()) {
            tts.stop();
        }
        if(labInd>0) labInd-=1;
        stepInd=0;
        speechStart(guide);

    }

    @Override
    public void sendCmd(String mac, int cmdType) {
        LogUtils.i(TAG, "sendCmd");
        if (!isUiDestroy()) {
            if (!BluetoothUtils.checkMac(mac)) {
                LogUtils.e(TAG, "Invalid MAC address.sendCmd fail ! ");
                getUi().onError("Invalid MAC address.sendCmd fail ! ");
                return;
            }
            getModel().sendCmd(mac, cmdType);
        }
    }

    @Override
    public void registerListener(String mac) {
        LogUtils.i(TAG, "registerListener");
        if (!isUiDestroy()) {
            getModel().registerListener(mac, result -> {
                LogUtils.i(TAG, "result = " + result);
                byte[] appData = result.getAppData();
                if (!isUiDestroy()) {
                    SensorData sensorData = SensorDataHelper.genSensorData(appData);
                    getUi().onSensorDataChanged(sensorData);
                }
            });
        }
    }

    @Override
    public void unregisterListener(String mac) {
        if (!isUiDestroy()) {
            getModel().unregisterListener(mac);
        }
    }

    @Override
    public void onDeviceFound(DeviceInfo deviceInfo) {
        if (!isUiDestroy()) {
            getUi().onDeviceFound(deviceInfo);
        }
    }

    @Override
    public void onConnectStateChanged(BluetoothDevice device, int state) {
        String stateInfo;
        switch (state) {
            case ConnectState.STATE_UNINITIALIZED:
                stateInfo = getUi().getContext().getResources().getString(R.string.not_initialized);
                break;
            case ConnectState.STATE_CONNECTING:
                stateInfo = getUi().getContext().getResources().getString(R.string.connecting);
                break;
            case ConnectState.STATE_CONNECTED:
                stateInfo = getUi().getContext().getResources().getString(R.string.connected);
                break;
            case ConnectState.STATE_DATA_READY:
                stateInfo = getUi().getContext().getResources().getString(R.string.data_channel_ready);
                registerListener(device.getAddress());
                break;
            case ConnectState.STATE_DISCONNECTED:
                stateInfo = getUi().getContext().getResources().getString(R.string.disconnected);
                unregisterListener(device.getAddress());
                break;
            default:
                stateInfo = getUi().getContext().getResources().getString(R.string.unknown) + state;
                break;
        }
        LogUtils.i(TAG,
                "onConnectStateChanged  state = " + state + "," + stateInfo + "," + ConnectState
                        .toString(state));
        if (!isUiDestroy()) {
            getUi().onConnectStateChanged(stateInfo);
            getUi().onDeviceChanged(device);
        }
    }


    @Override
    public void onSendCmdResult(boolean isSuccess, Object result) {
        LogUtils.i(TAG, "onSendCmdResult  isSuccess = " + isSuccess + ", result = " + result);
        if (!isUiDestroy()) {
            if (isSuccess) {
                getUi().onSendCmdSuccess(result);
            } else {
                getUi().onError("AT指令发送失败，错误码：" + result);
            }
        }
    }
}
