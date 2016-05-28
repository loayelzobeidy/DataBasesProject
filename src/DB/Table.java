package DB;

import java.util.Calendar;
import java.util.Date;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.text.DateFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class Table {
	public String getStrTableName() {
		return strTableName;
	}

	public void setStrTableName(String strTableName) {
		this.strTableName = strTableName;
	}

	public Hashtable<String, String> getHtblColNameType() {
		return htblColNameType;
	}

	public void setHtblColNameType(Hashtable<String, String> htblColNameType) {
		this.htblColNameType = htblColNameType;
	}

	public Hashtable<String, String> getHtblColNameRefs() {
		return htblColNameRefs;
	}

	public void setHtblColNameRefs(Hashtable<String, String> htblColNameRefs) {
		this.htblColNameRefs = htblColNameRefs;
	}

	public String getStrKeyColName() {
		return strKeyColName;
	}

	public void setStrKeyColName(String strKeyColName) {
		this.strKeyColName = strKeyColName;
	}

	public ArrayList<String> getPages() {
		return pages;
	}

	public void setPages(ArrayList<String> pages) {
		this.pages = pages;
	}

	public int getNoOfPages() {
		return noOfPages;
	}

	public void setNoOfPages(int noOfPages) {
		this.noOfPages = noOfPages;
	}

	String strTableName;
	Hashtable<String, String> htblColNameType;
	Hashtable<String, String> htblColNameRefs;
	String strKeyColName;
	ArrayList<String> pages;
	int noOfPages;
	int record;
	ArrayList<String> indices;

	public Table(String strTableName,
			Hashtable<String, String> htblColNameType,
			Hashtable<String, String> htblColNameRefs, String strKeyColName)
			throws ClassNotFoundException, IOException {
		this.strTableName = strTableName;
		this.htblColNameType = htblColNameType;
		this.htblColNameRefs = htblColNameRefs;
		this.strKeyColName = strKeyColName;
		this.pages = new ArrayList<String>();
		this.noOfPages = -1;
		this.indices = new ArrayList<String>();
		indices.add(strKeyColName);
		createPage();
	}

	public void createPage() throws IOException, ClassNotFoundException {
		noOfPages++;
		Page thisp = new Page(200, null, (strTableName + noOfPages + ""));
		pages.add(thisp.getId());
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				new File("classes/" + strTableName + (noOfPages) + ".class")));
		ois.close();

	}

	public void removeAll(Hashtable<String, Object> htblColNameValue,
			String strOperator) throws FileNotFoundException,
			ClassNotFoundException, IOException {
		boolean or = strOperator.equals("OR");
		try {
			for (int i = 0; i < pages.size() - 1; i++) {

				ObjectInputStream ois = new ObjectInputStream(
						new FileInputStream("classes/"
								+ new File((pages.get(i) + ".class"))));
				Page p = (Page) ois.readObject();
				ois.close();

				for (int j = 0; j < 200; j++) {
					Hashtable<String, Object> temp = (Hashtable<String, Object>) p
							.getInformation()[j];
					// System.out.println(Input);
					if (!or) {
						// System.out.println(values.size());
						if (checkAND(
								(Hashtable<String, Object>) p.information[j],
								htblColNameValue)) {
							System.out.println("classes/"
									+ new File((pages.get(i) + ".class")));
							p.information[j] = null;
							// System.out.println(p.getInformation()[j]+"delete");
						}
					} else if (checkOR(temp, htblColNameValue))
						p.getInformation()[j] = null;

				}
				ObjectOutputStream oos = new ObjectOutputStream(
						new FileOutputStream(new File("classes/" + p.getId()
								+ ".class")));
				oos.writeObject(p);
				oos.close();
			}
		} catch (Exception e) {
			System.out.println(pages.size());
			e.printStackTrace();
		}
	}

	public void removeOne(String value, String index_name)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				new File("classes/" + strTableName + index_name + ".class")));
		BTree t = (BTree) ois.readObject();
		int index = (int) t.search(value);
		int page_number = 0;
		while (index > 200) {
			index -= 200;
			page_number++;
		}
		ois = new ObjectInputStream(new FileInputStream(new File("classes/"
				+ strTableName + page_number)
				+ ".class"));
		Page p = (Page) ois.readObject();
		for (int i = 0; i < indices.size(); i++) {
			System.out.println(indices.get(i));
			ObjectInputStream ois1 = new ObjectInputStream(new FileInputStream(
					new File("classes/" + strTableName + indices.get(i)
							+ ".class")));
			BTree others = (BTree) ois1.readObject();
			others.delete(((Hashtable<String, Object>) p.information[index - 1])
					.get(indices.get(i))+"");
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(new File("classes/" + strTableName
							+ indices.get(i) + ".class")));
			oos.writeObject(others);
			oos.close();
		}
		p.information[index - 1] = null;
		ObjectOutputStream oos = new ObjectOutputStream(
				new FileOutputStream(new File("classes/" 
						+p.getId()+ ".class")));
		oos.writeObject(p);
		oos.close();

	}

	public Hashtable<String, Object> getfromTable(String ColName, String Treekey)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				new File("classes/" + strTableName + ColName + ".class")));
		BTree t = (BTree) ois.readObject();
	    if(	t.search(Treekey)==null)return null ;
	    
		int index = (int) 	t.search(Treekey);
		int page_number = 0;
		while (index > 200) {
			index -= 200;
			page_number++;
		}
		ois = new ObjectInputStream(new FileInputStream(new File("classes/"
				+ strTableName + page_number)
				+ ".class"));
		Page p = (Page) ois.readObject();

		Hashtable<String, Object> temp = (Hashtable<String, Object>) p.information[index - 1];
		return temp;
	}

	public void insert(String Name, Hashtable<String, Object> htblColNameValue)
			throws IOException, ClassNotFoundException {
		record++;
		htblColNameValue.put("record", record);
		for (int i = 0; i < indices.size(); i++) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					new File("classes/" + strTableName + indices.get(i)
							+ ".class")));
			BTree t = (BTree) ois.readObject();
			// System.out.println(strTableName+strKeyColName);
			t.insert((String) (htblColNameValue.get(indices.get(i)) + ""),
					record);
			t.save_Tree();
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// get current date time with Date()
		Date date = new Date();
		htblColNameValue.put("Date", date);
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				new File("classes/" + pages.get(noOfPages) + ".class")));
		Page p = (Page) ois.readObject();
		if (!p.saveInput(htblColNameValue)) {
			createPage();
			ois = new ObjectInputStream(new FileInputStream(new File("classes/"
					+ pages.get(noOfPages) + ".class")));
			p = (Page) ois.readObject();

			p.saveInput(htblColNameValue);

		}
		ois.close();

	}

	public ArrayList<Object> getRecordAll(boolean or,
			Hashtable<String, Object> Input) throws FileNotFoundException,
			IOException, ClassNotFoundException {
		ArrayList<Object> values = new ArrayList<Object>();
		for (int i = 0; i < pages.size() - 1; i++) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					"classes/" + new File((pages.get(i) + ".class"))));
			Page p = (Page) ois.readObject();
			ois.close();

			for (int j = 0; j < 200; j++) {
				Hashtable<String, Object> temp = (Hashtable<String, Object>) p
						.getInformation()[j];
				// System.out.println(Input);
				if (temp != null) {
					if (!or) {
						// System.out.println(values.size());
						if (checkAND(temp, Input)) {
							values.add(temp);

						}
					} else if (checkOR(temp, Input))
						values.add(temp);
				}
			}

		}

		return values;

	}

	public ArrayList<Object> getRecordB(boolean or,
			Hashtable<String, Object> Input) throws FileNotFoundException,
			IOException, ClassNotFoundException {
		ArrayList<Object> values = new ArrayList<Object>();
		// IF AND
		if (!or) {
			// Get one value list from one tree and call checkAND
			// Initialize a hashtable containing the components of the tree by
			// calling getfromTable
			Hashtable<String, Object> temp = null;
			for (String key : Input.keySet()) {
				if (indices.contains(key)) {
					temp = getfromTable(key, (String) (Input.get(key) + ""));
					break;
				}
			}
			if (checkAND(temp, Input)) {
				values.add(temp);
			}
		}
		// IF OR
		else {
			// Get all value lists from all trees, merge them and the remove
			// duplicates
			Hashtable<String, Object> temp = null;
			for (String key : Input.keySet()) {
				if (indices.contains(key)) {
					temp = getfromTable(key,Input.get(key)+"");
					if(temp!=null)
					values.add(temp);
				}
			}
			// remove duplicates by inserting elements into a hashset(don't
			// allow duplicates)
			// Ana gamed fashkh!!
			Set<Object> hs = new HashSet<>();
			hs.addAll(values);
			values.clear();
			values.addAll(hs);
		}

		return values;
	}

	public void UpdateTable(String key, Hashtable<String, Object> UpdateValue)
			throws FileNotFoundException, IOException, ClassNotFoundException {

		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				"classes/"
						+ new File((strTableName + strKeyColName + ".class"))));
		BTree t = (BTree) ois.readObject();
		ois.close();
		int index = (int) t.search(key);
		int page_number = 0;
		while (index > 200) {
			index -= 200;
			page_number++;
		}
		ois = new ObjectInputStream(new FileInputStream("classes/"
				+ new File((strTableName + page_number + ".class"))));
		Page p = (Page) ois.readObject();
		ois.close();
		Hashtable<String, Object> temp = (Hashtable<String, Object>) p
				.getInformation()[index - 1];
		for (String key1 : UpdateValue.keySet()) {
			if (indices.contains(key1)) {
				ObjectInputStream ois1 = new ObjectInputStream(
						new FileInputStream("classes/"
								+ new File((strTableName + key1 + ".class"))));
				BTree others = (BTree) ois.readObject();
				ois.close();
				others.delete((String) temp.get(key1));
				others.insert((String) UpdateValue.get(key1),
						temp.get("record"));
				ObjectOutputStream oos = new ObjectOutputStream(
						new FileOutputStream(new File("classes/" + strTableName
								+ key1 + ".class")));
				oos.writeObject(others);
				oos.close();
			}
		}

		UpdateRecord(temp, UpdateValue);
		System.out.println(temp.get(strKeyColName) + " " + key);

		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				new File("classes/" + p.getId() + ".class")));
		oos.writeObject(p);
		oos.close();

	}

	public void UpdateRecord(Hashtable<String, Object> h1,
			Hashtable<String, Object> h2) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// get current date time with Date()
		Date date = new Date();

		for (String key : h2.keySet()) {
			h1.put(key, h2.get(key));

		}
		h1.put("Date", date);

	}

	public boolean checkAND(Hashtable<String, Object> h1,
			Hashtable<String, Object> h2) {
		for (String key : h2.keySet()) {
			if (!(h2.get(key).equals(h1.get(key))))
				return false;
		}
		return true;

	}

	public boolean checkOR(Hashtable<String, Object> h1,
			Hashtable<String, Object> h2) {
		for (String key : h2.keySet())
			if (h1.get(key).equals(h2.get(key)))
				return true;

		return false;

	}

	public BTree load_Tree(String strColName) throws ClassNotFoundException,
			IOException {
		ObjectInputStream ois2 = new ObjectInputStream(new FileInputStream(
				new File("classes/" + strTableName + strColName + ".class")));
		BTree t2 = (BTree) ois2.readObject();
		ois2.close();
		return t2;

	}

}
