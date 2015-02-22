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

rm_2_cols = lambda x: ",".join(x.strip().split(",")[:-9]) + "," + x.strip().split(",")[-5]+ "\n"

f_train = open("data.csv", "w")
f_train.write(rm_2_cols(header))

i = 0
with open(dataf) as f:
    header = f.readline()
    for l in f.readlines():
        f_train.write(rm_2_cols(l))
