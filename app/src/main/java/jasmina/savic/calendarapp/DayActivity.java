package jasmina.savic.calendarapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DayActivity extends AppCompatActivity {

    private EditText eventName;
    private EventAdapter eventAdapter;
    private ImageButton addButton;
    private ListView eventList;
    private AlertDialog.Builder builder;
    private TextView dateDisplay;
    public String dayActivityDate;

    public EventDbHelper mDbHelper;

    public static final String LOG = "DAYACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        addButton = findViewById(R.id.add_event_button);
        addButton.setOnClickListener(new AddEventOnClickListener());

        eventName = findViewById(R.id.event_name);

        eventAdapter = new EventAdapter(this);

        eventList = findViewById(R.id.event_list);
        eventList.setAdapter(eventAdapter);
        eventList.setOnItemClickListener(new OnEventClickListener());
        eventList.setOnItemLongClickListener(new OnLongEventClickListener());
        eventList.setEmptyView(findViewById(R.id.text_empty_list));

        builder = new AlertDialog.Builder(this);

        dateDisplay = findViewById(R.id.date);

        int day = getIntent().getIntExtra("day", 0);
        int month = getIntent().getIntExtra("month", 0);
        int year = getIntent().getIntExtra("year", 0);
        getDate(day, month, year);
        if(day==0 && month == 0 & year==0) {
            dayActivityDate = getIntent().getStringExtra("date");
            dateDisplay.setText(dayActivityDate);
        }

        mDbHelper = EventDbHelper.getInstance(this);
    }

    public void getDate(int day, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        Date date = cal.getTime();

        String pattern = "dd.MM.yyyy.";

        Format formatter = new SimpleDateFormat(pattern);

        dayActivityDate = formatter.format(date);

        dateDisplay.setText(dayActivityDate);
    }

    /*
     * Pomoćna funkcija za pokretanje nove aktivnosti.
     */
    public void startEventActivity(String message, long eventId) {
        Intent intent = new Intent(DayActivity.this, EventActivity.class);
        intent.putExtra("eventName", message);
        intent.putExtra("eventDate", dayActivityDate);
        intent.putExtra("eventId", eventId);
        startActivity(intent);
    }


    private class AddEventOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.add_event_button:
                    String newEventName = eventName.getText().toString();

                    if (TextUtils.isEmpty(newEventName)) {
                        eventName.setError(getString(R.string.prazan_unos));
                        return;
                    }

                    startEventActivity(newEventName, -1);

                    eventName.getText().clear();
                    break;
            }
        }
    }

    private class OnEventClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Event event = (Event) eventAdapter.getItem(position);
            startEventActivity("", event.getId());
        }
    }

    private class OnLongEventClickListener implements AdapterView.OnItemLongClickListener {
        private Event event;
        private String name;
        private long eventId;

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            event = (Event) eventAdapter.getItem(position);
            name = event.getEventName();
            eventId = event.getId();

            DialogClickListener dialogClickListener = new DialogClickListener();

            builder.setTitle(name);
            builder.setMessage(R.string.alert_dialog_poruka);
            builder.setNegativeButton(R.string.uredi_dogadjaj, dialogClickListener);
            builder.setPositiveButton(R.string.obrisi_dogadjaj, dialogClickListener);
            builder.setCancelable(true);

            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        }

        private class DialogClickListener implements DialogInterface.OnClickListener {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        startEventActivity("", eventId);
                        break;
                    case DialogInterface.BUTTON_POSITIVE:
                        mDbHelper.deleteEvent(eventId);
                        Event[] events = mDbHelper.readEvents(dayActivityDate);
                        eventAdapter.update(events);
                        break;
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(LOG, "start day");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(LOG, "resume day");


        if (!dayActivityDate.isEmpty()) {
            Event[] events = mDbHelper.readEvents(dayActivityDate);

            eventAdapter.update(events);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(LOG, "stop day");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(LOG, "restart day");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(LOG, "destroy day");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(LOG, "pause day");
    }
}
