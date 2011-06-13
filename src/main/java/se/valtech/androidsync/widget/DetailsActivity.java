package se.valtech.androidsync.widget;

import android.accounts.AccountAuthenticatorActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import se.valtech.androidsync.Logger;
import se.valtech.androidsync.R;

public class DetailsActivity extends AccountAuthenticatorActivity {
    private static final Logger LOGGER = new Logger(DetailsActivity.class);

    private EditText username;
    private EditText password;
    private Button loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.info("create");
        setContentView(R.layout.main);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        loginButton = (Button) findViewById(R.id.login);
        loginButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                LOGGER.info("click");
                String user = username.getText().toString();
                String password = DetailsActivity.this.password.getText().toString();

            }

        });
    }
}
