package com.hirbod.randomnumbergenerator;

import java.math.BigInteger;
import java.util.Random;

class Functions {
    private static BigInteger randBig(BigInteger upperLimit){
        BigInteger randomNumber;
        do {
            randomNumber = new BigInteger(upperLimit.bitLength(), new Random());
        } while (randomNumber.compareTo(upperLimit) >= 0);
        return randomNumber;
    }
    static BigInteger fullRandomBig(BigInteger Min,BigInteger Max){
        if(Min.compareTo(BigInteger.ZERO) >= 0){//Both numbers positive
            BigInteger delta = Max.subtract(Min);
            return randBig(delta).add(Min);
        }else if(Max.compareTo(BigInteger.ZERO) >= 0) //Max > 0 and Min < 0
        {
            Max = Max.subtract(Min);
            Max = randBig(Max); //Max in now the random number
            return Max.add(Min);
        }else //Both numbers less than 0
        {
            //after the abs(), the order of numbers will swap; So Min is the bigger value
            Min = Min.abs();
            Max = Max.abs();
            BigInteger delta = Min.subtract(Max);
            Min = randBig(delta).add(Max).add(BigInteger.ONE); //Min in now the random number
            return Min.negate();
        }
    }
    static boolean isInteger(float str) { return str % 1 == 0; }
    static boolean isInteger(double str) {return str % 1 == 0;}
    static int getDecimal(float value){return String.valueOf(value).substring(String.valueOf(value).indexOf(".")).length();}
}
