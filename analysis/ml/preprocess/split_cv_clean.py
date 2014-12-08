import numpy as np
from sklearn import cross_validation
import sys

dataf = sys.argv[1]
num_keep = int(sys.argv[2])

# count MACs
mac_cnt = {}  # {room: [mac_count]}
with open(dataf) as f:
    header = f.readline()
    num_mac = len(header.strip().split(",")) - 3
    targets = []
    for l in f.readlines():
        splits = l.strip().split(",")
        room = splits[-1]
        if room not in mac_cnt:
            mac_cnt[room] = [0] * num_mac
        for i in range(2, len(splits)-1):
            if splits[i] != "?":
                mac_cnt[room][i-2] += 1

# get indexes of most appeared MACs
mac_idx = {} # {room: [mac indexes]}
for room, cnts in mac_cnt.iteritems():
    mac_idx[room] = sorted(range(len(cnts)), key=lambda i: cnts[i], reverse=True)[0:num_keep]
total_mac_idx = []
for room, idx in mac_idx.iteritems():
    total_mac_idx.extend(idx)
total_mac_idx = list(set(total_mac_idx))


with open(dataf) as f:
    header = f.readline()
    targets = []
    for l in f.readlines():
        splits = l.strip().split(",")
        targets.append(splits[-1])

cv = cross_validation.StratifiedKFold(targets, n_folds=100)
cv_index = [index for index in cv]
_, train_index = cv_index[0]
_, test_index = cv_index[99]

rm_2_cols_header = lambda x: ",".join(x.strip().split(",")[2:]) + "\n"

def rm_2_cols_data(x):
    vals = x.strip().split(",")
    room = vals[-1]
    data = vals[2:-1]
    data = [data[i] if i in total_mac_idx else '?' for i in range(len(data))]
    return ",".join(data + [room]) + "\n"

f_train = open("data.train.csv", "w")
f_train.write(rm_2_cols_header(header))
f_test = open("data.test.csv", "w")
f_test.write(rm_2_cols_header(header))

i = 0
with open(dataf) as f:
    header = f.readline()
    for l in f.readlines():
        if i in train_index:
            f_train.write(rm_2_cols_data(l))
        elif i in test_index:
            f_test.write(rm_2_cols_data(l))
        i += 1

f_train.close()
f_test.close()
