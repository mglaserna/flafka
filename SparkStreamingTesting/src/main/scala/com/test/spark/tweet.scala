package com.test.spark

import org.apache.spark.streaming._
import org.apache.spark.streaming.flume.FlumeUtils

object tweet {
  def main(args: Array[String]) {
   if (args.length < 3) {
     System.err.println(s"""
                           |Usage: KafkaTweet2Hive <hostname> <port>
                           |  <hostname> flume spark sink hostname
                           |  <port> port for spark-streaming to connect
                           |  <filePath> the path on HDFS where you want to write the file containing tweets
       """.stripMargin)
     System.exit(1)
   }
   //Matching the arguments
   val Array(hostname, port, path) = args
   // Create context with 2 second batch interval
   val sparkConf = new SparkConf().setAppName("KafkaTweet2Hive")
   val sc = new SparkContext(sparkConf)
   val ssc = new StreamingContext(sc, Seconds(5))
   //Create a PollingStream
   val stream = FlumeUtils.createPollingStream(ssc, hostname, port.toInt)
   //Parse the flume events to get tweets
   val tweets = stream.map(e => new String(e.event.getBody.array))
   //Write the parsed tweets to a file
   if(tweets.count().!=(0)){
     tweets.saveAsTextFiles(path)
   }
   // Start the computation
   ssc.start()
   ssc.awaitTermination()
 }
}