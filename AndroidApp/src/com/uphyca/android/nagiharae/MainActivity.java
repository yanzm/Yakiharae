/*
 * Copyright (C) 2011 yanzm, uPhyca Inc.,
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uphyca.android.nagiharae;

import java.io.IOException;
import java.util.ArrayList;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 0;
    private boolean sendCommandFlag = false;
    private Button mButton;
    private AdkFragment mAdkFragment;
    private SocketClientFragment mSocketClientFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        FragmentManager manager = getSupportFragmentManager();

        mSocketClientFragment = new SocketClientFragment();
        manager.beginTransaction().add(mSocketClientFragment, "socket").commit();

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mSocketClientFragment.connect();
            }
        });

        Fragment f = manager.findFragmentByTag("adk");
        if (f == null) {
            mAdkFragment = new AdkFragment();
            manager.beginTransaction().add(mAdkFragment, "adk").commit();
        } else {
            mAdkFragment = (AdkFragment) f;
        }

        mAdkFragment.setOnAccessoryStateChangedListener(new AdkFragment.OnAccessoryStateChangedListener() {

            @Override
            public void onOpend() {
                Log.d(TAG, "connected");
                mButton.setEnabled(true);
            }

            @Override
            public void onClosed() {
                Log.d(TAG, "not connected");
                mButton.setEnabled(false);
            }
        });

        mAdkFragment.setOnLedStateChangedListener(new AdkFragment.OnLedStateChangedListener() {

            @Override
            public void ledStateChanged(boolean isOn) {
                if (isOn) {
                    // トーストを使って結果を表示
                    Toast.makeText(MainActivity.this, "成功！", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "失敗！", Toast.LENGTH_LONG).show();
                }
            }
        });

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "薙ぎ払え");
                    startActivityForResult(intent, REQUEST_CODE);

                } catch (ActivityNotFoundException e) {
                    // このインテントに応答できるアクティビティがインストールされていない場合
                    Toast.makeText(MainActivity.this, "ActivityNotFoundException", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sendCommandFlag) {
            sendCommandFlag = false;
            byte command = 0x1;
            byte value = 0x1;
            mAdkFragment.sendCommand(command, value);
            Toast.makeText(this, "命令成功！", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (results.size() > 0) {
                if (results.get(0).equals("やきはらえ")) {
                    sendCommandFlag = true;
                    try {
                        mSocketClientFragment.sendCommand("NAGIHARAE");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(this, results.get(0), Toast.LENGTH_SHORT).show();
            }
            else {
                Log.d(TAG, "result.size() = 0");
            }
        }
        else {
            Log.d(TAG, requestCode + ", " + requestCode);
        }
    }
}