import numpy as np
import matplotlib.pyplot as plt
import sys

dataf = sys.argv[1]
num_rooms = len(sys.argv) - 2
rooms = []
for i in range(num_rooms):
    rooms.append(sys.argv[i+2])
nrows = 4
ncols = 6

data_by_mac = {} # {mac: {room: []}}
total_cnt = 0
missing_cnt = 0
with open(dataf) as f:
    header = f.readline() # header
    macs = header.strip().split(",")[2:-1] # work with data.csv version 2
    data_by_mac = {m:{r:[0.0, 0, 0, []] for r in rooms} for m in macs} # appear count, scan count, count start time, apearance possibility history
    for l in f.readlines():
        vals = l.strip().split(",")
        timestamp = int(vals[0])
        room = vals[-1]
        if room not in rooms:
            continue
        for i in range(len(macs)):
            mac = macs[i]
            rssi_str = vals[i+2]
            if rssi_str != "?":
                data_by_mac[mac][room][0] += 1
            data_by_mac[mac][room][1] += 1
            start_timestamp = data_by_mac[mac][room][2]
            if start_timestamp == 0:
                data_by_mac[mac][room][2] = timestamp
            if timestamp - start_timestamp > 10000: # calculate appearance after X ms
                appear = data_by_mac[mac][room][0]/data_by_mac[mac][room][1]
                data_by_mac[mac][room][3].append(appear)
                data_by_mac[mac][room][2] = timestamp
                data_by_mac[mac][room][0] = 0.0
                data_by_mac[mac][room][1] = 0


fig, axes = plt.subplots(nrows=nrows, ncols=ncols)
sorted_data = sorted(data_by_mac.items(), key=lambda x: sum(map(lambda y: np.mean(y[3]), x[1].values())), reverse=True) # sort by appearance 
count = 0
for mac, mac_data in sorted_data:
    #if mac not in ['00:22:90:39:07:15']:
    #    continue
    plot_data = [mac_data[r][3] for r in rooms]
    if nrows*ncols == 1:
        axes = np.array([[axes]])
    axes[count/ncols, count%ncols].boxplot(plot_data, labels=rooms)
    axes[count/ncols, count%ncols].set_title(mac)
    count += 1
    if count == nrows*ncols:
        break

plt.show()
