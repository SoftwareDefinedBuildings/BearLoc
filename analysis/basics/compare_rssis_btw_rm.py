import numpy as np
import matplotlib.pyplot as plt
import sys

dataf = sys.argv[1]
num_rooms = len(sys.argv) - 2
rooms = []
for i in range(num_rooms):
    rooms.append(sys.argv[i+2])
nrows = 1
ncols = 1

data_by_mac = {} # {mac: {room: []}}
total_cnt = 0
missing_cnt = 0
with open(dataf) as f:
    header = f.readline() # header
    macs = header.strip().split(",")[1:-1]
    data_by_mac = {m:{r:[] for r in rooms} for m in macs}
    for l in f.readlines():
        vals = l.strip().split(",")
        room = vals[-1]
        if room not in rooms:
            continue
        for i in range(len(macs)):
            rssi_str = vals[i+1]
            if rssi_str != "?":
                data_by_mac[macs[i]][room].append(int(rssi_str))

fig, axes = plt.subplots(nrows=nrows, ncols=ncols)
sorted_data = sorted(data_by_mac.items(), key=lambda x: sum(map(len, x[1].values())), reverse=True) # sort by rssi count
count = 0
for mac, mac_data in sorted_data:
    #if mac not in ['fa:8f:ca:71:6c:0c']:
    #if mac not in ['00:17:df:a7:4c:f0']:
    #if mac not in ['78:54:2e:af:15:a0']:
    if mac not in ['00:22:90:39:07:15']: # bad MAC
        continue
    plot_data = [mac_data[r] for r in rooms]
    if nrows*ncols == 1:
        axes = np.array([[axes]])
    axes[count/ncols, count%ncols].boxplot(plot_data, labels=rooms)
    axes[count/ncols, count%ncols].set_title(mac)
    count += 1
    if count == nrows*ncols:
        break

plt.show()
