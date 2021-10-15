import TM.{LEFT, RIGHT, State, Trans}
import org.scalajs.dom
import dom.document
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement, HTMLInputElement, HTMLSelectElement, HTMLTextAreaElement}
import scalatags.JsDom.all._

import scala.collection.mutable

// TODO make use of css stylesheets cuz sheesh

import scala.scalajs.js.timers.setTimeout
object index {
  val programInput = textarea(
    examples.palindrome,
    id := "source", title := "Write your machine's program here",
    backgroundColor := "transparent",
    width := 400.px, minWidth := 200.px, height := 450.px, maxHeight := 500.px,
    wrap := "off", borderWidth := 3.px, marginTop := (-1).px, spellcheck := false
  )

  var machine: TM = null // this is bad scala. dont do this
  var paused = false // use this flag to stop machine mid run

  // display the machine's tape in a table with its current state.
  val tapeDisplay = table(id := "tapeDisplay", width := 500.px, margin := "auto", borderCollapse := "collapse",
    thead(//display := "block",
      tr(display := "block",
        td(span("Tape", borderWidth := 3.px, borderStyle := "solid", padding := 3.px, borderBottomWidth := 0.px)),
        td(span("State", borderWidth := 3.px, borderStyle := "solid", padding := 3.px, borderBottomWidth := 0.px), textAlign := "right", width := 100.pct)
      )
    ),
    tbody(fontSize := 20.px, maxHeight := 430.px, overflowY := "auto", borderWidth := 3.px, borderStyle := "solid", borderColor := "black",
      display := "block", width := 100.pct,
      tr(width := 100.pct, height := 20.px, borderWidth := 3.px, borderStyle := "solid", borderColor := "black")
    )
  )

  val variantDescs = Array(
    "A Doubly Infinite tape means the machine's tape head may move left or right to a new cell infinitely many times",
    """A Semi-Infinite tape is only infinite in one direction.
      |The machine's tape head starts in the leftmost cell, and may not move to the left of it.
      |If the machine attempts a left move in the leftmost cell, the tape head will simply stay in place.
      |""".stripMargin,
    """A Linearly Bounded Automaton has a tape that is fixed to the length of the input. If the machine attempts to
      |move past the input, the tape head will simply stay put.
      |""".stripMargin,
    """A Non-deterministic turing machine may have multiple possible transitions given a current state and symbol.
      |The simulator will display the branching paths that the machine may take and then discard any dead branches
      |after each step.
      |""".stripMargin
  )

  val controls = div(
    button("Run", id := "runBtn", disabled, onclick := (() => run())),
    button("Pause", id := "pauseBtn", disabled, onclick := (() => paused = true)),
    button("Step", id := "stepBtn", disabled, onclick := (() => step())),
    button("Build", id := "buildBtn", onclick := (() => build())),
    br,
    span(id := "errorDisplay", color := "red", margin := 3.px),
    div(
      "Initial Input: ",
      input(value := "1001001", `type` := "text", id := "tapeInput", spellcheck := false)
    ),
    span(
      input(`type` := "checkbox", id := "fullSpeedCheckBox"),
      "Run at full speed"
    ),
    br,
    span(
      "Machine Variant: ",
      select(
        id := "machineVariantSelector",
        onchange := (() => updateVariantDesc()),
        option( value := "0", selected := "selected", "Doubly Infinite Tape"),
        option( value := "1", "Semi-Infinite Tape"),
        option( value := "2", "Bounded Tape"),
        option( value := "3", "Non-deterministic")
      ),
      br,
      span(id := "selectedVariantDesc", i(variantDescs(0)))
    )
  )

  val helpSection = div(
    h2("Instructions"),
    ul(
      li("Describe each transition on a single line: ", code("<current state> <current symbol> <new symbol> <direction> <new state>")),
      li("State labels can be any alphanumeric word (case-sensitive)"),
      li("Symbols should be single characters. Underscore '_' represents a blank"),
      li("Specify the direction to move the tape head with 'l' for left or 'r' for right"),
      li("Write comments with ", code("//")),
      li("The machine will halt once any of the conditions are met:",
        ul(
          li("The machine has no available transitions to follow"),
          li("The machine enters a state whose label starts with 'halt'"),
          li("The machine enters a state whose label starts with 'reject'"),
          li("The machine enters a state whose label starts with 'accept'",
            ul(li("Note: the Non-deterministic machine will continue until all of its branches halt, or if just one reaches an 'accept' state"))),
        )
      )
    ),
    h2("Notes"),
    ul(
      li("This simulator follows ", a(href := "http://morphett.info/turing/turing.html", "Anthony Morphett's")),
      li("This page was written entirely in Scala.js. Check the implementation ", a(href := "https://github.com/NicolasWinsten/turing", "here")),
      li("Send all feedback and complaints to nicolasd DOT winsten AT gmail com")
    )
  )


  def main(args: Array[String]): Unit = {
    val content = div(
      h1("Turing Machine Simulator", display := "inline"),
      small("go back to ", a(href :="http://nicolaswinsten.com", "my page"), display := "inline", float := "right"),
      p(
        "This is a ", a(href := "https://en.wikipedia.org/wiki/Turing_machine", "Turing machine"), " simulator",
        ol(
          li("Write your program in the text box and your program's input in the text field"),
          li("Press ", b("Build"), " to construct your machine"),
          li("Press ", b("Run"), " to run your machine"),
          li("Or load some other example machines: ",
            button("a", sup("n"), "b", sup("n"), "c", sup("n"), onclick := {() =>
              document.getElementById("source").asInstanceOf[HTMLTextAreaElement].value = examples.anbncn
              document.getElementById("machineVariantSelector").asInstanceOf[HTMLSelectElement].selectedIndex = 1
              document.getElementById("tapeInput").asInstanceOf[HTMLInputElement].value = "aaabbbccc"
            }),
            button("gen", margin := 5.px, onclick := {() =>
              document.getElementById("source").asInstanceOf[HTMLTextAreaElement].value = examples.gen
              document.getElementById("machineVariantSelector").asInstanceOf[HTMLSelectElement].selectedIndex = 3
              document.getElementById("tapeInput").asInstanceOf[HTMLInputElement].value = "111"
            })
          )
        )
      ),
      table(
        thead(
          tr(
            td(span("Program Input",
              borderWidth := 3.px, borderStyle := "solid", borderColor := "brown", padding := 3.px, borderBottomWidth := 0.px))
          )
        ),
        tbody(
          tr(td(programInput), td(tapeDisplay, width := 50.pct))
        )
      ),
      controls,
      helpSection,
      fontFamily := "monospace", fontSize := 15.px,
      backgroundColor := "beige", padding := 10.px, borderStyle := "solid", borderWidth := 3.px, borderColor := "brown",
    )

    val root = dom.document.getElementById("root")
    root.innerHTML = ""
    root.appendChild(content.render)
  }

  def updateVariantDesc(): Unit = {
    val variant = document.getElementById("machineVariantSelector").asInstanceOf[HTMLSelectElement].selectedIndex
    document.getElementById("selectedVariantDesc").innerHTML = ""
    document.getElementById("selectedVariantDesc").appendChild(i(variantDescs(variant)).render)
  }

  def updateTapeDisplay(): Unit = {
    val tapeTableBody = document.querySelector("#tapeDisplay tbody")
    tapeTableBody.innerHTML = ""

    // add the given configuration to the tape display as a row in the table
    def appendRow(tape: String, head: Int, state: String, canContinue: Boolean): Unit = {
      val stateColor = state match {
        case s"halt$_" => "lightgrey"
        case s"accept$_" => "lightgreen"
        case s"reject$_" | _ if !canContinue => "lightcoral"
        case _ => "transparent"
      }

      tapeTableBody.appendChild(
        tr(border := 0.px, borderBottomWidth := 1.px, borderStyle := "solid", borderColor := "black",
          td(padding := 2.px, width := 100.pct,
            div(overflowX := "auto", maxWidth := 450.px, cls := "tapeView",
              pre(tape.substring(0, head), display := "inline"),
              pre(tape(head).toString, color := "red", display := "inline"),
              pre(tape.substring(head + 1), display := "inline")
            )
          ),
          td(state, backgroundColor := stateColor, borderLeftStyle := "solid", borderLeftWidth := 1.px, padding := 2.px, textAlign := "right")
        ).render
      )
    }

    machine match {
      case m: DeterministicTM =>
        val (tapeStr, head, state, canContinue) = m.getTape
        appendRow(tapeStr, head, state, canContinue)
      case m: NondeterministicTM =>
        // cut off any dead branches we have already seen, display all living branches
        m.getTapes filterNot haltedBranches foreach { config =>
          val (tapeStr, head, state, canContinue) = config
          appendRow(tapeStr, head, state, canContinue)
          if (state.matches("[(halt)(reject)].*") || !canContinue) haltedBranches += config
        }
      case null => ()
    }
    updateScrolls()
  }

  // adjust the scroll bars in the tape display so that the tape head is visible
  def updateScrolls(): Unit = {
    val tapeViews = document.querySelectorAll(".tapeView")
    for (i <- 0 until tapeViews.length) {
      val tapeView = tapeViews(i).asInstanceOf[HTMLDivElement]
      val leftOfHead = tapeView.firstChild.innerText.length
      tapeView.scrollLeft = leftOfHead * 11 - 22 // is this awful way to do this?
    }
  }

  def build(): Unit = {
    // gather the user given instructions
    haltedBranches.clear()
    val program = document.getElementById("source").asInstanceOf[HTMLTextAreaElement].value
    val lines = program.split("\n")
    val instructions = lines filterNot (line => line.trim.startsWith("//") || line.matches("\\s*"))

    val pattern = """^([\w\-]+) (\S) (\S) ([lrLR]) ([\w\-]+)\s*(?://.*)?$"""
      .r("currState", "currSymbol", "newSymbol", "direction", "newState")

    val chosenVariant =
      document.getElementById("machineVariantSelector").asInstanceOf[HTMLSelectElement].selectedIndex

    // catch any malformed instructions
    instructions.find(!pattern.matches(_)) match {
      case Some(line) => reportError(s"line ${lines.indexOf(line)}: $line is malformed")
      case None =>
        val transitions = instructions map {
          case pattern(qCurr, currSym, newSym, dir, qNew) =>
            Trans((State(qCurr), currSym(0)), (State(qNew), newSym(0), if (dir.toLowerCase == "l") LEFT else RIGHT))
        }
        val tapeInput = document.getElementById("tapeInput").asInstanceOf[HTMLInputElement].value

        // check for nondeterministic transitions
        val movesByConfig = transitions.groupBy(_.old)
        val nonDeterministicChoice = movesByConfig.find(_._2.length > 1) map (_._1)
        if (chosenVariant < 3 && nonDeterministicChoice.nonEmpty) {
          val Some((state, symbol)) = nonDeterministicChoice
          reportError(s"Multiple transitions found for state ${state.label} on $symbol. Consider using a Non-deterministic TM instead.")
        } else if (transitions.isEmpty) {
          reportError("You must supply some rules")
        } else {
          // build the machine
          machine = chosenVariant match {
            case 0 => new DoublyInfiniteTM(tapeInput)(transitions)
            case 1 => new SemiInfiniteTM(tapeInput)(transitions)
            case 2 => new LBA(tapeInput)(transitions)
            case 3 => new NondeterministicTM(tapeInput)(transitions)
          }
          if (machineCanContinue) stepBtn.disabled = false; runBtn.disabled = false
          clearError()
          updateTapeDisplay()
        }
    }

  }

  // perform a transition and update the display
  def step(): Unit = {
    machine.step()
    updateTapeDisplay()
    if (!machineCanContinue) {
      stepBtn.disabled = true
      runBtn.disabled = true
      pauseBtn.disabled = true
    }
  }

  // continue running the machine until pause or halt
  def run(): Unit = {
    if (paused) {
      pauseBtn.disabled = true
      runBtn.disabled = false
      paused = false
    }
    else if (machineCanContinue) {
      runBtn.disabled = true
      pauseBtn.disabled = false
      if (fullSpeed) { // jump 25 steps and then update display
        for (_ <- 1 to 25) if (machineCanContinue) machine.step()
        updateTapeDisplay()
        setTimeout(10)(run())
      } else { // slow mode
        step()
        setTimeout(50)(run())
      }
    } else { // machine has halted
      stepBtn.disabled = true
      runBtn.disabled = true
      pauseBtn.disabled = true
    }
  }

  // return true if the current machine has not reached a halting state
  def machineCanContinue: Boolean = machine match {
    case m: DeterministicTM => m.canStep && (!m.getCurrState.label.matches("[(halt)(reject)(accept)].*"))
    case m: NondeterministicTM =>
      val allTapes = m.getTapes
      // a non deterministic TM can continue if no branch has accepted and there exists a branch that hasn't halted
      allTapes.forall(!_._3.startsWith("accept")) && allTapes.exists(c => c._4 && !c._3.matches("[(halt)(reject)(accept)].*"))
  }

  def runBtn = document.getElementById("runBtn").asInstanceOf[HTMLButtonElement]
  def pauseBtn = document.getElementById("pauseBtn").asInstanceOf[HTMLButtonElement]
  def stepBtn = document.getElementById("stepBtn").asInstanceOf[HTMLButtonElement]
  def buildBtn = document.getElementById("buildBtn").asInstanceOf[HTMLButtonElement]
  def fullSpeed: Boolean = document.getElementById("fullSpeedCheckBox").asInstanceOf[HTMLInputElement].checked

  def reportError(msg: String): Unit = document.getElementById("errorDisplay").innerHTML = "Error: " + msg
  def clearError(): Unit = document.getElementById("errorDisplay").innerHTML = ""

  val haltedBranches = mutable.Set[(String, Int, String, Boolean)]()
  // keep track of past halted branches so we can trim them from the display
}