package com.woodplantation.geburtstagsverwaltung.notifications;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Sebu on 22.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public class IdGenerator {

	private static final String filePath = "id";

	public static int getNewId(Context context) {
		int id = readNewId(context);
		return writeNewId(context, id);
	}

	private static int readNewId(Context context) {
		int id;
		try {
			FileInputStream fis = context.openFileInput(filePath);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream input = new ObjectInputStream(bis);

			id = (int) input.readObject();

			input.close();
			bis.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return 1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
		return id;
	}

	private static int writeNewId(Context context, int id) {
		if (id == -1) {
			return id;
		}
		try {
			FileOutputStream fos = context.openFileOutput(filePath, Context.MODE_PRIVATE);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ObjectOutputStream output = new ObjectOutputStream(bos);

			output.writeObject(id+1);

			output.close();
			bos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return id;
	}

}
