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

def groupby_epoch():
	data_list = import_data()
	scan = []
	data_list = sorted(data_list, key=lambda x:x[0])
	for group in groupby(data_list, lambda x:x[0]):
		group_data = list(group[1])
		group_data = sorted(group_data, key=lambda x:int(x[0]))
		scan.append(len(group_data))
	plot(scan)	


def plot(scan):
	print "ploting..."
	X = sorted(scan)
	Y = []
	l = len(X)
	Y.append(float(1)/l)
	for i in range(2,l+1):
	    Y.append(float(1)/l+Y[i-2])
	pyplot.plot(X,Y,marker='o',label='xyz')
	pyplot.show()


if __name__ == "__main__":
	groupby_epoch()
