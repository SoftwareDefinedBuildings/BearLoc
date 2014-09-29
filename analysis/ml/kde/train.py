import numpy as np
from sklearn.neighbors.kde import KernelDensity
import cPickle as pickle

trainf = "../data.train.csv"
testf = "../data.test.csv"

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
    for mi in range(len(macs)):
        m = macs[mi]
        X = [X_train[xi][mi] for xi in range(len(X_train)) if y_train[xi] == r and X_train[xi][mi] != -100]
        print r, m, len(X)
        if len(X) > 5:
            models[r][m] = KernelDensity(kernel='gaussian', bandwidth=0.2).fit(X)
        else:
            models[r][m] = None

#add appearance possibility

models_str = pickle.dumps(models)

with open("models", "w") as f:
    f.write(models_str)
