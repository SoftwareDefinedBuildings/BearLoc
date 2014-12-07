import numpy as np
from sklearn import cross_validation
import sys

dataf = sys.argv[1]

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

rm_2_cols = lambda x: ",".join(x.strip().split(",")[2:]) + "\n"

f_train = open("data.train.csv", "w")
f_train.write(rm_2_cols(header))
f_test = open("data.test.csv", "w")
f_test.write(rm_2_cols(header))

i = 0
with open(dataf) as f:
    header = f.readline()
    for l in f.readlines():
        if i in train_index:
            f_train.write(rm_2_cols(l))
        elif i in test_index:
            f_test.write(rm_2_cols(l))
        i += 1
