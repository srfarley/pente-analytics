package us.pente.spark

import org.apache.spark.sql.{DataFrame, SparkSession}

object TestApp {
  def main(args: Array[String]) = {
    val spark = SparkSession
      .builder()
      .appName("Pente Spark")
      .master("local")
      .getOrCreate()

    val df: DataFrame = spark.read
      .format("us.pente.spark")
      .load("data/search-*.zip")
    df.createOrReplaceTempView("games")

//    df.printSchema

    spark.sql("select * from games where player1Name = 'srfarley'").show()

    val movesRows = spark.sql("select moves from games where player1Name = 'srfarley'")
    movesRows.show()
  }
}
