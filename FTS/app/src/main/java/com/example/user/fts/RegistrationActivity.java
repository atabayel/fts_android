package com.example.user.fts;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RegistrationActivity extends AppCompatActivity {

    EditText name;
    EditText surname;
    EditText email;
    EditText phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        name = findViewById(R.id.regName);
        surname = findViewById(R.id.regSurename);
        email = findViewById(R.id.regEMAIL);
        phone = findViewById(R.id.regTelNum);
    }

    public void onClick(View view) {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MyContants.BASE_URL) // Адрес сервера
                .addConverterFactory(GsonConverterFactory.create(gson)) // говорим ретрофиту что для сериализации необходимо использовать GSON
                .build();

        API api = retrofit.create(API.class);

        Call<ServerResponse> call = api.registration(
                name.getText().toString(),
                surname.getText().toString(),
                email.getText().toString(),
                phone.getText().toString());

        if (MyMethods.isNetworkOnline(this)) {
            try {
                call.enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body().getResponse() == "record_ex") {
                                showToast(getResources().getString(R.string.reg_record_exists));
                            } else if (response.body().getResponse() == "field_er") {
                                showToast(getResources().getString(R.string.field_err));
                            } else {
                                createAccount(response.body().getResponse());
                            }
                        } else {
                            Log.d(MyContants.TAG, "failure response is: " + response.raw().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<ServerResponse> call, Throwable t) {
                        showToast(getResources().getString(R.string.cant_create_acc));
                        Log.d(MyContants.TAG, "Error: " + t.getMessage());
                    }
                });
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getResources().getString(R.string.inet_off), Toast.LENGTH_LONG).show();
        }
    }

    private void showToast(String msg) {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name", String.valueOf(name.getText()));
        outState.putString("surname", String.valueOf(surname.getText()));
        outState.putString("email", String.valueOf(email.getText()));
        outState.putString("telNum", String.valueOf(phone.getText()));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        name.setText(savedInstanceState.getString("name"));
        surname.setText(savedInstanceState.getString("surname"));
        email.setText(savedInstanceState.getString("email"));
        phone.setText(savedInstanceState.getString("telNum"));
    }

    private void createAccount(String id) {
        MyPreferences preferences = new MyPreferences(this, "tablevar");
        preferences.setVariable(MyContants.DB_TABLE_IS_ENTERED, "1");
        preferences.setVariable(MyContants.DB_TABLE_MY_ID, id);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}
