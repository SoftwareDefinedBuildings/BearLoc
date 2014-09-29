import numpy as np
from sklearn.neighbors.kde import KernelDensity
import cPickle as pickle

trainf = "../data.train.csv"

X_train = []
y_train = []

with open(trainf) as f:
    header = f.readline() # header
    lines = f.readlines()
    for l in lines:
        vals = l.strip().split(",")
        X_train.append([int(x) if x != "?" else -100 for x in vals[:-1]])
        y_train.append(vals[-1])

X_train = np.array(X_train)
y_train = np.array(y_train)


rooms = list(set(y_train))
macs = header.strip().split(",")[:-1]
models = {}
for r in rooms:
    models[r] = {}
    X_train_r = [X_train[xi] for xi in range(len(X_train)) if y_train[xi] == r]
    for mi in range(len(macs)):
        m = macs[mi]
        print r, m
        X = [x[mi] for x in X_train_r if x[mi] != -100]
        X = [[x] for x in X]
        p = float(len(X))/len(X_train_r)
        if len(X) > 5:
            models[r][m] = {"model": KernelDensity(kernel='gaussian', bandwidth=0.2).fit(X), "possibility": p}
        else:
            models[r][m] = {"model": None, "possibility": p}

#add appearance possibility


with open("models", "wb") as f:
    pickle.dump(models, f)
