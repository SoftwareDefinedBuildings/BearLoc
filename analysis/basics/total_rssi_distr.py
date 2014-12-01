import numpy as np
import matplotlib.pyplot as plt
import sys

dataf = sys.argv[1]

X = []
total_cnt = 0
missing_cnt = 0
with open(dataf) as f:
    header = f.readline() # header
    lines = f.readlines()
    for l in lines:
        vals = l.strip().split(",")
        X.extend([int(x) for x in vals[1:-1] if x != '?'])
        missing_cnt += vals.count('?')
        total_cnt += len(vals) - 2

print "missing count", missing_cnt
print "total count", total_cnt

X = np.array(X)

fig = plt.figure()
ax = fig.add_subplot(111)

numBins = 50
ax.hist(X, numBins, alpha=0.8)

plt.title('Total Received Signal Strengths Distribution')
plt.xlabel('Received Signal Strengths (dBm)')
plt.ylabel('Counts')
plt.tight_layout()
plt.show()
