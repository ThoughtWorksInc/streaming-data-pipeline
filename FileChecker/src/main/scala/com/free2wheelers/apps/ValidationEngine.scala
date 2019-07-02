package com.free2wheelers.apps

import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.spark.sql.{DataFrame, SparkSession}

class ValidationEngine(stationReportValidator: StationReportValidator, fileValidator: FileValidator) {

  def checkFile(outputFile: String, stationMartDF: DataFrame, hdfs: FileSystem) = {
    val status = hdfs.getFileStatus(new Path(outputFile))

    fileValidator.doesFileExist(outputFile, hdfs)
    validateFileExternals(status)

    validateFileContents(stationMartDF)
  }

  def validateFileContents(stationMartDF: DataFrame) = {
    val fileContentValidationResult = stationReportValidator.validate(stationMartDF)
    if (fileContentValidationResult("IS_VALID") == false) {
      throw new RuntimeException("File is not valid : " + fileContentValidationResult.toString())
    }
  }

  def validateFileExternals(status: FileStatus) = {
    val fileTimeValidationResult = fileValidator.isFileModifiedWithinTimeLimit(status)
    if (fileTimeValidationResult("is_delayed") == true) {
      throw new RuntimeException("File is too old")
    }
  }
}