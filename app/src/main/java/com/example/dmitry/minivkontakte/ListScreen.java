package com.example.dmitry.minivkontakte;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by Dmitry on 21.08.2015.
 */

public class ListScreen extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(ListScreen.this, "УРА!", Toast.LENGTH_LONG).show();
    }

}
