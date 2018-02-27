import java.text.SimpleDateFormat
import java.util.Date

import scala.annotation.tailrec

object Entities {

  //period of time in mins, in order to checking login series
  val MaxMinDiff = 60
  val MillisToMins: Long = 1000 * 60
  val actionedDTFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss")
  val FilePath = "src/main/resources/logins0.csv"

  case class Login(username: String, ip: String, actionedAt: Date) {
    def minsDifference(login: Login) = {
      math.abs(actionedAt.getTime - login.actionedAt.getTime) / MillisToMins
    }
  }

  object Login {
    def apply(username: String, ip: String, actionedAt: String): Login =
      new Login(username, ip, actionedDTFormat.parse(actionedAt))

    def parse(line: String): Login = {
      val Array(v1,v2,v3) = line.replace("\"", "").split(",")
      Login(v1, v2, v3)
    }
  }

  case class OutputLogin(ipAddress: String, start: List[Date], stop: List[Date], logins: List[Login]) {
    override def toString: String = {
      val allEntriesInOne = start zip stop zip logins
      val customDateFormat = (dt: Date) => actionedDTFormat.format(dt)
      val str = for (((startTime, endTime), login) <- allEntriesInOne) yield {
        s"$ipAddress,${customDateFormat(startTime)},${customDateFormat(endTime)},${login.username}:${customDateFormat(login.actionedAt)}"
      }
      str.mkString("\n")
    }

    def mCopy(entries: List[Login]) = OutputLogin(
      ipAddress,
      start ++ List.fill(entries.size)(entries.head.actionedAt),
      stop ++ List.fill(entries.size)(entries.last.actionedAt),
      logins ++ entries
    )

  }

  def convertToOuputLogin(list: Seq[Login]): Option[OutputLogin]= {

    @tailrec
    def scanByTwoItems(l: Seq[Login], prev: Login, isLast: Boolean, acc: List[Login]): List[Login] = l match {
      case head :: tail =>
        if (head.minsDifference(prev) < MaxMinDiff)
          scanByTwoItems(tail, head, true, acc :+ prev)
        else {
          if (isLast)
            scanByTwoItems(tail, head, false, acc :+ prev)
          else
            scanByTwoItems(tail, head, false, acc)
        }
      case Nil =>
        if (isLast) acc :+ prev
        else acc
    }

    val sortedList = list.sortBy(_.actionedAt)

    if (list.size > 1) {
      scanByTwoItems(sortedList.tail, sortedList.head, false, List.empty[Login]) match {
        case Nil => None
        case entries => Some(getEnterSeries(entries))
      }
    } else {
      None
    }

  }

  def getEnterSeries(list: List[Login]): OutputLogin = {
    def go(l: List[Login], prev: Login, tmp: List[Login], res: OutputLogin): OutputLogin = l match {
      case head :: tail =>
        if (head.minsDifference(prev) < MaxMinDiff) {
          go(tail, head, tmp :+ prev, res)
        } else {
          val outputLogin = res.mCopy(tmp)
          go(tail, head, Nil, outputLogin)
        }
      case Nil =>
        if (tmp.nonEmpty) res.mCopy(tmp :+ prev)
        else res
    }

    go(list.tail, list.head, Nil, OutputLogin(list.head.ip, Nil, Nil, Nil))
  }

}
