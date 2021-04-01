package com.lyd.mall.product.web;

import com.lyd.mall.product.entity.CategoryEntity;
import com.lyd.mall.product.service.CategoryService;
import com.lyd.mall.product.vo.CateLog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author Liuyunda
 * @Date 2021/3/22 23:27
 * @Email man021436@163.com
 * @Description: DOTO
 */
@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    RedisTemplate redisTemplate;
    @GetMapping({"/","/index.html"})
    public String indexPage(Model model) {
        // TODO 1.一级分类
        List<CategoryEntity> categoryEntities =  categoryService.getLevel1Categorys();

        // 视图解析器拼串->classpath:/template/+返回值+。html
        model.addAttribute("categorys",categoryEntities);
        return "index";
    }


    //index/json/catalog.json
    @ResponseBody
    @GetMapping("/index/json/catalog.json")
    public Map<String, Object> getCatalogJson() {
        Map<String, List<CateLog2Vo>> map = categoryService.getCatalogJson();
        return null;
    }


    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        // 获取锁，只要锁的名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");
        // 加锁
        // 阻塞式等待（看门狗自动续期）
        // lock.lock();
        /**
         * 10秒自动解锁，自动解锁时间一定要大于业务的执行时间
         * 如果传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时时间就是我们指定的时间
         * 如果我们没指定锁的超时时间，就使用LockWatchdogTimeout（看门狗）的默认时间，
         *    只要占锁成功，就会启动一个定时任务（重新给锁设置过期时间，新的过期时间就是看门狗的默认时间）
         *    每隔（internalLockleaseTime(看门狗时间)/3）时间进行一次续期操作
         */
        // lock.lock(10, TimeUnit.SECONDS);
        // 最佳实战(省掉了整个续期操作，手动解锁)
        lock.lock(30, TimeUnit.SECONDS);
        try {
            System.out.println("加锁成功，执行业务。。。。"+Thread.currentThread().getId());
            Thread.sleep(30000);
        }catch (Exception e) {


        } finally {
            // 解锁
            System.out.println("释放锁。。。"+Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    // 保证一定能读到最新数据，修改期间，写锁是一个排他锁（互斥锁。读锁是一个共享锁
    // 写锁没释放就必须等待
    // 读+读：相当于无锁，并发读，只会在redis中记录好，所有当前的读锁。他们都会同时加锁成功
    // 写+读：读必须等待写锁释放
    // 写+写：阻塞方式
    // 读+写：有读锁写也需要等待
    // 只要有写锁的存在，都必须等待
    @GetMapping("/write")
    @ResponseBody
    public String writeValue(){
        String s = "";
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        // 1.改数据加写锁
        RLock writeLockLock = readWriteLock.writeLock();
        try {
            writeLockLock.lock();
            System.out.println("写锁加锁成功..."+Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue",s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            writeLockLock.unlock();
            System.out.println("写锁释放成功..."+Thread.currentThread().getId());
        }
        return s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue(){
        String s = "";
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        // 1.读数据加读锁
        RLock readLock = readWriteLock.readLock();
        readLock.lock();
        System.out.println("读锁加锁成功..."+Thread.currentThread().getId());
        try {
            s = (String) redisTemplate.opsForValue().get("writeValue");
            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
            System.out.println("读锁释放成功..."+Thread.currentThread().getId());
        }
        return s;
    }

    /**
     * @Description: 放假，锁门
     * 所有班级走完才锁门
     * @Param: []
     * @return: java.lang.String
     * @Author: Liuyunda
     * @Date: 2021/4/1
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {

        RCountDownLatch door = redisson.getCountDownLatch("door");
        // 等待五个班
        door.trySetCount(5);
        // 等待闭锁都完成
        door.await();
        return "放假了！";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id")Long id){
        RCountDownLatch door = redisson.getCountDownLatch("door");
        // 计数减一
        door.countDown();
        return id+"班的人都走了....";
    }




    /**
     * @Description: 车库停车
     * 3个车位
     * 信号量也可以用作分布式限流
     * @Param: []
     * @return: java.lang.String
     * @Author: Liuyunda
     * @Date: 2021/4/1
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        // 获取一个信号，占一个车位
        // 阻塞式，如果获取不到则一直获取
        // park.acquire();
        // 尝试获取一个信号
        boolean tryAcquire = park.tryAcquire();
        if (tryAcquire){
            // 执行业务
        }else {
            return "信号量已满";
        }
        return "ok=>"+tryAcquire;
    }
    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        // 释放一个信号,释放一个车位
        park.release();
        return "ok";
    }
}
