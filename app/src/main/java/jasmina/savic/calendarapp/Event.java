package jasmina.savic.calendarapp;



public class Event {

    private boolean isCompleted;
    private String eventName;
    private String eventDate;
    private boolean eventReminder;
    private String eventLocation;
    private String eventTimeStart;
    private String eventTimeEnd;
    private int duration;
    private long id;
    private int minuteChecked;

    public Event(boolean isCompleted, String eventName, String eventDate, boolean eventReminder, String eventLocation, String eventTimeStart, String eventTimeEnd, int duration, long id, int minuteChecked) {
        this.isCompleted = isCompleted;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventReminder = eventReminder;
        this.eventLocation = eventLocation;
        this.eventTimeStart = eventTimeStart;
        this.eventTimeEnd = eventTimeEnd;
        this.id = id;
        this.minuteChecked = minuteChecked;
        this.duration = duration;

    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public boolean isEventReminder() {
        return eventReminder;
    }

    public void setEventReminder(boolean eventReminder) {
        this.eventReminder = eventReminder;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMinuteChecked() {
        return minuteChecked;
    }

    public void setMinuteChecked(int minuteChecked) {
        this.minuteChecked = minuteChecked;
    }

    public String getEventTimeStart() {
        return eventTimeStart;
    }

    public void setEventTimeStart(String eventTimeStart) {
        this.eventTimeStart = eventTimeStart;
    }

    public String getEventTimeEnd() {
        return eventTimeEnd;
    }

    public void setEventTimeEnd(String eventTimeEnd) {
        this.eventTimeEnd = eventTimeEnd;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}

