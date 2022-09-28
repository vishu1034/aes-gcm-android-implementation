package android.encryption.aes.gcm.implementation.open;

import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class AesGcmCipher {

    static final int NONCE_SIZE = 12;
    private static final int AES_KEY_SIZE = 256;
    private static final int AUTHENTICATION_TAG_SIZE = 128;
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String ALIAS_KEY = "my_key";
    private static final String CIPHER_AES_GCM = "AES/GCM/NoPadding";

    private KeyStore mKeyStore;
    private String encDecKey;
    private Key finalKey;

    AesGcmCipher(String key) {
        encDecKey=key;
        //setupKeystore();
        insertKeyIntoKeystore(createAesKey());
    }

    private void setupKeystore() {
        try {
            mKeyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            mKeyStore.load(null);
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException |
                IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertKeyIntoKeystore(Key key) {
        try {
            /*
            if (!mKeyStore.containsAlias(ALIAS_KEY)) {
                mKeyStore.setKeyEntry(ALIAS_KEY, key, null, null);
            }
            */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSha256Hash(String password) {
        try {
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            }
            digest.reset();
            return bin2hex(digest.digest(password.getBytes()));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String bin2hex(byte[] data) {
        StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(String.format("%02x", b & 0xFF));
        return hex.toString();
    }

    private Key createAesKey() {
        try {
            /*
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(ALIAS_KEY,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT);
            builder.setKeySize(AES_KEY_SIZE);
            builder.setBlockModes(KeyProperties.BLOCK_MODE_GCM);
            builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE);
            builder.setUnlockedDeviceRequired(true);

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES);
            keyGenerator.init(builder.build());
            return keyGenerator.generateKey();
            */
            String encDecKeyHash = getSha256Hash(encDecKey);
            String finalEncDec = encDecKeyHash.substring(0,32);
            finalKey = new SecretKeySpec(finalEncDec.getBytes(),0,finalEncDec.getBytes().length, "AES");
            return finalKey;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    byte[] doEncrypt(byte[] plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, finalKey);
            byte[] nonce = cipher.getIV();
            byte[] ciphertext = new byte[NONCE_SIZE + cipher.getOutputSize(plaintext.length)];

            System.arraycopy(nonce, 0, ciphertext, 0, NONCE_SIZE);

            cipher.doFinal(plaintext, 0, plaintext.length, ciphertext, NONCE_SIZE);
            return ciphertext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    byte[] doDecrypt(byte[] ciphertext) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_AES_GCM);
            GCMParameterSpec gcmParameterSpec =
                    new GCMParameterSpec(AUTHENTICATION_TAG_SIZE, ciphertext, 0, NONCE_SIZE);
            cipher.init(Cipher.DECRYPT_MODE, finalKey, gcmParameterSpec);

            byte[] plaintext = new byte[cipher.getOutputSize(ciphertext.length - NONCE_SIZE)];
            cipher.doFinal(ciphertext, NONCE_SIZE, ciphertext.length - NONCE_SIZE, plaintext, 0);
            return plaintext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
