
BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS semloc_bak (uuid TEXT NOT NULL, epoch INTEGER NOT NULL, country TEXT, state TEXT, city TEXT, street TEXT, building TEXT, floor TEXT, room TEXT, PRIMARY KEY (uuid, epoch));
INSERT INTO semloc_bak SELECT uuid, epoch, country, state, city, street, building, floor, room FROM semloc;
DROP TABLE semloc;
CREATE TABLE IF NOT EXISTS semloc (uuid TEXT NOT NULL, epoch INTEGER NOT NULL, country TEXT, state TEXT, city TEXT, street TEXT, building TEXT, floor TEXT, room TEXT, PRIMARY KEY (uuid, epoch));
INSERT INTO semloc SELECT uuid, epoch, country, state, city, street, building, floor, room FROM semloc_bak;
DROP TABLE semloc_bak;
COMMIT;