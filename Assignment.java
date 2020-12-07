import java.io.*;
import java.sql.*;

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
	// static void loop(){
	// 	int count=1;
    //     int[] productIDs = new int[50];
    //     int[] quantities = new int [50];
    //     String end="Y";
    //     while(end.charAt(0)=='Y'){
    //         productIDs[count-1]=Integer.parseInt(String.valueOf(readEntry("Enter a product ID:")));
    //         quantities[count-1]=Integer.parseInt(String.valueOf(readEntry("Enter the quantity sold:")));
    //         count++;
    //         end = readEntry("Is there another product in the order?:");
    //     } 
	// }
	public static void main(String args[]) throws SQLException, IOException {
		// You should only need to fetch the connection details once
		Connection conn = getConnection();
		// Incomplete
		// Code to present a looping menu, read in input data and call the appropriate option menu goes here
		// You may use readEntry to retrieve input data
        String selectOption = readEntry("Enter your choice:");
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
		conn.close();
	}
	
	/**
	* @param conn An open database connection 
	* @param productIDs An array of productIDs associated with an order
        * @param quantities An array of quantities of a product. The index of a quantity correspeonds with an index in productIDs
	* @param orderDate A string in the form of 'DD-Mon-YY' that represents the date the order was made
	* @param staffID The id of the staff member who sold the order
	*/
	public static void option1(Connection conn, int[] productIDs, int[] quantities, String orderDate, int staffID) {
		// Incomplete - Code for option 1 goes here
		String stmt1 = "SELECT OrderID FROM ORDERS WHERE OrderType='InStore'";

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
		// Incomplete - Code for option 2 goes here
		int count=0;
		while((productIDs[count])!= 0){
			System.out.println(productIDs[count]);
			System.out.println(quantities[count]);
			count++;
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
		// Incomplete - Code for option 3 goes here
	}

	/**
	* @param conn An open database connection 
	*/
	public static void option4(Connection conn) {
		// Incomplete - Code for option 4 goes here
	}

	/**
	* @param conn An open database connection 
	* @param date The target date to test collection deliveries against
	*/
	public static void option5(Connection conn, String date) {
		// Incomplete - Code for option 5 goes here
	}

	/**
	* @param conn An open database connection 
	*/
	public static void option6(Connection conn) {
		// Incomplete - Code for option 6 goes here
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

/*   public static Connection getConnection() {
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

    }

	*/
}
