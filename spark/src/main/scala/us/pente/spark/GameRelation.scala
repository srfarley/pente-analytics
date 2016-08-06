package us.pente.spark

import java.sql.{Date, Timestamp}

import org.apache.spark.input.PortableDataStream
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.sources.{BaseRelation, TableScan}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SQLContext}
import us.pente.graph.model.GameArchive

import scala.collection.JavaConversions._

// Reference https://medium.com/@anicolaspp/extending-our-spark-sql-query-engine-5f4a088de986#.akukshthx

class GameRelation(override val sqlContext: SQLContext, private val location: String)
  extends BaseRelation with TableScan with Serializable {

  private val gameSchema: StructType = StructType(Seq(
    StructField("id", StringType, nullable = false, Metadata.empty),
    StructField("name", StringType, nullable = false, Metadata.empty),
    StructField("site", StringType, nullable = false, Metadata.empty),
    StructField("event", StringType, nullable = false, Metadata.empty),
    StructField("round", StringType, nullable = true, Metadata.empty),
    StructField("section", StringType, nullable = true, Metadata.empty),
    StructField("date", DateType, nullable = false, Metadata.empty),
    StructField("time", TimestampType, nullable = false, Metadata.empty),
    StructField("timeControl", StringType, nullable = false, Metadata.empty),
    StructField("rated", BooleanType, nullable = false, Metadata.empty),
    StructField("player1Name", StringType, nullable = false, Metadata.empty),
    StructField("player2Name", StringType, nullable = false, Metadata.empty),
    StructField("player1Rating", IntegerType, nullable = false, Metadata.empty),
    StructField("player2Rating", IntegerType, nullable = false, Metadata.empty),
    StructField("result", StringType, nullable = false, Metadata.empty),
    StructField("winner", StringType, nullable = false, Metadata.empty),
    StructField("loser", StringType, nullable = false, Metadata.empty),
    StructField("moves", DataTypes.createArrayType(StringType, false), nullable = false, Metadata.empty)
  ))

  override def schema: StructType = {
    gameSchema
  }

  override def buildScan(): RDD[Row] = {
    val filesRDD = sqlContext.sparkContext.binaryFiles(location)
    filesRDD.flatMap(gameRows)
  }

  private def gameRows(tuple: (String, PortableDataStream)): Iterable[Row] = {
    val inputStream = tuple._2.open()
    try {
      val games = GameArchive.toList(GameArchive.parse(inputStream))
      games.map(
        game => Row.fromSeq(Seq(
          game.id,
          game.name,
          game.site,
          game.event,
          game.round,
          game.section,
          Date.valueOf(game.date),
          Timestamp.valueOf(game.time.atDate(game.date)),
          game.timeControl,
          game.rated,
          game.player1Name,
          game.player2Name,
          game.player1Rating,
          game.player2Rating,
          game.result,
          game.winner,
          game.loser,
          game.moves.map(move => move.number + " " + move.player1 + " " + move.player2)
        )))
    } finally {
      inputStream.close()
    }
  }
}
