package com.cloudpos.fingerprintdemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudpos.DeviceException;
import com.cloudpos.OperationResult;
import com.cloudpos.POSTerminal;
import com.cloudpos.TimeConstants;

import com.cloudpos.fingerprint.Fingerprint;
import com.cloudpos.fingerprint.FingerprintDevice;
import com.cloudpos.fingerprint.FingerprintOperationResult;
import com.cloudpos.util.ByteConvertStringUtil;

public class MainActivity extends AppCompatActivity {

    private Button btn, btn1, btn2, btn3;
    private TextView show;
    private Context context = this;
    private Handler handler;
    private FingerprintDevice fingerprintDevice;
    private static final int MSGID_SHOW_MESSAGE = 0;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static final String FINGERINDEX1 = "Finger1";
    private static final String FINGERINDEX2 = "Finger2";
    private static final String FINGERINDEX3 = "Finger3";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.button);
        btn1 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);
        btn2 = (Button) findViewById(R.id.button4);
        show = (TextView) findViewById(R.id.textView);
        show.setMovementMethod(ScrollingMovementMethod.getInstance());
        fingerprintDevice =
                (FingerprintDevice) POSTerminal.getInstance(context).getDevice("cloudpos.device.fingerprint");
        preferences = context.getSharedPreferences("userFinger", Context.MODE_PRIVATE);
        editor=preferences.edit();
        editor.putString(FINGERINDEX1,"0");
        editor.putString(FINGERINDEX2,"0");
        editor.putString(FINGERINDEX3,"0");
        editor.commit();
        handler = new Handler(new Handler.Callback() {
            //callback method
            @Override
            public boolean handleMessage(Message msg) {

                switch (msg.what) {
                    case MSGID_SHOW_MESSAGE:
                        show.append(msg.obj.toString() + "\n");//Through the back pass over the information displayed on the TextView
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    fingerprintDevice.open();
                    new Thread() {
                        @Override
                        public void run() {
                            fingerput();
                            closeDevice();

                        }
                    }.start();
                } catch (DeviceException e) {
                    e.printStackTrace();
                    handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.FAILEDINFO)+" Press the fingerprint again!").sendToTarget();
                }
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    fingerprintDevice.open();
                    new Thread() {
                        @Override
                        public void run() {
                            preferences = context.getSharedPreferences("userFinger", Context.MODE_PRIVATE);
                            String finger1 = preferences.getString(FINGERINDEX1, null);
                            String finger2 = preferences.getString(FINGERINDEX2, null);
                            String finger3 = preferences.getString(FINGERINDEX3, null);

                            byte[] bytes = hexToBytes(finger1);
                            byte[] bytes1 = hexToBytes(finger2);
                            byte[] bytes2 = hexToBytes(finger3);

                            Log.e("BUFFER", "" + bytes.length);
                            Log.e("BUFFER", "" + bytes1.length);
                            Log.e("BUFFER", "" + bytes2.length);
                            Fingerprint fingerprint1 = new Fingerprint();
                            fingerprint1.setFeature(bytes);
                            Fingerprint fingerprint2 = new Fingerprint();
                            fingerprint2.setFeature(bytes1);
                            Fingerprint fingerprint3 = new Fingerprint();
                            fingerprint3.setFeature(bytes2);
                            match(fingerprint1, fingerprint2, fingerprint3);
                            closeDevice();
                        }
                    }.start();
                } catch (DeviceException e) {
                    e.printStackTrace();
                    handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.DEVICEFAILED)).sendToTarget();
                }
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences = context.getSharedPreferences("userFinger", Context.MODE_PRIVATE);
                editor = preferences.edit();
                editor.putString(FINGERINDEX1, "");
                editor.putString(FINGERINDEX2, "");
                editor.putString(FINGERINDEX3, "");
                editor.commit();
                Toast.makeText(context,context.getText(R.string.Cleared), Toast.LENGTH_SHORT).show();
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show.setText("");
            }
        });
    }

    private Fingerprint getFingerprint() {
        Fingerprint fingerprint = null;
        try {
            FingerprintOperationResult operationResult = fingerprintDevice.waitForFingerprint(TimeConstants.FOREVER);
            if (operationResult.getResultCode() == OperationResult.SUCCESS) {
                fingerprint = operationResult.getFingerprint(0, 0);
                Log.e("TAGSSS", "SUCCESS!" + fingerprint.getFeature().length);
                handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.SUCCESSINFO)).sendToTarget();
            } else {
                handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.FAILEDINFO)+" Press the fingerprint again!").sendToTarget();
            }
        } catch (DeviceException e) {
            e.printStackTrace();
            handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.DEVICEFAILED)).sendToTarget();
        }
        return fingerprint;
    }

    private void match(Fingerprint fingerprint1, Fingerprint fingerprint2, Fingerprint fingerprint3) {
        try {
            handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.MESSAGE)).sendToTarget();
            Fingerprint fingerprint = getFingerprint();
            Log.e("TAGSSS", "SUCCESS1!");
            if (fingerprint != null && fingerprint1 != null && fingerprint2 != null && fingerprint3 != null) {
                int match1 = fingerprintDevice.match(fingerprint, fingerprint1);
                int match2 = fingerprintDevice.match(fingerprint, fingerprint2);
                int match3 = fingerprintDevice.match(fingerprint, fingerprint3);
                if (match1 > 50) {
                    handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.MatchSuccess)+ match1).sendToTarget();
                } else if (match2 > 50) {
                    handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.MatchSuccess)+ match2).sendToTarget();
                } else if (match3 > 50) {
                    handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.MatchSuccess) + match3).sendToTarget();
                } else {
                    handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.MatchFailed)).sendToTarget();
                }
            } else {
                handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.FAILEDINFO)).sendToTarget();
            }
            closeDevice();
        } catch (DeviceException e) {
            e.printStackTrace();
            handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.DEVICEFAILED)).sendToTarget();
        }
    }

    private void fingerput() {

        preferences = context.getSharedPreferences("userFinger", Context.MODE_PRIVATE);
        editor = preferences.edit();
        handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.MESSAGE1)).sendToTarget();
        Fingerprint fingerprint1 = getFingerprint();
        while(fingerprint1==null){
            fingerprint1 = getFingerprint();
        }
        if(fingerprint1!=null) {
            byte[] buffer1 = fingerprint1.getFeature();
            Log.e("TYAFA", "" + buffer1.length);
            editor.putString(FINGERINDEX1, ByteConvertStringUtil.bytesToHexString(buffer1));
            editor.commit();
            Log.e("TAGSSS", "SUCCESS1!");
        }
        handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.MESSAGE2)).sendToTarget();
        Fingerprint fingerprint2 = getFingerprint();
        while(fingerprint2==null){
            fingerprint2 = getFingerprint();
        }
        if(fingerprint2!=null) {
            byte[] buffer2 = fingerprint2.getFeature();
            Log.e("TYAFA", "" + buffer2.length);

            editor.putString(FINGERINDEX2, ByteConvertStringUtil.bytesToHexString(buffer2));
            editor.commit();
        }
        handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.MESSAGE3)).sendToTarget();
        Fingerprint fingerprint3 = getFingerprint();
        while(fingerprint3==null){
            fingerprint3 = getFingerprint();
        }
        if(fingerprint3!=null) {
            byte[] buffer3 = fingerprint3.getFeature();
            Log.e("TYAFA", "" + buffer3.length);

            editor.putString(FINGERINDEX3, ByteConvertStringUtil.bytesToHexString(buffer3));
            editor.commit();
            Log.e("TAGSSS", "SUCCESS2!");
        }
        handler.obtainMessage(MSGID_SHOW_MESSAGE, context.getString(R.string.entry)).sendToTarget();
    }

    private void closeDevice() {
        try {
            fingerprintDevice.close();
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }

    private static byte[] hexToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] bytes = new byte[length];
        String hexDigits = "0123456789abcdef";
        for (int i = 0; i < length; i++) {
            int pos = i * 2; // 两个字符对应一个byte
            int h = hexDigits.indexOf(hexChars[pos]) << 4; // 注1
            int l = hexDigits.indexOf(hexChars[pos + 1]); // 注2
            if (h == -1 || l == -1) { // 非16进制字符
                return null;
            }
            bytes[i] = (byte) (h | l);
        }
        return bytes;
    }
}
