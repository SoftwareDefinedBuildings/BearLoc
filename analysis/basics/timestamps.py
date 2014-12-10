import numpy as np
import matplotlib.pyplot as plt
import sys
import time
import matplotlib.dates as md
import datetime as dt

dataf = sys.argv[1]

#min_timestamp = int(time.time()) * 1000
#max_timestamp = 0
timestamps = {} # {phone: [timestamp]}
with open(dataf) as f:
    header = f.readline() # header
    for l in f.readlines():
        vals = l.strip().split(",")
        timestamp = int(vals[0])
        phone = vals[1]
        if phone not in timestamps:
            timestamps[phone] = []
        timestamps[phone].append(timestamp)
#        if timestamp < min_timestamp:
#            min_timestamp = timestamp
#        if timestamp > max_timestamp:
#            max_timestamp = timestamp

#ax=plt.gca()
xfmt = md.DateFormatter('%a %H:%M')
#ax.xaxis.set_major_formatter(xfmt)

phones = timestamps.keys()
num_phones = len(phones)
f, axarr = plt.subplots(num_phones)
for i in range(num_phones):
    data = [x/1000 for x in timestamps[phones[i]]]
    axarr[i].hist(data, 100, alpha=0.8)
    #axarr[i].xaxis.set_major_formatter(xfmt)
axarr[0].set_title('Timestmaps')

plt.tight_layout()
plt.show()
plt.close()
