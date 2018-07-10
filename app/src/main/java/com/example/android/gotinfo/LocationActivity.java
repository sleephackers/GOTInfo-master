package com.example.android.gotinfo;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.gotinfo.Api.ApiClient;
import com.example.android.gotinfo.Api.ApiInterface;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationActivity extends AppCompatActivity {

    TextView listLocation;
    String name;
    List<Datum> datum;
    List<String> locations;
    String locationString = "";
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) ;
        else {
            Toast.makeText(LocationActivity.this, "CHECK YOUR NETWORK CONNECTIVITY",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        setContentView(R.layout.activity_location);
        Intent myintent = getIntent();
        name = myintent.getStringExtra("name");

        listLocation = findViewById(R.id.list_location);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<LocationResponse> call = apiInterface.getLocations(name);
        call.enqueue(new Callback<LocationResponse>() {
            @Override
            public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                datum = response.body().getData();
                locations = datum.get(0).getLocations();

                for (int i = 0; i < locations.size(); i++) {
                    locationString += locations.get(i) + "\n\n";
                }
                listLocation.setText(locationString);
            }

            @Override
            public void onFailure(Call<LocationResponse> call, Throwable t) {

            }
        });

    }
}