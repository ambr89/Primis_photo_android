package com.brav.primisphoto;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.brav.primisphoto.services.IWsdl2CodeEvents;
import com.brav.primisphoto.util.Alerts;

/**
 * Created by ambra on 25/10/2017.
 */

public class MyBaseActivity extends MyBaseActionBarActivity implements IWsdl2CodeEvents {

    MyBaseActivity _this;
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _this = this;
    }


    @Override
    public void Wsdl2CodeStartedRequest() {

    }

    @Override
    public void Wsdl2CodeFinished(String methodName, Object Data) {

    }

    @Override
    public void Wsdl2CodeFinishedWithException(final Exception ex) {
        runOnUiThread(new Runnable() {
            public void run() {
                Alerts.promptOK("",ex.getMessage(), _this, null);
            }
        });
    }

    @Override
    public void Wsdl2CodeEndedRequest() {

    }
}
