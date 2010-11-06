package se.jimlar;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class ValtechIntraSync extends Activity {
    private SharedPreferences preferences;
    private static final String USERNAME_SETTING = "username";
    private static final String PASSWORD_SETTING = "password";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main);

        preferences = getSharedPreferences("valtechdroid",  MODE_PRIVATE);
        String savedUsername = preferences.getString(USERNAME_SETTING, "");
        String savedPassword = preferences.getString(PASSWORD_SETTING, "");

        getUsername().setText(savedUsername);
        getPassword().setText(savedPassword);

        Button button = (Button) findViewById(R.id.ok);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                List<Employee> employeeSet = new APIClient(getUsername().getText().toString(),
                                                           getPassword().getText().toString(),
                                                           new APIResponseParser()).getEmployees();
                getStatusLabel().setText(employeeSet.size() + " employes found");
            }
        });
    }

    private TextView getStatusLabel() {
        return (TextView) findViewById(R.id.status);
    }

    private EditText getPassword() {
        return (EditText) findViewById(R.id.passwordentry);
    }

    private EditText getUsername() {
        return (EditText) findViewById(R.id.userentry);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor ed = preferences.edit();
        ed.putString(USERNAME_SETTING, getUsername().getText().toString());
        ed.putString(PASSWORD_SETTING, getPassword().getText().toString());
        ed.commit();
    }
}
