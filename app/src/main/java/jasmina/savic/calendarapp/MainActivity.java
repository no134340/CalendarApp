package jasmina.savic.calendarapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.RadioButton;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RadioButton rb_year, rb_month;
    private LinearLayout layout_year, layout_calendar;
    private CalendarView calendar;
    private Button year_button;

    public EventDbHelper mDbHelper;


    public static final String LOG = "MAINACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rb_year = findViewById(R.id.rb_year);
        rb_month = findViewById(R.id.rb_month);

        layout_year = findViewById(R.id.layout_year);

        calendar = findViewById(R.id.calendar);
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Intent intent = new Intent(MainActivity.this, DayActivity.class);
                intent.putExtra("day", dayOfMonth);
                intent.putExtra("month", month);
                intent.putExtra("year", year);
                startActivity(intent);
            }
        });

        year_button = findViewById(R.id.year_button);
        layout_calendar = findViewById(R.id.calendar_layout);

        rb_year.setOnClickListener(this);

        rb_month.setOnClickListener(this);

        year_button.setOnClickListener(this);

        mDbHelper = new EventDbHelper(this);

        Intent intent = new Intent(getApplicationContext(), NotifService.class);
        startService(intent);
        Log.d(LOG, "service started");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.year_button:
                rb_year.setChecked(true);
            case R.id.rb_year:
                layout_calendar.setVisibility(View.INVISIBLE);
                layout_year.setVisibility(View.VISIBLE);
                break;
            case R.id.rb_month:
                layout_year.setVisibility(View.INVISIBLE);
                layout_calendar.setVisibility(View.VISIBLE);
                break;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.e(LOG, "start main");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(LOG, "resume main");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(LOG, "stop main");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(LOG, "restart main");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(LOG, "destroy main");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(LOG, "pause main");
    }
}
