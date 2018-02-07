package com.example.fanguangcheng.hotfixdemo;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.netease.android.patch.app.TinkerServerManager;
import com.tencent.tinker.lib.tinker.TinkerInstaller;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    Button btn_click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button requestPatchButton = (Button) findViewById(R.id.requestPatch);
        requestPatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String json = "{\"e\":1,\"url\":\"http:\\/\\/mzs0.hucdn.com\\/app\\/hotfix\\/beibei_android_v3\\/v_5.8.03_32.so\",\"v\":\"32\"}";
//                TinkerServerManager.checkTinkerUpdate(true, json);
                TinkerServerManager.checkTinkerUpdate(true);
            }
        });

        Button cleanPatchButton = (Button) findViewById(R.id.cleanPatch);
        cleanPatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/patch_signed.apk");
                if (!file.exists()) {
                    Toast.makeText(MainActivity.this, "file not exist just return !~ ", Toast.LENGTH_LONG).show();
                    return;
                }

//                Tinker.with(getApplicationContext()).cleanPatch();
                //进行补丁的操作
                Toast.makeText(MainActivity.this, "install patch !~ ", Toast.LENGTH_LONG).show();
                TinkerInstaller.onReceiveUpgradePatch(MainActivity.this,
                        Environment.getExternalStorageDirectory().getAbsolutePath()+"/patch_signed.apk");
            }
        });

        Button killSelfButton = (Button) findViewById(R.id.killSelf);
        killSelfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "666666 network load patch test  use hot fix hhhhhh !~~~~~ ", Toast.LENGTH_LONG).show();
//                ShareTinkerInternals.killAllOtherProcess(getApplicationContext());
//                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }
}
