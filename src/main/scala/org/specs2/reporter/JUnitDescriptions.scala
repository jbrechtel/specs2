package org.specs2
package reporter

import _root_.org.junit.runner._
import org.specs2.data.Trees._
import scalaz._
import scalaz.Scalaz._
import scalaz.Traverse._
import main.Arguments
import specification._
import control.{ExecutionOrigin, Stacktraces}

/**
 * The JUnit descriptions class transforms a list of fragments
 * to:
 * 
 * * a Description object having children Descriptions. It is used by the JUnitRunner
 *   to display the suites and tests to execute
 * * a Map of Fragments to execute, indexed by Description: Description -> Fragment 
 * 
 * The Description object creation works by using the Levels reducer to build a Tree[Description].
 * That Tree is then folded bottom-up to create the necessary associations between the 
 * Description objects. 
 * 
 */
private[specs2]
class JUnitDescriptions(specificationClass: Class[_]) extends DefaultSelection {
  import JUnitDescriptions._
  type DescribedFragment = (Description, Fragment)
  def foldAll(fs: Seq[Fragment])(implicit args: Arguments) = {
    import Levels._
    val leveledFragments = Levels.foldAll(select(fs))
    lazy val root = createDescription(specificationClass.getName, klassName=specificationClass.getName)
    implicit val initial: DescribedFragment = (root, Text(specificationClass.getName))

    if (leveledFragments.isEmpty) DescriptionAndExamples(root, Seq(initial).toStream)
    else {
      val descriptionTree = leveledFragments.toTree(mapper(specificationClass.getName))
      val removeDanglingText = (t: Tree[DescribedFragment]) => {
        t.rootLabel  match {
          case (desc, Text(_)) if t.subForest.isEmpty  => (None:Option[DescribedFragment])
          case other                                   => Some(t.rootLabel)
        }
      }
      val prunedDescriptionTree = descriptionTree.prune(removeDanglingText)
      DescriptionAndExamples(asOneDescription(prunedDescriptionTree), prunedDescriptionTree.flatten)
    }
  }

}
private[specs2]
object JUnitDescriptions extends ExecutionOrigin {
  type DescribedFragment = (Description, Fragment)
  /**
   * This function is used to map each node in a Tree[Fragment] to a pair of 
   * (Description, Fragment)
   * 
   * The Int argument is the numeric label of the current TreeNode being mapped.
   * It is used to create a unique description of the example to executed which is required
   * by JUnit
   */
  def mapper(className: String): (Fragment, Int) => Option[DescribedFragment] = (f: Fragment, nodeLabel: Int) => f match {
    case (SpecStart(t, _))            => Some(createDescription(testName(t.name), klassName=className) -> f)
    case (Text(t))                    => Some(createDescription(testName(t), klassName=className) -> f)
    case (Example(description, body)) => Some(createDescription(testName(description.toString), nodeLabel.toString, className) -> f)
    case (Step(action))               => Some(createDescription("step", nodeLabel.toString, className) -> f)
    case (Action(action))             => Some(createDescription("action", nodeLabel.toString, className) -> f)
    case other                        => None
  }
  /**
   * Utility class grouping the total description + fragments to execute for each Description 
   */
  case class DescriptionAndExamples(val description: Description, executions: Stream[DescribedFragment])
  /**
   * @return a Description with parent-child relationships to other Description objects
   *         from a Tree[Description]
   */
  def asOneDescription(descriptionTree: Tree[DescribedFragment])(implicit args: Arguments = Arguments()): Description = {
    if (args.noindent)
      descriptionTree.flatten.drop(1).foldLeft(descriptionTree.rootLabel)(flattenChildren)._1
    else
      descriptionTree.bottomUp(addChildren).rootLabel._1
  }
  /** 
   * unfolding function attaching children descriptions their parent
   */
  private val addChildren = (desc: (Description, Fragment), children: Stream[DescribedFragment]) => {
    children.foreach { child => desc._1.addChild(child._1) }
    desc
  }
  /**
   * unfolding function attaching children descriptions the root
   */
  private def flattenChildren = (result: DescribedFragment, current: DescribedFragment) => {
    result._1.addChild(current._1)
    result
  }
  import text.Trim._
  /** @return a test name with no newlines */
  private def testName(s: String)= Trimmed(s).trimNewLines
  /** @return replace () with [] because it cause display issues in JUnit plugins */
  private def sanitize(s: String) = {
    val trimmed = Trimmed(s).trimReplace("(" -> "[",  ")" -> "]")
    if (trimmed.isEmpty) " "
    else trimmed
  }
  /** @return a sanitized description */
  def createDescription(s: String, label: String= "", klassName: String="") = {
    val code =
      if ((isExecutedFromAnIDE || isExecutedFromSBT) && !label.isEmpty)
        "("+label+")"
      else if (isExecutedFromGradle && !klassName.isEmpty)
        "("+klassName+")"
      else ""
    Description.createSuiteDescription(sanitize(s)+code)
  }
  
}
