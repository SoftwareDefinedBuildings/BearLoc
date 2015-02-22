import numpy as np
from sklearn import cross_validation
import sys

dataf = sys.argv[1]
#check appearance possibility in every aggr_num scan
aggr_num = int(sys.argv[2])

with open(dataf) as f:
    header = f.readline()
    num_mac = len(header.strip().split(",")) - 9
    targets = []
    for l in f.readlines():
        splits = l.strip().split(",")
        targets.append(splits[-5])

cv = cross_validation.StratifiedKFold(targets, n_folds=2)
cv_index = [index for index in cv]
_, train_index = cv_index[0]
_, test_index = cv_index[1]

rm_2_cols = lambda x: ",".join(x.strip().split(",")[:-9]) + "\n"

f_train = open("data.train.csv", "w")
f_train.write(rm_2_cols(header))
f_test = open("data.test.csv", "w")
f_test.write(rm_2_cols(header))

i = 0
train_counter = {r:[[0.0, 0] for _ in range(num_mac)] for r in set(targets)}
test_counter = {r:[[0.0, 0] for _ in range(num_mac)] for r in set(targets)}
with open(dataf) as f:
    header = f.readline()
    for l in f.readlines():
        if i in train_index:
            vals = l.strip().split(",")
            room = vals[-5]
            for j in range(num_mac):
                if vals[j] != '100':
                    train_counter[room][j][0] += 1
                train_counter[room][j][1] += 1
            if train_counter[room][0][1] >= aggr_num:
                entry = [str(x[0]/x[1]) for x in train_counter[room]] + [room]
                f_train.write(",".join(entry) + '\n')
                train_counter[room] = [[0.0, 0] for _ in range(num_mac)]
        elif i in test_index:
            vals = l.strip().split(",")
            room = vals[-5]
            for j in range(num_mac):
                if vals[j] != '100':
                    test_counter[room][j][0] += 1
                test_counter[room][j][1] += 1
            if test_counter[room][0][1] >= aggr_num:
                entry = [str(x[0]/x[1]) for x in test_counter[room]] + [room]
                f_test.write(",".join(entry) + '\n')
                test_counter[room] = [[0.0, 0] for _ in range(num_mac)]
        i += 1

f_train.close()
f_test.close()
