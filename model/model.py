from sklearn import svm, datasets
import numpy as np
import os
from Levenshtein import distance
from sklearn.cross_validation import ShuffleSplit
import pandas as pd
from mlxtend.plotting import plot_decision_regions
import matplotlib.pyplot as plt
from sklearn.neighbors import KNeighborsClassifier
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
    

    d = data.split('\n')
    correct = d[0]
    user = d[1]

    return distance(correct, user)


def extract_features_labels(folder, task, train=True):
    features, labels = None, None
    for file in os.listdir(train_dir + folder):
        if file != task:
            continue
        for txt in os.listdir(train_dir + folder + '/' + file):
            if txt == '.DS_Store':
                continue
            with open(train_dir + folder + '/' + file + '/' + txt, 'r') as f:
                string_data = f.read()

            list_data = (string_data.split('\n'))
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
                    magnitude = (gx**2 + gy**2 + gz**2) ** 0.5
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

                    new_labels += [rev_idx[folder]]
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
    return features[2:], labels[1:]
###################### End load and Process Data ###########################


############### Model Stuff ################################
X_train = None
Y_train = None

tasks = ['still', 'one_leg', 'straight_line', 'heel_to_toe']

for task in tasks:
    X_train = None
    Y_train = None
    for file in os.listdir(train_dir):
        print(file)
        if file == '.DS_Store':  # remove soon
            continue
        features, labels = extract_features_labels(file, task)
        if X_train is None:
            if features is not None:
                X_train = np.array(features)
                Y_train = np.array(labels)
        else:
            X_train = np.append(X_train, features, axis=0)
            Y_train = np.append(Y_train, labels, axis=0)
    
    shuffle_split = ShuffleSplit(len(X_train), test_size=0.5, random_state=0)
    train_idx, test_idx = next(iter(shuffle_split))
    X = X_train[train_idx]
    y = Y_train[train_idx]
    
    test = X_train[test_idx]
    actual_labels = Y_train[test_idx]
    clf = KNeighborsClassifier(n_neighbors=15, weights='distance')
    clf.fit(X, y)
    predicted = clf.predict(test)
    print(task, "Accuracy: {:.2f}%".format(np.mean(predicted == actual_labels) * 100))
    # Create arbitrary dataset for example
    # df = pd.DataFrame({'Planned_End': np.random.uniform(low=-5, high=5, size=50),
    #                 'Actual_End':  np.random.uniform(low=-1, high=1, size=50),
    #                 'Late':        np.random.random_integers(low=0,  high=2, size=50)}
    # )

    # # Fit Support Vector Machine Classifier
    # X = df[['Planned_End', 'Actual_End']]
    # y = df['Late']

    # clf = svm.SVC(decision_function_shape='ovo')
    # clf.fit(X.values, y.values) 

    # Plot Decision Region using mlxtend's awesome plotting function
    clf = KNeighborsClassifier(n_neighbors=15)
    
    X = X_train
    y = Y_train
    print(X)
    clf.fit(X, y)
    plot_decision_regions(X=X, 
                        y=y,
                        clf=clf, 
                        legend=2,
                        colors='blue,green,red')
    key = {
        'heel_to_toe': 'Heel To Toe',
        'still': 'Still',
        'one_leg': 'One Leg Stand',
        'straight_line': 'Walking In Straight Line'
    }
    # Update plot object with X/Y axis labels and Figure Title
    plt.xlabel('Accelerometer Magnitude', size=14)
    plt.ylabel('Gyroscope Magnitude', size=14)
    plt.title('KNN Decision Region Boundary For ' + key[task] +' task', size=16)
    plt.show()
# print(X_train, Y_train)





############## End Model Stuff ############################
# for folder in os.listdir(train_dir):
#     print(folder)
#     if folder == '.DS_Store':
#         continue
#     extract_features_labels(folder)
