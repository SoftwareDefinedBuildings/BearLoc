import numpy as np
import matplotlib.pyplot as plt
import sys
import os
import datetime as dt
import matplotlib.dates as md
from sklearn.neighbors.kde import KernelDensity
from matplotlib.dates import date2num, num2date

dataf = sys.argv[1] # lowest level of folder that contains csv files
mac_order = int(sys.argv[2]) # lowest level of folder that contains csv files

# find mostly appeared MAC
mac_cnts = {}
for fname in os.listdir(dataf):
    if ~fname.endswith("csv") and "metadata" in fname:
        continue
    with open(os.path.join(dataf, fname)) as f:
        header = f.readline() # header
        lines = f.readlines()
        for l in lines:
            vals = l.strip().split(",")
            mac = vals[-3]
            if mac in mac_cnts:
                mac_cnts[mac][0] += 1
            else:
                mac_cnts[mac] = [0, ",".join(vals[1:-4])]
target_mac = sorted(mac_cnts.items(), key = lambda x: x[1][0], reverse=True)[mac_order][0]
target_net = sorted(mac_cnts.items(), key = lambda x: x[1][0], reverse=True)[mac_order][1][1]

timestamps = []
cur_timestamp = 0
miss = False
for fname in os.listdir(dataf):
    if ~fname.endswith("csv") and "metadata" in fname:
        continue
    with open(os.path.join(dataf, fname)) as f:
        header = f.readline() # header
        lines = f.readlines()
        for l in lines:
            vals = l.strip().split(",")
            next_timestamp = float(vals[0])
            mac = vals[-3]
            if mac == target_mac:
                miss = False
            if cur_timestamp != next_timestamp:
                if miss:
                    timestamps.append(cur_timestamp)
                cur_timestamp = next_timestamp
                miss = True

ax=plt.gca()
xfmt = md.DateFormatter('%a %H:%M')
ax.xaxis.set_major_formatter(xfmt)

dates=[dt.datetime.fromtimestamp(ts/1000) for ts in timestamps]
plt.hist(date2num(dates), 100, alpha=0.8)
#plt.scatter(dates, [1]*len(dates), alpha=0.05, color='r', edgecolors='none')

plt.xlim([min(dates), max(dates)])
plt.title(target_net + " " + target_mac)
#plt.xlabel("Time")
plt.ylabel("Counts")
plt.show()
plt.close()
