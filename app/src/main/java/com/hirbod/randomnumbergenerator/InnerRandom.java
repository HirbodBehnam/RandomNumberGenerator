package com.hirbod.randomnumbergenerator;

import java.security.SecureRandom;
import java.util.Random;

class InnerRandom {
    static boolean UseSecureRandom = false;
    static Random random = new Random();
    static SecureRandom secureRandom = new SecureRandom();
    static int nextInt(int bound){
        return UseSecureRandom ? secureRandom.nextInt(bound) : random.nextInt(bound);
    }
    static boolean nextBoolean(){
        return UseSecureRandom ? secureRandom.nextBoolean() : random.nextBoolean();
    }
}
