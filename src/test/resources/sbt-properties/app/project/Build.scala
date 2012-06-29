import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  def fromEnv(name: String) = System.getenv(name) match {
    case null => None
    case value => Some(value)
  }

  val appName = fromEnv("project.artifactId").getOrElse("my-app")
  val appVersion = fromEnv("project.version").getOrElse("1.0-SNAPSHOT")

  System.out.println("AppName => " + appName + " / " + fromEnv("project.artifactId"))


  val appDependencies = Seq(
    // Add your project dependencies here,
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
    // Target folder
    target <<= baseDirectory / "target",

    // Source folders
    sourceDirectory in Compile <<= baseDirectory / "src/main/java",
    sourceDirectory in Test <<= baseDirectory / "src/test/java",

    confDirectory <<= baseDirectory / "src/main/conf",
    resourceDirectory in Compile <<= baseDirectory / "src/main/conf",

    scalaSource in Compile <<= baseDirectory / "src/main/scala",
    scalaSource in Test <<= baseDirectory / "src/test/scala",

    javaSource in Compile <<= baseDirectory / "src/main/java",
    javaSource in Test <<= baseDirectory / "src/test/java",


    distDirectory <<= baseDirectory / "target/dist",

    playAssetsDirectories := Seq.empty[File],

    // The route file also needs to be updated...
    playAssetsDirectories <+= baseDirectory / "src/main/resources"


  )

}
