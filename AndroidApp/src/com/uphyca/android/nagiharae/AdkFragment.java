package com.uphyca.android.nagiharae;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class AdkFragment extends Fragment {

    private static final String ACTION_USB_PERMISSION = "com.uphyca.android.nagiharae.action.USB_PERMISSION";
    private static final String TAG = "AdkFragment";

    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;

    private UsbManager mUsbManager;
    private UsbAccessory mAccessory;

    ParcelFileDescriptor mFileDescriptor;

    FileInputStream mInputStream;
    FileOutputStream mOutputStream;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    // Intent からアクセサリを取得
                    UsbAccessory accessory = UsbManager.getAccessory(intent);

                    // パーミッションがあるかチェック
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        // 接続を開く
                        openAccessory(accessory);
                    } else {
                        Log.d(TAG, "permission denied for accessory " + accessory);
                    }
                    mPermissionRequestPending = false;
                }

            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                // Intent からアクセサリを取得
                UsbAccessory accessory = UsbManager.getAccessory(intent);
                if (accessory != null && accessory.equals(mAccessory)) {
                    // 接続を閉じる
                    closeAccessory();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Context context = getActivity();

        // UsbManager のインスタンスを取得
        mUsbManager = UsbManager.getInstance(context);

        // オレオレパーミッション用 Broadcast Intent
        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);

        // オレオレパーミッション Intent とアクセサリが取り外されたときの Intent を登録
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        context.registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mInputStream != null && mOutputStream != null) {
            return;
        }

        // USB Accessory の一覧を取得
        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            // Accessory にアクセスする権限があるかチェック
            if (mUsbManager.hasPermission(accessory)) {
                // 接続を開く
                openAccessory(accessory);
            } else {
                synchronized (mUsbReceiver) {
                    if (!mPermissionRequestPending) {
                        // パーミッションを依頼
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            Log.d(TAG, "mAccessory is null");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        closeAccessory();
        getActivity().unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }
    
    public interface OnAccessoryStateChangedListener {
        public void onOpend();
        public void onClosed();
    }
    
    public void setOnAccessoryStateChangedListener(OnAccessoryStateChangedListener l) {
        mListener = l;
    }
    
    private OnAccessoryStateChangedListener mListener;

    private void openAccessory(UsbAccessory accessory) {
        // アクセサリにアクセスするためのファイルディスクリプタを取得
        mFileDescriptor = mUsbManager.openAccessory(accessory);

        if (mFileDescriptor != null) {

            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();

            // 入出力用のストリームを確保
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);

            // この中でアクセサリとやりとりする
            mThead.start();
            Log.d(TAG, "accessory opened");

            if(mListener != null) {
                mListener.onOpend();
            }
        } else {
            Log.d(TAG, "accessory open fail");
        }
    }

    private void closeAccessory() {
        if(mListener != null) {
            mListener.onClosed();
        }

        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException e) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }


    private final Thread mThead = new Thread() {
        // ここでアクセサリと通信する
        @Override
        public void run() {
            int ret = 0;
            byte[] buffer = new byte[16384];
            int i;

            // アクセサリ -> アプリ
            while (ret >= 0) {
                try {
                    ret = mInputStream.read(buffer);
                } catch (IOException e) {
                    break;
                }

                i = 0;
                while (i < ret) {
                    int len = ret - i;

                    switch (buffer[i]) {
                        case 0x1:
                            // 2byte のオレオレプロトコル
                            // 0x1 0x0 や 0x1 0x1 など
                            if (len >= 2) {
                                Message m = Message.obtain(mHandler, MESSAGE_LED);
                                m.obj = new LedMsg(buffer[i + 1]);
                                mHandler.sendMessage(m);
                            }
                            i += 2;
                            break;

                        default:
                            Log.d(TAG, "unknown msg: " + buffer[i]);
                            i = len;
                            break;
                    }
                }

            }
        }
    };
    
    public interface OnLedStateChangedListener {
        public void ledStateChanged(boolean isOn);
    }
    
    public void setOnLedStateChangedListener(OnLedStateChangedListener l) {
        mLedStateListener = l;
    }
    
    private OnLedStateChangedListener mLedStateListener;

    private static final int MESSAGE_LED = 1;

    protected class LedMsg {
        private byte on;

        public LedMsg(byte on) {
            this.on = on;
        }

        public boolean isOn() {
            return (on == 0x1);
        }
    }

    // UI スレッドで画面上の表示を変更
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_LED:
                    LedMsg o = (LedMsg) msg.obj;
                    if(mLedStateListener != null) {
                        mLedStateListener.ledStateChanged(o.isOn());
                    }
                    break;
            }
        }
    };

    // アプリ -> アクセサリ
    public void sendCommand(byte command, byte value) {
        byte[] buffer = new byte[2];

        if (value != 0x1 && value != 0x0)
            value = 0x0;

        // 2byte のオレオレプロトコル
        // 0x1 0x0 や 0x1 0x1
        buffer[0] = command;
        buffer[1] = value;
        if (mOutputStream != null) {
            try {
                mOutputStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "write failed", e);
            }
        } else {
            Log.d(TAG, "output stream is null");
        }
    }
}
