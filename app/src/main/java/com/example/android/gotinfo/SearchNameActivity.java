package com.example.android.gotinfo;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.gotinfo.Api.ApiClient;
import com.example.android.gotinfo.Api.ApiInterface;
import com.example.android.gotinfo.DataPackage.DataContract;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchNameActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_DATA_LOADER = 0;
    TextView nameInfo;
    TextView titleInfo;
    TextView houseInfo;
    TextView spouseInfo;
    TextView noResult;
    String titles = "";
    ImageView imageView;
    Data data;
    Boolean nomatch = true;
    String text;
    String spouse;
    String house;
    int size;
    private Uri mCurrentDataUri;
    private String TAG = "searchActivity";
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mCurrentDataUri == null) {

            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) ;
            else {
                Toast.makeText(SearchNameActivity.this, "CHECK YOUR NETWORK CONNECTIVITY",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        setContentView(R.layout.activity_search_name);
        final Intent intent = getIntent();
        final String query = intent.getStringExtra("String");
        mCurrentDataUri = intent.getData();
        int FLAG = intent.getIntExtra("flag", 1);

        nameInfo = findViewById(R.id.char_info_name);
        titleInfo = findViewById(R.id.titles);
        houseInfo = findViewById(R.id.house);
        spouseInfo = findViewById(R.id.spouse);
        noResult = findViewById(R.id.no_result);

        imageView = findViewById(R.id.image_view);

        if (mCurrentDataUri == null) {
            apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<NameResponse> call = apiInterface.getCharacter(query);
            call.enqueue(new Callback<NameResponse>() {
                @Override
                public void onResponse(Call<NameResponse> call, Response<NameResponse> response) {

                    if (response.body() == null) {
                        imageView.setImageResource(R.drawable.no_results_found);
                        Log.e(TAG, "checking for log here 1");
                        nomatch = false;
                        noResult.setText("THERE IS NO SUCH CHARACTER," + "\nARE YOU SURE YOU TYPED THE RIGHT NAME?");
                        noResult.setTextSize(15);


                    } else {
                        Log.e(TAG, "checking for log here 2");
                        data = response.body().getData();
                        text = data.getName();
                        spouse = data.getSpouse();
                        house = data.getHouse();
                        size = data.getTitles().size();
                        final String[] titleArray = new String[size];
                        if (data.getTitles().isEmpty())
                            titles += "Information Unavailable";
                        else
                            for (int i = 0; i < size; i++) {
                                titleArray[i] = data.getTitles().get(i);
                                titles += titleArray[i] + "\n";
                            }
                        Log.e(TAG, "to check if name was returned " + text);
                        nameInfo.setText(text);
                        titleInfo.setText(titles);
                        houseInfo.setText(house);
                        if (spouse == null)
                            spouseInfo.setText("Unavailable");
                        else
                            spouseInfo.setText(spouse);
                        nameInfo.setVisibility(View.VISIBLE);
                        titleInfo.setVisibility(View.VISIBLE);
                        houseInfo.setVisibility(View.VISIBLE);
                        spouseInfo.setVisibility(View.VISIBLE);
                        Picasso.get().load("https://api.got.show" + data.getImageLink()).resize(400, 400).into(imageView);
                        saveData();

                    }
                }

                @Override
                public void onFailure(Call<NameResponse> call, Throwable t) {

                }
            });

        } else {
            getLoaderManager().initLoader(EXISTING_DATA_LOADER, null, this);
        }


    }

    private void saveData() {
        ContentValues values = new ContentValues();
        values.put(DataContract.DataEntry.COLUMN_NAME, text);
        values.put(DataContract.DataEntry.COLUMN_TITLES, titles);
        values.put(DataContract.DataEntry.COLUMN_HOUSE, house);
        values.put(DataContract.DataEntry.COLUMN_SPOUSE, spouse);

        Uri newUri = getContentResolver().insert(DataContract.DataEntry.CONTENT_URI, values);
        if (newUri == null) {
            Toast.makeText(this, "SEARCH RESULT WASNT SAVED",
                    Toast.LENGTH_SHORT).show();
        } else {
            {
                Toast.makeText(this, "SEARCH RESULT SAVED",
                        Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                DataContract.DataEntry._ID,
                DataContract.DataEntry.COLUMN_NAME,
                DataContract.DataEntry.COLUMN_TITLES,
                DataContract.DataEntry.COLUMN_HOUSE,
                DataContract.DataEntry.COLUMN_SPOUSE};


        return new CursorLoader(this,
                mCurrentDataUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_NAME);
            int houseColumnIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_HOUSE);
            int spouseColumnIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_SPOUSE);
            int titleColumnIndex = cursor.getColumnIndex(DataContract.DataEntry.COLUMN_TITLES);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String house = cursor.getString(houseColumnIndex);
            String spouse = cursor.getString(spouseColumnIndex);
            String titles = cursor.getString(titleColumnIndex);
            imageView.setImageResource(R.drawable.dbimage);
            nameInfo.setText(name);
            houseInfo.setText(house);
            spouseInfo.setText(spouse);
            titleInfo.setText(titles);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameInfo.setText("");
        houseInfo.setText("");
        spouseInfo.setText("");
        titleInfo.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.locations:
                if (mCurrentDataUri != null)
                    Toast.makeText(SearchNameActivity.this, "LOCATIONS UNAVAILABLE IN HISTORY",
                            Toast.LENGTH_SHORT).show();
                else if (!nomatch)
                    Toast.makeText(SearchNameActivity.this, "NEED A VALID NAME",
                            Toast.LENGTH_SHORT).show();
                else {
                    Intent myIntent = new Intent(SearchNameActivity.this, LocationActivity.class);
                    myIntent.putExtra("name", text);
                    SearchNameActivity.this.startActivity(myIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}



