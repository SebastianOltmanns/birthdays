package com.woodplantation.geburtstagsverwaltung.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.ListView;
import android.widget.RemoteViews;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.adapter.DataListViewAdapter;
import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.storage.StorageHandler;

import java.util.ArrayList;

/**
 * Created by Sebu on 19.10.2019.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
public class WidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		ArrayList<DataSet> data = new StorageHandler(context).loadData();
		DataListViewAdapter dataListViewAdapter = new DataListViewAdapter(context, R.layout.data_list_view_item);
		for (int appWidgetId : appWidgetIds) {
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
			views.setRemoteAdapter(R.id.data_list_view, );
		}
	}

	@Override
	public void onEnabled(Context context) {

	}

	@Override
	public void onDisabled(Context context) {

	}

}
