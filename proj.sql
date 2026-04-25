CREATE DATABASE IF NOT EXISTS farmtofork;
USE farmtofork;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Sale;
DROP TABLE IF EXISTS Purchase;
DROP TABLE IF EXISTS Product;
DROP TABLE IF EXISTS Client;
DROP TABLE IF EXISTS Farmer;
DROP TABLE IF EXISTS Branch;
DROP TABLE IF EXISTS Warehouse;
DROP TABLE IF EXISTS Category;

SET FOREIGN_KEY_CHECKS = 1;

/* =========================
   Master tables
   ========================= */

CREATE TABLE Category (
  categoryID INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(200),
  PRIMARY KEY (categoryID)
);

CREATE TABLE Warehouse (
  warehouseID INT NOT NULL AUTO_INCREMENT,
  location VARCHAR(150) NOT NULL,
  capacity INT NOT NULL,
  PRIMARY KEY (warehouseID)
);

CREATE TABLE Branch (
  branchID INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  city VARCHAR(100),
  address VARCHAR(200),
  phone VARCHAR(30),
  PRIMARY KEY (branchID)
);

CREATE TABLE Client (
  clientID INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  type VARCHAR(50),
  contactInfo VARCHAR(200),
  PRIMARY KEY (clientID)
);



CREATE TABLE Farmer (
  farmerID INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  region VARCHAR(100),
  contactInfo VARCHAR(200),
  PRIMARY KEY (farmerID)
);

/* =========================
   Product
   ========================= */
CREATE TABLE Product (
  productID INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  expiryDate DATE,
  price DECIMAL(10,2) NOT NULL,
  quantity INT NOT NULL DEFAULT 0,
  categoryID INT NOT NULL,
  warehouseID INT NOT NULL,
  PRIMARY KEY (productID),
  FOREIGN KEY (categoryID) REFERENCES Category(categoryID),
  FOREIGN KEY (warehouseID) REFERENCES Warehouse(warehouseID)
);

/* =========================
   Purchase 
   ========================= */
CREATE TABLE Purchase (
  purchaseID INT NOT NULL AUTO_INCREMENT,
  purchaseDate DATE NOT NULL,
  quantity INT NOT NULL,
  unitPrice DECIMAL(10,2) NOT NULL,
  totalCost DECIMAL(10,2) NOT NULL,
  warehouseEntryDate DATE,
  farmerID INT NOT NULL,
  productID INT NOT NULL,
  branchID INT NOT NULL,
  PRIMARY KEY (purchaseID),
  FOREIGN KEY (farmerID) REFERENCES Farmer(farmerID),
  FOREIGN KEY (productID) REFERENCES Product(productID),
  FOREIGN KEY (branchID) REFERENCES Branch(branchID)
);

/* =========================
   Sale (matches SaleDAO + ReportDAO)
   ========================= */
CREATE TABLE Sale (
  saleID INT NOT NULL AUTO_INCREMENT,
  saleDate DATE NOT NULL,
  quantity INT NOT NULL,
  unitPrice DECIMAL(10,2) NOT NULL,
  totalRevenue DECIMAL(10,2) NOT NULL,
  clientID INT NOT NULL,
  productID INT NOT NULL,
  branchID INT NOT NULL,
  PRIMARY KEY (saleID),
  FOREIGN KEY (clientID) REFERENCES Client(clientID),
  FOREIGN KEY (productID) REFERENCES Product(productID),
  FOREIGN KEY (branchID) REFERENCES Branch(branchID)
);







