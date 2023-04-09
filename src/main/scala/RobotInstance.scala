package bb
package cc

import java.awt.Robot

trait RobotInstance {
  def robot: Robot
}

object RobotInstance {
  def apply(robot: => Robot): RobotInstance = {
    def robotAlias = robot
    new RobotInstance {
      override val robot: Robot = robotAlias
    }
  }
}
