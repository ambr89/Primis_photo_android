package com.brav.primisphoto.customClass;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.brav.primisphoto.R;

public class Footer extends LinearLayout {

    private Button btnProsegui;
    private Button btnTop;
    private String _onClickNextMethod = "";

    public Footer(Context context) {
        super(context);
        init(context);
    }

    public Footer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initAttibuteSet(context, attrs);

    }

    public Footer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        initAttibuteSet(context, attrs);
    }

    private void init(Context context) {
        // Definisco l'inflater per inserirlo in ogni pagina richiesta
        LayoutInflater li;
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.footer, this, true);

        // Aggancio gli eventi dei bottoni
        btnProsegui = (Button) findViewById(R.id.btnProsegui);
        //btnProsegui.setOnClickListener(listener);
    }




    private void initAttibuteSet(Context ctx, AttributeSet attrs) {
        TypedArray arr = ctx.obtainStyledAttributes(attrs, R.styleable.CtrlFooter);
        String onClickNextMethod = arr.getString(R.styleable.CtrlFooter_onClickButton);
        String _txtBtn = arr.getString(R.styleable.CtrlFooter_textBtn);


        if (!TextUtils.isEmpty(onClickNextMethod))
            _onClickNextMethod = onClickNextMethod;

        if (ctx.isRestricted()) {
            throw new IllegalStateException("The scat:onClickNext attribute cannot "
                    + "be used within a restricted context");
        } else {
            if (!TextUtils.isEmpty(_onClickNextMethod)) {
                btnProsegui.setOnClickListener(new DeclaredOnClickListener(btnProsegui, _onClickNextMethod));
            }
        }


        if(!TextUtils.isEmpty(_txtBtn)){
            btnProsegui.setText(_txtBtn);
        }else
            btnProsegui.setText("");

        arr.recycle();  // Do this when done.

    }


    /*
    * Ambra aggiunto per sostituire il click con l'xml
    * */
    private static class DeclaredOnClickListener implements OnClickListener {
        private final View mHostView;
        private final String mMethodName;

        private Method mResolvedMethod;
        private Context mResolvedContext;

        public DeclaredOnClickListener(View hostView, String methodName) {
            mHostView = hostView;
            mMethodName = methodName;
        }

        @Override
        public void onClick(View v) {
            if (mResolvedMethod == null) {
                resolveMethod(mHostView.getContext(), mMethodName);
            }

            try {
                mResolvedMethod.invoke(mResolvedContext, v);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(
                        "Could not execute non-public method for android:onClick", e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(
                        "Could not execute method for android:onClick", e);
            }
        }

        private void resolveMethod(Context context, String name) {
            while (context != null) {
                try {
                    if (!context.isRestricted()) {
                        final Method method = context.getClass().getMethod(mMethodName, View.class);
                        if (method != null) {
                            mResolvedMethod = method;
                            mResolvedContext = context;
                            return;
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // Failed to find method, keep searching up the hierarchy.
                }

                if (context instanceof ContextWrapper) {
                    context = ((ContextWrapper) context).getBaseContext();
                } else {
                    // Can't search up the hierarchy, null out and fail.
                    context = null;
                }
            }

            final int id = mHostView.getId();
            final String idText = id == NO_ID ? "" : " with id '"
                    + mHostView.getContext().getResources().getResourceEntryName(id) + "'";
            throw new IllegalStateException("Could not find method " + mMethodName
                    + "(View) in a parent or ancestor Context for android:onClick "
                    + "attribute defined on view " + mHostView.getClass() + idText);
        }
    }
}
