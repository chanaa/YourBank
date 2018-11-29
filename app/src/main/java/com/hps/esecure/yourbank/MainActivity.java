package com.hps.esecure.yourbank;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hps.esecure.yourbank.common.DummyOobTrans;
import com.hps.esecure.yourbank.common.JSONSenderUtilsSim;
import com.hps.esecure.yourbank.settings.SettingsActivity;
import com.hps.esecure.yourbank.settings.SettingsPreferenceHelper;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, FingerprintHandler.FingerprintHandlerCallback {


    private Spinner mSpinner;
    private ProgressBar mProgressBar;
    private TextView mTransactionText;
    private ImageView mImageView;
    private ArrayAdapter<DummyOobTrans> mAdapter;


    // Declare a string variable for the key we’re going to use in our fingerprint authentication
    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    private Button mButtonAuthenticate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpinner = (Spinner) findViewById(R.id.pans);
        mAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new ArrayList<DummyOobTrans>());
        mSpinner.setAdapter(mAdapter);

        mSpinner.setOnItemSelectedListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTransactionText = (TextView) findViewById(R.id.transaction);

        mImageView = (ImageView) findViewById(R.id.fingerprint_image);

        mButtonAuthenticate = (Button) findViewById(R.id.button_authenticate);

        mButtonAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAuthenticationSucceeded();
            }
        });

        // If you’ve set your app’s minSdkVersion to anything lower than 23, then you’ll need to verify that the device is running Marshmallow
        // or higher before executing any fingerprint-related code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Get an instance of KeyguardManager and FingerprintManager//
            keyguardManager =
                    (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager =
                    (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            //Check whether the device has a fingerprint sensor//
            if (!fingerprintManager.isHardwareDetected()) {
                // If a fingerprint sensor isn’t available, then inform the user that they’ll be unable to use your app’s fingerprint functionality//

                Toast.makeText(this, "Your device doesn't support fingerprint authentication", Toast.LENGTH_LONG).show();
                return;
            }

            //Check whether the user has granted your app the USE_FINGERPRINT permission//
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                // If your app doesn't have this permission, then display the following text//

                Toast.makeText(this, "Please enable the fingerprint permission", Toast.LENGTH_LONG).show();
                return;
            }

            //Check that the user has registered at least one fingerprint//
            if (!fingerprintManager.hasEnrolledFingerprints()) {
                // If the user hasn’t configured any fingerprints, then display the following message//

                Toast.makeText(this, "No fingerprint configured. Please register at least one fingerprint in your device's SettingsActivity", Toast.LENGTH_LONG).show();
                return;
            }

            //Check that the lockscreen is secured//
            if (!keyguardManager.isKeyguardSecure()) {
                // If the user hasn’t secured their lockscreen with a PIN password or pattern, then display the following text//

                Toast.makeText(this, "Please enable lockscreen security in your device's SettingsActivity", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                generateKey();
            } catch (FingerprintException e) {
                e.printStackTrace();

                Toast.makeText(this, "Fingerprint initialisation exception", Toast.LENGTH_LONG).show();
            }

            if (initCipher()) {
                //If the cipher is initialized successfully, then create a CryptoObject instance//
                cryptoObject = new FingerprintManager.CryptoObject(cipher);

            } else {
                Toast.makeText(this, "Fingerprint initialisation exception", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        OobDisplayAsyncTask oobDisplayAsyncTask = new OobDisplayAsyncTask();
        oobDisplayAsyncTask.execute(SettingsPreferenceHelper.getOobVerifyUrl(this), "");
    }

    //Create the generateKey method that we’ll use to gain access to the Android keystore and generate the encryption key//
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void generateKey() throws FingerprintException {
        try {
            // Obtain a reference to the Keystore using the standard Android keystore container identifier (“AndroidKeystore”)//
            keyStore = KeyStore.getInstance("AndroidKeyStore");

            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            keyStore.load(null);

            //Initialize the KeyGenerator//
            keyGenerator.init(new

                    //Specify the operation(s) this key can be used for//
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)

                    //Configure this key so that the user has to confirm their identity with a fingerprint each time they want to use it//
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            //Generate the key//
            keyGenerator.generateKey();

        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException
                | CertificateException
                | IOException exc) {
            throw new FingerprintException(exc);
        }
    }

    //Create a new method that we’ll use to initialize our cipher//
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean initCipher() {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the cipher has been initialized successfully//
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {

            //Return false if cipher initialization failed//
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }


    @Override
    public void onAuthenticationSucceeded() {
        DummyOobTrans selectedItem = (DummyOobTrans) mSpinner.getSelectedItem();
        selectedItem.setVerified(true);
        Gson gson = new Gson();

        OobDisplayAsyncTask oobDisplayAsyncTask = new OobDisplayAsyncTask();
        oobDisplayAsyncTask.execute(SettingsPreferenceHelper.getOobVerifyUrl(this), gson.toJson(selectedItem));
    }

    @Override
    public void onAuthenticationError() {
        finish();
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        DummyOobTrans selectedItem = (DummyOobTrans) mSpinner.getSelectedItem();
        selectTransaction(selectedItem);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        mTransactionText.setText("Please choose a card number.");
    }

    private void selectTransaction(DummyOobTrans selectedItem){
        mTransactionText.setText("An authentication is needed to process your purchase of "+selectedItem.getAmount());
    }

    private class OobDisplayAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            JSONSenderUtilsSim jsonSenderUtilsSim = new JSONSenderUtilsSim();

            return jsonSenderUtilsSim.sendPostRequest(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressBar.setVisibility(View.GONE);
            mTransactionText.setVisibility(View.VISIBLE);
            mAdapter.clear();

            List<DummyOobTrans> transactionList = new Gson().fromJson(result, new TypeToken<List<DummyOobTrans>>(){}.getType());

            if (transactionList!=null && !transactionList.isEmpty()) {

                for (DummyOobTrans dummyOobTrans : transactionList) {
                    if (!dummyOobTrans.isVerified()) {
                        mAdapter.add(dummyOobTrans);
                    }
                }
            }

            if (!mAdapter.isEmpty()){

                mSpinner.setVisibility(View.VISIBLE);
                mSpinner.setSelection(0);

                DummyOobTrans selectedItem = (DummyOobTrans) mSpinner.getSelectedItem();
                selectTransaction(selectedItem);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!fingerprintManager.isHardwareDetected()) {
                        mImageView.setVisibility(View.GONE);

                        mButtonAuthenticate.setVisibility(View.VISIBLE);

                    } else {
                        mImageView.setVisibility(View.VISIBLE);
                        mButtonAuthenticate.setVisibility(View.GONE);


                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                            // If your app doesn't have this permission, then display the following text//

                            Toast.makeText(MainActivity.this, "Please enable the fingerprint permission", Toast.LENGTH_LONG).show();
                            return;
                        }

                        FingerprintHandler helper = new FingerprintHandler(MainActivity.this);
                        helper.startAuth(fingerprintManager, cryptoObject, MainActivity.this);
                    }
                } else {
                    mImageView.setVisibility(View.GONE);

                    mButtonAuthenticate.setVisibility(View.VISIBLE);
                }

            } else {
                mTransactionText.setText("There is no pending transaction.");
                mImageView.setVisibility(View.GONE);

                mSpinner.setVisibility(View.GONE);

                mButtonAuthenticate.setVisibility(View.GONE);
            }

            mAdapter.notifyDataSetChanged();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.welcome_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
