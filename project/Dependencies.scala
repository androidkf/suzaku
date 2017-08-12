import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object Dependencies {
  private object versions {
    val scalaTest  = "3.0.1"
    val scalaMock  = "3.6.0"
    val arteria    = "0.1.1-SNAPSHOT"
    val scalaJSDOM = "0.9.3"
  }

  val scalaTest  = Def.setting("org.scalatest" %%% "scalatest"                   % versions.scalaTest % Test)
  val scalaMock  = Def.setting("org.scalamock" %%% "scalamock-scalatest-support" % versions.scalaMock % Test)
  val arteria    = Def.setting("io.suzaku"     %%% "arteria-core"                % versions.arteria)
  val scalaJSDOM = Def.setting("org.scala-js"  %%% "scalajs-dom"                 % versions.scalaJSDOM)

}
