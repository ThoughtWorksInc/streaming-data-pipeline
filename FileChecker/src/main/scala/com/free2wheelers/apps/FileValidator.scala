package com.free2wheelers.apps

import java.util.concurrent.TimeUnit

import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}

class FileValidator {

  def isFileOlderThan(file: FileStatus, limitInMinutes: Int): Boolean = {
    val durationSinceLastModified = System.currentTimeMillis() - file.getModificationTime
    val durationInMinutes = TimeUnit.MILLISECONDS.toMinutes(durationSinceLastModified)

    durationInMinutes > limitInMinutes
  }

  def checkFileExist(outputFile: String, hdfs: FileSystem) = {
    if (!hdfs.exists(new Path(outputFile))) {
      throw new RuntimeException("File does not exist")
    }
  }
}
