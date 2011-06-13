package se.valtech.androidsync.widget;

import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import se.valtech.androidsync.Logger;
import se.valtech.androidsync.R;

public class DetailsActivity extends Activity {
    private static final Logger LOGGER = new Logger(DetailsActivity.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.info("create");
        setContentView(R.layout.status_appwidget_details);

        View view = findViewById(R.id.widgetdetails);
    }
}
