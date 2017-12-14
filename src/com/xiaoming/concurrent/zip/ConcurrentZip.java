/*
 * Copyright (c) 2017 xiaoming software Technology Co., Ltd.
 * All Rights Reserved.
 */
package com.xiaoming.concurrent.zip;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSONObject;


/**
 * 并发压缩
 * @author xiaoming
 */
public class ConcurrentZip {
	
	private static int threadCount = 4;
	
	public static String concurrentGUZip(String compressedStr) {
		Map<String, String> blocks = (Map<String, String>) JSONObject.parse(compressedStr);
		
		Map<String, String> newBlocks = new Hashtable<String, String>();
		for(int i = 0;i < blocks.size();i++){
			String key = i + "";
			String block = blocks.get(key);
			//异步分发解压
			ExecutorUtil.executor(new Runnable() {
				@Override
				public void run() {
					try {
						newBlocks.put(key, ZipUtils.gunzip(block));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		
		StringBuilder uzipStr = new StringBuilder(); 
		//等待解压完毕
		while(true){
			if(blocks.size() == newBlocks.size()){
				for(int i = 0;i < blocks.size();i++){
					String key = i + "";
					uzipStr.append(newBlocks.get(key));
				}
				
				break;
			}
			
			sleep();
		}
		
		return uzipStr.toString();
	}
	
	public static String concurrentGZip(String text, int threadCount) {
		if(threadCount != 0){
			ConcurrentZip.threadCount = threadCount;
		}
		
		Map<String, String> blocks = new Hashtable<String, String>();
		if(text.length() > threadCount){
			/* 切片 */
			int blockSize = text.length()/ConcurrentZip.threadCount;
			for(int i = 0 ;i < ConcurrentZip.threadCount; i++){
				int startIndex = blockSize * i;
				int lastIndex = startIndex + blockSize;
				if(i == (ConcurrentZip.threadCount - 1)){
					lastIndex = text.length();//最后一块
				}
				
				String block = text.substring(startIndex, lastIndex);
				String index = i + "";
				//异步分发压缩
				ExecutorUtil.executor(new Runnable() {
					@Override
					public void run() {
						try {
							String zipBlock = ZipUtils.gzip(block);
							blocks.put(index, zipBlock);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				
			}
			
		}else{
			//单线程压缩
			try {
				return ZipUtils.gzip(text);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		String zipStr = ""; 
		//等待压缩完毕
		while(true){
			if(blocks.size() == threadCount){
				zipStr = JSONObject.toJSONString(blocks);
				break;
			}
			
			sleep();
		}
		
		return zipStr;
	}
	
	private static void sleep(){
		try {
			Thread.yield();
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}

/**
 * Thread pool
 * @author xiaoming
 * 2017年4月20日
 */
class ExecutorUtil {  
    private static Executor executor = null;
    
    static{
    	if(executor == null){
    		executor = asyncConfig();
    	}
    }
    
	private static Executor asyncConfig() {
		return Executors.newCachedThreadPool();
	} 
	/**
	 * 异步执行
	 * @param task
	 * @author xiaoming
	 */
	public static void executor(Runnable task){
		executor.execute(task);
	}
}  
