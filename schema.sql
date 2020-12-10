--Table stores the information for items 
--ProductID as primary key
DROP TABLE INVENTORY CASCADE;
CREATE TABLE INVENTORY (
  ProductID   INTEGER not null, 
  ProductDesc    VARCHAR(30) not null,
  ProductPrice     NUMERIC(8,2) not null,
  ProductStockAmount    INTEGER not null,
  PRIMARY KEY (ProductID),
  CHECK(ProductPrice>0),
  CHECK(ProductStockAmount>0)
);

-- This SEQENCE used by ORDERS table so that the orderID can increases automatically without duplicate
DROP SEQUENCE sequence_1;
CREATE SEQUENCE sequence_1 
    START WITH 1  
    INCREMENT BY 1 ;  
DROP TABLE ORDERS CASCADE;

--This table stores the information for orders
--OrderID as primary key
CREATE TABLE ORDERS (
OrderID    INTEGER not null,
OrderType    VARCHAR(30) not null,
OrderCompleted    INTEGER not null,
OrderPlaced    DATE not null,
PRIMARY KEY (OrderID),
CHECK(OrderType='InStore'OR OrderType='Collection'OR OrderType='Delivery' ),
CHECK(OrderCompleted=1 OR OrderCompleted=0)
);
DROP TABLE ORDER_PRODUCTS CASCADE;

--this table stores the what items contain and their quantaties for each order
--orderId as foreign key reference orderid in order table
--ProductID as foreign key reference ProductID in inventory table
CREATE TABLE ORDER_PRODUCTS(
OrderID     INTEGER ,
ProductID    INTEGER ,
ProductQuantity    INTEGER not null,
CHECK(ProductQuantity>0),
UNIQUE(OrderID,ProductID),
FOREIGN KEY(OrderID) REFERENCES ORDERS(OrderID) ON DELETE CASCADE ON UPDATE CASCADE,
FOREIGN KEY(ProductID) REFERENCES INVENTORY(ProductID)  ON DELETE CASCADE ON UPDATE CASCADE
);
DROP TABLE DELIVERIES CASCADE;

--This table stores the delievery orders' information
--orderID as foreign key reference in orders table
CREATE TABLE DELIVERIES(
OrderID     INTEGER ,
FName    VARCHAR(30) not null,
LName    VARCHAR(30) not null,
House    VARCHAR(30) not null,
Street    VARCHAR(30) not null,
City    VARCHAR(30) not null,
DeliveryDate   DATE not null,
FOREIGN KEY(OrderID) REFERENCES ORDERS(OrderID)  ON DELETE CASCADE ON UPDATE CASCADE
);
DROP TABLE COLLECTIONS CASCADE ;

--This table stores the collection orders' information
--orderID as foreign key reference in orders table
CREATE TABLE COLLECTIONS(
OrderID    INTEGER ,
FName    VARCHAR(30) not null,
LName    VARCHAR(30) not null,
CollectionDate   DATE not null,
PRIMARY KEY(OrderID);
FOREIGN KEY(OrderID) REFERENCES ORDERS(OrderID)  ON DELETE CASCADE ON UPDATE CASCADE
);
DROP TABLE STAFF CASCADE;

--This table stores all staff's information
--staffID as primary key
CREATE TABLE STAFF(
StaffID    INTEGER not null,
FName    VARCHAR(30) not null,
LName    VARCHAR(30) not null,
PRIMARY KEY(StaffID)
);
DROP TABLE STAFF_ORDERS CASCADE;

--This table stores orders orderd by which staff
--orderId as foreign key reference OrderID in order table
--staffID as foreign key reference staffID in inventory table
CREATE TABLE STAFF_ORDERS(
StaffID    INTEGER ,
OrderID    INTEGER ,
UNIQUE(OrderID),
FOREIGN KEY(StaffID) REFERENCES STAFF(StaffID)  ON DELETE CASCADE ON UPDATE CASCADE,
FOREIGN KEY(OrderID) REFERENCES ORDERS(OrderID)  ON DELETE CASCADE ON UPDATE CASCADE
);
DROP FUNCTION  TotalOrderValue();

--This Function returns a table contain orderid and total value made by this order
--quary by inner joined inventory table and order_products table
CREATE FUNCTION TotalOrderValue() 
RETURNS TABLE (oid INTEGER,orderValue NUMERIC(8,3)) AS $orderFigure$
BEGIN 
   RETURN QUERY
    SELECT a.OrderID,SUM(a.total)
    FROM (SELECT OrderID,ORDER_PRODUCTS.productID,INVENTORY.ProductPrice*ORDER_PRODUCTS.ProductQuantity AS total FROM INVENTORY INNER JOIN ORDER_PRODUCTS ON INVENTORY.ProductID=ORDER_PRODUCTS.ProductID)a
    GROUP BY a.OrderID;
END $orderFigure$
LANGUAGE plpgsql;
DROP FUNCTION sellsFigure(l INTEGER);

--This function returns total value sold by ach staff
--query by inner joined table produced by TotaOrderValue() function and staff_orders table
CREATE FUNCTION sellsFigure()
RETURNS TABLE (staffid INTEGER, totalsold numeric(8,3))AS $soldFigure$
BEGIN 
    RETURN QUERY
    SELECT a.StaffID,SUM(a.orderValue)
    FROM (SELECT * FROM STAFF_ORDERS INNER JOIN (select * from TotalOrderValue()) AS f ON STAFF_ORDERS.OrderID=f.oid) a
    GROUP BY a.StaffID;
END $soldFigure$
LANGUAGE plpgsql;
DROP FUNCTION seller();

-- This table returns the name of staff who sells total value more than £50000 and order by descending
--query from table inner joined by table returned from sellsFigure() function and staff table
CREATE FUNCTION seller()
RETURNS TABLE ( fname VARCHAR(30), lname VARCHAR(30),totalValue numeric(8,3)) AS $result$
BEGIN 
    RETURN QUERY 
    SELECT a.FName,a.LName,a.totalsold
    FROM (SELECT STAFF.FName,STAFF.LName,totalsold FROM STAFF INNER JOIN 
            (SELECT * FROM sellsFigure()) AS f ON STAFF.StaffID=f.staffid WHERE f.totalsold>50000) a
    ORDER BY a.totalsold DESC;
END $result$
LANGUAGE plpgsql;
DROP FUNCTION year(inputyear INTEGER);

--This function returns the orderis which orderd for particular year
CREATE FUNCTION year(inputyear INTEGER)
RETURNS TABLE (oid INTEGER) AS $$
BEGIN 
    RETURN QUERY
    SELECT OrderID
    FROM ORDERS 
    WHERE EXTRACT(year FROM ORDERS.OrderPLACED)=inputyear;
END $$
LANGUAGE plpgsql;
DROP FUNCTION op8(inputyear INTEGER);

-- This function returns the productid and orderid ordering this product 
--as well as the total slls value of this product in particular year
CREATE FUNCTION op8(inputyear INTEGER)
RETURNS TABLE (pid INTEGER, oid INTEGER, sum numeric(8,3))AS $$
BEGIN
    RETURN QUERY
    SELECT a.ProductID,a.OrderID,SUM(a.total)as sum
    FROM (SELECT s.OrderID,s.productID,INVENTORY.ProductPrice*s.ProductQuantity AS total FROM INVENTORY INNER JOIN (SELECT * FROM (SELECT * FROM year(inputyear))AS f INNER JOIN ORDER_PRODUCTS ON f.oid= ORDER_PRODUCTS.OrderID )s ON INVENTORY.ProductID=s.ProductID)a 
    GROUP BY a.ProductID,a.OrderID
    ORDER BY SUM(a.total) DESC;
END $$
LANGUAGE plpgsql;

--This function returns table contain staffid whose at least sold £30000 and have sold one of the 
--procucted which totle sold moer than £20000 for particular year
DROP FUNCTION id(inputyear INTEGER);
CREATE FUNCTION id(inputyear INTEGER)
RETURNS TABLE ( staffid INTEGER) AS $$
BEGIN 
    RETURN QUERY 
    SELECT a.StaffID
    FROM (SELECT s.StaffID FROM (SELECT STAFF_ORDERS.StaffID FROM (SELECT * FROM op8(inputyear))AS m INNER JOIN 
                STAFF_ORDERS  ON m.oid=STAFF_ORDERS.OrderID WHERE m.sum>20000)s INNER JOIN (SELECT * FROM sellsFigure())AS n ON s.StaffID=n.staffid WHERE n.totalSold>=30000)a;
END $$
LANGUAGE plpgsql;
--this table used to convert the id produced by id() into correspond name
DROP FUNCTION name(inputyear INTEGER);
CREATE FUNCTION name(inputyear INTEGER)
RETURNS TABLE (fname VARCHAR(30), lname VARCHAR(30) )AS $$
BEGIN   
    RETURN QUERY
    SELECT a.FName, a.LName
    FROM(SELECT STAFF.FName,STAFF.LName FROM STAFF INNER JOIN 
            (SELECT * FROM id(inputyear )) AS f ON STAFF.StaffID=f.staffid) a;
END $$
LANGUAGE plpgsql;
DROP FUNCTION op5(date DATE);
CREATE FUNCTION op5(date DATE)
RETURNS TABLE (oid INT)AS $$
BEGIN
    RETURN QUERY
    SELECT a.OrderID
    FROM (SELECT ORDERS.OrderID,CollectionDate,OrderCompleted FROM ORDERS INNER JOIN COLLECTIONS ON ORDERS.OrderID=COLLECTIONS.OrderID )a
    WHERE date-a.CollectionDate>=8 AND a.OrderCompleted=0;
END $$
LANGUAGE plpgsql;
    



    
