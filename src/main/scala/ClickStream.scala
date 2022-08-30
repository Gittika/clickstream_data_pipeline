
import com.typesafe.config.Config
import exceptions.Exceptions._
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import service.DataPipeline.execute
import utils.ApplicationUtils.{configuration, createSparkSession}

import scala.sys.exit

object ClickStream extends Logging{
  def main(args: Array[String]): Unit = {
    val confPath = args(0)
    val appConf: Config = configuration(confPath)
    implicit val spark: SparkSession = createSparkSession(appConf)
    var exitCode = 0

    try {
      //performing reader and cleanser operations on both dataset
      execute(appConf)
    }
    catch {
      case ex: FileReaderException => log.error("File Reader Exception: " + ex.message)
        exitCode = 1
      case ex: DataframeIsEmptyException => log.error("DataFrameIsEmptyException:" + ex.message)
        exitCode = 1
      case ex: ColumnNotFoundException => log.error("ColumnNotFoundException:" + ex.message)
        exitCode = 1
      case ex: EmptyFilePathException => log.error("EmptyFilePathException:" + ex.message)
        exitCode = 1
      case ex: FilePathNotFoundException => log.error("FilePathNotFoundException:" + ex.message)
        exitCode = 1
      case ex: InvalidInputFormatException => log.error("InvalidInputFormatException: " + ex.message)
        exitCode = 1
    }
    finally {
      if(exitCode == 1){
        exit(1)
      }
      else{
        log.info("PipeLine execution completed")
      }
      spark.stop()
    }
  }
}
