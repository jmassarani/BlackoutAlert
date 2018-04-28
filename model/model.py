from sklearn import svm
import numpy as np
import os
from Levenshtein import distance

ACCEL = 0
GYRO = 1
WINDOW_SIZE = 120  # should be x2 since two lines corresponds to one reading
############### LOAD AND PROCESS DATA #####################
rootdir = 'data/'
train_dir = rootdir + 'train/'
test_dir = rootdir + 'test/'

rev_idx = {
    'sober': 0,
    'euphoria': 1,
    'coma': 2
}

def calculate_levenshtein(filename):
    with open(filename, 'r') as f:
        data = f.read()
    f.close()

    d = data.split('\n')
    correct = d[0]
    user = d[1]
    
    return distance(correct, user)

def extract_features_labels(folder, task, train=True):
    features, labels = None, None
    for file in os.listdir(train_dir + folder):
        
        if file != task or folder == 'euphoria':
            continue
        print(file, folder, 'wut')
        for txt in os.listdir(train_dir+ folder + '/' +file):
            if txt == '.DS_Store':
                continue
            with open(train_dir + folder + '/' + file + '/' + txt, 'r') as f:
                string_data = f.read()

            list_data = (string_data.split('\n'))
            length = len(list_data)
            list_data = list(filter(None, list_data))[500:len(list_data)-500]
            new_features, new_labels = [], []
            acc = 0
            accel_acc, gyro_acc = [], []
            for entry in list_data:
                acc += 1
                sensor_type = GYRO if entry[0] == '|' else ACCEL
                if sensor_type == GYRO:  # purposely split them up so we can handle them differently perhaps?
                    actual_data = list(eval(entry[1:]))
                    try:
                        gx, gy, gz = actual_data[0], actual_data[1], actual_data[2]
                    except:
                        continue
                    magnitude = (gx**2 + gz**2) ** 0.5
                    gyro_acc += [magnitude]
                else:  # accel
                    actual_data = list(eval(entry.replace('\ufeff', '')))
                    try:
                        ax, ay, az = actual_data[0], actual_data[1], actual_data[2]
                    except:
                        continue
                    magnitude = (ax**2 + ay**2 + az**2) ** 0.5
                    accel_acc += [magnitude]
                if WINDOW_SIZE - acc <= 1:
                    accel_mean = np.mean(accel_acc)
                    gyro_mean = np.mean(gyro_acc)
                    new_features += [accel_mean, gyro_mean]
                    
                    new_labels +=[rev_idx[folder]]
                    if features is not None:
                        features += [new_features]
                        labels += (new_labels)
                    else:
                        if new_features is None or new_labels is None:
                            print('fuck up')
                        features = new_features
                        labels = new_labels
                    new_features, new_labels = [], []
                    acc = 0
    print(features, labels)
    return features[2:], labels[1:]
###################### End load and Process Data ###########################

############### Model Stuff ################################
X_train = None  
Y_train = None 
X_test = None 
Y_test = None 
for file in os.listdir(train_dir):
    if file == '.DS_Store' or file == 'euphoria':  # remove soon
        continue
    features, labels = extract_features_labels(file, 'still')
    if X_train is None:
        if features is not None:
            X_train = np.array(features)
            Y_train = np.array(labels)
    else:
        X_train = np.append(X_train, features, axis=0)
        Y_train = np.append(Y_train, labels, axis= 0)
# print(X_train, Y_train)
print(X_train.shape, Y_train.shape)
clf = svm.SVC()
X, y = X_train, Y_train
clf.fit(X, y)

print(clf.predict(X_train))
print(Y_train)


############## End Model Stuff ############################
for folder in os.listdir(train_dir):
    print(folder)
    if folder == '.DS_Store':
        continue
    extract_features_labels(folder)
