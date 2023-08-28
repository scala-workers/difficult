package bb
package cc

import java.awt.Robot
import cats.*
import cats.effect.*
import com.caoccao.javet.interop.{NodeRuntime, V8Host}

object Test01 extends IOApp.Simple {

  class V8RuntimeBuilder(host: V8Host) {
    val instance: Resource[IO, NodeRuntime] = Resource.fromAutoCloseable(IO.delay(host.createV8Runtime: NodeRuntime))
  }

  val v8Instance: Resource[IO, V8Host] = Resource.fromAutoCloseable(IO.delay(V8Host.getNodeInstance()))

  def exec(nodeRuntime: NodeRuntime): IO[Unit] = {
    val execString: String =
      """
        |//引入readline模块
        |const readline = require('readline');
        |//创建readline接口实例
        |let r1 = readline.createInterface({
        |    input:process.stdin,
        |    output:process.stdout
        |});
        |//使用question方法
        |r1.question('你想吃什么？',function (anwser){
        |    console.log(`我想吃${anwser}`);
        |    //添加close事件，不然不会结束
        |    r1.close();
        |});
        |//close事件监听
        |r1.on('close',function (){
        |    //结束程序
        |    process.exit(0);
        |});
        |""".stripMargin
    def execAction: Unit = {
      nodeRuntime.getExecutor(execString).executeVoid()
    }
    IO.delay(System.out.println("1 + 1 = " + nodeRuntime.getExecutor("1 + 1").executeInteger()))
    IO.delay(execAction)
  }

  override val run: IO[Unit] = {
    val runtimeResource: Resource[IO, NodeRuntime] = for {
      given V8Host <- v8Instance
      nodeRuntime  <- V8RuntimeBuilder(summon).instance
    } yield nodeRuntime

    runtimeResource.use(v => exec(v))
  }

}
