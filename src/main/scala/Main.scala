import java.io.{FileWriter}

import scala.io.Source

object Main extends App {
  val datasets = Source.fromFile("datasets.txt").getLines().toList.sorted
  val sids = Map(
    "Rnd" -> ((clid: Int) => 0),
    "Mar" -> ((clid: Int) => 3000000 + clid),
    "SG" -> ((clid: Int) => 14000000 + clid),
    "ERE" -> ((clid: Int) => 11000000 + clid),
    "OER" -> ((clid: Int) => 74000000 + clid),
    "HS" -> ((clid: Int) => 1),
    "TUman" -> ((clid: Int) => 127177 + clid),
    "TUmah" -> ((clid: Int) => 127179 + clid),
    "TUeuc" -> ((clid: Int) => 127176 + clid),
    "HTUman" -> ((clid: Int) => 94172007 + clid),
    "HTUmah" -> ((clid: Int) => 94172009 + clid),
    "HTUeuc" -> ((clid: Int) => 94172006 + clid),
    "ATUman" -> ((clid: Int) => 791),
    "ATUmah" -> ((clid: Int) => 991),
    "ATUeuc" -> ((clid: Int) => 691)
  )
  val lids = Map("5NN" -> 2, "C4.5" -> 666003, "NB" -> 12, "SVM" -> 2651110, "RF" -> 773)
  val convlids = Map("5NN" -> 20, "C4.5" -> 50, "NB" -> 30, "SVM" -> 10, "RF" -> 0)
  val learners = lids.keys
  val strategies = sids.keys

  val fw = new FileWriter("neurocomputing2019-balacc-5x5-fold-75datasets-15strats-5classifs.csv")
  var c = 0
  val cmax = datasets.length * strategies.size * learners.size * 25
  for {
    dataset <- datasets
  } {
    println(dataset)
    val db = Db(dataset)
    for {
      strategy <- strategies
      learner <- learners
      run <- 0 to 4
      fold <- 0 to 4
    } {
      val lid = lids(learner)
      val sid = sids(strategy)(convlids(learner))
      var pool = -1.0
      var sql = s"select id from p where s=$sid and l=$lid and r=$run and f=$fold"
      try {
        pool = db.read(sql).head.head
        sql = s"select v from r where m>=100000000 and m<100000000+2000000 and p=$pool order by m"
        val result = db.read(sql).flatten
        result.zipWithIndex foreach { case (kappa, query) =>
          //          println(s"$dataset, $strategy, $learner, $run, $fold, $query, $kappa")
          fw.write(s"$dataset, $strategy, $learner, $run, $fold, $query, $kappa\n")
        }
        c += 1
        println(s"${(10000 * c / cmax.toDouble).round / 100.0}%")
      } catch {
        case ex: Throwable =>
          println("query:" + sql)
          sys.exit()
      }
    }
    db.close()
  }
  fw.close()
}
