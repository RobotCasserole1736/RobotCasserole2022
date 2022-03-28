import json, os

fileList = [
    "many_Pickup1.path",
    "many_Pickup2.path"
]

def mirror(obj):
    if(obj is not None):
        obj['x'] = FIELD_X_M - obj['x']
        obj['y'] = FIELD_Y_M - obj['y']

FIELD_X_M = 16.4592
FIELD_Y_M = 8.2296

abspath = os.path.abspath(__file__)
dname = os.path.dirname(abspath)
os.chdir(dname)

for f in fileList:
    with open(f, "r") as in_fp:
        with open ("mirrored_" + f, "w") as out_fp:
            data = json.load(in_fp)
            for wp in data['waypoints']:
                mirror(wp["anchorPoint"])
                mirror(wp["nextControl"])
                mirror(wp["prevControl"])
                wp['holonomicAngle'] -= 180


            json.dump(data, out_fp)
