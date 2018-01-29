package com.example.fanguangcheng.hotfixdemo;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.husor.android.hbpatch.app.TinkerServerManager;
import com.tencent.tinker.lib.tinker.Tinker;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.shareutil.ShareTinkerInternals;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    Button btn_click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        btn_click = findViewById(R.id.btn_click);
//        btn_click.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/patch_signed.apk");
//                Log.d("hotfix", "patch file is exist : " + file.exists());
//
//                Toast.makeText(MainActivity.this, "1 + 1 = ? " + 44444, Toast.LENGTH_LONG).show();
//
//                if (!file.exists()) {
//                    Log.d("hotfix", "file is not exist just return!");
//                    return;
//                }
//
//                //进行补丁的操作
//                TinkerInstaller.onReceiveUpgradePatch(MainActivity.this,
//                        Environment.getExternalStorageDirectory().getAbsolutePath()+"/patch_signed.apk");
//            }
//        });
//
//        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/patch_signed.apk");
//        Log.d("hotfix", "patch file is exist : " + file.exists());
        Button requestPatchButton = (Button) findViewById(R.id.requestPatch);
        requestPatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String json = "{\"e\":1,\"url\":\"http:\\/\\/mzs0.hucdn.com\\/app\\/hotfix\\/beibei_android_v3\\/v_5.8.03_32.so\",\"v\":\"32\"}";
                TinkerServerManager.checkTinkerUpdate(true, json);
            }
        });

        Button cleanPatchButton = (Button) findViewById(R.id.cleanPatch);
        cleanPatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tinker.with(getApplicationContext()).cleanPatch();
            }
        });

        Button killSelfButton = (Button) findViewById(R.id.killSelf);
        killSelfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareTinkerInternals.killAllOtherProcess(getApplicationContext());
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }
}
