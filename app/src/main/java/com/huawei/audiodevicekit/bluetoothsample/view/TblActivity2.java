package com.huawei.audiodevicekit.bluetoothsample.view;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.huawei.audiodevicekit.R;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TblActivity2 extends Activity implements View.OnClickListener{
    private int CODE_FOR_WRITE_PERMISSION;
    private EditText mResultText;
    private Button btn;
    private int table_length;
    private int entry_length;
    private int x;
    private int y;
    private ListView tableListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);
        //设置表格标题的背景颜色
        ViewGroup tableTitle = (ViewGroup) findViewById(R.id.table_title2);
        tableTitle.setBackgroundColor(Color.rgb(177, 173, 172));
        btn = (Button) findViewById(R.id.button2);
        btn.setGravity(Gravity.CENTER);
        mResultText = ((EditText) findViewById(R.id.result));
        table_length=18;
        entry_length=4;
        x=0;
        y=0;
        List<Entry2> list = new ArrayList<Entry2>();
        for(int i=0;i<table_length;i++){
            list.add(new Entry2());
        }
        tableListView = (ListView) findViewById(R.id.list);
        TblAdapter2 adapter = new TblAdapter2(this, list);
        tableListView.setAdapter(adapter);
        btn.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        btnVoice();
    }

    //TODO 开始说话：
    private void btnVoice() {
        RecognizerDialog dialog = new RecognizerDialog(this,null);
        // 设置参数
        dialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        dialog.setParameter(SpeechConstant.ACCENT, "mandarin");
        dialog.setParameter(SpeechConstant.VAD_BOS, "10000");
        dialog.setParameter(SpeechConstant.VAD_EOS, "500");

        dialog.setListener(new RecognizerDialogListener() {
            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                printResult(recognizerResult);
            }
            @Override
            public void onError(SpeechError speechError) {
            }
        });
        dialog.show();
        Toast.makeText(this, "请开始说话", Toast.LENGTH_SHORT).show();
        System.out.println("编辑框内容："+mResultText.getText().toString());
    }

    //回调结果：
    private void printResult(RecognizerResult results) {
        String text = parseIatResult(results.getResultString());
        // 自动填写地址
        mResultText.append(text);
        String res = mResultText.getText().toString();// 本次识别出的结果
        mResultText.setText("");
        String[] res_list = res.split(" |,|，|。");//根据停顿将各格内容隔开
        for(String content:res_list){
            WriteTable(content);
            if(x>=table_length){
                break;
            }
        }
        if(x>=table_length){//表格填满后保存文件
            String str=table2Text();
            Toast msg = Toast.makeText(getBaseContext(),str,
                    Toast.LENGTH_LONG);
            msg.show();
            msg.show();
            byte[] bfile = str.getBytes();
            String fileName = "text2.txt";
            saveFile(bfile,fileName);
            x=y=0;
        }
    }

    public EditText getGrid(int i,int j){
        if(i<0 || j<0) return null;
        return (EditText)((ViewGroup)tableListView.getChildAt(i)).getChildAt(j*2);
    }

    public void WriteTable(String content){
        EditText grid=getGrid(x,y);
        if(grid==null) {
            System.out.println("invalid grid:x="+x+",y="+y);
            return;
        }
        grid.setText(content);
        if(y<3){
            y++;
        }else{
            x++;
            y=0;
        }
    }

    public String table2Text(){
        String res="";
        for(int i=0;i<table_length;i++){
            for(int j=0;j<entry_length;j++){
                res += getGrid(i,j).getText().toString();
                if(j+1<entry_length){
                    if(j==1){
                        res+="\n";
                    }else{
                        res+="\t";
                    }
                }
            }
            if(i+1<table_length) res+="\n";
            System.out.println(res);
        }
        return res;
    }
    // 参数一、文件的byte流
    // 参数二、文件要保存的路径
    // 参数三、文件保存的名字
    public void saveFile(byte[] bfile, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        String rootPath = "";
        String[] permissions=new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        PackageManager pm = getPackageManager();
        int permission = pm.checkPermission("android.permission.WRITE_EXTERNAL_STORAGE","com.example.hellotable");
        // Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, CODE_FOR_WRITE_PERMISSION);
        if(permission == PackageManager.PERMISSION_GRANTED){
            try {
                File dir = this.getExternalFilesDir(null);
                System.out.println(dir.getAbsolutePath());
                //通过创建对应路径的下是否有相应的文件夹。
                if (!dir.exists()) {
                    boolean mkdir_success = dir.mkdirs();
                    if(mkdir_success) System.out.println("mkdirs success");
                    else System.out.println("mkdirs failure");
                }
                //如果文件存在则删除文件
                file = new File(dir.getAbsolutePath(),fileName);
                if(file.exists()){
                    file.delete();
                    System.out.println("delete");
                }
                fos = new FileOutputStream(file);
                bos = new BufferedOutputStream(fos);
                //把需要保存的文件保存到SD卡中
                bos.write(bfile);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("write exception");
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                        System.out.println("bos.close");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("bos.close exception");
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                        System.out.println("fos.close");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        System.out.println("fos.close exception");
                    }
                }
            }
        }else{
            System.out.println("Debug log:permission denied");
        }
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
}
