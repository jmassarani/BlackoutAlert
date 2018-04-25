from sklearn import svm
import numpy as np
import os

ACCEL = 0
GYRO = 1
WINDOW_SIZE = 120  # should be x2 since two lines corresponds to one reading
############### LOAD AND PROCESS DATA #####################
rootdir = 'data/'
train_dir = rootdir + 'train/'
test_dir = rootdir + 'test/'

for file in os.listdir(train_dir):
    if file == '.DS_Store':
        continue
    with open(train_dir + file, 'r') as f:
        string_data = f.read()

    list_data = (string_data.split('\n'))
    list_data = list(filter(None, list_data))
    features, labels = [], []
    acc = 0
    accel_acc, gyro_acc = [], []

    for entry in list_data:
        acc += 1
        sensor_type = GYRO if entry[0] == '|' else ACCEL
        if sensor_type == GYRO:  # purposely split them up so we can handle them differently perhaps?
            actual_data = list(eval(entry[1:]))
            gx, gy, gz = actual_data[0], actual_data[1], actual_data[2]
            magnitude = (gx**2 + gy**2 + gz**2) ** 0.5
            gyro_acc.append(magnitude)
        else:  # accel
            actual_data = list(eval(entry.replace('\ufeff', '')))
            ax, ay, az = actual_data[0], actual_data[1], actual_data[2]
            magnitude = (ax**2 + ay**2 + az**2) ** 0.5
            accel_acc.append(magnitude)
        if WINDOW_SIZE - acc <= 1:
            accel_mean = np.mean(accel_acc)
            gyro_mean = np.mean(gyro_acc)
            features.append([accel_mean, gyro_mean])
            labels.append('LABEL TODO')
            accel_acc, gyro_acc = [], []
            acc = 0
    print(features, labels)
###################### End load and Process Data ###########################

############### Model Stuff ################################
# clf = svm.SVC()
# X, y = [], []
# clf.fit(X, y)
# test_data = np.zeros(shape=(2,2))
# clf.predict(test_data)
############### End Model Stuff ############################
