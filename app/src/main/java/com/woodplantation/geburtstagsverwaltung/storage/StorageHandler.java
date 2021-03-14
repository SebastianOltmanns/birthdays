package com.woodplantation.geburtstagsverwaltung.storage;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Created by Sebu on 10.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
@Deprecated
public class StorageHandler {

	private final Context context;
	public static final String filePath = "data";

	@Inject
	public StorageHandler(@ApplicationContext Context context) {
		this.context = context;
	}

	@Deprecated
	public ArrayList<DataSet> loadData() {
		try {
			FileInputStream fis = context.openFileInput(filePath);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream input = new ObjectInputStream(bis);

			ArrayList<DataSet> result = (ArrayList<DataSet>) input.readObject();

			input.close();
			bis.close();
			fis.close();
			return result;
		} catch (IOException | ClassNotFoundException e) {
			return new ArrayList<>();
		}
	}

}
