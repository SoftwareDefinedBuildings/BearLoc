import sqlite3
from pymongo import MongoClient


client = MongoClient()
db = client.bearloc
data = db.data

conn = sqlite3.connect('bearloc.DOP.wifi.db')
c = conn.cursor()
c.execute('''SELECT * from wifi''')


for record in c.fetchall():
	post = {'type': 'wifi', 
			'id': record[0], 
			'epoch': record[1], 
			'BSSID': record[2], 
			'SSID': record[3], 
			'RSSI': record[4], 
			'capability': record[5], 
			'frequency': record[6]}
	data.insert(post)


conn = sqlite3.connect('bearloc.DOP.wifi.db')
c = conn.cursor()
c.execute('''SELECT * from semloc''')

for record in c.fetchall():
	post = {'type': 'reported semloc', 
			'id': record[0], 
			'epoch': record[1], 
			'country': record[2], 
			'state': record[3], 
			'city': record[4], 
			'street': record[5], 
			'building': record[6],
			'locale': record[8]}
	data.insert(post)