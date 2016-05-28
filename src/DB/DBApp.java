package DB;

import java.util.Calendar;
import java.util.Date;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.io.*;

@SuppressWarnings("serial")
class DBAppException extends Exception {

}

@SuppressWarnings("serial")
class DBEngineException extends Exception {

}

public class DBApp {

	ArrayList<Table> tables;
	static String COMMA_DELIMITER;
	static String NEW_LINE_SEPARATOR;
	static String FILE_HEADER;
	static FileWriter fileWriter;
	static BufferedWriter bw;

	/*
	 * public static void writeCsvFile(String table_Name, String column_Name,
	 * String column_Type, boolean key, boolean indexed, String referencese)
	 * throws IOException {
	 * 
	 * String COMMA_DELIMITER = ","; String NEW_LINE_SEPARATOR = "\n"; String
	 * FILE_HEADER =
	 * "Table Name, Column Name, Column Type, Key, Indexed, Referencese";
	 * 
	 * FileWriter fileWriter = null; fileWriter = new
	 * FileWriter("metadata.csv"); fileWriter.append(FILE_HEADER.toString());
	 * fileWriter.append(NEW_LINE_SEPARATOR);
	 * 
	 * fileWriter.append(table_Name); fileWriter.append(COMMA_DELIMITER);
	 * fileWriter.append(column_Name); fileWriter.append(COMMA_DELIMITER);
	 * fileWriter.append(column_Type); fileWriter.append(COMMA_DELIMITER);
	 * fileWriter.append(String.valueOf(key));
	 * fileWriter.append(COMMA_DELIMITER);
	 * fileWriter.append(String.valueOf(indexed));
	 * fileWriter.append(COMMA_DELIMITER); fileWriter.append(referencese);
	 * fileWriter.append(NEW_LINE_SEPARATOR);
	 * 
	 * fileWriter.flush(); fileWriter.close();
	 * 
	 * }
	 */
	// create the meta data. add in the constructor
	public DBApp() {
		tables = new ArrayList<Table>();
		COMMA_DELIMITER = ",";
		NEW_LINE_SEPARATOR = "\n";
		FILE_HEADER = "Table Name, Column Name, Column Type, Key, Indexed, Referencese";
	}

	public void init() throws IOException {
		fileWriter = new FileWriter("data/metadata.csv", true);
		fileWriter.append(FILE_HEADER.toString());
		fileWriter.append(NEW_LINE_SEPARATOR);
		// fileWriter.flush();
		fileWriter.close();
	}

	// writing in the csv file (create table)
	public static void writeCsvFile(String strTableName,
			Hashtable<String, String> htblColNameType,
			Hashtable<String, String> htblColNameRefs, String strKeyColName)
			throws IOException {
		// load
		fileWriter = new FileWriter("data/metadata.csv", true);

		// write and loop
		for (String key : htblColNameType.keySet()) {
			// bw = new BufferedWriter(fileWriter);
			fileWriter.append((strTableName));
			fileWriter.append((COMMA_DELIMITER));
			fileWriter.append(key);
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(htblColNameType.get(key));
			fileWriter.append(COMMA_DELIMITER);
			if (strKeyColName.equals(key))
				fileWriter.append(String.valueOf(true));
			else
				fileWriter.append(String.valueOf(false));
			fileWriter.append(COMMA_DELIMITER);
			fileWriter.append(String.valueOf(false));
			fileWriter.append(COMMA_DELIMITER);
			// compare ref with names
			for (String key1 : htblColNameRefs.keySet()) {
				if (htblColNameType.get(key).equals(htblColNameRefs.get(key1))) {
					fileWriter.append(htblColNameRefs.get(key1));
					// break;
				} else
					fileWriter.append("null");

			}

			// bw.flush();
			fileWriter.append(NEW_LINE_SEPARATOR);
		}
		// close
		fileWriter.flush();
		// don't work
		fileWriter.close();

	}

	public void createTable(String strTableName,
			Hashtable<String, String> htblColNameType,
			Hashtable<String, String> htblColNameRefs, String strKeyColName)
			throws DBAppException, ClassNotFoundException, IOException {
		Table new_table = new Table(strTableName, htblColNameType,
				htblColNameRefs, strKeyColName);
		tables.add(new_table);
		writeCsvFile(strTableName, htblColNameType, htblColNameRefs,
				strKeyColName);
		BTree primary_key = new BTree(strTableName+strKeyColName);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("classes/"+strTableName+strKeyColName+".class")));
		oos.writeObject(primary_key);
		oos.close();		

	}

	public void createIndex(String strTableName, String strColName)
			throws DBAppException, FileNotFoundException, IOException, ClassNotFoundException {
		Table temp =  getTable(strTableName);
		BTree new_index = new BTree (strTableName+strColName);
		temp.indices.add(strColName);
		for(int i = 0 ;i<temp.noOfPages-1;i++){
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("classes/"
					+strTableName+i+".class")));
		          Page 	t = (Page)ois.readObject();
		          for(int j = 0;j<200;j++){
		         Hashtable<String,Object> record =(Hashtable<String,Object>) t.information[j];
		        if( record!=null)
		        	new_index.insert((String)record.get(strTableName),record.get("record"));
		        	
		          }
		}
		
		
		
		
		
		temp.indices.add(new_index.name);
		new_index.save_Tree();

	}

	public Table getTable(String strTableName) {

		for (int i = 0; i < tables.size(); i++) {
			if (tables.get(i).strTableName.equals(strTableName)) {
				return tables.get(i);
			}
		}
		return null;
	}

	/*
	 * public void insertIntoTable(String strTableName, Hashtable<String,
	 * Object> htblColNameValue) throws DBAppException, ClassNotFoundException,
	 * IOException { for (int i = 0; i < tables.size(); i++) { if
	 * (tables.get(i).getStrTableName().equals(strTableName)) { Table temp =
	 * tables.get(i); temp.insert(htblColNameValue); } } }
	 */
	public void insertIntoTable(String strTableName,
			Hashtable<String, Object> htblColNameValue) throws DBAppException,
			ClassNotFoundException, IOException {
		Table temp = getTable(strTableName);
		temp.insert(strTableName, htblColNameValue);
		
	}

	public void updateTable(String strTableName, String strKey,
			Hashtable<String, Object> htblColNameValue) throws DBAppException,
			FileNotFoundException, ClassNotFoundException, IOException {
		Table Update = getTable(strTableName);
		Update.UpdateTable(strKey, htblColNameValue);

	}
	public void deleteFromTable(String strTableName,
			Hashtable<String, Object> htblColNameValue, String strOperator)
			throws DBEngineException, FileNotFoundException,
			ClassNotFoundException, IOException {
		Table temp = this.getTable(strTableName); //Temp is the table we are looking for
		ArrayList<String> trees=temp.indices;
		if(htblColNameValue.size()==1){
			 for(String key : htblColNameValue.keySet())
			 {
				 if(trees.contains(key))
						 temp.removeOne((htblColNameValue.get(key)+""),key);
				 else
					 temp.removeAll(htblColNameValue, strOperator);
			 }
		return ;
		}
		//The list of BPlusTrees in this table
		//IF OR
//		if (strOperator.equals("OR")){
//			//Loop on the htblColNameValue to see if they all have trees
//			//If one doesn't have a tree, call removeAll. Else, call removeB on all the trees
//			for (String key : htblColNameValue.keySet()){
//				if(!trees.contains(key))
//				{
//				temp.removeAll(htblColNameValue, strOperator);
//				return;
//				}
//			}
//			for (String key : htblColNameValue.keySet()){
//			temp.removefromTable(key, (String) htblColNameValue.get(key));		
//			}
//		}
		// IF AND
//		else{
//			//Loop on the htblColNameValue to see if they all have trees
//			//If one have a tree, call removeAND. Else, call removeAll
//			for (String key : htblColNameValue.keySet()){
//				if(trees.contains(key))
//				{
//				temp.removeAND(htblColNameValue);
//				return;
//				}
//			}
			temp.removeAll(htblColNameValue, strOperator);
	
		}
		
//		ArrayList<String> trees=temp.trees; //The list of BPlusTrees in this table
//		//If this column has a tree, use removefromTable. Else, use remove
//		for (String key : htblColNameValue.keySet()){
//			if(trees.contains(key))
//			{
//			temp.removefromTable(key, (String) htblColNameValue.get(key));
//			//Remove this column from the hashtable.
//			htblColNameValue.remove(key);
//			}
//		}
//		temp.remove(htblColNameValue, strOperator);

	
//	public void deleteFromTable(String strTableName,
//			Hashtable<String, Object> htblColNameValue, String strOperator)
//			throws DBEngineException, FileNotFoundException,
//			ClassNotFoundException, IOException {
//
//		Table temp = this.getTable(strTableName);
//		temp.removeall(htblColNameValue, strOperator);
//
//	}

//	@SuppressWarnings("rawtypes")
//	public Iterator selectFromTable(String strTable,
//			Hashtable<String, Object> htblColNameValue, String strOperator)
//			throws DBEngineException, FileNotFoundException,
//			ClassNotFoundException, IOException {
//		for (int i = 0; i < tables.size(); i++) {
//			if (tables.get(i).getStrTableName().equals(strTable)) {
//				Table temp = tables.get(i);
//				if (strOperator.equals("OR"))
//					return (temp.getRecordAll(true, htblColNameValue)).iterator();
//				else
//					return (temp.getRecordAll(false, htblColNameValue)).iterator();
//			}
//		}
//		return null;
//
//	}
	public Iterator selectFromTable(String strTable,
			Hashtable<String, Object> htblColNameValue, String strOperator)
			throws DBEngineException, FileNotFoundException,
			ClassNotFoundException, IOException {
		Table temp =getTable(strTable);
//		for (int i = 0; i < tables.size(); i++) {
//			if (tables.get(i).getStrTableName().equals(strTable)) {
//				temp = tables.get(i); 	//Temp is the table we are looking for
//			}
//		}
		
		ArrayList<String> trees=temp.indices; //The list of BPlusTrees in this table
		//IF OR
		if (strOperator.equals("OR")){
			//Loop on the htblColNameValue to see if they all have trees
			//If one doesn't have a tree, call getRecordAll. Else, call getRecordB
			for (String key : htblColNameValue.keySet()){
				if(!trees.contains(key))
				{
				return (temp.getRecordAll(true, htblColNameValue)).iterator();
				}
			}
			return (temp.getRecordB(true, htblColNameValue)).iterator();
		}
		// IF AND
		else{
			//Loop on the htblColNameValue to see if they all have trees
			//If one have a tree, call getRecordB. Else, call getRecordAll
			for (String key : htblColNameValue.keySet()){
				if(trees.contains(key))
				{
				return (temp.getRecordB(false, htblColNameValue)).iterator();	
				}
			}
			return (temp.getRecordAll(false, htblColNameValue)).iterator();
	
		}

	}
	@SuppressWarnings({ "unused", "rawtypes" })
	/*
	 * public static void main(String[] args) throws DBAppException,
	 * DBEngineException, ClassNotFoundException, IOException { // create a new
	 * DBApp DBApp myDB = new DBApp();
	 * 
	 * // initialize it myDB.init();
	 * 
	 * // creating table "Faculty"
	 * 
	 * Hashtable<String, String> fTblColNameType = new Hashtable<String,
	 * String>(); fTblColNameType.put("ID", "Integer");
	 * fTblColNameType.put("Name", "String");
	 * 
	 * Hashtable<String, String> fTblColNameRefs = new Hashtable<String,
	 * String>();
	 * 
	 * myDB.createTable("Faculty", fTblColNameType, fTblColNameRefs, "ID");
	 * 
	 * // creating table "Major"
	 * 
	 * Hashtable<String, String> mTblColNameType = new Hashtable<String,
	 * String>(); fTblColNameType.put("ID", "Integer");
	 * fTblColNameType.put("Name", "String"); fTblColNameType.put("Faculty_ID",
	 * "Integer");
	 * 
	 * Hashtable<String, String> mTblColNameRefs = new Hashtable<String,
	 * String>(); mTblColNameRefs.put("Faculty_ID", "Faculty.ID");
	 * 
	 * myDB.createTable("Major", mTblColNameType, mTblColNameRefs, "ID");
	 * 
	 * // creating table "Course"
	 * 
	 * Hashtable<String, String> coTblColNameType = new Hashtable<String,
	 * String>(); coTblColNameType.put("ID", "Integer");
	 * coTblColNameType.put("Name", "String"); coTblColNameType.put("Code",
	 * "String"); coTblColNameType.put("Hours", "Integer");
	 * coTblColNameType.put("Semester", "Integer");
	 * coTblColNameType.put("Major_ID", "Integer");
	 * 
	 * Hashtable<String, String> coTblColNameRefs = new Hashtable<String,
	 * String>(); coTblColNameRefs.put("Major_ID", "Major.ID");
	 * 
	 * myDB.createTable("Course", coTblColNameType, coTblColNameRefs, "ID");
	 * 
	 * // creating table "Student"
	 * 
	 * Hashtable<String, String> stTblColNameType = new Hashtable<String,
	 * String>(); stTblColNameType.put("ID", "Integer");
	 * stTblColNameType.put("First_Name", "String");
	 * stTblColNameType.put("Last_Name", "String"); stTblColNameType.put("GPA",
	 * "Double"); stTblColNameType.put("Age", "Integer");
	 * 
	 * Hashtable<String, String> stTblColNameRefs = new Hashtable<String,
	 * String>();
	 * 
	 * myDB.createTable("Student", stTblColNameType, stTblColNameRefs, "ID");
	 * 
	 * // creating table "Student in Course"
	 * 
	 * Hashtable<String, String> scTblColNameType = new Hashtable<String,
	 * String>(); scTblColNameType.put("ID", "Integer");
	 * scTblColNameType.put("Student_ID", "Integer");
	 * scTblColNameType.put("Course_ID", "Integer");
	 * 
	 * Hashtable<String, String> scTblColNameRefs = new Hashtable<String,
	 * String>(); scTblColNameRefs.put("Student_ID", "Student.ID");
	 * scTblColNameRefs.put("Course_ID", "Course.ID");
	 * 
	 * myDB.createTable("Student_in_Course", scTblColNameType, scTblColNameRefs,
	 * "ID");
	 * 
	 * // insert in table "Faculty"
	 * 
	 * Hashtable<String, Object> ftblColNameValue1 = new Hashtable<String,
	 * Object>(); ftblColNameValue1.put("ID", Integer.valueOf("1"));
	 * ftblColNameValue1.put("Name", "Media Engineering and Technology");
	 * myDB.insertIntoTable("Faculty", ftblColNameValue1);
	 * 
	 * Hashtable<String, Object> ftblColNameValue2 = new Hashtable<String,
	 * Object>(); ftblColNameValue2.put("ID", Integer.valueOf("2"));
	 * ftblColNameValue2.put("Name", "Management Technology");
	 * myDB.insertIntoTable("Faculty", ftblColNameValue2);
	 * 
	 * for (int i = 0; i < 1000; i++) { Hashtable<String, Object>
	 * ftblColNameValueI = new Hashtable<String, Object>();
	 * ftblColNameValueI.put("ID", Integer.valueOf(("" + (i + 2))));
	 * ftblColNameValueI.put("Name", "f" + (i + 2));
	 * myDB.insertIntoTable("Faculty", ftblColNameValueI); }
	 * 
	 * // insert in table "Major"
	 * 
	 * Hashtable<String, Object> mtblColNameValue1 = new Hashtable<String,
	 * Object>(); mtblColNameValue1.put("ID", Integer.valueOf("1"));
	 * mtblColNameValue1.put("Name", "Computer Science & Engineering");
	 * mtblColNameValue1.put("Faculty_ID", Integer.valueOf("1"));
	 * myDB.insertIntoTable("Major", mtblColNameValue1);
	 * 
	 * Hashtable<String, Object> mtblColNameValue2 = new Hashtable<String,
	 * Object>(); mtblColNameValue2.put("ID", Integer.valueOf("2"));
	 * mtblColNameValue2.put("Name", "Business Informatics");
	 * mtblColNameValue2.put("Faculty_ID", Integer.valueOf("2"));
	 * myDB.insertIntoTable("Major", mtblColNameValue2);
	 * 
	 * for (int i = 0; i < 1000; i++) { Hashtable<String, Object>
	 * mtblColNameValueI = new Hashtable<String, Object>();
	 * mtblColNameValueI.put("ID", Integer.valueOf(("" + (i + 2))));
	 * mtblColNameValueI.put("Name", "m" + (i + 2)); mtblColNameValueI
	 * .put("Faculty_ID", Integer.valueOf(("" + (i + 2))));
	 * myDB.insertIntoTable("Major", mtblColNameValueI); }
	 * 
	 * // insert in table "Course"
	 * 
	 * Hashtable<String, Object> ctblColNameValue1 = new Hashtable<String,
	 * Object>(); ctblColNameValue1.put("ID", Integer.valueOf("1"));
	 * ctblColNameValue1.put("Name", "Data Bases II");
	 * ctblColNameValue1.put("Code", "CSEN 604"); ctblColNameValue1.put("Hours",
	 * Integer.valueOf("4")); ctblColNameValue1.put("Semester",
	 * Integer.valueOf("6")); ctblColNameValue1.put("Major_ID",
	 * Integer.valueOf("1")); myDB.insertIntoTable("Course", mtblColNameValue1);
	 * 
	 * Hashtable<String, Object> ctblColNameValue2 = new Hashtable<String,
	 * Object>(); ctblColNameValue2.put("ID", Integer.valueOf("1"));
	 * ctblColNameValue2.put("Name", "Data Bases II");
	 * ctblColNameValue2.put("Code", "CSEN 604"); ctblColNameValue2.put("Hours",
	 * Integer.valueOf("4")); ctblColNameValue2.put("Semester",
	 * Integer.valueOf("6")); ctblColNameValue2.put("Major_ID",
	 * Integer.valueOf("2")); myDB.insertIntoTable("Course", mtblColNameValue2);
	 * 
	 * for (int i = 0; i < 1000; i++) { Hashtable<String, Object>
	 * ctblColNameValueI = new Hashtable<String, Object>();
	 * ctblColNameValueI.put("ID", Integer.valueOf(("" + (i + 2))));
	 * ctblColNameValueI.put("Name", "c" + (i + 2));
	 * ctblColNameValueI.put("Code", "co " + (i + 2));
	 * ctblColNameValueI.put("Hours", Integer.valueOf("4"));
	 * ctblColNameValueI.put("Semester", Integer.valueOf("6"));
	 * ctblColNameValueI.put("Major_ID", Integer.valueOf(("" + (i + 2))));
	 * myDB.insertIntoTable("Course", ctblColNameValueI); }
	 * 
	 * // insert in table "Student"
	 * 
	 * for (int i = 0; i < 1000; i++) { Hashtable<String, Object>
	 * sttblColNameValueI = new Hashtable<String, Object>();
	 * sttblColNameValueI.put("ID", Integer.valueOf(("" + i)));
	 * sttblColNameValueI.put("First_Name", "FN" + i);
	 * sttblColNameValueI.put("Last_Name", "LN" + i);
	 * sttblColNameValueI.put("GPA", Double.valueOf("0.7"));
	 * sttblColNameValueI.put("Age", Integer.valueOf("20"));
	 * myDB.insertIntoTable("Student", sttblColNameValueI); // changed it to
	 * student instead of course } ObjectInputStream oos = new
	 * ObjectInputStream(new FileInputStream(new File("Student0"+".class")));
	 * Page p = (Page)oos.readObject(); for(int i = 0;i<p.getMaxRows();i++)
	 * System.out.println(p.getInformation()[i]); oos.close();
	 * 
	 * // selecting
	 * 
	 * Hashtable<String, Object> stblColNameValue = new Hashtable<String,
	 * Object>(); stblColNameValue.put("ID", Integer.valueOf("550"));
	 * stblColNameValue.put("Age", Integer.valueOf("20"));
	 * 
	 * long startTime = System.currentTimeMillis(); Iterator myIt = myDB
	 * .selectFromTable("Student", stblColNameValue, "AND"); long endTime =
	 * System.currentTimeMillis(); long totalTime = endTime - startTime;
	 * System.out.println(totalTime); while (myIt.hasNext()) {
	 * System.out.println(myIt.next()); }
	 * 
	 * // feel free to add more tests Hashtable<String, Object>
	 * stblColNameValue3 = new Hashtable<String, Object>();
	 * stblColNameValue3.put("Name", "m7"); stblColNameValue3.put("Faculty_ID",
	 * Integer.valueOf("7"));
	 * 
	 * long startTime2 = System.currentTimeMillis(); Iterator myIt2 = myDB
	 * .selectFromTable("Major", stblColNameValue3, "AND"); long endTime2 =
	 * System.currentTimeMillis(); long totalTime2 = endTime - startTime;
	 * System.out.println(totalTime2); while (myIt2.hasNext()) {
	 * System.out.println(myIt.next()); } }
	 */
	public static void main(String[] args) throws Exception {
//		 ObjectInputStream ois2 = new ObjectInputStream(new
//				  FileInputStream(new File("classes/StudentID"+ ".class"))); 
//				  BTree t2 = (BTree)ois2.readObject(); 
//				  ois2.close();
//				  
//				 System.out.println(t2.toString());
				  
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// get current date time with Date()
		Date date = new Date();
		System.out.println(dateFormat.format(date));

		// get current date time with Calendar()
		// Calendar cal = Calendar.getInstance();
		// System.out.println(dateFormat.format(cal.getTime()));

		// creat a new DBApp
		DBApp myDB = new DBApp();

		// initialize it
		try {
			myDB.init();
		} catch (Exception e) {
			System.err.println("Could not Start Application");
			e.printStackTrace();
		}
//
//		// creating table "Faculty"

		Hashtable<String, String> fTblColNameType = new Hashtable<String, String>();
		fTblColNameType.put("ID", "Integer");
		fTblColNameType.put("Name", "String");

		Hashtable<String, String> fTblColNameRefs = new Hashtable<String, String>();

		myDB.createTable("Faculty", fTblColNameType, fTblColNameRefs, "ID");

		// creating table "Major"

		Hashtable<String, String> mTblColNameType = new Hashtable<String, String>(); // bug
																						// cost
																						// me
																						// 3.5
																						// hours
		mTblColNameType.put("ID", "Integer");
		mTblColNameType.put("Name", "String");
		mTblColNameType.put("Faculty_ID", "Integer");

		Hashtable<String, String> mTblColNameRefs = new Hashtable<String, String>();
		mTblColNameRefs.put("Faculty_ID", "Faculty.ID");

		myDB.createTable("Major", mTblColNameType, mTblColNameRefs, "ID");

		// creating table "Course"

		Hashtable<String, String> coTblColNameType = new Hashtable<String, String>();
		coTblColNameType.put("ID", "Integer");
		coTblColNameType.put("Name", "String");
		coTblColNameType.put("Code", "String");
		coTblColNameType.put("Hours", "Integer");
		coTblColNameType.put("Semester", "Integer");
		coTblColNameType.put("Major_ID", "Integer");

		Hashtable<String, String> coTblColNameRefs = new Hashtable<String, String>();
		coTblColNameRefs.put("Major_ID", "Major.ID");

		myDB.createTable("Course", coTblColNameType, coTblColNameRefs, "ID");

		// creating table "Student"

		Hashtable<String, String> stTblColNameType = new Hashtable<String, String>();
		stTblColNameType.put("ID", "Integer");
		stTblColNameType.put("First_Name", "String");
		stTblColNameType.put("Last_Name", "String");
		stTblColNameType.put("GPA", "Double");
		stTblColNameType.put("Age", "Integer");

		Hashtable<String, String> stTblColNameRefs = new Hashtable<String, String>();

		myDB.createTable("Student", stTblColNameType, stTblColNameRefs, "ID");

		// creating table "Student in Course"

		Hashtable<String, String> scTblColNameType = new Hashtable<String, String>();
		scTblColNameType.put("ID", "Integer");
		scTblColNameType.put("Student_ID", "Integer");
		scTblColNameType.put("Course_ID", "Integer");

		Hashtable<String, String> scTblColNameRefs = new Hashtable<String, String>();
		scTblColNameRefs.put("Student_ID", "Student.ID");
		scTblColNameRefs.put("Course_ID", "Course.ID");

		myDB.createTable("Student_in_Course", scTblColNameType,
				scTblColNameRefs, "ID");

		// insert in table "Faculty"

		Hashtable<String, Object> ftblColNameValue1 = new Hashtable<String, Object>();
		ftblColNameValue1.put("ID", Integer.valueOf("1"));
		ftblColNameValue1.put("Name", "Media Engineering and Technology");
		myDB.insertIntoTable("Faculty", ftblColNameValue1);

		Hashtable<String, Object> ftblColNameValue2 = new Hashtable<String, Object>();
		ftblColNameValue2.put("ID", Integer.valueOf("2"));
		ftblColNameValue2.put("Name", "Management Technology");
		myDB.insertIntoTable("Faculty", ftblColNameValue2);

		for (int i = 0; i < 1000; i++) {
			Hashtable<String, Object> ftblColNameValueI = new Hashtable<String, Object>();
			ftblColNameValueI.put("ID", Integer.valueOf(("" + (i + 2))));
			ftblColNameValueI.put("Name", "f" + (i + 2));
			myDB.insertIntoTable("Faculty", ftblColNameValueI);
		}

		// insert in table "Major"

		Hashtable<String, Object> mtblColNameValue1 = new Hashtable<String, Object>();
		mtblColNameValue1.put("ID", Integer.valueOf("1"));
		mtblColNameValue1.put("Name", "Computer Science & Engineering");
		mtblColNameValue1.put("Faculty_ID", Integer.valueOf("1"));
		myDB.insertIntoTable("Major", mtblColNameValue1);

		Hashtable<String, Object> mtblColNameValue2 = new Hashtable<String, Object>();
		mtblColNameValue2.put("ID", Integer.valueOf("2"));
		mtblColNameValue2.put("Name", "Business Informatics");
		mtblColNameValue2.put("Faculty_ID", Integer.valueOf("2"));
		myDB.insertIntoTable("Major", mtblColNameValue2);

		for (int i = 0; i < 1000; i++) {
			Hashtable<String, Object> mtblColNameValueI = new Hashtable<String, Object>();
			mtblColNameValueI.put("ID", Integer.valueOf(("" + (i + 2))));
			mtblColNameValueI.put("Name", "m" + (i + 2));
			mtblColNameValueI
					.put("Faculty_ID", Integer.valueOf(("" + (i + 2))));
			myDB.insertIntoTable("Major", mtblColNameValueI);
		}

		// insert in table "Course"

		Hashtable<String, Object> ctblColNameValue1 = new Hashtable<String, Object>();
		ctblColNameValue1.put("ID", Integer.valueOf("1"));
		ctblColNameValue1.put("Name", "Data Bases II");
		ctblColNameValue1.put("Code", "CSEN 604");
		ctblColNameValue1.put("Hours", Integer.valueOf("4"));
		ctblColNameValue1.put("Semester", Integer.valueOf("6"));
		ctblColNameValue1.put("Major_ID", Integer.valueOf("1"));
		myDB.insertIntoTable("Course", ctblColNameValue1);

		Hashtable<String, Object> ctblColNameValue2 = new Hashtable<String, Object>();
		ctblColNameValue2.put("ID", Integer.valueOf("1"));
		ctblColNameValue2.put("Name", "Data Bases II");
		ctblColNameValue2.put("Code", "CSEN 604");
		ctblColNameValue2.put("Hours", Integer.valueOf("4"));
		ctblColNameValue2.put("Semester", Integer.valueOf("6"));
		ctblColNameValue2.put("Major_ID", Integer.valueOf("2"));
		myDB.insertIntoTable("Course", ctblColNameValue2);

		for (int i = 0; i < 1000; i++) {
			Hashtable<String, Object> ctblColNameValueI = new Hashtable<String, Object>();
			ctblColNameValueI.put("ID", Integer.valueOf(("" + (i + 2))));
			ctblColNameValueI.put("Name", "c" + (i + 2));
			ctblColNameValueI.put("Code", "co " + (i + 2));
			ctblColNameValueI.put("Hours", Integer.valueOf("4"));
			ctblColNameValueI.put("Semester", Integer.valueOf("6"));
			ctblColNameValueI.put("Major_ID", Integer.valueOf(("" + (i + 2))));
			myDB.insertIntoTable("Course", ctblColNameValueI);
		}

		// insert in table "Student"

		for (int i = 0; i < 1000; i++) {
			Hashtable<String, Object> sttblColNameValueI = new Hashtable<String, Object>();
			sttblColNameValueI.put("ID", Integer.valueOf(("" + i)));
			sttblColNameValueI.put("First_Name", "FN" + i);
			sttblColNameValueI.put("Last_Name", "LN" + i);
			sttblColNameValueI.put("GPA", Double.valueOf("0.7"));
			sttblColNameValueI.put("Age", Integer.valueOf("20"));
			myDB.insertIntoTable("Student", sttblColNameValueI);
			// changed it to student instead of course
		}

		// selecting

		Hashtable<String, Object> stblColNameValuetest = new Hashtable<String, Object>();
		stblColNameValuetest.put("ID", Integer.valueOf("550"));

		Hashtable<String, Object> stblColNameValue = new Hashtable<String, Object>();
		stblColNameValue.put("ID", Integer.valueOf("550"));
		stblColNameValue.put("Age", Integer.valueOf("20"));

		long startTime = System.currentTimeMillis();
		myDB.deleteFromTable("Student",stblColNameValuetest, "AND");
		Iterator myIt = myDB
				.selectFromTable("Student", stblColNameValue, "OR");
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println(totalTime);
		while (myIt.hasNext()) {
			System.out.println(myIt.next());
		}

		// feel free to add more tests
		Hashtable<String, Object> stblColNameValue3 = new Hashtable<String, Object>();
		stblColNameValue3.put("Name", "m7");
		stblColNameValue3.put("Faculty_ID", Integer.valueOf("7"));

		long startTime2 = System.currentTimeMillis();
		myDB.deleteFromTable("Major", stblColNameValue3,"AND");
		Iterator myIt2 = myDB
				.selectFromTable("Major", stblColNameValue3, "AND");
		long endTime2 = System.currentTimeMillis();
		long totalTime2 = endTime - startTime;
		System.out.println(totalTime2);
		while (myIt2.hasNext()) {
			System.out.println(myIt2.next());
		}
		Hashtable<String, Object> stblColNameValue4 = new Hashtable<String, Object>();
		stblColNameValue3.put("First_Name", "m9");
		myDB.updateTable("Student", "32",stblColNameValue3);
		//stblColNameValue3.put("Faculty_ID", Integer.valueOf("20"));
		//myDB.deleteFromTable("Student",stblColNameValue3,"AND");
	/*	Iterator myIt3 = myDB
				.selectFromTable("Major", stblColNameValue3, "AND");
		long endTime3 = System.currentTimeMillis();
		long totalTime3 = endTime - startTime;
		System.out.println(totalTime2);
		while (myIt2.hasNext()) {
			System.out.println(myIt2.next());
		}
		*/
		
		  ObjectInputStream ois3 = new ObjectInputStream(new
		  FileInputStream(new File("classes/Student0"+ ".class"))); 
		   Page p = (Page)ois3.readObject();
		  
		  for(int i = 0;i<200;i++)
			 System.out.println(p.information[i]);
   
		  startTime = System.currentTimeMillis();
			stblColNameValuetest.put("ID",Integer.valueOf("551"));
			 myIt = myDB
					.selectFromTable("Student", stblColNameValuetest, "OR");
			 endTime = System.currentTimeMillis();
			 totalTime = endTime - startTime;
			System.out.println(totalTime);
			while (myIt.hasNext()) {
				System.out.println(myIt.next());
			}
		 
	}
}
