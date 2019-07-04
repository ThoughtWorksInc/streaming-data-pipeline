package com.free2wheelers.apps

import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.spark.sql.{DataFrame}


class ValidationEngine(stationReportValidator: StationReportValidator, fileValidator: FileValidator) {
  val LIMIT_IN_MINUTES = 5
  def checkFile(outputFile: String, stationMartDF: DataFrame, hdfs: FileSystem) = {
    val file = hdfs.getFileStatus(new Path(outputFile))

    fileValidator.checkFileExist(outputFile, hdfs)
    validateFileExternals(file)

    validateFileContents(stationMartDF)
  }

  private def validateFileContents(stationMartDF: DataFrame) = {
    val fileContentValidationResult = stationReportValidator.isDFValid(stationMartDF)
    if (!fileContentValidationResult) {
      throw new RuntimeException("File is not valid : " + fileContentValidationResult.toString())
    }
    fileContentValidationResult
  }

  private def validateFileExternals(file: FileStatus) = {
    val fileTimeValidationResult = fileValidator.isFileOlderThan(file, LIMIT_IN_MINUTES)
    if (fileTimeValidationResult) {
      throw new RuntimeException("File is too old")
    }
    fileTimeValidationResult
  }
}