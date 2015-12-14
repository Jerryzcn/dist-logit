#!/usr/bin/python
from __future__ import division
from numpy import *


def main():
    training_data = genfromtxt('train.csv', delimiter=',')
    test_data = genfromtxt('test.csv', delimiter=',')
    Y_train = training_data[:,0]
    X_train = training_data[:, 1:]
    Y_test = test_data[:,0]
    X_test = test_data[:, 1:]
    w = zeros(X_train.shape[1] + 1)
    while True:
        # print 'w',w
        w = descent(0.1,0.1,w,X_train,Y_train)

    #predict(X_test,Y_test,w)
  

def loss(X, y, w, lamda):
    
    reg_sum = sum(square(w[1::])) * (lamda/2)
    # print w.shape
    xw = (dot(X,w[1::]) + w[0])
    # print "xw", xw
    # print "exp", exp(xw)
    lw = sum(xw*y-log(1+exp(xw)))
    
    loss = reg_sum-lw/X.shape[0]
    newy = y-(exp(xw)/(1+exp(xw)))

    grad = zeros(X.shape[1]+1)
    grad[0] = -sum(newy)/X.shape[0]

    grad[1::] = -dot(X.transpose(),(newy))/X.shape[0] + lamda*w[1::]

    return (loss, grad)

def sigmoid(x):
    return 1/(1+exp(-x))

def loss_2(X, y, w, lamda):
    one = ones((X.shape[0],1))

    X = concatenate((one,X),1)
    m = X.shape[0]
    wx = dot(X,w)
    loss = (1/m)*sum(-y*log(sigmoid(wx))-(1-y)*log(1-sigmoid(wx))) + (lamda/2/m)*sum(square(w[1::]))
    tmp = (1/m)*(dot((sigmoid(wx)-y),X))
    grad = zeros(X.shape[1])
    grad[0] = tmp[0]
    grad[1::] = tmp[1::] + (lamda/m)*(w[1::])
    return (loss, grad)
    

def predict(X,y,w):
    xw = (dot(X,w[1::]) + w[0])
    correct = 0
    for i in range(1000):
        print sigmoid(xw)[i]
        if abs(sigmoid(xw)[i]-y[i]) < 0.5:
            correct = correct +1
    print correct


def descent(n, lamda, w, X, y):
    los, grad = loss_2(X, y, w, lamda)
    w = w - n * grad
    print los
    return w


if __name__ == '__main__':
    main()
