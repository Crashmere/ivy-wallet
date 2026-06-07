package com.ivy.base.random

import java.util.Random

fun numberBetween(min: Double, max: Double): Double {
    return Random().nextDouble() * (max - min) + min
}
