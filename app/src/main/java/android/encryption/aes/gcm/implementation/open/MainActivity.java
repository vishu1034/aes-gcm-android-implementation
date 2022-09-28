package android.encryption.aes.gcm.implementation.open;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {

    private EditText secretKeyET;
    private EditText plainTextET;

    private Button encryptBtn;
    private Button encryptCopyBtn;
    private Button encryptPasteBtn;
    private Button encryptShareBtn;
    private Button encryptClearBtn;

    private EditText cipherTextET;

    private Button decryptBtn;
    private Button decryptCopyBtn;
    private Button decryptPasteBtn;
    private Button decryptShareBtn;
    private Button decryptClearBtn;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        secretKeyET = findViewById(R.id.secretKeyET);
        plainTextET = findViewById(R.id.plainTextET);
        encryptBtn  = findViewById(R.id.encryptBtn);
        encryptCopyBtn = findViewById(R.id.encryptCopyBtn);
        encryptPasteBtn = findViewById(R.id.encryptPasteBtn);
        encryptShareBtn = findViewById(R.id.encryptShareBtn);
        encryptClearBtn = findViewById(R.id.encryptClearBtn);
        cipherTextET = findViewById(R.id.cipherTextET);
        decryptBtn = findViewById(R.id.decryptBtn);
        decryptCopyBtn = findViewById(R.id.decryptCopyBtn);
        decryptPasteBtn = findViewById(R.id.decryptPasteBtn);
        decryptShareBtn = findViewById(R.id.decryptShareBtn);
        decryptClearBtn = findViewById(R.id.decryptClearBtn);

        //AesGcmCipher mAesGcmCipher = new AesGcmCipher("SgUkXp2s5v8y/B?E(H+MbQeThWmYq3t6");

        encryptPasteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard(MainActivity.this);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if(clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)){
                    plainTextET.setText(clipboard.getPrimaryClip().getItemAt(0).getText().toString().trim());
                    Toast.makeText(MainActivity.this, "Text Pasted", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "Nothing To Paste", Toast.LENGTH_SHORT).show();
                }

            }
        });

        encryptClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard(MainActivity.this);
                plainTextET.setText("");
                Toast.makeText(MainActivity.this, "Text Cleared", Toast.LENGTH_SHORT).show();

            }
        });

        encryptCopyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard(MainActivity.this);

                String PlainText = plainTextET.getText().toString().trim();
                if(PlainText.isEmpty()){
                    Toast.makeText(MainActivity.this, "Plain Text Empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Plain Text", PlainText);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(MainActivity.this, "Text Copied", Toast.LENGTH_SHORT).show();

            }
        });

        encryptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard(MainActivity.this);

                String SecreyKey = secretKeyET.getText().toString().trim();
                if(SecreyKey.isEmpty()){
                    Toast.makeText(MainActivity.this, "Secret Key Empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String PlainText = plainTextET.getText().toString().trim();
                if(PlainText.isEmpty()){
                    Toast.makeText(MainActivity.this, "Plain Text Empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                AesGcmCipher mAesGcmCipher = new AesGcmCipher(SecreyKey);
                byte[] PlainTextBytes = PlainText.getBytes(StandardCharsets.UTF_8);
                byte[] CipherTextBytes = mAesGcmCipher.doEncrypt(PlainTextBytes);

                String CipherText = Base64.encodeToString(CipherTextBytes, AesGcmCipher.NONCE_SIZE, CipherTextBytes.length - AesGcmCipher.NONCE_SIZE, Base64.NO_WRAP);
                String NonceText = Base64.encodeToString(CipherTextBytes, 0, AesGcmCipher.NONCE_SIZE, Base64.NO_WRAP);

                cipherTextET.setText(CipherText+"(#)"+NonceText);

                Toast.makeText(MainActivity.this, "Text Encrypted", Toast.LENGTH_SHORT).show();

            }
        });

        encryptShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideKeyboard(MainActivity.this);

                String PlainText = plainTextET.getText().toString().trim();
                if(PlainText.isEmpty()){
                    Toast.makeText(MainActivity.this, "Plain Text Empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                String shareBody = "Plain Text: "+PlainText;
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "AES256 Plain Text");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(intent, "Share Plain Text"));

            }
        });

        decryptShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideKeyboard(MainActivity.this);

                String CipherText = cipherTextET.getText().toString().trim();
                if(CipherText.isEmpty()){
                    Toast.makeText(MainActivity.this, "Cipher Text Empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                String shareBody = "Cipher Text: "+CipherText;
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "AES256 Cipher Text");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(intent, "Share Cipher Text"));

            }
        });

        decryptClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideKeyboard(MainActivity.this);
                cipherTextET.setText("");
                Toast.makeText(MainActivity.this, "Text Cleared", Toast.LENGTH_SHORT).show();

            }
        });

        decryptPasteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideKeyboard(MainActivity.this);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if(clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)){
                    cipherTextET.setText(clipboard.getPrimaryClip().getItemAt(0).getText().toString().trim());
                    Toast.makeText(MainActivity.this, "Text Pasted", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "Nothing To Paste", Toast.LENGTH_SHORT).show();
                }

            }
        });

        decryptCopyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideKeyboard(MainActivity.this);

                String CipherText = cipherTextET.getText().toString().trim();
                if(CipherText.isEmpty()){
                    Toast.makeText(MainActivity.this, "Cipher Text Empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Cipher Text", CipherText);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(MainActivity.this, "Text Copied", Toast.LENGTH_SHORT).show();

            }
        });

        decryptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard(MainActivity.this);

                String SecreyKey = secretKeyET.getText().toString().trim();
                if(SecreyKey.isEmpty()){
                    Toast.makeText(MainActivity.this, "Secret Key Empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String CipherTextComplete = cipherTextET.getText().toString().trim();
                if(CipherTextComplete.isEmpty()){
                    Toast.makeText(MainActivity.this, "Cipher Text Empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] CipherTextParts = CipherTextComplete.split("(#)");
                if(CipherTextParts.length==2){

                    try{

                        String CipherTextTemp = CipherTextParts[0];
                        String NonceTemp = CipherTextParts[1];

                        byte[] EncryptedBytes = Base64.decode(CipherTextTemp, Base64.NO_WRAP);
                        byte[] NonceBytes = Base64.decode(NonceTemp, Base64.NO_WRAP);

                        byte[] ciphertext = new byte[NonceBytes.length + EncryptedBytes.length];

                        System.arraycopy(NonceBytes, 0, ciphertext, 0, NonceBytes.length);
                        System.arraycopy(EncryptedBytes, 0, ciphertext, NonceBytes.length, EncryptedBytes.length);

                        AesGcmCipher mAesGcmCipher = new AesGcmCipher(SecreyKey);
                        byte[] plaintext = mAesGcmCipher.doDecrypt(ciphertext);

                        plainTextET.setText(new String(plaintext, StandardCharsets.UTF_8));
                        Toast.makeText(MainActivity.this, "Text Decrypted", Toast.LENGTH_SHORT).show();

                    }catch(Exception ex){
                        Toast.makeText(MainActivity.this, "Invalid Cipher Text or Key", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }else{
                    Toast.makeText(MainActivity.this, "Invalid Cipher Text", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });

    }

    public void onClickDoEncrypt(View view) {

        byte[] plaintext = "Plain Text".getBytes(StandardCharsets.UTF_8);
        //byte[] ciphertext = mAesGcmCipher.doEncrypt(plaintext);

        // ENC - Base64.encodeToString(ciphertext, AesGcmCipher.NONCE_SIZE, ciphertext.length - AesGcmCipher.NONCE_SIZE, Base64.NO_WRAP)
        // NONCE - Base64.encodeToString(ciphertext, 0, AesGcmCipher.NONCE_SIZE, Base64.NO_WRAP)

    }

    public void onClickDoDecrypt(View view) {

        byte[] encrypted = Base64.decode("Encrypted Text", Base64.NO_WRAP);
        byte[] nonce = Base64.decode("Nonce Text", Base64.NO_WRAP);

        byte[] ciphertext = new byte[nonce.length + encrypted.length];
        System.arraycopy(nonce, 0, ciphertext, 0, nonce.length);
        System.arraycopy(encrypted, 0, ciphertext, nonce.length, encrypted.length);

        //byte[] plaintext = mAesGcmCipher.doDecrypt(ciphertext);

        // DEC - new String(plaintext, StandardCharsets.UTF_8)

    }
}
