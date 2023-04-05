package com.brav.primisphoto;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.brav.primisphoto.services.IWsdl2CodeEvents;
import com.brav.primisphoto.util.Alerts;

/**
 * Created by ambra on 25/10/2017.
 */

public class MyBaseActionBarActivity extends AppCompatActivity  {

    MyBaseActionBarActivity _this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.toolbar_top);
        }

        _this = this;
    }

    @Override
    public void setTitle(CharSequence title) {
        ((TextView) findViewById(R.id.title_customTopBar)).setText(title);
    }



}
