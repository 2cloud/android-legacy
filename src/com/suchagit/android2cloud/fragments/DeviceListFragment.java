package com.suchagit.android2cloud.fragments;

import com.suchagit.android2cloud.R;

import com.suchagit.android2cloud.TwoCloud;

import android.widget.SimpleCursorAdapter;
import android.app.Activity;
import android.app.ListFragment;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Displays a list of devices. Will display devices from the {@link Uri}
 * provided in the incoming Intent if there is one, otherwise it defaults to displaying the
 * contents of the {@link com.suchagit.android2cloud.providers.DeviceProvider}.
 * 
 * TODO: Get the provider operations off the UI thread. {@link android.content.AsyncQueryHandler} or
 * {@link android.os.AsyncTask} are recommended.
 */
public class DeviceListFragment extends ListFragment {
	
	// For logging and debugging
	private static final String TAG = "DeviceListFragment";
	
	/**
	 * The columns needed by the cursor adapter
	 */
	private static final String[] PROJECTION = new String[] {
		TwoCloud.Devices._ID,					// 0
		TwoCloud.Devices.COLUMN_NAME_USER,		// 1
		TwoCloud.Devices.COLUMN_NAME_NAME,		// 2
		TwoCloud.Devices.COLUMN_NAME_ADDRESS,	// 3
		TwoCloud.Devices.COLUMN_NAME_SELECTED	// 4
	};
	
	/** The index of the name column */
	private static final int COLUMN_INDEX_NAME = 2;
	private static final int COLUMN_INDEX_ID = 0;
	
	// Define colours to use when toggling selected state of devices
	private static final int SELECTED_BG_COLOR = Color.parseColor("#A4C639");
	private static final int SELECTED_TEXT_COLOR = Color.BLACK;
	private static final int UNSELECTED_BG_COLOR = Color.BLACK;
	private static final int UNSELECTED_TEXT_COLOR = Color.LTGRAY;
	
	private SimpleCursorAdapter adapter;
	private Bundle checkedItems;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(savedInstanceState != null && savedInstanceState.getBundle("checked_items") != null) {
			checkedItems = savedInstanceState.getBundle("checked_items");
		} else {
			checkedItems = new Bundle();
		}
		
		/* If no data is given in the Intent that started this Activity, then this Activity
		 * was started when the intent filter matched a MAIN action. We should use the default
		 * provider URI.
		 * 
		 * TODO: Filter based on the last used account, instead of just showing all accounts' devices
		 */
		// Gets the intent that started this Activity
		Intent intent = getActivity().getIntent();
		
		// If there is no data associated with the Intent, sets the data to the default URI, which
		// accesses a list of devices across all users
		if (intent.getData() == null) {
			intent.setData(TwoCloud.Devices.CONTENT_URI);
		}
		
		/*
		 * Sets the callback for context menu activation for the ListView. The listener is set to be
		 * this Fragment. The effect is that the context menus are enabled for items in the ListView,
		 * and the context menu is handled by a method in DeviceListFragment
		 */
		//getListView().setOnCreateContextMenuListener(this);
		
		/* Performs a managed query. The Fragment handles closing and requerying the cursor when needed.
		 * TODO: This really shouldn't be in the UI thread... See opening note.
		 */
		QueryHandler queryHandler = new QueryHandler(this.getActivity());
		queryHandler.startQuery(
				42,
				new Bundle(),
				intent.getData(),					// Use the default content URI for the provider
				PROJECTION,							// Return the data defined in PROJECTION for each note.
				null,								// No where clause, return all records
				null,								// No where clause, therefore no where column values
				TwoCloud.Devices.DEFAULT_SORT_ORDER	// Use the default sort order
		);
		
		/*
		 * The following two arrays create a "map" between columns in the cursor and view IDs
		 * for items in the ListView. Each element in the dataColumns array represents a column
		 * name; each element in the viewID array represents the ID of a View.
		 * The SimpleCursorAdapter maps them in ascending order to determine where each column
		 * value will appear in the ListView.
		 */
		
		// The names of the cursor columns to display in the view, initialised to the name column
		String[] dataColumns = { TwoCloud.Devices.COLUMN_NAME_NAME};
		
		//The view IDs that will display the cursor columns, initialised to the TextView
		int[] viewIDs = { R.id.column_item };
		
		// Creates a backing adapter for the ListView
		adapter = new SimpleCursorAdapter(
				this.getActivity(),
				R.layout.column_item,
				null,
				dataColumns,
				viewIDs
		);
    	adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				long id = cursor.getLong(COLUMN_INDEX_ID);
				boolean checked = checkedItems.getBoolean(id + "");
				Log.e(TAG, "id " + id + ": " + checked);
				((CheckedTextView) view).setChecked(checked);
				if(checked) {
					view.setBackgroundColor(SELECTED_BG_COLOR);
					((CheckedTextView) view).setTextColor(SELECTED_TEXT_COLOR);
				} else {
					view.setBackgroundColor(UNSELECTED_BG_COLOR);
					((CheckedTextView) view).setTextColor(UNSELECTED_TEXT_COLOR);
				}
				((CheckedTextView) view).setText(cursor.getString(COLUMN_INDEX_NAME));
				return true;
			}
		});
		this.setListAdapter(adapter);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.column, container, false);
        ListView list = (ListView) v.findViewById(android.R.id.list);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        list.setItemsCanFocus(false);
        return v;
	}
	
	@Override
	public void onListItemClick(ListView parent, View view, int position, long id) {
		Log.e(TAG, "position: "+position+" id: "+id);
		toggleItem(view, id);
	}
	
	private final void toggleItem(View v, long id) {
		CheckedTextView view = (CheckedTextView) v;
		if(!view.isChecked()) {
			view.setBackgroundColor(SELECTED_BG_COLOR);
			view.setTextColor(SELECTED_TEXT_COLOR);
			view.setChecked(true);
			checkedItems.putBoolean(""+id, true);
		} else {
			view.setBackgroundColor(UNSELECTED_BG_COLOR);
			view.setTextColor(UNSELECTED_TEXT_COLOR);
			view.setChecked(false);
			checkedItems.putBoolean(""+id, false);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBundle("checked_items", checkedItems);
		super.onSaveInstanceState(savedInstanceState);
	}
	

    private final class QueryHandler extends AsyncQueryHandler {
    	Context context;
    	
        public QueryHandler(Context context) {
            super(context.getContentResolver());
        	this.context = context;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (!((Activity) this.context).isFinishing()) {
                adapter.changeCursor(cursor);
            } else {
                cursor.close();
            }
        }
    }
}