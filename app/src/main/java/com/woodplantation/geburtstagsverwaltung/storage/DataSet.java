package com.woodplantation.geburtstagsverwaltung.storage;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Sebu on 10.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
@Deprecated
public class DataSet implements Serializable {

	static final long serialVersionUID =-2227872867228907805L;

	public int id;
	public Calendar birthday;
	public String firstName;
	public String lastName;
	public String others;

}
