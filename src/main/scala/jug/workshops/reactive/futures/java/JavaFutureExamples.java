package jug.workshops.reactive.futures.java;

import static jug.workshops.reactive.futures.java.JavaMethodAliases.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class JavaFutureExamples {


    public static void main(String[] args) throws InterruptedException {
//        exampleSimpleAsync();  //1 - simple async
//        exampleErrorAsync();  //2 - async error
        CompletableFuture<Integer> fut1 = sypplyAsyncCreation();    //3 - supply async
        fut1.thenAccept(r->print("result from supply async : "+r));  //3 - supply async
        print("Main thread : "+Thread.currentThread().getName());
        TimeUnit.SECONDS.sleep(1);
    }

    private static void exampleSimpleAsync() {
        print(" *** ASYNC INTEGR CALCULATION ***");
        CompletableFuture<Integer> future = asyncIntCalculation();
//        future.complete(5); //mutable future -> this so so BAD!!!
        future.thenAccept(r-> System.out.println("result from future : "+r));
        print("WHILE PERFORMING ASYNC INTEGER CALCULATION");
    }

    private static void exampleErrorAsync() {
        print(" *** ASYNC INTEGR CALCULATION ***");
        CompletableFuture<Integer> future = asyncErrorCalculation();
        future.thenAccept(r-> System.out.println("result from error future : "+r));
        print(future.toString());
        print("WHILE PERFORMING ASYNC ERROR INTEGER CALCULATION");
    }

    static CompletableFuture<Integer> asyncIntCalculation() {
        CompletableFuture<Integer> f=new CompletableFuture<>();
        new Thread(()->{
            sleep(500);
            f.complete(21);  //work as promise
        }).start();
        return f;
    }


    static CompletableFuture<Integer> asyncErrorCalculation() {
        CompletableFuture<Integer> f=new CompletableFuture<>();
        new Thread(()->{
            sleep(500);
            f.completeExceptionally(new RuntimeException("error"));  //work as promise
        }).start();
        return f;
    }

    static CompletableFuture<Integer> sypplyAsyncCreation(){
        CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> {
            sleep(100);
            return 99;
        });

        return f;
    }

}
