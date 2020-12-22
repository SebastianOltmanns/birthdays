package com.woodplantation.geburtstagsverwaltung.storage;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Created by Sebu on 10.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public class StorageHandler {

	private Context context;
	public static final String filePath = "data";

	@Inject
	public StorageHandler(@ApplicationContext Context context) {
		this.context = context;
	}

	public void saveData(ArrayList<DataSet> data) {
		try {
			FileOutputStream fos = context.openFileOutput(filePath, Context.MODE_PRIVATE);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ObjectOutputStream output = new ObjectOutputStream(bos);

			output.writeObject(data);

			output.close();
			bos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
		} catch (FileNotFoundException e) {
			return new ArrayList<DataSet>();
		} catch (IOException e) {
			return new ArrayList<DataSet>();
		} catch (ClassNotFoundException e) {
			return new ArrayList<DataSet>();
		}
	}

}
