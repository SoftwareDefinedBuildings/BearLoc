from glob import glob
import os

datadir = "./data/"
output_fname = "data.csv"


# get all files' full paths
fpaths = []
for root, subFolders, files in os.walk(datadir):
    for fname in files:
        if fname.endswith("csv") and "metadata" not in fname:
            fpath = os.path.join(root, fname)
            fpaths.append(fpath)

# parse them
macs = [] # list of MAC addresses
data = {} # dict with timestamp as keys, with dict as values
for fpath in fpaths:
    room = os.path.basename(os.path.dirname(fpath)).split("_")[1]
    print "handling", fpath
    with open(fpath) as f:
        f.readline() # skip header
        lines = f.readlines()
    records = [l.strip().split(",") for l in lines]
    last_timestamp = 0
    for r in records:
        timestamp = int(r[0])
        # validate timestamp
        if 0 < timestamp - last_timestamp < 5:
            raise Exception("timestamp error " + str(timestamp) + " " + str(last_timestamp))
        last_timestamp = timestamp
        mac = r[3]
        rssi = int(r[5])
        macs.append(mac)
        if timestamp not in data:
            data[timestamp] = {}
        data[timestamp][mac] = rssi
        data[timestamp]["room"] = room
macs = list(set(macs))

# write to output file
with open(output_fname, "w") as f:
    f.write(",".join(macs + ["room"]) + "\n")
    for items in data.values():
        row_vals = [str(items.get(m, "?")) for m in macs+["room"]]
        f.write(",".join(row_vals) + "\n")
