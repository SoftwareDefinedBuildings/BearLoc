import numpy as np
from sklearn.neighbors.kde import KernelDensity
import cPickle as pickle
from sklearn.metrics import confusion_matrix

testf = "../data.test.csv"
modelf = "models"

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

with open(modelf, 'rb') as f:
    models = pickle.load(f)

rooms = list(set(y_test))
macs = header.strip().split(",")[:-1]
y_pred = []
for i in range(len(X_test)):
    print i
    X = X_test[i]
    scores = {}
    for r in rooms:
        score = 0
        model_count = models[r]['count']['model']
        score += np.exp(model_count.score(len(X)))
        for j in range(len(macs)):
            m = macs[j]
            model = models[r]['rssi'][m]["model"]
            p = models[r]['rssi'][m]["possibility"]
            if X[j] == -100:
                # score += 1 - p
                pass
            else:
                if model:
                    score += np.exp(model.score(X[j])) * p
                else:
                    # score += p
                    pass
        scores[r] = score
    r_pred = max(scores.items(), key=lambda x: x[1])[0]
    y_pred.append(r_pred)

cm = confusion_matrix(y_test, y_pred)
