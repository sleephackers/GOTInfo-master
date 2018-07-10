package com.example.android.gotinfo;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.example.android.gotinfo.DataPackage.DataContract;
import com.example.android.gotinfo.DataPackage.DataDbHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int DATA_LOADER = 0;
    baseAdapter mAdapter;
    int FLAG = 0;
    DataDbHelper dbHelper;
    ListView dataListView;
    int textlength;
    ArrayList<String> names, array_sort;
    NetworkInfo networkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DataDbHelper(this);
        dataListView = (ListView) findViewById(R.id.list);
        names = new ArrayList<>();
        array_sort = new ArrayList<>();
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        dataListView.setEmptyView(emptyView);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
        refreshSearch();
        dataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final Uri currentUri = ContentUris.withAppendedId(DataContract.DataEntry.CONTENT_URI, dbHelper.getCharId(names.get(position)));
                Intent intent = new Intent(MainActivity.this, SearchNameActivity.class);
                intent.setData(currentUri);
                startActivity(intent);
                refreshSearch();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.search_name);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                boolean recordExists = dbHelper.checkIfRecordExist(query);
                if (recordExists) {
                    final Uri currentUri = ContentUris.withAppendedId(DataContract.DataEntry.CONTENT_URI, dbHelper.getCharId(query));
                    Intent intent = new Intent(MainActivity.this, SearchNameActivity.class);
                    intent.setData(currentUri);
                    startActivity(intent);
                } else {
                    Intent myIntent = new Intent(MainActivity.this, SearchNameActivity.class);
                    myIntent.putExtra("String", query);
                    MainActivity.this.startActivity(myIntent);
                }
                refreshSearch();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (names == null) ;
                else {
                    textlength = newText.length();
                    if (!array_sort.isEmpty())
                        array_sort.clear();
                    for (int i = 0; i < names.size(); i++) {
                        if (textlength <= names.get(i).length()) {
                            if (names.get(i).toLowerCase().contains(
                                    newText.toLowerCase().trim())) {
                                array_sort.add(names.get(i));
                            }
                        }
                    }
                    AppendList(array_sort);
                }

                return false;
            }
        });
        return true;
    }


    public void AppendList(ArrayList<String> str) {
        baseAdapter adapter = new baseAdapter(this, str);

        dataListView.setAdapter(adapter);
    }

    public void refreshSearch() {
        names = dbHelper.getNames();
        if (names == null)
            mAdapter = new baseAdapter(this);
        else
            mAdapter = new baseAdapter(this, names);
        dataListView.setAdapter(mAdapter);

    }

}