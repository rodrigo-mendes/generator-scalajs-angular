package todomvc.example

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.Any.{ fromBoolean, fromFunction1, fromString, jsArrayOps, wrapArray }
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.JSConverters.JSRichGenTraversableOnce
import scala.scalajs.js.UndefOr
import scala.scalajs.js.UndefOr.undefOr2ops
import scala.scalajs.js.annotation.JSExport
import scala.util.{ Failure, Success }
import org.scalajs.dom.console
import com.greencatsoft.angularjs.{ inject, injectable }
import com.greencatsoft.angularjs.AbstractController
import com.greencatsoft.angularjs.core.{ Location, Scope }

/**
 * The main controller for the application.
 *
 * The controller
 * <ul>
 *   <li>retrieves and persists the model via the $http service</li>
 *   <li>exposes the model to the template and provides event handlers</li>
 * </ul>
 */
@JSExport
@injectable("todoCtrl")
class TodoCtrl(scope: TodoScope, location: Location, service: TaskService)
  extends AbstractController[TodoScope](scope) {

  // Need to initialize scope properties here, since we cannot declare default values  
  // for properties of a class which extends js.Object. 
  scope.todos = js.Array[Task]()
  scope.newTitle = ""
  scope.location = location
  scope.statusFilter = literal()

  service.findAll() onComplete {
    case Success(tasks) =>
      scope.todos = tasks.toJSArray
      update()
    case Failure(t) => handleError(t)
  }

  def todos: Seq[Task] = scope.todos

  @JSExport
  def save(todo: Task) {
    service.update(todo) onComplete {
      case Success(_) => update()
      case Failure(t) => handleError(t)
    }
  }

  @JSExport
  def add() {
    val title = scope.newTitle.trim
    if (title != "") add(Task(title), scope)
  }

  private def add(todo: Task, scope: TodoScope) {
    service.create(todo) onComplete {
      case Success(newTask) =>
        scope.todos :+= newTask
        scope.newTitle = ""

        update()
      case Failure(t) => handleError(t)
    }
  }

  @JSExport
  def remove(todo: Task) {
    service.delete(todo.id) onComplete {
      case Success(_) =>
        scope.todos = todos.filter(_ != todo).toJSArray
        update()
      case Failure(t) => handleError(t)
    }
  }

  @JSExport
  def clearCompleted() {
    service.clearAll() onComplete {
      case Success(_) =>
        scope.todos = todos.filter(!_.completed).toJSArray
        update()
      case Failure(t) => handleError(t)
    }
  }

  @JSExport
  def markAll(completed: Boolean) {
    service.markAll(completed) onComplete {
      case Success(_) =>
        todos.foreach(_.completed = !completed)
        update()
      case Failure(t) => handleError(t)
    }
  }

  private def update() {
    scope.remainingCount = todos.count(!_.completed)
    scope.allChecked = scope.remainingCount == 0
  }

  private def handleError(t: Throwable) {
    console.error(s"An error has occured: $t")
  }
}
