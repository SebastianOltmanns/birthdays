package com.woodplantation.geburtstagsverwaltung.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.comparators.NextBirthdayComparator;
import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.repository.Repository;
import com.woodplantation.geburtstagsverwaltung.util.DateUtil;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Created by Sebu on 21.10.2019.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
@AndroidEntryPoint
public class WidgetService extends RemoteViewsService {

	@Inject
	Repository repository;

	@Override
	public RemoteViewsFactory onGetViewFactory(Intent intent) {
		return new WidgetFactory(repository, getApplicationContext());
	}

	public static void notifyDataChanged(Context context) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName componentName = new ComponentName(context, WidgetProvider.class);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
		appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.data_list_view);
	}

	static class WidgetFactory implements RemoteViewsService.RemoteViewsFactory {

		private final Repository repository;
		private final Context context;
		private final String lineSeparator = System.getProperty("line.separator") == null ? "\n" : System.getProperty("line.separator");
		private List<Entry> dataList;

		WidgetFactory(Repository repository, Context context) {
			this.repository = repository;
			this.context = context;
		}

		@Override
		public void onCreate() {
		}

		@Override
		public void onDataSetChanged() {
			dataList = repository.getDataSynchronously();
			Collections.sort(dataList, new NextBirthdayComparator());
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
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.viewholder);
			Entry entry = dataList.get(i);

			views.setTextViewText(R.id.viewholder_name, entry.getFullName());
			views.setTextViewText(R.id.viewholder_notes, entry.notes.replaceAll(lineSeparator, " "));
			views.setTextViewText(R.id.viewholder_birthday, DateUtil.getBirthdayString(entry.birthday, entry.ignoreYear));
			views.setTextViewText(R.id.viewholder_remaining, DateUtil.getRemainingWithAge(context, entry.birthday, !entry.ignoreYear));
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
