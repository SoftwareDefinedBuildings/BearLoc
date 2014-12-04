import numpy as np
import matplotlib.pyplot as plt
import sys
import os
import datetime as dt
import matplotlib.dates as md

dataf = sys.argv[1] # lowest level of folder that contains csv files
mac_order = int(sys.argv[2]) 

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
max_mac = sorted(mac_cnts.items(), key = lambda x: x[1][0], reverse=True)[mac_order][0]
max_net = sorted(mac_cnts.items(), key = lambda x: x[1][0], reverse=True)[mac_order][1][1]

timestamps = []
rssis = []
for fname in os.listdir(dataf):
    if ~fname.endswith("csv") and "metadata" in fname:
        continue
    with open(os.path.join(dataf, fname)) as f:
        header = f.readline() # header
        lines = f.readlines()
        for l in lines:
            vals = l.strip().split(",")
            mac = vals[-3]
            if mac == max_mac:
                timestamps.append(float(vals[0]))
                rssis.append(int(vals[-1]))

ax=plt.gca()
xfmt = md.DateFormatter('%a %H:%M')
ax.xaxis.set_major_formatter(xfmt)

dates=[dt.datetime.fromtimestamp(ts/1000) for ts in timestamps]
plt.scatter(dates, rssis, alpha=0.05, edgecolors='none')

plt.xlim([min(dates), max(dates)])
plt.ylim([min(rssis), max(rssis)])
plt.title(max_net + " " + max_mac)
#plt.xlabel("Time")
plt.ylabel("Recevied Signal Strengths (dBm)")
plt.show()
