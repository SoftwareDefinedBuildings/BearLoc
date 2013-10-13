# Convert the old semloc table to the new one
# Do this to the old semloc for only once

import sqlite3
conn = sqlite3.connect('bearloc.db')

c = conn.cursor()

c.execute("ALTER TABLE semloc RENAME TO oldsemloc;")

c.execute("CREATE TABLE semloc \
           (uuid TEXT NOT NULL, \
            epoch INTEGER NOT NULL, \
            country TEXT, \
            state TEXT, \
            city TEXT, \
            street TEXT, \
            district TEXT, \
            building TEXT, \
            floor TEXT, \
            room TEXT, \
            PRIMARY KEY (uuid, epoch));")

c.execute("SELECT * FROM oldsemloc")
oldsemlocs = c.fetchall()

uuidepochs = list(set([oldsemloc[0:2] for oldsemloc in oldsemlocs]))
semlocs = {(uuid, epoch):{oldsemloc[2]:oldsemloc[3] for oldsemloc in oldsemlocs if oldsemloc[0]==uuid and oldsemloc[1]==epoch}  for uuid,epoch in uuidepochs}

for uuid, epoch in semlocs.keys():
  semloc = semlocs[(uuid, epoch)]
  data = (uuid, epoch, semloc["country"], semloc["state"], semloc["city"], semloc["street"], semloc["district"], semloc["building"], semloc["floor"], semloc["room"])
  operation = "INSERT OR REPLACE INTO semloc VALUES (?,?,?,?,?,?,?,?,?,?)"
  c.execute(operation, data)

c.execute("DROP TABLE oldsemloc;")

conn.commit()
conn.close()
