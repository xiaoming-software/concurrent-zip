# concurrent-zip 
Concurrent zip big string or uzip( 并发压缩解压大数据文本，只有在2.5MB以上的数据才体现压缩优势！ )

# 如何使用？
  压缩：
  ```java
  ConcurrentZip.concurrentGZip(text, threadCount);// text 文本， threadCount 线程数
  ```
  
  解压： 
  ```java
  ConcurrentZip.concurrentGUZip(compressedStr);
  ```
