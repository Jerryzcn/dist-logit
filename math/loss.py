#!/usr/bin/python
from __future__ import division
from numpy import *


def sigmoid(x):
    return 1 / (1 + exp(-x))


def loss_l2(X, y, w, lamda):
    one = ones((X.shape[0], 1))

    X = concatenate((one, X), 1)
    m = X.shape[0]
    wx = dot(X, w)
    loss = (1 / m) * sum(-y * log(sigmoid(wx)) - (1 - y) * log(1 - sigmoid(wx))) + (lamda / 2 / m) * sum(square(w[1::]))
    tmp = (1 / m) * (dot((sigmoid(wx) - y), X))
    grad = zeros(X.shape[1])
    grad[0] = tmp[0]
    grad[1::] = tmp[1::] + (lamda / m) * square(w[1::])
    return loss, grad


if __name__ == '__main__':
    main()
