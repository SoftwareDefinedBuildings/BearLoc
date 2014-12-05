import numpy as np
import matplotlib.pyplot as plt
import sys
import os
import datetime as dt
import matplotlib.dates as md

dataf = sys.argv[1] # lowest level of folder that contains csv files

# get timestmaps
mac_timestamps = {}
for fname in os.listdir(dataf):
    if ~fname.endswith("csv") and "metadata" in fname:
        continue
    with open(os.path.join(dataf, fname)) as f:
        header = f.readline() # header
        lines = f.readlines()
        for l in lines:
            vals = l.strip().split(",")
            timestamp = int(vals[0])
            mac = vals[-3]
            if mac in mac_timestamps:
                mac_timestamps[mac][0].append(timestamp)
            else:
                mac_timestamps[mac] = [[], ",".join(vals[1:-4])]

# calculate span
for mac, mac_data in mac_timestamps.iteritems():
    timestamps, ssid = mac_data
    timestamps.sort()
    span = 0
    if len(timestamps) > 0:
        span = timestamps[-1] - timestamps[0]
    mac_data.append(span)

# generate plot data
plot_macs = []
plot_spans = []
for mac, (_, _, span) in mac_timestamps.iteritems():
    plot_macs.append(mac)
    plot_spans.append(span/1000/3600)

plot_spans, plot_macs = zip(*sorted(zip(plot_spans, plot_macs), key = lambda x: x[0], reverse=True))

ind = np.arange(len(plot_spans))
width = 0.8
plt.xticks(rotation=90)
plt.bar(ind, plot_spans, width, alpha=0.5)
plt.ylabel('Appearance Duration (hour)')
plt.title('Wi-Fi Access Points Appearance Durations')
plt.xticks(ind+width/2., plot_macs)
#plt.yticks(np.arange(0,81,10))
plt.tight_layout()
plt.show()


# timestamps = []
# rssis = []
# for fname in os.listdir(dataf):
#     if ~fname.endswith("csv") and "metadata" in fname:
#         continue
#     with open(os.path.join(dataf, fname)) as f:
#         header = f.readline() # header
#         lines = f.readlines()
#         for l in lines:
#             vals = l.strip().split(",")
#             mac = vals[-3]
#             if mac == max_mac:
#                 timestamps.append(float(vals[0]))
#                 rssis.append(int(vals[-1]))
# 
# ax=plt.gca()
# xfmt = md.DateFormatter('%a %H:%M')
# ax.xaxis.set_major_formatter(xfmt)
# 
# dates=[dt.datetime.fromtimestamp(ts/1000) for ts in timestamps]
# plt.scatter(dates, rssis, alpha=0.05, edgecolors='none')
# 
# plt.xlim([min(dates), max(dates)])
# plt.ylim([min(rssis), max(rssis)])
# plt.title(max_net + " " + max_mac)
# #plt.xlabel("Time")
# plt.ylabel("Recevied Signal Strengths (dBm)")
# plt.show()
