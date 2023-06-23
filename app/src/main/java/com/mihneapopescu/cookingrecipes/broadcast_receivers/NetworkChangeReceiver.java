package com.mihneapopescu.cookingrecipes.broadcast_receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.mihneapopescu.cookingrecipes.services.ServiceManager;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if(checkInternet(context)){
            Toast.makeText(context, "Connected to internet.", Toast.LENGTH_LONG).show();
        }

    }

    boolean checkInternet(Context context) {
        ServiceManager serviceManager = new ServiceManager(context);
        if (serviceManager.isInternetAvailable(context)) {
            return true;
        } else {
            return false;
        }
    }

}

