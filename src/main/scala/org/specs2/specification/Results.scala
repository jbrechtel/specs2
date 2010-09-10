package org.specs2
package specification

sealed abstract class Result(val message: String = "", val expectationsNb: Int = 1)
case class Success(m: String = "")  extends Result(m)
case class Failure(m: String = "")  extends Result(m)
case class Error  (m: String = "")  extends Result(m)
case class Pending(m: String = "")  extends Result(m)
case class Skipped(m: String = "")  extends Result(m)