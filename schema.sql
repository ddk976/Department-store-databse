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
DROP SEQUENCE sequence_1;
CREATE SEQUENCE sequence_1 
    START WITH 1  
    INCREMENT BY 1 ;  
DROP TABLE ORDERS CASCADE;
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
CREATE TABLE ORDER_PRODUCTS(
OrderID     INTEGER ,
ProductID    INTEGER ,
ProductQuantity    INTEGER not null,
 CHECK(ProductQuantity>0),
UNIQUE(OrderID,ProductID),
FOREIGN KEY(OrderID) REFERENCES ORDERS(OrderID) ON DELETE SET NULL ON UPDATE CASCADE,
FOREIGN KEY(ProductID) REFERENCES INVENTORY(ProductID)  ON DELETE SET NULL ON UPDATE CASCADE
);
DROP TABLE DELIVERIES CASCADE;
CREATE TABLE DELIVERIES(
OrderID     INTEGER ,
FName    VARCHAR(30) not null,
LName    VARCHAR(30) not null,
House    VARCHAR(30) not null,
Street    VARCHAR(30) not null,
City    VARCHAR(30) not null,
DeliveryDate   DATE not null,
FOREIGN KEY(OrderID) REFERENCES ORDERS(OrderID)  ON DELETE SET NULL ON UPDATE CASCADE
);
DROP TABLE COLLECTIONS CASCADE ;
CREATE TABLE COLLECTIONS(
OrderID    INTEGER ,
FName    VARCHAR(30) not null,
LName    VARCHAR(30) not null,
CollectionDate   DATE not null,
FOREIGN KEY(OrderID) REFERENCES ORDERS(OrderID)  ON DELETE SET NULL ON UPDATE CASCADE
);
DROP TABLE STAFF CASCADE;
CREATE TABLE STAFF(
StaffID    INTEGER not null,
FName    VARCHAR(30) not null,
LName    VARCHAR(30) not null,
PRIMARY KEY(StaffID)
);
DROP TABLE STAFF_ORDERS CASCADE;
CREATE TABLE STAFF_ORDERS(
StaffID    INTEGER ,
OrderID    INTEGER ,
UNIQUE(OrderID),
FOREIGN KEY(StaffID) REFERENCES STAFF(StaffID)  ON DELETE SET NULL ON UPDATE CASCADE,
FOREIGN KEY(OrderID) REFERENCES ORDERS(OrderID)  ON DELETE SET NULL ON UPDATE CASCADE
);
DROP FUNCTION  TotalOrderValue();
CREATE FUNCTION TotalOrderValue() 
RETURNS TABLE (oid INTEGER,orderValue NUMERIC(8,3)) AS $orderFigure$
BEGIN 
   RETURN QUERY
    SELECT a.OrderID,SUM(a.total)
    FROM (SELECT OrderID,ORDER_PRODUCTS.productID,INVENTORY.ProductPrice*ORDER_PRODUCTS.ProductQuantity AS total FROM INVENTORY INNER JOIN ORDER_PRODUCTS ON INVENTORY.ProductID=ORDER_PRODUCTS.ProductID)a
    GROUP BY a.OrderID;
END $orderFigure$
LANGUAGE plpgsql;
--SELECT TotalOrderValue();
DROP FUNCTION sellsFigure(l INTEGER);
-- CREATE FUNCTION sellsFigure()
-- RETURNS TABLE (staffid INTEGER, totalsold NUMERIC(8,3))AS $soldFigure$
-- BEGIN 
--     --SELECT TotalOrderValue() AS orderFigure;
    
--     RETURN QUERY
--     SELECT TotalOrderValue() AS orderFigure;
--     SELECT a.StaffID,SUM(a.StaffID)
--     FROM (SELECT * FROM STAFF_ORDERS INNER JOIN orderFigure ON orderFigure.oid=STAFF_ORDERS.OrderID)a
--     GROUP BY a.SaffID;
-- END $soldFigure$
-- LANGUAGE plpgsql;
CREATE FUNCTION sellsFigure()
RETURNS TABLE (staffid INTEGER, totalsold numeric(8,3))AS $soldFigure$
BEGIN 
    RETURN QUERY
    SELECT a.StaffID,SUM(a.orderValue)
    FROM (SELECT * FROM STAFF_ORDERS INNER JOIN (select * from TotalOrderValue()) AS f ON STAFF_ORDERS.OrderID=f.oid) a
   -- WHERE totalsold>l
    GROUP BY a.StaffID;
END $soldFigure$
LANGUAGE plpgsql;
DROP FUNCTION seller();

-- CREATE FUNCTION seller()
-- RETURNS TABLE ( staffName VARCHAR(30), totalValue INTEGER) AS $result$
-- BEGIN 
--     RETURN QUERY 
--     SELECT TotalOrderValue()AS soldFigure;
--     SELECT a.name, a.totalsold
--     FROM (SELECT totalsold,FName.STAFF+LName.STAFF AS name FROM STAFF INNER JOIN soldFigure ON soldFigure.staffid=STAFF.StaffID)a
--     ORDER BY a.totalsold DESC;
-- END $result$
-- LANGUAGE plpgsql;
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
CREATE FUNCTION op8(inputyear INTEGER)
RETURNS TABLE (pid INTEGER, oid INTEGER, sum numeric(8,3))AS $$
BEGIN
    RETURN QUERY
    SELECT a.ProductID,a.OrderID,SUM(a.total)as sum
    FROM (SELECT s.OrderID,s.productID,INVENTORY.ProductPrice*s.ProductQuantity AS total FROM INVENTORY INNER JOIN (SELECT * FROM (SELECT * FROM year(inputyear))AS f INNER JOIN ORDER_PRODUCTS ON f.oid= ORDER_PRODUCTS.OrderID )s ON INVENTORY.ProductID=s.ProductID)a 
    -- WHERE sum>20000
    GROUP BY a.ProductID,a.OrderID
    ORDER BY SUM(a.total) DESC;
END $$
LANGUAGE plpgsql;
DROP FUNCTION id(inputyear INTEGER);
CREATE FUNCTION id(inputyear INTEGER)
RETURNS TABLE ( staffid INTEGER) AS $$
BEGIN 
    RETURN QUERY 
    SELECT a.StaffID
    FROM (SELECT s.StaffID FROM (SELECT STAFF_ORDERS.StaffID FROM (SELECT * FROM op8(inputyear))AS m INNER JOIN 
                STAFF_ORDERS  ON m.oid=STAFF_ORDERS.OrderID WHERE m.sum>20000)s INNER JOIN (SELECT * FROM sellsFigure())AS n ON s.StaffID=n.staffid WHERE n.totalSold>30000)a;
END $$
LANGUAGE plpgsql;
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
    



    
