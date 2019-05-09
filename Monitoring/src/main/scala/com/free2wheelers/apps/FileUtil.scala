package com.free2wheelers.apps

import java.io.File

import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.spark.sql.SparkSession

object FileUtil {

  def getCSVPathFromDirectory(directory: String): String = {
    val d = new File(directory)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList.filter(_.getAbsolutePath.endsWith(".csv")).head.getAbsolutePath
    } else {
      throw new Exception("csv doesn't exist")
    }
  }

  def getLastModifiedFileStatus(spark: SparkSession, csvPath: String) = {
    val conf = spark.sparkContext.hadoopConfiguration
    val fs = FileSystem.get(conf)
    val dirPath = new Path(csvPath)
    val fileStatus: Array[FileStatus] = fs.listStatus(dirPath)
    fileStatus.head.getModificationTime()
  }
}
