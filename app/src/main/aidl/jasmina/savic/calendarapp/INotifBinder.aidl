// INotifBinder.aidl
package jasmina.savic.calendarapp;

// Declare any non-default types here with import statements

interface INotifBinder {
    void addEventReminder(long id, int mins);
    void removeEventReminder(long id);
}
