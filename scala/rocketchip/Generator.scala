// See LICENSE for license details.

package rocketchip

import rocket.{XLen, UseVM, UseAtomics, UseCompressed, FPUKey}
import scala.collection.mutable.LinkedHashSet

/** A Generator for platforms containing Rocket Coreplexes */
object Generator extends util.GeneratorApp {

  val rv64RegrTestNames = LinkedHashSet(
        "rv64ud-v-fcvt",
        "rv64ud-p-fdiv",
        "rv64ud-v-fadd",
        "rv64uf-v-fadd",
        "rv64um-v-mul",
        "rv64mi-p-breakpoint",
        "rv64uc-v-rvc",
        "rv64ud-v-structural",
        "rv64si-p-wfi",
        "rv64um-v-divw",
        "rv64ua-v-lrsc",
        "rv64ui-v-fence_i",
        "rv64ud-v-fcvt_w",
        "rv64uf-v-fmin",
        "rv64ui-v-sb",
        "rv64ua-v-amomax_d",
        "rv64ud-v-move",
        "rv64ud-v-fclass",
        "rv64ua-v-amoand_d",
        "rv64ua-v-amoxor_d",
        "rv64si-p-sbreak",
        "rv64ud-v-fmadd",
        "rv64uf-v-ldst",
        "rv64um-v-mulh",
        "rv64si-p-dirty")

  val rv32RegrTestNames = LinkedHashSet(
      "rv32mi-p-ma_addr",
      "rv32mi-p-csr",
      "rv32ui-p-sh",
      "rv32ui-p-lh",
      "rv32uc-p-rvc",
      "rv32mi-p-sbreak",
      "rv32ui-p-sll")

  override def addTestSuites {
    import DefaultTestSuites._
    val xlen = params(XLen)
    val vm = params(UseVM)
    val env = if (vm) List("p","v") else List("p")
    params(FPUKey) foreach { case cfg =>
      if (xlen == 32) {
        TestGeneration.addSuites(env.map(rv32ufNoDiv))
      } else {
        TestGeneration.addSuite(rv32udBenchmarks)
        TestGeneration.addSuites(env.map(rv64ufNoDiv))
        TestGeneration.addSuites(env.map(rv64udNoDiv))
        if (cfg.divSqrt) {
          TestGeneration.addSuites(env.map(rv64uf))
          TestGeneration.addSuites(env.map(rv64ud))
        }
      }
    }
    if (params(UseAtomics))    TestGeneration.addSuites(env.map(if (xlen == 64) rv64ua else rv32ua))
    if (params(UseCompressed)) TestGeneration.addSuites(env.map(if (xlen == 64) rv64uc else rv32uc))
    val (rvi, rvu) =
      if (xlen == 64) ((if (vm) rv64i else rv64pi), rv64u)
      else            ((if (vm) rv32i else rv32pi), rv32u)

    TestGeneration.addSuites(rvi.map(_("p")))
    TestGeneration.addSuites((if (vm) List("v") else List()).flatMap(env => rvu.map(_(env))))
    TestGeneration.addSuite(benchmarks)
    TestGeneration.addSuite(new RegressionTestSuite(if (xlen == 64) rv64RegrTestNames else rv32RegrTestNames))
  }

  val longName = names.topModuleProject + "." + names.configs
  generateFirrtl
  generateTestSuiteMakefrags
  generateDSEConstraints
  generateConfigString
  generateGraphML
  generateParameterDump
}
