/*
 * Copyright 2017 Nafundi
 * Modifications 2018 AFeas1987
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.nccnestapp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nccnestapp.R;
import com.example.nccnestapp.adapters.SimpleListAdapter;
import com.example.nccnestapp.fragments.TestFragment;
import com.example.nccnestapp.utilities.ListElement;
import com.example.nccnestapp.utilities.PantryGuest;

import java.util.ArrayList;
import java.util.List;

import static com.example.nccnestapp.utilities.Constants.DISPLAY_NAME;
import static com.example.nccnestapp.utilities.Constants.DISPLAY_SUBTEXT;
import static com.example.nccnestapp.utilities.Constants.FORMS_URI;

public class MainActivity extends AbstractActivity {

    private int formId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (!isCollectAppInstalled()) {
            finish();
            Toast.makeText(this, getString(R.string.collect_app_not_installed), Toast.LENGTH_LONG).show();
            return;
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SimpleListAdapter(getListFromCursor(getCursor()), item -> {
            recyclerView.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .add(R.id.frame_layout_main, new TestFragment()).commit();
            formId = item.getId();
        }));

        TextView emptyView = (TextView) findViewById(R.id.empty_view);
        if (getCursor().getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }


    public void showPinDialog(String email) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pin, null);
        EditText pinEdit = dialogView.findViewById(R.id.edit_pin);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pin creation")
                .setMessage("Create a pin number")
                .setView(dialogView)
                .setPositiveButton("Ok", null);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(alertView -> {
            if (isValidPin(pinEdit.getText())) {
                this.realm.executeTransactionAsync(realm -> {
                    PantryGuest guest = new PantryGuest(email, pinEdit.getText().toString());
                    realm.insert(guest);
                });
                startActivity(new Intent(getApplicationContext(), SheetsActivity.class));
                dialog.dismiss();
                finish();
            } else
                Toast.makeText(dialog.getContext(), "Invalid pin", Toast.LENGTH_LONG).show();
        });
    }


    private void launchCollectApp() {
        startActivityIfAvailable(new Intent(Intent.ACTION_EDIT, Uri.parse(FORMS_URI + "/" + formId)));
    }


    @Override
    protected void makeSheetsApiCall() {
        //  Do nothing
    }


    private Cursor getCursor() {
        return getContentResolver().query(Uri.parse(FORMS_URI), null, null, null, null);
    }


    private List<ListElement> getListFromCursor(Cursor cursor) {
        List<ListElement> listElements = new ArrayList<>();

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                    String text1 = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                    String text2 = cursor.getString(cursor.getColumnIndex(DISPLAY_SUBTEXT));
                    listElements.add(new ListElement(id, text1, text2));
                }
            } finally {
                cursor.close();
            }
        }

        return listElements;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_admin:
                Intent i = new Intent(getApplicationContext(), SheetsActivity.class);
                startActivity(i);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private boolean isValidPin(CharSequence target) {
        Integer i = null;
        try {i = Integer.parseInt(target.toString());}
        catch (NumberFormatException ex) {Log.d("DEBUG", ex.getMessage());}
        return i != null && i >= 0 && target.toString().length() == 4;
    }
}
