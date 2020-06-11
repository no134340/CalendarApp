package jasmina.savic.calendarapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

public class EventActivity extends AppCompatActivity implements View.OnClickListener, ServiceConnection {

    private TextView eventName, eventDate;
    private TimePicker timePickerStart, timePickerEnd;
    private TextView weatherText, tempText;
    private LinearLayout weatherLayout;
    private Button getWeather, saveEvent;
    private EditText locationInput;
    private ImageView weatherIcon;
    private CheckBox reminderOn;
    private String eventTimeStart, eventTimeEnd;
    public EventDbHelper mDbHelper;
    private Event mEvent;
    private boolean edit;
    private Toast toast;

    private HttpHelper httpHelper;
    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    public static final String API_SUFFIX = "&appid=c050c36b1b0a9c54d6d7afddad80471c";
    public static final String BASE_ICON_URL = "http://openweathermap.org/img/wn/";
    public static final String ICON_URL_SUFFIX = "@2x.png";

    public static final String LOG = "EVENTACTIVITY";

    private INotifBinder mService = null;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        toast = Toast.makeText(getApplicationContext(), getString(R.string.prazan_unos_lokacija), Toast.LENGTH_SHORT);

        httpHelper = new HttpHelper();

        mDbHelper = EventDbHelper.getInstance(this);

        eventName = findViewById(R.id.event_activity_name);
        eventName.setText(getIntent().getStringExtra("eventName"));

        timePickerStart = findViewById(R.id.time_picker_start);
        timePickerStart.setIs24HourView(true);
        timePickerStart.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                eventTimeStart = hourOfDay + ":" + minute;
            }
        });

        timePickerEnd = findViewById(R.id.time_picker_end);
        timePickerEnd.setIs24HourView(true);
        timePickerEnd.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                eventTimeEnd = hourOfDay + ":" + minute;
            }
        });

        eventDate = findViewById(R.id.event_activity_date);
        eventDate.setText(getIntent().getStringExtra("eventDate"));

        weatherLayout = findViewById(R.id.weather_layout);

        getWeather = findViewById(R.id.get_weather_button);
        getWeather.setOnClickListener(this);

        locationInput = findViewById(R.id.location_input);

        tempText = findViewById(R.id.temperature);

        weatherText = findViewById(R.id.weather_text);

        weatherIcon = findViewById(R.id.weather_icon);

        reminderOn = findViewById(R.id.reminder_on);
        reminderOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEvent.setEventReminder(isChecked);
                if (isChecked) {
                    Calendar now = Calendar.getInstance();
                    mEvent.setMinuteChecked(now.get(Calendar.MINUTE));
                }
            }
        });

        saveEvent = findViewById(R.id.button_save);
        saveEvent.setOnClickListener(this);

        eventTimeStart = timePickerStart.getHour() + ":" + timePickerStart.getMinute();
        eventTimeEnd = timePickerEnd.getHour() + ":" + timePickerEnd.getMinute();

        long id = getIntent().getLongExtra("eventId", -1);

        mEvent = null;
        mEvent = mDbHelper.readEvent(id);

        if (id != -1 && mEvent == null) {
            finish();
        }

        if (mEvent != null) {
            edit = true;
            String[] timeStart = mEvent.getEventTimeStart().split(":");
            int hourStart = Integer.parseInt(timeStart[0]);
            int minuteStart = Integer.parseInt(timeStart[1]);
            timePickerStart.setCurrentHour(hourStart);
            timePickerStart.setCurrentMinute(minuteStart);
            String[] timeEnd = mEvent.getEventTimeEnd().split(":");
            int hourEnd = Integer.parseInt(timeEnd[0]);
            int minuteEnd = Integer.parseInt(timeEnd[1]);
            timePickerEnd.setCurrentHour(hourEnd);
            timePickerEnd.setCurrentMinute(minuteEnd);
            locationInput.setText(mEvent.getEventLocation());
            reminderOn.setChecked(mEvent.isEventReminder());
            eventName.setText(mEvent.getEventName());
        } else {
            mEvent = new Event(reminderOn.isChecked(), getIntent().getStringExtra("eventName"), getIntent().getStringExtra("eventDate"), false, "", eventTimeStart, eventTimeEnd, 0, id, -1);
            edit = false;
        }
    }


    public void getWeatherData(final String locationData) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    final String url = BASE_URL + locationData + API_SUFFIX;
                    Log.d("WEATHER_URL", url);
                    JSONObject jsonobject = httpHelper.getJSONObjectFromURL(url);

                    if (jsonobject == null) {
                        Log.d(LOG, "HTTP REQUEST UNSUCCESSFUL: Location data not available");
                        runOnUiThread(new Runnable() {
                            public void run() {
                                weatherText.setVisibility(View.VISIBLE);
                                weatherLayout.setVisibility(View.INVISIBLE);
                            }
                        });
                        return;
                    }

                    JSONArray icon = jsonobject.getJSONArray("weather");
                    JSONObject weatherData = jsonobject.getJSONObject("main");
                    JSONObject iconObject = icon.getJSONObject(0);

                    final String icon_id = iconObject.getString("icon");
                    String tempString = weatherData.getString("temp");

                    final double tempK = Double.parseDouble(tempString);
                    final double temp = tempK - 273.15;
                    final Bitmap icon_bitmap = httpHelper.getBitmap(BASE_ICON_URL + icon_id + ICON_URL_SUFFIX);

                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (icon_bitmap != null) {
                                tempText.setText(String.format("%.1f°C", temp));
                                weatherIcon.setImageBitmap(icon_bitmap);
                                weatherLayout.setVisibility(View.VISIBLE);
                                weatherText.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    Log.e(LOG, "JSONERROR");
                    Log.e(LOG, e.getMessage());
                }
            }
        }).start();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        String locationData = locationInput.getText().toString();
        switch (v.getId()) {
            case R.id.get_weather_button:
                if (TextUtils.isEmpty(locationData)) {
                    locationInput.setError(getString(R.string.prazan_unos_lokacija));
                    return;
                }
                getWeatherData(locationData);
                break;
            case R.id.button_save:
                if (TextUtils.isEmpty(locationData)) {
                    if (!toast.getView().isShown()) {
                        toast.show();
                    }
                    return;
                }
                mEvent.setEventReminder(reminderOn.isChecked());
                mEvent.setEventTimeStart(eventTimeStart);
                mEvent.setEventTimeEnd(eventTimeEnd);
                mEvent.setEventLocation(locationData);

                int startM, startH, endM, endH;
                startM = timePickerStart.getMinute();
                startH = timePickerStart.getHour();
                endM = timePickerEnd.getMinute();
                endH = timePickerEnd.getHour();

                DurationCalculate dc = new DurationCalculate();
                int duration = dc.durationCalculate(startH, startM, endH, endM);

                if(duration <= 0) {
                    Toast mToast = Toast.makeText(getApplicationContext(), getString(R.string.trajanje), Toast.LENGTH_SHORT);
                    if (!mToast.getView().isShown()) {
                        mToast.show();
                        break;
                    }
                }
                mEvent.setDuration(duration);
                if (!edit) {
                    // pokupimo ID iz baze kad stavljamo event u bazu
                    mEvent.setId(mDbHelper.insert(mEvent));
                } else {
                    mDbHelper.updateEvent(mEvent);
                }

                Intent intent = new Intent(getApplicationContext(), NotifService.class);
                if (!bindService(intent, this, Context.BIND_AUTO_CREATE)) {
                    Log.d(LOG, "bind failed");
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(LOG, "start event");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(LOG, "resume event");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(LOG, "stop event");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(LOG, "restart event");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(this);
            Log.d(LOG, "unbound");
        }
        Log.e(LOG, "destroy event");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(LOG, "pause event");


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = INotifBinder.Stub.asInterface(service);

        if (mService != null) {
            Log.d(LOG, "bound");
            if (mEvent.isEventReminder()) {
                try {
                    mService.addEventReminder(mEvent.getId(), mEvent.getMinuteChecked());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    mService.removeEventReminder(mEvent.getId());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        finish();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }
}
