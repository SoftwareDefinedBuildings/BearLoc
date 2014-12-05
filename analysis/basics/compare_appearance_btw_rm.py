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
    macs = header.strip().split(",")[1:-1]
    data_by_mac = {m:{r:[0.0, 0] for r in rooms} for m in macs}
    for l in f.readlines():
        vals = l.strip().split(",")
        room = vals[-1]
        if room not in rooms:
            continue
        for i in range(len(macs)):
            mac = macs[i]
            rssi_str = vals[i+1]
            if rssi_str != "?":
                data_by_mac[mac][room][0] += 1
            data_by_mac[mac][room][1] += 1

fig, axes = plt.subplots(nrows=nrows, ncols=ncols)
sorted_data = sorted(data_by_mac.items(), key=lambda x: sum(map(lambda y: y[0]/y[1], x[1].values())), reverse=True) # sort by appearance 
count = 0
for mac, mac_data in sorted_data:
    #if mac not in ['00:22:90:39:07:15']:
    #    continue
    plot_data = [mac_data[r][0]/mac_data[r][1] for r in rooms]
    ind = np.arange(len(plot_data))  # the x locations for the groups
    width = 0.8       # the width of the bars
    if nrows*ncols == 1:
        axes = np.array([[axes]])
    axes[count/ncols, count%ncols].bar(ind, plot_data, width)
    #axes[count/ncols, count%ncols].xticks(ind+width/2., rooms)
    axes[count/ncols, count%ncols].set_title(mac)
    count += 1
    if count == nrows*ncols:
        break

plt.show()
