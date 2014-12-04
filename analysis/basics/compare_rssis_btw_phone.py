import numpy as np
import matplotlib.pyplot as plt
import sys
from pytimeparse.timeparse import timeparse
import datetime as dt

dataf = sys.argv[1]
target_room = sys.argv[2]
start_timedelta = None
stop_timedelta = None
if len(sys.argv) >= 5:
  start_timedelta = dt.timedelta(seconds=timeparse(sys.argv[3]))
  stop_timedelta = dt.timedelta(seconds=timeparse(sys.argv[4]))

nrows = 4
ncols = 6

data_by_mac = {} # {mac: {phone: []}}
total_cnt = 0
missing_cnt = 0
phones = []
start_timestamps = {}
with open(dataf) as f:
    header = f.readline() # header
    macs = header.strip().split(",")[2:-1] # data v2
    data_by_mac = {m:{} for m in macs}
    for l in f.readlines():
        vals = l.strip().split(",")
        timestamp = int(vals[0])
        timestamp = dt.datetime.fromtimestamp(timestamp/1000)
        room = vals[-1]
        phone = vals[1]
        if phone not in phones:
            phones.append(phone)
        if room != target_room:
            continue
        if phone not in start_timestamps:
            start_timestamps[phone] = timestamp
        if timestamp < start_timestamps[phone] + start_timedelta \
           or \
           timestamp > start_timestamps[phone] + stop_timedelta:
            continue
        for i in range(len(macs)):
            if phone not in data_by_mac[macs[i]]:
                data_by_mac[macs[i]][phone] = []
            rssi_str = vals[i+2]
            if rssi_str != "?":
                data_by_mac[macs[i]][phone].append(int(rssi_str))

fig, axes = plt.subplots(nrows=nrows, ncols=ncols)
sorted_data = sorted(data_by_mac.items(), key=lambda x: sum(map(len, x[1].values())), reverse=True) # sort by rssi count
count = 0
for mac, mac_data in sorted_data:
    #if mac not in ['00:22:90:39:07:15']: # bad MAC
    #    continue
    plot_data = [mac_data[p] for p in phones]
    if nrows*ncols == 1:
        axes = np.array([[axes]])
    axes[count/ncols, count%ncols].boxplot(plot_data, labels=phones)
    axes[count/ncols, count%ncols].set_title(mac)
    count += 1
    if count == nrows*ncols:
        break

plt.show()
