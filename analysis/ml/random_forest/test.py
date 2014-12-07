import numpy as np
from sklearn.ensemble import RandomForestClassifier
import cPickle as pickle
from sklearn.metrics import confusion_matrix
import sys

testf = sys.argv[1]
modelf = sys.argv[2]

X_test = []
y_test = []

with open(testf) as f:
    header = f.readline() # header
    lines = f.readlines()
    for l in lines:
        vals = l.strip().split(",")
        X_test.append([int(x) if x != "?" else -150 for x in vals[:-1]])
        y_test.append(vals[-1])

X_test = np.array(X_test)
y_test = np.array(y_test)

with open(modelf, 'rb') as f:
    model = pickle.load(f)

rooms = list(set(y_test))
macs = header.strip().split(",")[:-1]
y_pred = model.predict(X_test)

cm = confusion_matrix(y_test, y_pred)

true_cnt = 0.0
for i in range(len(cm)):
    true_cnt += cm[i, i]
total_cnt = np.sum(cm)
print "Accuracy:", true_cnt/total_cnt
