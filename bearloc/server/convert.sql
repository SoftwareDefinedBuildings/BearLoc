BEGIN TRANSACTION;
CREATE TEMPORARY TABLE acc_backup(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL,  eventnano INTEGER NOT NULL,  x REAL NOT NULL,  y REAL NOT NULL,  z REAL NOT NULL,  accuracy REAL,  PRIMARY KEY (uuid, epoch));
INSERT INTO acc_backup SELECT * FROM acc;
DROP TABLE acc;
CREATE TABLE acc(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL,  eventnano INTEGER NOT NULL,  x REAL NOT NULL,  y REAL NOT NULL,  z REAL NOT NULL,  accuracy REAL,  PRIMARY KEY (uuid, epoch, sysnano, eventnano));
INSERT INTO acc SELECT * FROM acc_backup;
DROP TABLE acc_backup;
COMMIT;

BEGIN TRANSACTION;
CREATE TEMPORARY TABLE lacc_backup(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL,  eventnano INTEGER NOT NULL,  x REAL NOT NULL,  y REAL NOT NULL,  z REAL NOT NULL,  accuracy REAL,  PRIMARY KEY (uuid, epoch));
INSERT INTO lacc_backup SELECT * FROM lacc;
DROP TABLE lacc;
CREATE TABLE lacc(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL,  eventnano INTEGER NOT NULL,  x REAL NOT NULL,  y REAL NOT NULL,  z REAL NOT NULL,  accuracy REAL,  PRIMARY KEY (uuid, epoch, sysnano, eventnano));
INSERT INTO lacc SELECT * FROM lacc_backup;
DROP TABLE lacc_backup;
COMMIT;

BEGIN TRANSACTION;
CREATE TEMPORARY TABLE gravity_backup(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL,  eventnano INTEGER NOT NULL,  x REAL NOT NULL,  y REAL NOT NULL,  z REAL NOT NULL,  accuracy REAL,  PRIMARY KEY (uuid, epoch));
INSERT INTO gravity_backup SELECT * FROM gravity;
DROP TABLE gravity;
CREATE TABLE gravity(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL,  eventnano INTEGER NOT NULL,  x REAL NOT NULL,  y REAL NOT NULL,  z REAL NOT NULL,  accuracy REAL,  PRIMARY KEY (uuid, epoch, sysnano, eventnano));
INSERT INTO gravity SELECT * FROM gravity_backup;
DROP TABLE gravity_backup;
COMMIT;

BEGIN TRANSACTION;
CREATE TEMPORARY TABLE gyro_backup(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL,  eventnano INTEGER NOT NULL,  x REAL NOT NULL,  y REAL NOT NULL,  z REAL NOT NULL,  accuracy REAL,  PRIMARY KEY (uuid, epoch));
INSERT INTO gyro_backup SELECT * FROM gyro;
DROP TABLE gyro;
CREATE TABLE gyro(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL,  eventnano INTEGER NOT NULL,  x REAL NOT NULL,  y REAL NOT NULL,  z REAL NOT NULL,  accuracy REAL,  PRIMARY KEY (uuid, epoch, sysnano, eventnano));
INSERT INTO gyro SELECT * FROM gyro_backup;
DROP TABLE gyro_backup;
COMMIT;

BEGIN TRANSACTION;
CREATE TEMPORARY TABLE rotation_backup(uuid TEXT NOT NULL, epoch INTEGER NOT NULL, sysnano INTEGER NOT NULL, eventnano INTEGER NOT NULL, xr REAL NOT NULL, yr REAL NOT NULL, zr REAL NOT NULL, cos REAL, head_accuracy REAL, accuracy REAL, PRIMARY KEY (uuid, epoch));
INSERT INTO rotation_backup SELECT * FROM rotation;
DROP TABLE rotation;
CREATE TABLE rotation(uuid TEXT NOT NULL, epoch INTEGER NOT NULL, sysnano INTEGER NOT NULL, eventnano INTEGER NOT NULL, xr REAL NOT NULL, yr REAL NOT NULL, zr REAL NOT NULL, cos REAL, head_accuracy REAL, accuracy REAL, PRIMARY KEY (uuid, epoch, sysnano, eventnano));
INSERT INTO rotation SELECT * FROM rotation_backup;
DROP TABLE rotation_backup;
COMMIT;

BEGIN TRANSACTION;
CREATE TEMPORARY TABLE magnetic_backup(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL,  eventnano INTEGER NOT NULL,  x REAL NOT NULL,  y REAL NOT NULL,  z REAL NOT NULL,  accuracy REAL,  PRIMARY KEY (uuid, epoch));
INSERT INTO magnetic_backup SELECT * FROM magnetic;
DROP TABLE magnetic;
CREATE TABLE magnetic(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL,  eventnano INTEGER NOT NULL,  x REAL NOT NULL,  y REAL NOT NULL,  z REAL NOT NULL,  accuracy REAL,  PRIMARY KEY (uuid, epoch, sysnano, eventnano));
INSERT INTO magnetic SELECT * FROM magnetic_backup;
DROP TABLE magnetic_backup;
COMMIT;

BEGIN TRANSACTION;
CREATE TEMPORARY TABLE light_backup(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL, eventnano INTEGER NOT NULL, light REAL NOT NULL, accuracy REAL, PRIMARY KEY (uuid, epoch));
INSERT INTO light_backup SELECT * FROM light;
DROP TABLE light;
CREATE TABLE light(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL, eventnano INTEGER NOT NULL, light REAL NOT NULL, accuracy REAL, PRIMARY KEY (uuid, epoch, sysnano, eventnano));
INSERT INTO light SELECT * FROM light_backup;
DROP TABLE light_backup;
COMMIT;

BEGIN TRANSACTION;
CREATE TEMPORARY TABLE temp_backup(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL, eventnano INTEGER NOT NULL, temp REAL NOT NULL, accuracy REAL, PRIMARY KEY (uuid, epoch));
INSERT INTO temp_backup SELECT * FROM temp;
DROP TABLE temp;
CREATE TABLE temp(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL, eventnano INTEGER NOT NULL, temp REAL NOT NULL, accuracy REAL, PRIMARY KEY (uuid, epoch, sysnano, eventnano));
INSERT INTO temp SELECT * FROM temp_backup;
DROP TABLE temp_backup;
COMMIT;

BEGIN TRANSACTION;
CREATE TEMPORARY TABLE pressure_backup(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL, eventnano INTEGER NOT NULL, pressure REAL NOT NULL, accuracy REAL, PRIMARY KEY (uuid, epoch));
INSERT INTO pressure_backup SELECT * FROM pressure;
DROP TABLE pressure;
CREATE TABLE pressure(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL, eventnano INTEGER NOT NULL, pressure REAL NOT NULL, accuracy REAL, PRIMARY KEY (uuid, epoch, sysnano, eventnano));
INSERT INTO pressure SELECT * FROM pressure_backup;
DROP TABLE pressure_backup;
COMMIT;

BEGIN TRANSACTION;
CREATE TEMPORARY TABLE proximity_backup(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL, eventnano INTEGER NOT NULL, proximity REAL NOT NULL, accuracy REAL, PRIMARY KEY (uuid, epoch));
INSERT INTO proximity_backup SELECT * FROM proximity;
DROP TABLE proximity;
CREATE TABLE proximity(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL, eventnano INTEGER NOT NULL, proximity REAL NOT NULL, accuracy REAL, PRIMARY KEY (uuid, epoch, sysnano, eventnano));
INSERT INTO proximity SELECT * FROM proximity_backup;
DROP TABLE proximity_backup;
COMMIT;

BEGIN TRANSACTION;
CREATE TEMPORARY TABLE humidity_backup(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL, eventnano INTEGER NOT NULL, humidity REAL NOT NULL, accuracy REAL, PRIMARY KEY (uuid, epoch));
INSERT INTO humidity_backup SELECT * FROM humidity;
DROP TABLE humidity;
CREATE TABLE humidity(uuid TEXT NOT NULL, epoch INTEGER NOT NULL,  sysnano INTEGER NOT NULL, eventnano INTEGER NOT NULL, humidity REAL NOT NULL, accuracy REAL, PRIMARY KEY (uuid, epoch, sysnano, eventnano));
INSERT INTO humidity SELECT * FROM humidity_backup;
DROP TABLE humidity_backup;
COMMIT;

