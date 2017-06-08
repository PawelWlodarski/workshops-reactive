package jug.workshops.reactive.futures.java;

import java.util.concurrent.TimeUnit;

public class JavaMethodAliases {

    static void sleep(Integer millis){
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

    static void print(String text){
        System.out.println(text);
    }

}
