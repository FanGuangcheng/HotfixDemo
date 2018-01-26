package com.example.fanguangcheng.hotfixdemo;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tencent.tinker.lib.tinker.TinkerInstaller;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    Button btn_click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_click = findViewById(R.id.btn_click);
        btn_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/patch_signed.apk");
                Log.d("hotfix", "patch file is exist : " + file.exists());

                Toast.makeText(MainActivity.this, "1 + 1 = ? " + 55555, Toast.LENGTH_LONG).show();

                if (!file.exists()) {
                    Log.d("hotfix", "file is not exist just return!");
                    return;
                }

                //进行补丁的操作
                TinkerInstaller.onReceiveUpgradePatch(MainActivity.this,
                        Environment.getExternalStorageDirectory().getAbsolutePath()+"/patch_signed.apk");
            }
        });

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/patch_signed.apk");
        Log.d("hotfix", "patch file is exist : " + file.exists());

    }
}
