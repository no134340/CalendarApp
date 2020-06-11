package jasmina.savic.calendarapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EventDbHelper extends SQLiteOpenHelper {

    private static EventDbHelper instance = null;
    private static Context mContext = null;

    public static final String DATABASE_NAME = "calendar_events.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "events";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_EVENT_NAME = "name";
    public static final String COLUMN_EVENT_TIME_START = "time_start";
    public static final String COLUMN_EVENT_TIME_END = "time_end";
    public static final String COLUMN_EVENT_DURATION = "duration";
    public static final String COLUMN_EVENT_REMINDER = "reminder";
    public static final String COLUMN_EVENT_COMPLETED = "completed";
    public static final String COLUMN_EVENT_LOCATION = "location";
    public static final String COLUMN_MINUTE_CHECKED = "minute";
    public static final String COLUMN_ID = "id";

    private SQLiteDatabase mDb = null;

    public EventDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static EventDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new EventDbHelper(context);
            mContext = context;
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_EVENT_NAME + " TEXT, " +
                COLUMN_EVENT_TIME_START + " TEXT, " +
                COLUMN_EVENT_TIME_END + " TEXT, " +
                COLUMN_EVENT_DURATION + " INTEGER, " +
                COLUMN_EVENT_REMINDER + " INTEGER, " +
                COLUMN_EVENT_COMPLETED + " INTEGER, " +
                COLUMN_MINUTE_CHECKED + " INTEGER, " +
                COLUMN_EVENT_LOCATION + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insert(Event event) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, event.getEventDate());
        values.put(COLUMN_EVENT_NAME, event.getEventName());
        values.put(COLUMN_EVENT_TIME_START, event.getEventTimeStart());
        values.put(COLUMN_EVENT_TIME_END, event.getEventTimeEnd());
        values.put(COLUMN_EVENT_DURATION, event.getDuration());
        int reminder;
        if (event.isEventReminder()) {
            reminder = 1;
        } else {
            reminder = 0;
        }
        values.put(COLUMN_EVENT_REMINDER, reminder);
        int completed;
        if (event.isCompleted()) {
            completed = 1;
        } else {
            completed = 0;
        }
        values.put(COLUMN_EVENT_COMPLETED, completed);
        values.put(COLUMN_EVENT_LOCATION, event.getEventLocation());

        values.put(COLUMN_MINUTE_CHECKED, event.getMinuteChecked());

        long eventId = db.insert(TABLE_NAME, null, values);
        close();
        return eventId;
    }

    public Event[] readEvents(String date) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_DATE + "=?", new String[]{date}, null, null, null, null);

        if (cursor.getCount() <= 0) {
            return null;
        }

        Event[] events = new Event[cursor.getCount()];
        int i = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            events[i++] = createEvent(cursor);
        }

        close();
        if(cursor.getCount() != 0){
            return events;
        } else {
            return null;
        }

    }

    public Event readEvent(long id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor.getCount() <= 0) {
            close();
            return null;
        }
        cursor.moveToFirst();
        Event event = createEvent(cursor);

        close();
        return event;
    }

    public void deleteEvent(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        close();
    }

    private Event createEvent(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_NAME));
        long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
        String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
        String time_start = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_TIME_START));
        String time_end = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_TIME_END));
        int duration = cursor.getInt(cursor.getColumnIndex(COLUMN_EVENT_DURATION));
        String location = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_LOCATION));
        int minuteChecked = cursor.getInt(cursor.getColumnIndex(COLUMN_MINUTE_CHECKED));
        int reminder = cursor.getInt(cursor.getColumnIndex(COLUMN_EVENT_REMINDER));

        boolean reminderOn;
        if (reminder == 1) {
            reminderOn = true;
        } else {
            reminderOn = false;
        }

        int completed = cursor.getInt(cursor.getColumnIndex(COLUMN_EVENT_COMPLETED));

        boolean isCompleted;
        if (completed == 1) {
            isCompleted = true;
        } else {
            isCompleted = false;
        }

        return new Event(isCompleted, name, date, reminderOn, location, time_start, time_end, duration, id, minuteChecked);
    }

    public void updateEvent(Event event) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, event.getId());
        values.put(COLUMN_DATE, event.getEventDate());
        values.put(COLUMN_EVENT_NAME, event.getEventName());
        values.put(COLUMN_EVENT_TIME_START, event.getEventTimeStart());
        values.put(COLUMN_EVENT_TIME_END, event.getEventTimeEnd());
        values.put(COLUMN_EVENT_DURATION, event.getDuration());
        int reminder;
        if (event.isEventReminder()) {
            reminder = 1;
        } else {
            reminder = 0;
        }
        values.put(COLUMN_EVENT_REMINDER, reminder);
        int completed;
        if (event.isCompleted()) {
            completed = 1;
        } else {
            completed = 0;
        }
        values.put(COLUMN_EVENT_COMPLETED, completed);
        values.put(COLUMN_EVENT_LOCATION, event.getEventLocation());

        values.put(COLUMN_MINUTE_CHECKED, event.getMinuteChecked());

        db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(event.getId())});
        close();

    }

    public Event[] readEventsReminder() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_EVENT_REMINDER + "=? AND " + COLUMN_EVENT_COMPLETED + "=?", new String[]{"1", "0"}, null, null, null, null);

        if (cursor.getCount() <= 0) {
            return null;
        }

        Event[] events = new Event[cursor.getCount()];
        int i = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            events[i++] = createEvent(cursor);
        }

        close();
        return events;
    }
}
