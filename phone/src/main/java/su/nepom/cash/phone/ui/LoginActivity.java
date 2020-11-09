package su.nepom.cash.phone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import su.nepom.cash.phone.App;
import su.nepom.cash.phone.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("MyApp", "create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginButton.setEnabled(true);

        loginButton.setOnClickListener(v -> {
            loginButton.setEnabled(false);
            loadingProgressBar.setVisibility(View.VISIBLE);

            App.get(this).getStorages().login(usernameEditText.getText().toString(), passwordEditText.getText().toString())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(() -> {
                        loadingProgressBar.setVisibility(View.INVISIBLE);
                        loginButton.setEnabled(true);
                    })
                    .subscribe(
                            () -> startActivity(new Intent(this, MainActivity.class)),
                            error -> Toast.makeText(getApplicationContext(), "ERROR:" + error, Toast.LENGTH_LONG).show()
                    );
        });
    }

    @Override
    protected void onStop() {
        Log.i("MyApp", "stop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.i("MyApp", "pause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i("MyApp", "destroy");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.i("MyApp", "resume");
        super.onResume();
    }

    @Override
    protected void onStart() {
        Log.i("MyApp", "start");
        super.onStart();
    }
}
