package bb
package cc

import java.awt.Robot

object AA {

  def main(arr: Array[String]): Unit = {
    implicit val robotInstance: RobotInstance   = RobotInstance(new Robot)
    implicit val moveMouseRobot: MoveMouseRobot = new MoveMouseRobotImpl1
    for (_ <- 1 to 10) {
      moveMouseRobot.mouseMove(600, 600)
      Thread.sleep(100)
      moveMouseRobot.mouseMove(980, 980)
      Thread.sleep(100)
      moveMouseRobot.mouseMove(200, 200)
      Thread.sleep(100)
    }
  }

}
