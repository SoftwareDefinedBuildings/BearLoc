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

def groupby_macaddr(percent):
    data_list = import_data()
    ap_list = get_number_ap(data_list)
    print len(ap_list)
    time_to_ap = {}
    interval_list = []
    data_list = sorted(data_list, key=lambda x:x[0])
    for group in groupby(data_list, lambda x:x[0]):
        group_data = list(group[1])
        ap_current = get_ap_macaddr(group_data)
        time_to_ap[group[0]] = ap_current
    time_list = sorted(time_to_ap, key=lambda key:int(key))
    init_counter = 0
    for init_time in time_list:
        terminate = False
        goal = percent*len(ap_list)
        current = time_to_ap[init_time]
        ap_number = len(current)
        counter = init_counter+1
        while ap_number<goal:
            if counter == len(time_list):
                terminate = True
                print "got here"
                break;
            current = union(current, time_to_ap[time_list[counter]])
            ap_number = len(current)
            counter += 1
        if terminate:
            break;
        # print counter
        interval = int(time_list[counter-1]) - int(time_list[init_counter])
        interval_list.append(interval)
        init_counter += 1
    #   group_data = sorted(group_data, key=lambda x:int(x[0]))
    if len(interval_list)==0:
        print "no"
    else:
        plot(interval_list)

def get_number_ap(data):
    data_list = data
    ap_list = []
    data_list = sorted(data_list, key=lambda x:x[3])
    for group in groupby(data_list, lambda x:x[3]):
        ap_list.append(group[0])
    return ap_list


def get_ap_macaddr(data):
    time_list = [data[i][3] for i in range(len(data)-1)]
    return time_list


def union(list1, list2):
    return list(set(list1) | set(list2))

def plot(interval_list):
    # print interval_list
    print "ploting..."
    X = sorted(interval_list)
    # X = filter(lambda x:x<10000, X)
    Y = []
    l = len(X)
    Y.append(float(1)/l)
    for i in range(2,l+1):
        Y.append(float(1)/l+Y[i-2])
    pyplot.plot(X,Y,marker='o',label='xyz')
    pyplot.show()


if __name__ == "__main__":
    groupby_macaddr(0.7)
