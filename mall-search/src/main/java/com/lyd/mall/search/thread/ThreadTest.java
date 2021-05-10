package com.lyd.mall.search.thread;

import java.util.concurrent.*;

/**
 * @Author Liuyunda
 * @Date 2021/4/15 21:01
 * @Email man021436@163.com
 * @Description: TODO
 */
public class ThreadTest {
    public static ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // CompletableFuture.runAsync(()->{
        //     System.out.println("当前线程："+Thread.currentThread().getId());
        //     int i = 10/2;
        //     System.out.println("运行结果："+i);
        // },service);

        /**
         * 方法完成后的感知
         */
        // CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
        //     System.out.println("当前线程：" + Thread.currentThread().getId());
        //     int i = 10 / 2;
        //     System.out.println("运行结果：" + i);
        //     return i;
        // }, service).whenComplete((result,exception)->{
        //     System.out.println(result);
        //     System.out.println(exception);
        // }).exceptionally(throwable -> {
        //     return 10;
        // });

        /**
         * 方法完成后的处理
         */
        // CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
        //     System.out.println("当前线程：" + Thread.currentThread().getId());
        //     int i = 10 / 0;
        //     System.out.println("运行结果：" + i);
        //     return i;
        // }, service).handle((integer, throwable) -> {
        //     if (integer!=null){
        //         return integer;
        //     }
        //     if (throwable!=null){
        //         return  0;
        //     }
        //     return 1;
        // });


        /**
         * 线程串行化
         *  thenRun 不能获取到上一步的执行结果，并且没有返回值
         *  thenAccept 能获取到上一步的执行结果，并且没有返回值
         *  thenApply 能获取到上一步的执行结果，而且还有返回值
         */
        // CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
        //     System.out.println("当前线程：" + Thread.currentThread().getId());
        //     int i = 10 / 0;
        //     System.out.println("运行结果：" + i);
        //     return i;
        // }, service)/*.thenRunAsync(()->{
        //     System.out.println("任务2启动了");
        // },service);*//*.thenAcceptAsync(integer -> {
        //     System.out.println("上一步的执行结果："+integer);
        // }, service)*/.thenApplyAsync(res -> {
        //     System.out.println("上一步的执行结果：" + res);
        //     return res;
        // },service);

        /**
         * 两任务组合
         * thenCombine 组合两个future，获取两个future的返回结果，并返回当前任务的返回值
         * thenAcceptBoth 组合两个future，获取两个future任务的返回结构，然后处理任务，没有返回值
         * thenAfterBoth 组合两个future，不需要获取future的结果，只需两个future处理完任务后，处理该任务
         */
        // CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
        //     System.out.println("当前线程1：" + Thread.currentThread().getId());
        //     int i = 10 / 0;
        //     System.out.println("运行结果1：" + i);
        //     return i;
        // }, service);
        //
        // CompletableFuture<Integer> future02 = CompletableFuture.supplyAsync(() -> {
        //     System.out.println("当前线程2：" + Thread.currentThread().getId());
        //     int i = 10 / 2;
        //     try { TimeUnit.SECONDS.sleep(3); }catch (InterruptedException e) { e.printStackTrace(); }
        //     System.out.println("运行结果2：" + i);
        //     return i;
        // }, service);
        // future01.runAfterBothAsync(future02,()->{
        //     System.out.println("任务3");
        // },service);
        // future01.thenAcceptBothAsync(future02,(res1,res2)->{
        //     System.out.println("任务3:"+res1+res2);
        // },service);
        // CompletableFuture<String> future = future01.thenCombineAsync(future02, (res1, res2) -> {
        //     System.out.println("任务3:" + res1 + res2);
        //     return res1 + ":" + res2;
        // }, service);


        /**
         * 两个任务，任意一个future任务完成的时候执行任务
         * applyToEither 两个任务有一个执行完成，获取他的返回值，处理任务并有新的返回值
         * acceptEither 两个任务有一个执行完成，获取他的返回值，处理任务没有新的返回值
         * runAfterEither 两个任务有一个执行完成，不需要获取future的结果，处理任务，也没有返回值
         */
        // future01.runAfterEitherAsync(future02,()->{
        //     System.out.println("任务3");
        // },service);
        // future01.acceptEitherAsync(future02,(res1)->{
        //     System.out.println("任务3"+res1);
        // },service);
        // CompletableFuture<Integer> future = future01.applyToEitherAsync(future02, (res1) -> {
        //     System.out.println(res1);
        //     return res1;
        // }, service);

        /**
         * 多任务组合
         * allOf 等待所有任务完成
         * andOf 只要有一个任务完成
         */
        CompletableFuture<String> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询1");
            return "1";
        },service);
        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询2");
            return "2";
        },service);
        CompletableFuture<String> future03 = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询3");
            return "3";
        },service);

        // CompletableFuture<Void> future = CompletableFuture.allOf(future01, future02, future03);
        // future.get();
        CompletableFuture<Object> future = CompletableFuture.anyOf(future01, future02, future03);
        System.out.println(future.get());
    }

    public  void thread1(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("mainstart....");
        // 1. 继承Thread类
        // Thread01 thread01 = new Thread01();
        // thread01.start();
        // 2. 实现Runnable接口
        // Runnable01 runnable01 = new Runnable01();
        // new Thread(runnable01).start();
        // 3. 实现Callable接口+FutureTask（可以拿到返回结果，可以处理异常）
        // FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
        // new Thread(futureTask).start();
        // // 阻塞等待整个线程执行完成，获取返回结果
        // Integer integer = futureTask.get();
        // System.out.println("...."+integer);
        // 4. 线程池
        //
        service.execute(new Runnable01());
        System.out.println("mainend....");

    }

    public static class Thread01 extends Thread{
        @Override
        public void run() {
            System.out.println("当前线程："+Thread.currentThread().getId());
            int i = 10/2;
            System.out.println("运行结果："+i);
        }
    }

    public static class Runnable01 implements Runnable{
        @Override
        public void run() {
            System.out.println("当前线程："+Thread.currentThread().getId());
            int i = 10/2;
            System.out.println("运行结果："+i);
        }
    }

    public static class Callable01 implements Callable<Integer>{
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程："+Thread.currentThread().getId());
            int i = 10/2;
            System.out.println("运行结果："+i);
            return i;
        }
    }
}
