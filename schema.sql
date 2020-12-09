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
RETURNS TABLE (oid INTEGER,orderValue INTEGER) AS $orderFigure$
BEGIN 
   -- RETURN QUERY
    SELECT a.OrderID,SUM(a.total)
    FROM (SELECT OrderID,ORDER_PRODUCTS.productID,INVENTORY.ProductPrice*ORDER_PRODUCTS.ProductQuantity AS total FROM INVENTORY INNER JOIN ORDER_PRODUCTS ON INVENTORY.ProductID=ORDER_PRODUCTS.ProductID)a
    GROUP BY a.OrderID;
END $orderFigure$
LANGUAGE plpgsql;
-- SELECT TotalOrderValue();
DROP FUNCTION sellsFigure();
CREATE FUNCTION sellsFigure()
RETURNS TABLE (staffid INTEGER, totalsold INTEGER )AS $soldFigure$
BEGIN 
    RETURN QUERY SELECT TotalOrderValue() AS orderFigure;
    SELECT TotalOrderValue() AS orderFigure;
    SELECT a.StaffID,SUM(a.StaffID)
    FROM (SELECT * FROM STAFF_ORDERS INNER JOIN orderFigure ON orderFigure.oid=STAFF_ORDERS.OrderID)a
    GROUP BY a.SaffID;
END $soldFigure$
LANGUAGE plpgsql;
DROP FUNCTION seller();
CREATE FUNCTION seller()
RETURNS TABLE ( staffName VARCHAR(30), totalValue INTEGER) AS $$
BEGIN 
    RETURN QUERY SELECT sellsFigure() AS soldFigure;
    SELECT TotalOrderValue()AS soldFigure;
    SELECT a.name, a.totalsold
    FROM (SELECT totalsold,FName.STAFF+LName.STAFF AS name FROM STAFF INNER JOIN soldFigure ON soldFigure.staffid=STAFF.StaffID)a
    ORDER BY a.totalsold DESC;
END $$
LANGUAGE plpgsql;
