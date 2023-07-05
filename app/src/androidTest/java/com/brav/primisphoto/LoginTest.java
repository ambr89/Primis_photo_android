package com.brav.primisphoto;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static com.google.android.gms.common.internal.Asserts.checkNotNull;

import android.Manifest;
import android.app.Instrumentation;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.brav.primisphoto.customClass.MyRvAdapter;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


public class LoginTest {

    long waitingTime = DateUtils.MINUTE_IN_MILLIS/2;

    @Rule
    public ActivityScenarioRule<Login> rule = new ActivityScenarioRule<>(Login.class);

    @Rule
    public GrantPermissionRule permissionCamera = GrantPermissionRule.grant(Manifest.permission.CAMERA);
    @Rule
    public GrantPermissionRule permissionLocation = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionRead = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);


    public static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                Log.v("url", "uri:  " + ((MyRvAdapter.ViewHolder)(viewHolder)).getUri().toString());
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }


    @Test
    public void enterData_showLogin(){

        onView(withId(R.id.edt_username)).perform(ViewActions.clearText());
        onView(withId(R.id.edt_password)).perform(ViewActions.clearText());
        onView(withId(R.id.edt_username)).perform(ViewActions.typeText("testmichael"));
        onView(withId(R.id.edt_password)).perform(ViewActions.typeText("testmichael"));
        onView(withId(R.id.edt_n_scheda)).perform(ViewActions.typeText("rxa.007"))
                .perform(ViewActions.closeSoftKeyboard());
        //onView(withId(R.id.remember)).perform(scrollTo(), setChecked(true));

        onView(withId(R.id.btnProsegui)).check(matches(isDisplayed()))
                .perform(click());

        onView(withId(android.R.id.button1)).perform(click());
        //controllo di essere nell'activity photo
        intended(hasComponent(Photo.class.getName()));
    }


    @Test
    public void enterData_makePhoto() throws UiObjectNotFoundException{

        onView(withId(R.id.edt_username)).perform(ViewActions.clearText());
        onView(withId(R.id.edt_password)).perform(ViewActions.clearText());
        onView(withId(R.id.edt_username)).perform(ViewActions.typeText("testmichael"));
        onView(withId(R.id.edt_password)).perform(ViewActions.typeText("testmichael"));
        onView(withId(R.id.edt_n_scheda)).perform(ViewActions.typeText("rxa.007"))
                .perform(ViewActions.closeSoftKeyboard());

        onView(withId(R.id.btnProsegui)).check(matches(isDisplayed()))
                .perform(click());

        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.makePhoto)).perform(click());


        Instrumentation ins = InstrumentationRegistry.getInstrumentation();
        UiDevice device = UiDevice.getInstance(ins);
        UiObject obj = device.findObject(new UiSelector().resourceId("com.android.camera2:id/shutter_button"));
        if(obj.waitForExists(4000L)){
            obj.click();
        }

        obj = device.findObject(new UiSelector().resourceId("com.android.camera2:id/done_button"));
        if(obj.waitForExists(4000L)){
            obj.click();
        }

        onView(withId(R.id.rv_gallery))
                .perform(scrollToPosition(0))
                .check(matches(atPosition(0,  isDisplayed())));
    }


    @Test
    public void enterData_takePhoto() throws UiObjectNotFoundException {

        onView(withId(R.id.edt_username)).perform(ViewActions.clearText());
        onView(withId(R.id.edt_password)).perform(ViewActions.clearText());
        onView(withId(R.id.edt_username)).perform(ViewActions.typeText("testmichael"));
        onView(withId(R.id.edt_password)).perform(ViewActions.typeText("testmichael"));
        onView(withId(R.id.edt_n_scheda)).perform(ViewActions.typeText("rxa.007"))
                .perform(ViewActions.closeSoftKeyboard());
        //onView(withId(R.id.remember)).perform(scrollTo(), setChecked(true));

        onView(withId(R.id.btnProsegui)).check(matches(isDisplayed()))
                .perform(click());

        onView(withId(android.R.id.button1)).perform(click());

        onView(withId(R.id.openGallery)).perform(click());


        Instrumentation ins = InstrumentationRegistry.getInstrumentation();
        UiDevice device = UiDevice.getInstance(ins);

        UiObject obj = device.findObject(new UiSelector().textContains("Foto"));
        if(obj.waitForExists(4000L)){
            obj.click();
        }else {
            obj = device.findObject(new UiSelector().textContains("Photo"));
            if(obj.waitForExists(4000L)){
                obj.click();
            }
        }

        obj = device.findObject(new UiSelector().textContains("Fotocamera"));
        if(obj.waitForExists(4000L)){
            obj.click();
        }else{
            obj = device.findObject(new UiSelector().textContains("Pictures"));
            if(obj.waitForExists(4000L)){
                obj.click();
            }
        }

        obj = device.findObject(new UiSelector().index(2));
        boolean hoUnaFoto = true;
        if(obj.waitForExists(4000L)){
            obj.click();
        }else{
            hoUnaFoto = false;
        }

        if(hoUnaFoto) {
            obj = device.findObject(new UiSelector().textContains("Fine"));
            if (obj.waitForExists(4000L)) {
                obj.click();
            }else{
                obj = device.findObject(new UiSelector().textContains("Done"));
                if (obj.waitForExists(4000L)) {
                    obj.click();
                }
            }

            // check che esiste
            onView(withId(R.id.rv_gallery))
                    .perform(scrollToPosition(0))
                    .check(matches(atPosition(0,  isDisplayed())));

        }
        else{
                assert(false);
        }

//
//        obj = device.findObject(new UiSelector().resourceId("com.android.camera2:id/done_button"));
//        if(obj.waitForExists(4000L)){
//            obj.click();
//        }

    }

}
