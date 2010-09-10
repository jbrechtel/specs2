package org.specs2
import specification._

class FeaturesSpec extends Specification {
  val examples = 
"""
 These are the list of features to develop / port for specs2.
 
 The prioritization idea is to:
 
 * check the feasability of old features in a fully functional setting
 * re-evaluate the necessity of some existing features and provide the most important ones first
     including the ones necessary for a proper development of specs2 itself
 * make sure at each step that everything is well specified
 * dependencies are well-controled
 * implicits visibility is reduced
 * specs2 API is kept private unless necessary
 """^^
  "High priority"^
    "A Console reporter"^
      "with statistics" ! done^
    "Matchers"^
      "for strings" ! todo^
      "for iterables" ! todo^
    "Spec for before/after/around"^
      "before/after" ! todo^
      "example isolation" ! todo^
      "around" ! todo^
      "first/last" ! todo^
      "beforeSpec/afterSpec" ! todo^
    "A literate specs environment"^
      "with an html reporter" ! todo^
      "with non mutable forms" ! todo^
    "A JUnit4 reporter"^
      "with suites and test cases" ! todo^
    "A sbt reporter"^
      "based on a generic notifier" ! todo^
  par^    
  "Low priority"^
    "A Console reporter"^
      "with a timer"    ! todo
      "with colored output"    ! todo
      "with intermediary stats where required"    ! todo
    "Detailed diffs"^
      "non mutable version"    ! todo
    
    val done = Success("DONE")
    val todo = Pending("TODO")
}