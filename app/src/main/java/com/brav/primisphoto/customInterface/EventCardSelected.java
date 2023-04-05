package com.brav.primisphoto.customInterface;

import android.net.Uri;

/**
 * Created by ambra on 27/10/2017.
 */

public interface EventCardSelected {
    public void selectedItem();
    public void deselectedItem();
    public void showItem(Uri uri);
}
