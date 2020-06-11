package jasmina.savic.calendarapp;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class EventAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Event> events;
    public EventDbHelper mDbHelper;

    public static final String LOG = "HOWMANYTIMES";

    public EventAdapter(Context mContext) {
        this.mContext = mContext;
        this.events = new ArrayList<Event>();
        mDbHelper = EventDbHelper.getInstance(mContext);
    }

    public void update(Event[] mevents) {
        events.clear();
        if (mevents != null) {
            for (Event event : mevents) {
                events.add(event);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        Object retVal = null;
        try {
            retVal = events.get(position);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        public CheckBox isCompleted;
        public TextView eventName;
        public ImageView bellImage;
        public TextView duration;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.event_element, null);
            ViewHolder holder = new ViewHolder();
            holder.bellImage = (ImageView) view.findViewById(R.id.bell_image);
            holder.eventName = (TextView) view.findViewById(R.id.element_text);
            holder.isCompleted = (CheckBox) view.findViewById(R.id.event_checkbox);
            holder.duration = (TextView) view.findViewById(R.id.element_duration);
            view.setTag(holder);
        }

        final Event event = (Event) getItem(position);
        final ViewHolder holder = (ViewHolder) view.getTag();

        holder.isCompleted.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                event.setCompleted(isChecked);
                if (event.isCompleted() && !holder.eventName.getPaint().isStrikeThruText()) {
                    holder.eventName.setPaintFlags(holder.eventName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.bellImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.bell_empty));
                } else if (holder.eventName.getPaint().isStrikeThruText() && !event.isCompleted()) {
                    holder.eventName.setPaintFlags(holder.eventName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    if (event.isEventReminder())
                        holder.bellImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.bell));
                }
                if(event.isCompleted()) {
                    event.setMinuteChecked(0);
                    event.setEventReminder(false);
                }
                mDbHelper.updateEvent(event);
            }
        });

        holder.eventName.setText(event.getEventName());

        String eventDuration = "";
        int hrs = 0;
        int mins = 0;
        int duration = event.getDuration();
        if(duration >= 60) {
            hrs = duration / 60;
            mins = duration % 60;
            eventDuration += String.valueOf(hrs) + "h " + String.valueOf(mins) + "m";
        } else {
            mins = duration;
            eventDuration += String.valueOf(mins) + "m";
        }
        holder.duration.setText(eventDuration);

        if (event.isEventReminder() && !event.isCompleted())
            holder.bellImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.bell));
        else
            holder.bellImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.bell_empty));

        holder.isCompleted.setChecked(event.isCompleted());

        holder.isCompleted.jumpDrawablesToCurrentState();

        return view;
    }
}
