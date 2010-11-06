package se.jimlar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

public class ValtechIntraSync extends Activity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        TextView tv = new TextView(this);

        List<Employee> employeeSet = new APIClient("jimmy.larsson", "", new APIResponseParser()).getEmployees();

        tv.setText("Hello, Android, from IntelliJ IDEA!\n" + employeeSet.size() + " employees found");
        setContentView(tv);

    }


}
