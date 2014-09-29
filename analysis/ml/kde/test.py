import numpy as np
from sklearn.neighbors.kde import KernelDensity

trainf = "../data.train.csv"
testf = "../data.test.csv"

X_test = []
y_test = []

with open(testf) as f:
    header = f.readline() # header
    lines = f.readlines()
    for l in lines:
        vals = l.strip().split(",")
        X_test.append([int(x) if x != "?" else -100 for x in vals[:-1]])
        y_test.append(vals[-1])

X_test = np.array(X_test)
y_test = np.array(y_test)
