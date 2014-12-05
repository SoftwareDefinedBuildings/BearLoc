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

# calculate frequencies
for mac, mac_data in mac_timestamps.iteritems():
    timestamps, ssid = mac_data
    timestamps.sort()
    freqs = [1000.0/(timestamps[i] - timestamps[i-1]) for i in range(1, len(timestamps))]
    mac_data.append(freqs)

# generate plot data
plot_macs = []
plot_freqs = []
plot_lows = []
plot_ups = []
for mac, (_, _, freqs) in mac_timestamps.iteritems():
    if len(freqs) > 0:
        plot_macs.append(mac)
        plot_freqs.append(np.percentile(freqs, 50))
        plot_lows.append(np.percentile(freqs, 5))
        plot_ups.append(np.percentile(freqs, 95))

plot_freqs, plot_lows, plot_ups = zip(*sorted(zip(plot_freqs, plot_lows, plot_ups), key = lambda x: x[0], reverse=True))
plow_lows = [plot_freqs[i] - plot_lows[i] for i in range(len(plot_freqs))]
plow_ups = [plot_ups[i] - plot_freqs[i] for i in range(len(plot_freqs))]

ind = np.arange(len(plot_freqs))
width = 0.8
plt.xticks(rotation=90)
plt.bar(ind, plot_freqs, width, alpha=0.5, yerr=[plot_lows, plot_ups], error_kw=dict(ecolor='k'))
plt.ylabel('Frequencies (Hz)')
plt.title('Wi-Fi Access Points Appearance Frequencies')
plt.xticks(ind+width/2., plot_macs)
#plt.yticks(np.arange(0,81,10))

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
