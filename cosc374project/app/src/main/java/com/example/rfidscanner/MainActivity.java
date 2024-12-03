package com.example.rfidscanner;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity {
    public static final String Error_Detected = " No NFC detected";
    public static final String Write_Success = "Text written";
    public static final String Write_Error = "Error writing";

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter intentFilter[];
    boolean writeMode;
    Tag myTag;
    Context context;

    TextView unencrypted_contents;
    TextView nfc_contents;

    TextView decrypt_contents;
    Button decrypt_button;

    Button encrypt_button;
    String SECRET_KEY
            = "cosc374rocks!!!!11!";
    String passValue;

    private static final String SALT = "corsetvirgopresetpliers";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        unencrypted_contents = (TextView) findViewById(R.id.unencrypted_contents);
        nfc_contents = (TextView) findViewById(R.id.nfc_contents);
        context = this;
        nfc_contents.setText("Test NFC content display.");
        nfc_contents.setText("rYpY1MKPXVYIXQvOMJTVLgrG1fW99PgE6Qnw//OTx5k=");


        decrypt_button = findViewById(R.id.decrypt_button);
        encrypt_button = findViewById(R.id.encrypt_button);
        decrypt_contents = findViewById(R.id.decrypt_contents);

        decrypt_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passValue = nfc_contents.getText().toString();
                try {

                    // Default byte array
                    byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0};
                    // Create IvParameterSpec object and assign with
                    // constructor
                    IvParameterSpec ivspec
                            = new IvParameterSpec(iv);

                    // Create SecretKeyFactory Object
                    SecretKeyFactory factory
                            = SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256");

                    // Create KeySpec object and assign with
                    // constructor
                    KeySpec spec = new PBEKeySpec(
                            SECRET_KEY.toCharArray(), SALT.getBytes(),
                            65536, 256);
                    SecretKey tmp = factory.generateSecret(spec);
                    SecretKeySpec secretKey = new SecretKeySpec(
                            tmp.getEncoded(), "AES");

                    Cipher cipher = Cipher.getInstance(
                            "AES/CBC/PKCS5PADDING");
                    cipher.init(Cipher.DECRYPT_MODE, secretKey,
                            ivspec);
//                    decrypt_contents.setText("bruh");
                    // Return decrypted string
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(passValue));
                        String decryptedString = new String(decryptedBytes, StandardCharsets.UTF_8); // Convert byte array to String
                        decrypt_contents.setText(decryptedString);
                    }
                } catch (Exception e) {
                    System.out.println("Error while decrypting: "
                            + e.toString());
                    decrypt_contents.setText(e.toString());
                }
            }
        });

        encrypt_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passValue = unencrypted_contents.getText().toString();
                try {

                    // Create default byte array
                    byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0,
                            0, 0, 0, 0, 0, 0, 0, 0};
                    IvParameterSpec ivspec
                            = new IvParameterSpec(iv);

                    // Create SecretKeyFactory object
                    SecretKeyFactory factory
                            = SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256");

                    // Create KeySpec object and assign with
                    // constructor
                    KeySpec spec = new PBEKeySpec(
                            SECRET_KEY.toCharArray(), SALT.getBytes(),
                            65536, 256);
                    SecretKey tmp = factory.generateSecret(spec);
                    SecretKeySpec secretKey = new SecretKeySpec(
                            tmp.getEncoded(), "AES");

                    Cipher cipher = Cipher.getInstance(
                            "AES/CBC/PKCS5Padding");
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey,
                            ivspec);
                    // Return encrypted string
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            nfc_contents.setText(Base64.getEncoder().encodeToString(
                                    cipher.doFinal(passValue.getBytes(StandardCharsets.UTF_8))));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error while encrypting: "
                            + e.toString());
                }
            }
        });

//        ActivateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    if(myTag == null){
//                        Toast.makeText(context, Error_Detected, Toast.LENGTH_LONG).show();
//
//                    }else{
//                        write("PlainText|" + edit_message.getText().toString(), myTag);
//                        Toast.makeText(context, Write_Success, Toast.LENGTH_LONG).show();
//                    }
//                } catch (IOException e){
//                    Toast.makeText(context, Write_Error, Toast.LENGTH_LONG).show();
//                    e.printStackTrace();
//                } catch (FormatException e){
//                    Toast.makeText(context, Write_Error, Toast.LENGTH_LONG).show();
//                    e.printStackTrace();
//                }
//            }
//        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
//        if(nfcAdapter == null){
//            Toast.makeText(this,"This device does not support NFC", Toast.LENGTH_SHORT).show();
//            finish();
//        }
        readfromIntent(getIntent());
        pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter = new IntentFilter[] { tagDetected };
    }



    private void readfromIntent(Intent intent){
        String action = intent.getAction();
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if(rawMsgs != null){
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++){
                    msgs[i] = (NdefMessage)  rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }
    private void buildTagViews(NdefMessage[] msgs){
        if (msgs == null || msgs.length == 0) return;

        String text = "";
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0063;

        try {
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }catch (UnsupportedEncodingException e){
            Log.e("Unsupported Encoding", e.toString());
        }

        nfc_contents.setText("NFC Content: " + text);
    }

    private void write(String text, Tag tag) throws IOException, FormatException{
        NdefRecord[] records = {createRecord(text)};
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }

    private NdefRecord createRecord(String text) throws UnsupportedEncodingException{
        String lang = "en";
        byte[] textBytes = text.getBytes();
        byte[] langBytes = lang.getBytes("US-ASCII");
        int langLength = langBytes.length;
        int textLength = textBytes.length;
        byte[] payload = new byte[1 + langLength + textLength];

        payload[0] = (byte) langLength;

        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);


        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT,new byte[0], payload);
        return recordNFC;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null) {
            Log.e("NFC", "Received null intent in onNewIntent.");
            return;
        }

        Log.d("NFC", "New intent received: " + intent.getAction());
        setIntent(intent);
        readfromIntent(intent);

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (myTag != null) {
                Log.d("NFC", "NFC tag detected and stored in myTag.");
            } else {
                Log.e("NFC", "myTag is null despite ACTION_TAG_DISCOVERED.");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        WriteModeOn();
        Log.d("NFC", "Foreground dispatch activated in onResume.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        WriteModeOff();
        Log.d("NFC", "Foreground dispatch deactivated in onPause.");
    }


    private void WriteModeOn() {
        writeMode = true;
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilter, null);
            Log.d("NFC", "Foreground dispatch enabled.");
        } else {
            Log.e("NFC", "NfcAdapter is null; cannot enable foreground dispatch.");
        }
    }

    private void WriteModeOff() {
        writeMode = false;
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
            Log.d("NFC", "Foreground dispatch disabled.");
        } else {
            Log.e("NFC", "NfcAdapter is null; cannot disable foreground dispatch.");
        }
    }



}


