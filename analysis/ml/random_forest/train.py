import numpy as np
from sklearn.ensemble import RandomForestClassifier
import cPickle as pickle
import sys

trainf = sys.argv[1]
modelf = sys.argv[2]

X_train = []
y_train = []

with open(trainf) as f:
    header = f.readline() # header
    lines = f.readlines()
    for l in lines:
        vals = l.strip().split(",")
        X_train.append([float(x) if x != "?" else -150 for x in vals[:-1]])
        y_train.append(vals[-1])

X_train = np.array(X_train)
y_train = np.array(y_train)


rooms = list(set(y_train))
macs = header.strip().split(",")[:-1]
model = RandomForestClassifier().fit(X_train, y_train)


with open(modelf, "wb") as f:
    pickle.dump(model, f)
