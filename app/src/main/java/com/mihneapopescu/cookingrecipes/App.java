package com.mihneapopescu.cookingrecipes;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Create Realm database
        Realm.init(this);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("cooking-app.db")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .allowWritesOnUiThread(true)
                .allowQueriesOnUiThread(true)
                .build();

        Realm.setDefaultConfiguration(config);
    }
}
