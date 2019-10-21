package com.woodplantation.geburtstagsverwaltung.widget;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.storage.StorageHandler;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Sebu on 21.10.2019.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
public class WidgetService extends RemoteViewsService {

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new WidgetFactory(this.getApplicationContext());
	}

	class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {

		private Context context;
		private ArrayList<DataSet> dataList;

		WidgetFactory(Context context) {
			this.context = context;
		}

		@Override
		public void onCreate() {
		}

		@Override
		public void onDataSetChanged() {
			dataList = new StorageHandler(context).loadData();
		}

		@Override
		public void onDestroy() {

		}

		@Override
		public int getCount() {
			return dataList.size();
		}

		@Override
		public RemoteViews getViewAt(int i) {
			Log.d("widgetservice", "getviewat " + i);
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.data_list_view_item);
			DataSet dataSet = dataList.get(i);

			String remaining = dataSet.getRemainingWithAge(context);
			String firstname = dataSet.firstName;
			String lastname = dataSet.lastName;
			String others = dataSet.others;
			others = others.replaceAll(System.getProperty("line.separator"), " ");
			Calendar birthday = dataSet.birthday;

			String name;
			if (TextUtils.isEmpty(firstname)) {
				name = lastname;
			} else if (TextUtils.isEmpty(lastname)) {
				name = firstname;
			} else {
				name = firstname + " " + lastname;
			}

			Log.d("widgetservice", "data... " + remaining + name);
			views.setTextViewText(R.id.data_list_view_text_name, name);
			views.setTextViewText(R.id.data_list_view_text_others, others);
			views.setTextViewText(R.id.data_list_view_text_birthday, DateFormat.getDateInstance(DateFormat.MEDIUM).format(birthday.getTime()));
			views.setTextViewText(R.id.data_list_view_text_remaining, remaining);
			Log.d("widgetservice", "views: " + views);
			return views;
		}

		@Override
		public RemoteViews getLoadingView() {
			return null;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}
	}
}
