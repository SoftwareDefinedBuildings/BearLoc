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

cv = cross_validation.StratifiedKFold(targets, n_folds=2000)
cv_index = [index for index in cv]
train_index = []
for i in range(100):
  _, _train_index = cv_index[i]
  train_index.extend(_train_index)
_, test_index = cv_index[100]


with open("data.train.csv", "w") as f:
    f.write(header)
    for i in train_index:
        f.write(lines[i])

with open("data.test.csv", "w") as f:
    f.write(header)
    for i in test_index:
        f.write(lines[i])
