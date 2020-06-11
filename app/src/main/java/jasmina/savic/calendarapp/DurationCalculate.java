package jasmina.savic.calendarapp;

public class DurationCalculate {

    static {
        System.loadLibrary("DurationCalculate");
    }

    public native int durationCalculate(int startH, int startM, int endH, int endM);
}
