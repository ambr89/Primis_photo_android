package com.brav.primisphoto;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.brav.primisphoto.services.Message;
import com.brav.primisphoto.services.PrimisWS;
import com.brav.primisphoto.util.Alerts;
import com.brav.primisphoto.util.Constants;
import com.brav.primisphoto.util.PrimisType;
import com.brav.primisphoto.util.Utility;

import java.io.File;
import java.util.ArrayList;


public class Login extends MyBaseActivity implements DialogInterface.OnClickListener{

    private EditText username;
    private EditText password;
    private EditText n_scheda;
    private ProgressDialog dialog;
    private CheckBox remember;
    private PrimisWS ws;
    private int MaxLengthResponseNote = 300;


    private  ArrayList<Uri> imageUris;

    private CompoundButton.OnCheckedChangeListener ckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if(b){
                Utility.setStringInternalPreference(getApplicationContext(), Constants.USERNAME, username.getText().toString());
                Utility.setStringInternalPreference(getApplicationContext(), Constants.PASSWORD, password.getText().toString());
            }else {
                Utility.setStringInternalPreference(getApplicationContext(), Constants.USERNAME, "");
                Utility.setStringInternalPreference(getApplicationContext(), Constants.PASSWORD, "");
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(getString(R.string.title_login));

        ws = new PrimisWS(this);
        //ws.setUrl(Constants.Url);

        username = findViewById(R.id.edt_username);
        password = findViewById(R.id.edt_password);
        n_scheda = findViewById(R.id.edt_n_scheda);
        remember = findViewById(R.id.remember);
        remember.setOnCheckedChangeListener(ckListener);

        String version = "";
        try{
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        }catch (Exception e){

        }

        TextView txtVersione = findViewById(R.id.txtVersione);
        txtVersione.setText("v: "+ version);

        String strDef_username = Utility.getStringInternalPreference(this,Constants.USERNAME);
        if(!TextUtils.isEmpty(strDef_username))
            username.setText(strDef_username);

        String strDef_password = Utility.getStringInternalPreference(this,Constants.PASSWORD);
        if(!TextUtils.isEmpty(strDef_password))
            password.setText(strDef_password);


        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }


    public void onClickNext(View target)
    {
        String _username = username.getText().toString();
        String _password = password.getText().toString();
        String _n_scheda = n_scheda.getText().toString();
        if(TextUtils.isEmpty(_username) ||
                TextUtils.isEmpty(_password) ||
                TextUtils.isEmpty(_n_scheda)){

            Alerts.promptOK("ffff",R.string.attenzione_campi_vuoti, this, null);

            return;
        }

        if(remember.isChecked()) {
            Utility.setStringInternalPreference(this, Constants.USERNAME, _username);
            Utility.setStringInternalPreference(this, Constants.PASSWORD, _password);
        }

        if(_n_scheda.toUpperCase().startsWith(Constants.PREFIX_PRIMIS_RED)) {
            ws.setUrl(Constants.Url_primis_red);
            Constants.primisType = PrimisType.Red;
        }else{
            ws.setUrl(Constants.Url_primis_green);
            Constants.primisType = PrimisType.Green;
        }

        dialog = ProgressDialog.show(this, "", getString(R.string.progress), true);
        new wsTask().execute();
    }




    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            if(this.imageUris != null)
                this.imageUris.clear();
            this.imageUris = new ArrayList<>();
            this.imageUris.add(imageUri);
            // Update UI to reflect image being shared
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
            this.imageUris = imageUris;
        }
    }

    private Message GetSchedaByNumero(){
        String _username = username.getText().toString();
        String _password = password.getText().toString();
        String _n_scheda = n_scheda.getText().toString();
        Message retVal = ws.GetSchedaByNumero(_n_scheda,_username,_password);
        return retVal;

    }

    private void responseGetSchedaByNumero(Object... value){

        //if(value == null || (value.length == 1 && value[0] == null)){
        //    Alerts.promptOK("",R.string.errore, this, null);
        //}else
        if(value != null && value.length == 1 && value[0] != null){
            Message response = (Message) value[0];
            MaxLengthResponseNote = response.MaxLengthResponseNote;
            if(response.ok)
                Alerts.promptOK("",response.note, this, this);
            else
                Alerts.promptOK("",response.note, this, null);
        }


    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

        //rimuovo le immagini interne che ho spedito!!!
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir(Constants.INTERNAL_IMAGE_FOlDER, Context.MODE_PRIVATE);
        Utility.deleteRecursive(directory);

        String _username = username.getText().toString();
        String _password = password.getText().toString();
        String _n_scheda = n_scheda.getText().toString();
        Intent intent = new Intent(this, Photo.class);
        intent.putParcelableArrayListExtra(Constants.URIs_photo, imageUris);
        intent.putExtra(Constants.USERNAME, _username);
        intent.putExtra(Constants.PASSWORD, _password);
        intent.putExtra(Constants.N_SCHEDA, _n_scheda);
        intent.putExtra(Constants.N_MAXLENNOTE, MaxLengthResponseNote);

        startActivity(intent);
    }

    private class wsTask extends AsyncTask<Object, Object, Object> {

        public wsTask( ) {

        }

        @Override
        protected Object doInBackground(Object... params) {
            this.publishProgress(GetSchedaByNumero());
            return null;
        }

        @Override
        public void onProgressUpdate(Object... value) {
            if (dialog != null)
                dialog.dismiss();

            responseGetSchedaByNumero(value);

        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
    }
}
