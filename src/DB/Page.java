package DB;
import java.io.*;
import java.util.*;
public class Page implements Serializable{
 private String id;
 private int counter;
 Object [] information;
 private int maxRows ;
  Page next_page;
 public Page(){
	 
	 
 }
 public Page(int maxRows,Page next_page,String id) throws IOException{
	 this.id = id ; 
	 this.maxRows = maxRows;
	 this.next_page=next_page;
	 counter = 0 ;
	 
	 information = new Hashtable [maxRows];
	 this.createPage();
	 
	
		}
 public void createPage( ) throws FileNotFoundException, IOException{
	 System.out.println(this.getId());
	 ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("classes/"+this.getId()+".class")));
		oos.writeObject(this);
		oos.close();
 }
 public boolean saveInput(Hashtable<String,Object> htblColNameValue) throws FileNotFoundException, IOException{
	 if(counter<maxRows){
	 information[counter++]=htblColNameValue ;
	 ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("classes/"+this.getId()+".class")));
		oos.writeObject(this);
		oos.close();
		
		return true;
		
	 }
	 else{
		
			 return false;
	 }
	 
 }
public Object [] getInformation() {
	return information;
}

public int getMaxRows() {
	return maxRows;
}
public void setMaxRows(int maxRows) {
	this.maxRows = maxRows;
}
public String getId() {
	return id;
}
public void setId(String id) {
	this.id = id;
}
public static void main( String [] args ) throws Exception
{
     
}
}
