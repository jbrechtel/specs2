package org.specs2
package specification
import scalaz._
import Scalaz._
import main.Arguments
import Fragments._
/**
 * A Base specification contains the minimum elements for a Specification
 * 
 * * a Seq of Fragments, available through the SpecificationStructure trait
 * * methods for creating Fragments from the FragmentsBuilder trait
 * * methods to include other specifications
 *
 */
trait BaseSpecification extends SpecificationStructure with FragmentsBuilder with SpecificationInclusion

/**
 * additional methods to include other specifications or Fragments
 */
trait SpecificationInclusion { this: FragmentsBuilder =>
  def include(f: Fragments): FragmentsFragment = fragmentsFragments(f)
  implicit def include(s: SpecificationStructure): FragmentsFragment = include(s.content)
  def include(s: SpecificationStructure, ss: SpecificationStructure*): FragmentsFragment = include((Seq(s)++ss).map(_.content).∑)
  def include(args: Arguments, s: SpecificationStructure*): FragmentsFragment = include(s.map(_.content).∑.overrideArgs(args))
}
/**
 * The structure of a Specification is simply defined as a sequence of fragments
 */
trait SpecificationStructure { 
  /** declaration of Fragments from the user */
  def is: Fragments
  
  /** 
   * this "cached" version of the Fragments is kept hidden from the user to avoid polluting
   * the Specification namespace.
   * SpecStart and SpecEnd fragments are added if the user haven't inserted any
   */
  private[specs2] lazy val content: Fragments = Fragments.withSpecStartEnd(is, this)
}
