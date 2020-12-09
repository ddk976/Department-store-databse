import java.io.*;
import java.sql.*;
import java.sql.Date; 
// import java.util.Calendar;
 import java.time.*;
import java.time.format.*;
import java.util.Locale;

import java.util.Properties;


class Assignment {

	private static String readEntry(String prompt) {
		try {
			StringBuffer buffer = new StringBuffer();
			System.out.print(prompt);
			System.out.flush();
			int c = System.in.read();
			while(c != '\n' && c != -1) {
				buffer.append((char)c);
				c = System.in.read();
			}
			return buffer.toString().trim();
		} catch (IOException e) {
			return "";
		}
 	}
	 static void print(){
		 System.out.println("Menu:");
		 System.out.println("(1) In-Store Purchases");
		 System.out.println("(2) Collection");
		 System.out.println("(3) Delivery");
		 System.out.println("(4) Biggest Sellers");
		 System.out.println("(5) Reserved Stock");
		 System.out.println("(6) Staff Life-Time Success");
		 System.out.println("(7) Staff Contribution");
		 System.out.println("(8) Employees of the Year");
		 System.out.println("(0) Quit");
		 
	 }
	public static void main(String args[]) throws SQLException, IOException {
		// You should only need to fetch the connection details once
		Connection conn = getConnection();
		// Incomplete
		// Code to present a looping menu, read in input data and call the appropriate option menu goes here
		// You may use readEntry to retrieve input data
		String selectOption="initial";
		while(selectOption.charAt(0)!='0'){
			print();
			System.out.println(selectOption);
			selectOption = readEntry("Enter your choice:");
			int count=1;
			int[] productIDs = new int[50];
			int[] quantities = new int [50];
			String end="Y";
			while(end.charAt(0)=='Y'&&(selectOption.charAt(0)=='1'||selectOption.charAt(0)=='2'||selectOption.charAt(0)=='3')){
				productIDs[count-1]=Integer.parseInt(String.valueOf(readEntry("Enter a product ID:")));
				quantities[count-1]=Integer.parseInt(String.valueOf(readEntry("Enter the quantity sold:")));
				count++;
				end = readEntry("Is there another product in the order?:");
			} 
			if (selectOption.charAt(0)=='1'){
				String orderDate = readEntry("Enter the date sold:");
				int staffID = Integer.parseInt(String.valueOf(readEntry("Enter your staff ID:")));
				option1(conn,productIDs,quantities,orderDate,staffID);
			} else if (selectOption.charAt(0)=='2'){
				String orderDate = readEntry("Enter the date sold:");
				String collectionDate = readEntry("Enter the date of collection:");
				String fName = readEntry("Enter the first name of collection:");
				String LName = readEntry("Enter the last neme of the collection:");
				int staffID = Integer.parseInt(String.valueOf(readEntry("Enter your staff ID:")));
				option2(conn,productIDs,quantities,orderDate,collectionDate,fName,LName,staffID);
			} else if(selectOption.charAt(0)=='3'){
				String orderDate = readEntry("Enter the date sold:");
				String collectionDate = readEntry("Enter the date of collection:");
				String fName = readEntry("Enter the first name of collection:");
				String LName = readEntry("Enter the last neme of the collection:");
				String house = readEntry("Enter the house name:");
				String street = readEntry("Enter the street:");
				String city = readEntry("Enter the city:");
				int staffID = Integer.parseInt(String.valueOf(readEntry("Enter your staff ID:")));
				option3(conn,productIDs,quantities,orderDate,collectionDate,fName,LName,house,street,city,staffID);
			}else if(selectOption.charAt(0)=='4'){
				option4(conn);
			}else if (selectOption.charAt(0)=='5'){
				String date = readEntry("Enter the date:");
				option5(conn,date);
			}else if(selectOption.charAt(0)=='6'){
				option6(conn);
			}
		}
		conn.close();
	}
	 static void process1(Connection conn,int oid,int[] productIDs, int[] quantities, int staffID){
		 String stmt2 = "UPDATE INVENTORY SET ProductStockAmount=? WHERE ProductID = ?";
		String stockAmount="select ProductStockAmount From INVENTORY WHERE ProductID=?";
		String stmt3 = "INSERT INTO ORDER_PRODUCTS VALUES"+"(?,?,?)";
		String stmt4="INSERT INTO STAFF_ORDERS VALUES"+"(?,?)";
		try{
			PreparedStatement p2=conn.prepareStatement(stmt2);
			PreparedStatement pStockAmount=conn.prepareStatement(stockAmount);
			int[] stock = new int[productIDs.length];
			int i =0;
			while(productIDs[i]!=0){
				pStockAmount.setInt(1,productIDs[i]);
				ResultSet r1=pStockAmount.executeQuery();
				while(r1.next()){
				stock[i]=r1.getInt(1);
				}
				i++;
			}
			i=0;
			while(productIDs[i]!=0){
				p2.setInt(1,productIDs[i]);
				System.out.println(stock[i]-quantities[i]);
				p2.setInt(2,stock[i]-quantities[i]);
				p2.executeUpdate();
				i++;
			}		
			System.out.println("Success p2");
			PreparedStatement p3=conn.prepareStatement(stmt3);
			 i=0;
			while(productIDs[i]!=0){
				p3.setInt(1,oid);
				System.out.println(productIDs[i]);
				p3.setInt(2,productIDs[i]);
				System.out.println(productIDs[i]);
				p3.setInt(3,quantities[i]);
				p3.executeUpdate();
				i++;
			}
			System.out.println("Success p3");
			PreparedStatement p4=conn.prepareStatement(stmt4);
			i=0;
			while(productIDs[i]!=0){
				p4.setInt(1,staffID);
				p4.setInt(2,productIDs[i]);
				p4.executeUpdate();
				i++;
			}
			System.out.println("Success p4");
			int[] print = new int[productIDs.length];
			i=0;
			while(productIDs[i]!=0){
				pStockAmount.setInt(1,productIDs[i]);
				ResultSet r2=pStockAmount.executeQuery();
				System.out.println("Im in for loop");
				while(r2.next()){
				print[i]=r2.getInt(1);
				System.out.println("Product ID"+productIDs[i]+"is now at"+print[i]);
				i++;
				}
			}
		}catch(SQLException se){
			System.out.println("Could not do process");
			se.printStackTrace();
		}
			
	 }
	
	/**
	* @param conn An open database connection 
	* @param productIDs An array of productIDs associated with an order
        * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option1(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID) {
	
		String stmt1 = "INSERT INTO ORDERS VALUES"+"(?,?,?,?)";
		String id = "SELECT nextval('sequence_1')";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH);
		LocalDate dateTime = LocalDate.parse(orderDate, formatter);
		Date date=Date.valueOf(dateTime);
		try{
			PreparedStatement pid = conn.prepareStatement(id);
			ResultSet rid = pid.executeQuery(); 
			int oid=0;
			if(rid.next())
    			 oid = rid.getInt(1);
			PreparedStatement p1=conn.prepareStatement(stmt1,Statement.RETURN_GENERATED_KEYS);
			p1.setInt(1, oid); 
			p1.setString(2,"InStore");
			p1.setInt(3,1);
			p1.setDate(4,date);
			p1.executeUpdate();
			// ResultSet r1=p1.getGeneratedKeys();
			// r1.next();
			// int oid=r1.getInt(1);
			process1(conn,oid,productIDs,quantities,staffID);
		}catch(SQLException se){
			System.out.println("Could not option");
			se.printStackTrace();
		}
	
	
	}

	/**
	* @param conn An open database connection 
	* @param productIDs An array of productIDs associated with an order
        * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param collectionDate A string in the form of 'DD-Mon-YY' that represents the date the order will be collected
	* @param fName The first name of the customer who will collect the order
	* @param LName The last name of the customer who will collect the order
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option2(Connection conn, int[] productIDs, int[] quantities, String orderDate, String collectionDate, String fName, String LName, int staffID) {
	
		String stmt1 = "INSERT INTO ORDERS VALUES"+"(?,?,?,?)";
		String stmt2 = "INSERT INTO COLLECTIONS VALUES"+"(?,?,?,?)";
		String id = "SELECT nextval('sequence_1')";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH);
		LocalDate odateTime = LocalDate.parse(orderDate, formatter);
		LocalDate cdateTime = LocalDate.parse(collectionDate, formatter);
		Date oDate=Date.valueOf(odateTime);
		Date cDate=Date.valueOf(cdateTime);
	try{
		PreparedStatement pid = conn.prepareStatement(id);
			ResultSet rid = pid.executeQuery(); 
			int oid=0;
			if(rid.next())
    			 oid = rid.getInt(1);
		PreparedStatement p1=conn.prepareStatement(stmt1,Statement.RETURN_GENERATED_KEYS);
			p1.setInt(1,oid);
			p1.setString(2,"Collection");
			p1.setInt(3,0);
			p1.setDate(4,oDate);
			p1.executeUpdate();
			// ResultSet r1=p1.getGeneratedKeys();
			// r1.next();
			// int oid=r1.getInt(1);
			process1(conn,oid,productIDs,quantities,staffID);
		PreparedStatement p2=conn.prepareStatement(stmt2);
			p2.setInt(1,oid);
			p2.setString(2,fName);
			p2.setString(3,LName);
			p2.setDate(4,cDate);
			p2.executeUpdate();
		}catch(SQLException se){
			System.out.println("Could not option 2");
			se.printStackTrace();
		}
	
		
	}

	/**
	* @param conn An open database connection 
	* @param productIDs An array of productIDs associated with an order
        * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param deliveryDate A string in the form of 'DD-Mon-YY' that represents the date the order will be delivered
	* @param fName The first name of the customer who will receive the order
	* @param LName The last name of the customer who will receive the order
	* @param house The house name or number of the delivery address
	* @param street The street name of the delivery address
	* @param city The city name of the delivery address
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option3(Connection conn, int[] productIDs, int[] quantities, String orderDate, String deliveryDate, String fName, String LName,
				   String house, String street, String city, int staffID) {

		String stmt1 = "INSERT INTO ORDERS VALUES"+"(?,?,?,?)";
		String stmt2 = "INSERT INTO DELIVERIES VALUES"+"(?,?,?,?,?,?,?)";
		String id = "SELECT nextval('sequence_1')";
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH);
		LocalDate odateTime = LocalDate.parse(orderDate, formatter);
		LocalDate ddateTime = LocalDate.parse(deliveryDate, formatter);
		Date oDate=Date.valueOf(odateTime);
		Date dDate=Date.valueOf(ddateTime);
	try{
		PreparedStatement pid = conn.prepareStatement(id);
			ResultSet rid = pid.executeQuery(); 
			
			int oid=0;
			if(rid.next())
    			 oid = rid.getInt(1);
		PreparedStatement p1=conn.prepareStatement(stmt1,Statement.RETURN_GENERATED_KEYS);
			p1.setInt(1,oid);
			p1.setString(2,"Delivery");
			p1.setInt(3,0);
			p1.setDate(4,oDate);
			p1.executeUpdate();
			// ResultSet r1=p1.getGeneratedKeys();
			// r1.next();
			// int oid=r1.getInt(1);
			process1(conn,oid,productIDs,quantities,staffID);
		PreparedStatement p2=conn.prepareStatement(stmt2);
			p2.setInt(1,oid);
			p2.setString(2,fName);
			p2.setString(3,LName);
			p2.setString(4,house);
			p2.setString(5,street);
			p2.setString(6,city);
			p2.setDate(7,dDate);
			p2.executeUpdate();
			int i=0;
			while(productIDs[i]!=0){
			System.out.println(productIDs[i]);
			System.out.println(quantities[i]);
			i++;
			}
	}
	catch(SQLException se){
		System.out.println("Could not option3");
			se.printStackTrace();
		}
	
	}

	/**
	* @param conn An open database connection 
	*/
	public static void option4(Connection conn) {
		// Incomplete - Code for option 4 goes here
		// String stmt2 = "SELECT INVENTORY.ProductID,INVENTORY.ProductDesc,totalValve"
		// 				+"SUM(a.value) AS totalValue"
		// 				+"FROM (ORDER_PRODUCTS  INNER JOIN INVENTORY ON INVENTORY.ProductID=INVENTORY.ProductIDINVENTORY.ProductID"
		// 				+"GROUP BY INVENTORY.ProductID ";
		String stmt1="SELECT IO.ProductID,IO.ProductDesc,SUM(IO.total)"
					+"FROM(SELECT productID,INVENTRY.ProductPrice*ORDER_PRODUCTS.ProductQuantity AS total FROM INVENTORY INNER JOIN ORDER_PRODUCTS ON INVENTORY.ProductID=INVENTORY.ProductIDINVENTORY.ProductID) IO "
					+"GROUP BY IO.ProductID "
					+"ORDER BY DESC ";
		
		
		try{
			PreparedStatement p1=conn.prepareStatement(stmt1);
			ResultSet r1 = p1.executeQuery(); 
			System.out.printf("%-9s%-9s%-9d%\n","ProductID","ProductDesc","TotalValueSold");
			while(r1.next()){
				System.out.println("Im in loop");
					int id = r1.getInt(1);
					System.out.println(id);
					String desc = r1.getString(2);
					System.out.println(desc);
					int value = r1.getInt(3);
					System.out.println(value);
					System.out.printf("%-9d%-9s%-9d\n",id,desc,value);
			}
		}catch(SQLException se){
		System.out.println("Could not option3");
			se.printStackTrace();
		}

	}

	/**
	* @param conn An open database connection 
	* @param date The target date to test collection deliveries against
	*/
	public static void option5(Connection conn, String date) {
		// Incomplete - Code for option 5 goes here
		// String stmt1="SELECT OrderID FROM ORDERS WHERE OrderType='Collection'AND OrderCompleted=0 AND OrderPlaced<? ";
		// try{
		// 	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.ENGLISH);
		// 	LocalDate dateTime = LocalDate.parse(date, formatter);
		// 	Date nDate=Date.valueOf(dateTime);
	
		// }catch(SQLException se){
		// System.out.println("Could not option3");
		// 	se.printStackTrace();
		// }

		// Date nDate=Date.valueOf(odateTime);
		// PreparedStatement p1=conn.prepareStatement(stmt1);

	}

	/**
	* @param conn An open database connection 
	*/
	public static void option6(Connection conn) {
		// Incomplete - Code for option 6 goes here
		String stmt1="SELECT seller()";
		try{
			PreparedStatement p1=conn.prepareStatement(stmt1);
			ResultSet r1 = p1.executeQuery(); 
			System.out.printf("%-9s%-9s%\n","EmployeeName"," , ","TotalValueSold");
			while(r1.next()){
					String name = r1.getString(1);
					int value = r1.getInt(2);
					System.out.println(name+" , "+value);
			}
		}catch(SQLException se){
		System.out.println("Could not option3");
			se.printStackTrace();
		}


	}

	/**
	* @param conn An open database connection 
	*/
	public static void option7(Connection conn) {
		// Incomplete - Code for option 7 goes here
	}

	/**
	* @param conn An open database connection 
	* @param year The target year we match employee and product sales against
	*/
	public static void option8(Connection conn, int year) {
		// Incomplete - Code for option 8 goes here
	}

    public static Connection getConnection(){
        Properties props = new Properties();
        props.setProperty("socketFactory", "org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg");

        props.setProperty("socketFactoryArg",System.getenv("PGHOST") + "/.s.PGSQL.5432");
        Connection conn;
        try{
          conn = DriverManager.getConnection("jdbc:postgresql://localhost/deptstore", props);
          return conn;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

  /* public static Connection getConnection() {
        //This version of getConnection uses ports to connect to the server rather than sockets
        //If you use this method, you should comment out the above getConnection method, and comment out lines 19 and 21
        String user = "postgres";
        String passwrd = "976666";
        Connection conn;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException x) {
            System.out.println("Driver could not be loaded");
        }

        try {
            conn = DriverManager.getConnection("http://127.0.0.1:52810/?key=a29bc70e-e150-4822-856d-5daa08be9e5c");

            return conn;
        } catch(SQLException e) {
                e.printStackTrace();
            System.out.println("Error retrieving connection");
            return null;
        }

    }*/

	
}
