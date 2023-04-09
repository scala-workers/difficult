package bb
package cc

trait MoveMouseRobot {
  def mouseMove(x: Int, y: Int): Unit
}

class MoveMouseRobotImpl1(implicit robotInstance: RobotInstance) extends MoveMouseRobot {
  override def mouseMove(x: Int, y: Int): Unit = robotInstance.robot.mouseMove(x, y)
}
