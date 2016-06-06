package us.pente.spark

import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

object App {
  def main(args: Array[String]) = {
    val config: SparkConf = new SparkConf().setAppName("Pente Spark").setMaster("local")
    val sc = new SparkContext(config)
    val sqlContext = new SQLContext(sc)

    val df: DataFrame = sqlContext.read
      .format("us.pente.spark")
      .load("/Users/sfarley/Desktop/PenteGameArchives/search-*.zip")

    df.printSchema
//    df.show

    df.registerTempTable("games")
    sqlContext.sql("select * from games where player1Name = 'srfarley'").show()

    val movesRows = sqlContext.sql("select moves from games where player1Name = 'srfarley'").take(10)
    movesRows.foreach(movesRow => {
      val moves = movesRow.getSeq(0)
      print("Moves: ")
      moves.foreach(move => print(move) + " ")
      println()
    })
  }
}
