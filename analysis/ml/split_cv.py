import numpy as np
from sklearn import cross_validation

dataf = "data.csv"

with open(dataf) as f:
    header = f.readline()
    targets = []
    lines = f.readlines()
    for l in lines:
        splits = l.strip().split(",")
        targets.append(splits[-1])

cv = cross_validation.StratifiedKFold(targets, n_folds=5)
train_index, test_index = [(train_index, test_index) for train_index, test_index in cv][0]


with open("data.train.csv", "w") as f:
    f.write(header + "\n")
    for i in train_index:
        f.write(lines[i] + "\n")

with open("data.test.csv", "w") as f:
    f.write(header + "\n")
    for i in test_index:
        f.write(lines[i] + "\n")
