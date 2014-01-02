package org.calendar;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Events;
import android.util.Log;

public class Calendar extends CordovaPlugin {
	
	public static final String ACTION_GET_EVENTS_CALENDAR = "getCalendarEvents";
	public static final String ACTION_ADD_EVENTS_CALENDAR = "addCalendarEvents";
	public static Context context;
	
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.context = cordova.getActivity();
		try{
			if (ACTION_GET_EVENTS_CALENDAR.equals(action)) {
				this.readAllEventsCalendar(callbackContext);
				return true;
			}else if(ACTION_ADD_EVENTS_CALENDAR.equals(action)) {
				this.addEventsCalendar(callbackContext,args);
				return true;
			}
			return false;
		}catch(Exception e) {
		    System.err.println("Exception: " + e.getMessage());
		    callbackContext.error(e.getMessage());
		    return false;
		} 		
	}
	
	// Read all events from calendar
	public void readAllEventsCalendar(CallbackContext callbackContext)
	{
		Date now = new Date(); 

		Cursor cursor = this.context.getContentResolver()
            .query(
            		Uri.parse("content://com.android.calendar/events"),
		            new String[] { "calendar_id", "title", "description","dtstart", "dtend", "eventLocation","duration", "_id", "original_id", "originalInstanceTime", "rrule"}, 
		            "((dtstart >= "+now.getTime()+") OR (dtend is NULL))"
		            ,null, null);
		 
		 JSONArray listEventsJson = new JSONArray();
		 
		 if(cursor.moveToFirst()){
			 do{
				 HashMap<String, String> eventInfo = new HashMap<String, String>();
				 eventInfo.put("calendar_id",cursor.getString(0));
				 eventInfo.put("title",cursor.getString(1));
				 eventInfo.put("description",cursor.getString(2));
				 eventInfo.put("dtstart",cursor.getString(3));
				 eventInfo.put("dtend",cursor.getString(4));
				 eventInfo.put("eventLocation",cursor.getString(5));
				 eventInfo.put("duration",cursor.getString(6));
				 eventInfo.put("_id",cursor.getString(7));
				 eventInfo.put("original_id",cursor.getString(8));
				 eventInfo.put("originalInstanceTime",cursor.getString(9));
				 eventInfo.put("rrule",cursor.getString(10));
							 
				 JSONObject eventInfoJson = new JSONObject(eventInfo);
				 listEventsJson.put(eventInfoJson);
				 
			 }while(cursor.moveToNext());
			 cursor.close();
		 }
		 callbackContext.success(listEventsJson);		 
	}
	
	// Add event to calendar
	@SuppressLint("NewApi")
	public void addEventsCalendar(CallbackContext callbackContext, JSONArray args) throws JSONException
	{
		JSONObject arg_object = args.getJSONObject(0);
		ContentResolver cr = this.context.getContentResolver();
		ContentValues values = new ContentValues();
		
		List<String[]> listCalendars = this.getCalendars();
		for(String[] calen : listCalendars){
			values.put(Events.DTSTART, arg_object.getLong("startTimeMillis"));
			values.put(Events.DTEND, arg_object.getLong("endTimeMillis"));
			values.put(Events.TITLE, arg_object.getString("title"));
			values.put(Events.DESCRIPTION, arg_object.getString("description"));
			values.put(Events.EVENT_LOCATION, arg_object.getString("location"));
			values.put(Events.CALENDAR_ID, Long.parseLong(calen[0]));
			values.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
				
			Log.e("Time",""+arg_object.getLong("startTimeMillis"));
			Uri uri = cr.insert(Events.CONTENT_URI, values);
		}
		
		callbackContext.success("Success");
	}
	
	// Get all calendar sync
	public List<String[]> getCalendars()
	{
		String projection[] = {"_id", "calendar_displayName"};
		Uri calendars;
        calendars = Uri.parse("content://com.android.calendar/calendars");
        ContentResolver contentResolver = this.context.getContentResolver();
        Cursor managedCursor = contentResolver.query(calendars, projection, null, null, null);
        List<String[]> listCalendars = new ArrayList<String[]>();
        
        if (managedCursor.moveToFirst()){        	
            String calName;
            String calID;
            int cont= 0;
            int nameCol = managedCursor.getColumnIndex(projection[1]);
            int idCol = managedCursor.getColumnIndex(projection[0]);
            do {
                calName = managedCursor.getString(nameCol);
                calID = managedCursor.getString(idCol);
                String[] calendarInfo = new String[2];
				calendarInfo[0] = calID;
				calendarInfo[1] = calName;
				listCalendars.add(calendarInfo);
                cont++;                
            } while(managedCursor.moveToNext());
            managedCursor.close();
        }
        
		return listCalendars;
	}
}
