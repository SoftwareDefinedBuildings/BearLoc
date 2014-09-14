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
	appeared_times = []
	data_list = sorted(data_list, key=lambda x:x[3])
	for group in groupby(data_list, lambda x:x[3]):
		print group[0]
		group_data = list(group[1])
		group_data = sorted(group_data, key=lambda x:int(x[0]))
		appeared_times.append(len(group_data))
	plot(appeared_times)	


def plot(appeared_times):
	print "ploting..."
	X = sorted(appeared_times)
	Y = []
	l = len(X)
	Y.append(float(1)/l)
	for i in range(2,l+1):
	    Y.append(float(1)/l+Y[i-2])
	pyplot.plot(X,Y,marker='o',label='xyz')
	pyplot.show()


if __name__ == "__main__":
	groupby_macaddr()
