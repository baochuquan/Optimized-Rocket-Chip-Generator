// See LICENSE for license details.

package util

import Chisel._
import cde._
import diplomacy.LazyModule
import java.io.{File, FileWriter}

/** Representation of the information this Generator needs to collect from external sources. */
case class ParsedInputNames(
    targetDir: String,
    topModuleProject: String,
    topModuleClass: String,
    configProject: String,
    configs: String) {
  val configClasses: Seq[String] = configs.split('_')
  val fullConfigClasses: Seq[String] = configClasses.map(configProject + "." + _)
  val fullTopModuleClass: String = topModuleProject + "." + topModuleClass
}

/** Common utilities we supply to all Generators. In particular, supplies the
  * canonical ways of building various JVM elaboration-time structures.
  */
trait HasGeneratorUtilities {
  def getConfig(names: ParsedInputNames): Config = {
    names.fullConfigClasses.foldRight(new Config()) { case (currentName, config) =>
      val currentConfig = try {
        Class.forName(currentName).newInstance.asInstanceOf[Config]
      } catch {
        case e: java.lang.ClassNotFoundException =>
          throwException(s"""Unable to find part "$currentName" from "${names.configs}", did you misspell it?""", e)
      }
      currentConfig ++ config
    }
  }

  def getParameters(names: ParsedInputNames): Parameters = getParameters(getConfig(names))

  def getParameters(config: Config): Parameters = Parameters.root(config.toInstance)

  import chisel3.internal.firrtl.Circuit
  def elaborate(names: ParsedInputNames, params: Parameters): Circuit = {
    val gen = () =>
      Class.forName(names.fullTopModuleClass)
        .getConstructor(classOf[cde.Parameters])
        .newInstance(params)
        .asInstanceOf[Module]

    Driver.elaborate(gen)
  }

  def writeOutputFile(targetDir: String, fname: String, contents: String): File = {
    val f = new File(targetDir, fname) 
    val fw = new FileWriter(f)
    fw.write(contents)
    fw.close
    f
  }
}


/** Standardized command line interface for Scala entry point */
trait GeneratorApp extends App with HasGeneratorUtilities {
  lazy val names: ParsedInputNames = {
    require(args.size == 5, "Usage: sbt> " + 
      "run TargetDir TopModuleProjectName TopModuleName " +
      "ConfigProjectName ConfigNameString")
    ParsedInputNames(
      targetDir = args(0),
      topModuleProject = args(1),
      topModuleClass = args(2),
      configProject = args(3),
      configs = args(4))
  }

  // Canonical ways of building various JVM elaboration-time structures
  lazy val td = names.targetDir
  lazy val config = getConfig(names)
  lazy val world = config.toInstance
  lazy val params = Parameters.root(world)
  lazy val circuit = elaborate(names, params)

  val longName: String // Exhaustive name used to interface with external build tool targets

  /** Output FIRRTL, which an external compiler can turn into Verilog. */
  def generateFirrtl {
    Driver.dumpFirrtl(circuit, Some(new File(td, s"$longName.fir"))) // FIRRTL
  }

  /** Output software test Makefrags, which provide targets for integration testing. */
  def generateTestSuiteMakefrags {
    addTestSuites
    writeOutputFile(td, s"$longName.d", rocketchip.TestGeneration.generateMakefrag) // Coreplex-specific test suites
  }

  def addTestSuites {
    // TODO: better job of Makefrag generation
    //       for non-RocketChip testing platforms
    import rocketchip.{DefaultTestSuites, TestGeneration}
    TestGeneration.addSuite(DefaultTestSuites.groundtest64("p"))
    TestGeneration.addSuite(DefaultTestSuites.emptyBmarks)
    TestGeneration.addSuite(DefaultTestSuites.singleRegression)
  } 

  /** Output Design Space Exploration knobs and constraints. */
  def generateDSEConstraints {
    writeOutputFile(td, s"${names.configs}.knb", world.getKnobs) // Knobs for DSE
    writeOutputFile(td, s"${names.configs}.cst", world.getConstraints) // Constraints for DSE
  }

  /** Output a global Parameter dump, which an external script can turn into Verilog headers. */
  def generateParameterDump {
    writeOutputFile(td, s"$longName.prm", ParameterDump.getDump) // Parameters flagged with Dump()
  }

  /** Output a global ConfigString, for use by the RISC-V software ecosystem. */
  def generateConfigString {
    ConfigStringOutput.contents.foreach(c => writeOutputFile(td, s"${names.configs}.cfg", c))
  }

  /** Output a global LazyModule topology for documentation purposes. */
  def generateGraphML {
    TopModule.contents.foreach(lm => writeOutputFile(td, s"${names.configs}.graphml", lm.graphML))
  }
}

object ConfigStringOutput {
  var contents: Option[String] = None
}

object TopModule {
  var contents: Option[LazyModule] = None
}
