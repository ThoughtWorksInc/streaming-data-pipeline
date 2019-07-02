package com.free2wheelers.apps

import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.spark.sql.{DataFrame, SparkSession}


class ValidationEngine(stationReportValidator: StationReportValidator, fileValidator: FileValidator) {
  var LIMIT_IN_MINUTES = 5
  def checkFile(outputFile: String, stationMartDF: DataFrame, hdfs: FileSystem) = {
    val status = hdfs.getFileStatus(new Path(outputFile))

    fileValidator.doesFileExist(outputFile, hdfs)
    validateFileExternals(status)

    validateFileContents(stationMartDF)
  }

  private def validateFileContents(stationMartDF: DataFrame) = {
    val fileContentValidationResult = stationReportValidator.isValid(stationMartDF)
    if (!fileContentValidationResult) {
      throw new RuntimeException("File is not valid : " + fileContentValidationResult.toString())
    }
    fileContentValidationResult
  }

  private def validateFileExternals(status: FileStatus) = {
    val fileTimeValidationResult = fileValidator.isFileModifiedWithinTimeLimit(status, LIMIT_IN_MINUTES)
    if (fileTimeValidationResult) {
      throw new RuntimeException("File is too old")
    }
    fileTimeValidationResult
  }
}