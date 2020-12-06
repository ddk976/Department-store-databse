For inventory table I set ProductID as primary key. To store the description fforr every item, we should use unique ID to distinguish items. Items may have same discription,price and amount. But should have different id so that when placed in order we known which exactly product placed.  I also added a check constraint to check is the price added is non-zero. 
In order table the OrderID was set to be a primary key used to identify each order. A check constraint checking the input of OrderType value if belongs to InStore, Collection or Delivery. Another Check clause checking the input of OrderCompleted state is 0 or 1. 

The order_products table does not have a primary key, as there is not any other table's attribute need reference this table. The unique key should be combination order and productid as same product should not added to same order twice. OrderID should be a foreign key who reference to OrderID in table orders. ProductID references to ProductID in inventory table. These foreign keys added a contraint 'ON DELETE SET NULL ON UPDATE CASCADE,to make sure when this value change in table orders and inventory they should need to update to same value or set to null. 

 Table deliveries and collections both have a foreign key OrderID references OrderID in table orders.This one is also ON DELET SET NULL ON UPDATE CASCADE 
 
Table staff have a primary key StaffID used to identify each staff. 

Table staff_orders have two foreign keys staffid which reference to staffid in table staff and orderID in table order. These foreign keys are ON DELET SET NULL ON UPDATE CASCADE. 

I have not changes anything for these table as I can't find way to make this better.
