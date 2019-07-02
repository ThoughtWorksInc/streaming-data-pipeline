package com.free2wheelers.apps

import java.util.concurrent.TimeUnit

import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}

class FileValidator {

  def isFileModifiedWithinTimeLimit(status: FileStatus, limitInMinutes: Int): Boolean = {
    val durationSinceLastModified = System.currentTimeMillis() - status.getModificationTime
    val durationInMinutes = TimeUnit.MILLISECONDS.toMinutes(durationSinceLastModified)

    durationInMinutes > limitInMinutes
  }

  def doesFileExist(outputFile: String, hdfs: FileSystem) = {
    if (!hdfs.exists(new Path(outputFile))) {
      throw new RuntimeException("File does not exist")
    }
  }
}
