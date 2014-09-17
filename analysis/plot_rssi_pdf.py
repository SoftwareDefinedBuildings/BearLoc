import operator
import glob
import os
import csv
import numpy as np

from itertools import groupby
from matplotlib import pyplot 

def import_data():
	os.chdir("BearLoc1398720331923")
	return_list = []
	for filename in glob.glob("*.csv"):
		if ("metadata" not in filename):
			with open(filename, 'rb') as csvfile:
				reader = csv.reader(csvfile, delimiter=',')
				next(reader, None)
				for row in reader:
					return_list.append(row)
	return return_list


def groupby_macaddr():
	data_list = import_data()
	data_list = sorted(data_list, key=lambda x:x[3])
	for group in groupby(data_list, lambda x:x[3]):
		# print group[0]
		group_data = list(group[1])
		# group_data = sorted(group_data, key=lambda x:int(x[0]))
		rssi_point = [int(group_data[i][-1]) for i in range(len(group_data))]
		# print rssi_point
		if len(rssi_point)>1000:
			plot(rssi_point)
			break	


def plot(rssi_point):
	print "ploting..."
	# print rssi_point
	pyplot.hist(rssi_point)
	fig1 = pyplot.gcf()
	pyplot.draw()
	fig1.savefig('tessstttyyy.png', dpi=100)

if __name__ == "__main__":
	groupby_macaddr()