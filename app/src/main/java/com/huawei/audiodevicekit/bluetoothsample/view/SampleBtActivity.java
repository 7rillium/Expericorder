package com.huawei.audiodevicekit.bluetoothsample.view;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.audiobluetooth.api.Cmd;
import com.huawei.audiobluetooth.api.data.SensorData;
import com.huawei.audiobluetooth.layer.protocol.mbb.DeviceInfo;
import com.huawei.audiobluetooth.utils.DateUtils;
import com.huawei.audiobluetooth.utils.LogUtils;
import com.huawei.audiodevicekit.R;
import com.huawei.audiodevicekit.bluetoothsample.contract.SampleBtContract;
import com.huawei.audiodevicekit.bluetoothsample.presenter.SampleBtPresenter;
import com.huawei.audiodevicekit.bluetoothsample.view.adapter.SingleChoiceAdapter;
import com.huawei.audiodevicekit.mvp.view.support.BaseAppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialogListener;


public class SampleBtActivity
        extends BaseAppCompatActivity<SampleBtContract.Presenter, SampleBtContract.View>
        implements SampleBtContract.View  {
    private static final String TAG = "SampleBtActivity";

    private TextView tvDevice;

    private TextView tvStatus;

    private ListView listView;

    private TextView tvSendCmdResult;


    private ImageButton btnStart;
    private ImageButton btnNextStep;
    private ImageButton btnLastStep;

    private Button btnSendCmd;

    private RecyclerView rvFoundDevice;

    private SingleChoiceAdapter mAdapter;

    private Cmd mATCmd = Cmd.SENSOR_DATA_UPLOAD_OPEN;

    private String mMac;

    private List<Map<String, String>> maps;

    private SimpleAdapter simpleAdapter;

    private MyBluetooth bluetooth;

    private int int_yaw;
    private int int_roll;
    private int int_pitch;


    private ImageButton btnRecord;

    private EditText mResultText;


    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public SampleBtContract.Presenter createPresenter() {
        return new SampleBtPresenter();
    }

    @Override
    public SampleBtContract.View getUiImplement() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetooth = new MyBluetooth();
        bluetooth.initBlueToothHeadset(this,this);
        RecognizerDialogListener speechListener = new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {

            }

            @Override
            public void onError(SpeechError speechError) {

            }
        };
    }


    @Override
    protected int getResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        tvDevice = findViewById(R.id.tv_device);
        tvStatus = findViewById(R.id.tv_status);
        listView = findViewById(R.id.listview);
        tvSendCmdResult = findViewById(R.id.tv_send_cmd_result);

        btnStart = findViewById(R.id.btn_startspeech);

        btnNextStep = findViewById(R.id.btn_nextstep);
        btnLastStep = findViewById(R.id.btn_laststep);

        btnRecord = findViewById(R.id.btn_record);
//        mResultText = ((EditText) findViewById(R.id.result));
        btnSendCmd = findViewById(R.id.btn_send_cmd);
        rvFoundDevice = findViewById(R.id.found_device);

        initRecyclerView();
        maps = new ArrayList<>();
        simpleAdapter = new SimpleAdapter(this, maps, android.R.layout.simple_list_item_1,
                new String[] {"data"}, new int[] {android.R.id.text1});
        listView.setAdapter(simpleAdapter);

        int_pitch=15;
        int_roll=15;
        int_yaw=15;
    }

    private void initRecyclerView() {
        SingleChoiceAdapter.SaveOptionListener mOptionListener = new SingleChoiceAdapter.SaveOptionListener() {
            @Override
            public void saveOption(String optionText, int pos) {
                LogUtils.i(TAG, "saveOption optionText = " + optionText + ",pos = " + pos);
                mMac = optionText.substring(1, 18);
                boolean connected = getPresenter().isConnected(mMac);
                if (connected) {
                    getPresenter().disConnect(mMac);
                } else {
                    getPresenter().connect(mMac);
                }
            }

            @Override
            public void longClickOption(String optionText, int pos) {
                LogUtils.i(TAG, "longClickOption optionText = " + optionText + ",pos = " + pos);
            }
        };
        mAdapter = new SingleChoiceAdapter(this, new ArrayList<>());
        mAdapter.setSaveOptionListener(mOptionListener);
        rvFoundDevice.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rvFoundDevice.setAdapter(mAdapter);
    }

    @Override
    protected void initData() {
        getPresenter().initBluetooth(this);
    }

    @Override
    protected void setOnclick() {
        super.setOnclick();
        Resources res = getResources();
        String[] textguide = res.getStringArray(R.array.guide);



        btnStart.setOnClickListener(v -> getPresenter().speechStartStop(textguide));
        btnNextStep.setOnClickListener(v->getPresenter().nextStep(textguide));
        btnLastStep.setOnClickListener(v->getPresenter().lastStep(textguide));

        btnSendCmd.setOnClickListener(v -> getPresenter().sendCmd(mMac, mATCmd.getType()));

        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=a030f13f");
//        btnRecord.setOnClickListener(v-> getPresenter().btnVoice(mResultText));
        btnRecord.setOnClickListener(v-> getPresenter().noteRecord());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getPresenter().processLocationPermissionsResult(requestCode, grantResults);
    }

    @Override
    public void onDeviceFound(DeviceInfo info) {
        if (mAdapter == null) {
            return;
        }
        runOnUiThread(() -> mAdapter
                .pushData(String.format("[%s] %s", info.getDeviceBtMac(), "HUAWEI Eyewear")));
    }

    @Override
    public void onStartSearch() {
        if (mAdapter != null) {
            runOnUiThread(() -> mAdapter.clearData());
        }
    }

    @Override
    public void onDeviceChanged(BluetoothDevice device) {
        if (tvDevice != null) {
            runOnUiThread(() -> tvDevice
                    .setText(String.format("[%s] %s", device.getAddress(), "HUAWEI Eyewear")));
        }
    }

    @Override
    public void onConnectStateChanged(String stateInfo) {
        if (tvStatus != null) {
            runOnUiThread(() -> tvStatus.setText(stateInfo));
        }
    }

    @Override
    public void onSensorDataChanged(SensorData sensorData) {
        runOnUiThread(() -> {
            if(sensorData.wearDetect==1){
                int_yaw+=1;
                int_roll+=1;
                int_pitch+=1;
//            if(sensorData.accelDataLen!=0&&sensorData.gyroDataLen!=0)
//                System.out.println(sensorData.accelData[0].toString()+sensorData.gyroData[0].toString());

                // double knock: start/stop
                if (sensorData.knockDetect > 0) {
                    btnStart.performClick();
                }

                // nod: take notes/ finish input
                else if(int_yaw>15 && (sensorData.gyroData[0].yaw>150000 || sensorData.gyroData[0].yaw<-150000)){
                    int_yaw=0;
                    btnRecord.performClick();
                }

                // shake: next
                else if(int_roll>15 && (sensorData.gyroData[0].roll>300000 || sensorData.gyroData[0].roll<-300000)){
                    int_roll=0;
                    btnNextStep.performClick();
                }

                // tilt: last
                else if(int_pitch>15 && (sensorData.gyroData[0].pitch>150000 || sensorData.gyroData[0].pitch<-150000)){
                    int_pitch=0;
                    btnLastStep.performClick();
                }
            }
        });
    }

    @Override
    public void onSendCmdSuccess(Object result) {
        runOnUiThread(() -> {
//            String info = DateUtils.getCurrentDate() + "\n" + result.toString();
            String info = result.toString();
            tvSendCmdResult.setText(info);
        });
    }

    @Override
    public void onError(String errorMsg) {
        runOnUiThread(
                () -> Toast.makeText(SampleBtActivity.this, errorMsg, Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPresenter().deInit();
    }
}
