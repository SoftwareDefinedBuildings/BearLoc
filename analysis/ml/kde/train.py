import numpy as np

trainf = "../data.train.csv"
testf = "../data.test.csv"

X_train = []
y_train = []
X_test = []
y_test = []

with open(trainf) as f:
    f.readline() # header
    lines = f.readlines()
    for l in lines:
        vals = l.strip().split(",")
        X_train.append([int(x) if x != "?" else -100 for x in vals[:-1]])
        y_train.append(vals[-1])

with open(testf) as f:
    f.readline() # header
    lines = f.readlines()
    for l in lines:
        vals = l.strip().split(",")
        X_test.append([int(x) if x != "?" else -100 for x in vals[:-1]])
        y_test.append(vals[-1])

X_train = np.array(X_train)
y_train = np.array(y_train)
X_test = np.array(X_test)
y_test = np.array(y_test)

