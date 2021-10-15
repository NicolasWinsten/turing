import TM._
import scala.collection.mutable.ListBuffer

// a turing machine takes an input and has a list of transitions
abstract class TM(input: String)(transits: Iterable[Trans]) {
  require(transits.nonEmpty)
  protected val blank = '_'
  protected var head = 0
  protected val tape = new StringBuilder(if (input.isEmpty) blank.toString else input)

  // choose start state to be the first state mentioned in the transition list
  protected var currState: State = transits.head.old._1

  // follow the appropriate transition given the current state and head symbol
  def step(): Unit

  /**
   * default behavior of moving the tape head assumes a doubly infinite tape
   * @param to direction to move the tape head
   */
  def move(to: Dir): Unit = to match {
    case LEFT if head == 0 => tape.insert(0, blank)
    case RIGHT if head == tape.length - 1 => tape.append(blank); head += 1
    case RIGHT => head += 1
    case LEFT => head -= 1
  }

  // true if this machine has some transition to follow
  def canStep: Boolean

  def getCurrState: State = currState
}

// non-deterministic machines may have multiple possible transitions to follow given a current state and head symbol.
// the machine will spawn more children machines to follow any additional transitions
class NondeterministicTM(input: String)(transits: Iterable[Trans]) extends TM(input)(transits) {
  private val delta: Map[oldConf, Set[newConf]] = transits.groupBy(_.old) map { group =>
    val (on, choices) = group
    on -> (choices map (_.to)).toSet
  }

  private val children: ListBuffer[NondeterministicTM] = ListBuffer.empty

  override def step(): Unit = {
    children foreach (_.step())

    if (canStep) delta get (currState, tape(head)) match {
      case Some(choices) =>
        val firstChoice = choices.head

        // spawn new simulation using the remaining possible transitions
        for ((otherChoiceState, otherChoiceSymbol, dir) <- choices - firstChoice) {
          val newTM = new NondeterministicTM(this.tape.toString())(transits)
          newTM.head = this.head
          newTM.currState = otherChoiceState
          newTM.tape(newTM.head) = otherChoiceSymbol
          newTM.move(dir)
          children += newTM
        }

        // perform transition on this TM
        val (newState, newSymbol, dir) = firstChoice
        currState = newState
        tape(head) = newSymbol
        move(dir)
      case None => throw new RuntimeException(s"No transition on ($currState, ${tape.head})")
    }
  }

  override def canStep: Boolean = delta.contains((currState, tape(head)))

  // retrieve all the tapes spawned from this turing machine
  def getTapes: List[(String, Int, String, Boolean)] = { // list of (tape, head, state, couldContinue?)
    (tape.toString(), head, currState.label, canStep) :: (children.toList flatMap (_.getTapes))
  }
}

// deterministic turing machines will always have at most one transition to follow at any instant
abstract class DeterministicTM(input: String)(transits: Iterable[Trans]) extends TM(input)(transits){
  protected val delta: Map[oldConf, newConf] =
   transits.foldLeft(Map[oldConf, newConf]())((acc, t) => acc + (t.old -> t.to))
  require(delta.size == transits.size)

  def step(): Unit = delta get (currState, tape(head)) match {
    case Some((newState, newSymbol, dir)) =>
      currState = newState
      tape(head) = newSymbol
      move(dir)
    case None => throw new RuntimeException(s"No transition on ($currState, ${tape.head})")
  }

  def getTape: (String, Int, String, Boolean) = (tape.toString(), head, currState.label, canStep)
  def canStep: Boolean = delta.contains((currState, tape(head)))
}

// standard machine. its tape can grow arbitrarily large in either direction
class DoublyInfiniteTM(input: String)(transits: Iterable[Trans]) extends DeterministicTM(input)(transits)

// machine with tape bounded on the left. tape head starts on that leftmost cell.
// any left move attempted on that leftmost cell will result in the head simply staying in place
class SemiInfiniteTM(input: String)(transits: Iterable[Trans]) extends DeterministicTM(input)(transits) {
  override def move(to: Dir): Unit = to match {
    case LEFT if head == 0 => ()
    case RIGHT if head == tape.length - 1 => tape.append(blank); head += 1
    case RIGHT => head += 1
    case LEFT => head -= 1
  }
}

// linearly bounded automaton has a tape fixed to the size of the input.
class LBA(input: String)(transits: Iterable[Trans]) extends DeterministicTM(input)(transits) {
  private var configRepeated = false
  private val configs: scala.collection.mutable.Set[String] = scala.collection.mutable.Set(currentConfig)

  override def step(): Unit = {
    super.step()
    if (!configRepeated) configRepeated = configs.contains(currentConfig)
    configs += currentConfig
  }

  override def move(to: Dir): Unit =
    to match {
      case LEFT if head == 0 => ()
      case RIGHT if head == tape.length - 1 => ()
      case RIGHT => head += 1
      case LEFT => head -= 1
    }

  def isInLoop: Boolean = configRepeated
  def currentConfig: String = {
    val content = tape.toString()
    content.substring(0, head) + currState.label + content.substring(head)
  }
}

object TM {
  type oldConf = (State, Char) // a machine's current status
  type newConf = (State, Char, Dir) // new status to transition machine into

  case class Trans(old: oldConf, to: newConf)
  case class State(label: String) extends AnyVal

  sealed trait Dir
  object LEFT extends Dir
  object RIGHT extends Dir
}
