import numpy as np
import matplotlib.pyplot as plt
import sys
import os
import datetime as dt
import matplotlib.dates as md
from sklearn.neighbors.kde import KernelDensity
from matplotlib.dates import date2num, num2date
from pytimeparse.timeparse import timeparse

dataf = sys.argv[1] # lowest level of folder that contains csv files
mac_order = int(sys.argv[2])
start_timedelta = None
stop_timedelta = None
if len(sys.argv) >= 5:
  start_timedelta = dt.timedelta(seconds=timeparse(sys.argv[3]))
  stop_timedelta = dt.timedelta(seconds=timeparse(sys.argv[4]))

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

recv_timestamps = []
miss_timestamps = []
cur_timestamp = 0
rssis = []
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
                rssis.append(int(vals[-1]))
            if cur_timestamp != next_timestamp:
                if miss:
                    if cur_timestamp != 0:
                        miss_timestamps.append(cur_timestamp)
                else:
                    if cur_timestamp != 0:
                        recv_timestamps.append(cur_timestamp)
                cur_timestamp = next_timestamp
                miss = True

ax=plt.gca()
xfmt = md.DateFormatter('%a %H:%M')
ax.xaxis.set_major_formatter(xfmt)

miss_dates = [dt.datetime.fromtimestamp(ts/1000) for ts in miss_timestamps]
recv_dates = [dt.datetime.fromtimestamp(ts/1000) for ts in recv_timestamps]
start_date = min(min(miss_dates), min(recv_dates))
stop_date = max(max(miss_dates), max(recv_dates))

if start_timedelta and stop_timedelta:
    ff = lambda x: start_date + start_timedelta < x < start_date + stop_timedelta
    recv_dates = filter(ff, recv_dates)
    miss_dates = filter(ff, miss_dates)
data = [date2num(recv_dates), date2num(miss_dates)]
plt.hist(data, 100, histtype='bar', stacked=True, alpha=0.8, color=['g', 'r'], label=['Received', "Missing"])
#plt.scatter(dates, [1]*len(dates), alpha=0.05, color='r', edgecolors='none')
plt.legend(bbox_to_anchor=(.78, 1), loc=2, borderaxespad=0.)

plt.xlim([min(min(miss_dates), min(recv_dates)), max(max(miss_dates), max(recv_dates))])
plt.title(target_net + " " + target_mac + " RSSI: " + '%.2f' % (np.mean(rssis)) + "+/-" + '%.2f' % (np.std(rssis)) + " dBm")
#plt.xlabel("Time")
plt.ylabel("Counts")
plt.tight_layout()
plt.show()
plt.close()
