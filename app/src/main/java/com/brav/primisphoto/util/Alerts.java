package com.brav.primisphoto.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.brav.primisphoto.MyBaseActivity;
import com.brav.primisphoto.R;


/**
 * Created by ambra on 25/10/2017.
 */

public class Alerts extends MyBaseActivity {



    public static void promptOK(int title, int message, Context ctx, DialogInterface.OnClickListener ocl) {
        promptOK(ctx.getString(title), ctx.getString(message), ctx, ocl);
    }

    public static void promptOK(String title, int message, Context ctx, DialogInterface.OnClickListener ocl) {
        promptOK(title, ctx.getString(message), ctx, ocl);
    }

    public static void promptOK(String title, String message, Context ctx, DialogInterface.OnClickListener ocl) {
        try {
            LayoutInflater li = LayoutInflater.from(ctx);
            View view = li.inflate(R.layout.alerts, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            if(!TextUtils.isEmpty(title)){
                builder.setTitle(title);
            }
            builder.setTitle("");
            builder.setView(view);
            //TextView v = (TextView)view.findViewById(R.id.lblAlert);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                builder.setMessage(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));
                //v.setText(Html.fromHtml(message,Html.FROM_HTML_MODE_LEGACY));
            } else {
                builder.setMessage(Html.fromHtml(message));
                //v.setText(Html.fromHtml(message));
            }



//        if (title == Utility.getStringResourceByName("attenzione"))
//            builder.setIcon(R.drawable.icon_alert_warning);
//        else
//            builder.setIcon(R.drawable.icon_alert_info);

            builder.setPositiveButton(R.string.ok, ocl);
            AlertDialog alert = builder.show();


            setTextSizeStyleAlert(alert, ctx);

        } catch (Exception ex) {
            //Utility.FileLog(ex);
        }
    }

    public static void promptYesNo(String title, int message, int btnYes, int btnNo,  Context ctx, DialogInterface.OnClickListener ocYes, DialogInterface.OnClickListener ocNo) {
        promptYesNo(title, ctx.getString(message),ctx.getString(btnYes),ctx.getString(btnNo), ctx, ocYes, ocNo);
    }

    public static void promptYesNo(String title, String message, String btnYes, String btnNo,  Context ctx, DialogInterface.OnClickListener ocYes, DialogInterface.OnClickListener ocNo) {
        try {
            LayoutInflater li = LayoutInflater.from(ctx);
            View view = li.inflate(R.layout.alerts, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            if(!TextUtils.isEmpty(title)){
                builder.setTitle(title);
            }
            builder.setTitle("");
            builder.setView(view);
            //TextView v = (TextView)view.findViewById(R.id.lblAlert);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                builder.setMessage(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));
                //v.setText(Html.fromHtml(message,Html.FROM_HTML_MODE_LEGACY));
            } else {
                builder.setMessage(Html.fromHtml(message));
                //v.setText(Html.fromHtml(message));
            }



//        if (title == Utility.getStringResourceByName("attenzione"))
//            builder.setIcon(R.drawable.icon_alert_warning);
//        else
//            builder.setIcon(R.drawable.icon_alert_info);

            builder.setPositiveButton(btnYes, ocYes);
            builder.setNegativeButton(btnNo, ocNo);
            AlertDialog alert = builder.show();


            setTextSizeStyleAlert(alert, ctx);

        } catch (Exception ex) {
            //Utility.FileLog(ex);
        }
    }


    public static void setTextSizeStyleAlert(AlertDialog alert, Context ctx){
        TextView textView = (TextView) alert.findViewById(android.R.id.message);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.txt_font_size));
        Button btyes = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        btyes.setTextSize(TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.txt_font_size));
        Button btno = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        btno.setTextSize(TypedValue.COMPLEX_UNIT_PX, ctx.getResources().getDimension(R.dimen.txt_font_size));

    }
}
