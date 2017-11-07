// See LICENSE for license details.
package uncore.tilelink2

import Chisel._
import diplomacy._

class IDMapGenerator(numIds: Int) extends Module {
  val w = log2Up(numIds)
  val io = new Bundle {
    val free = Decoupled(UInt(width = w)).flip
    val alloc = Decoupled(UInt(width = w))
  }

  // True indicates that the id is available
  val bitmap = RegInit(UInt((BigInt(1) << numIds) -  1, width = numIds))

  io.free.ready := Bool(true)
  assert (!io.free.valid || !bitmap(io.free.bits)) // No double freeing

  val select = ~(highOR(bitmap) << 1) & bitmap
  io.alloc.bits := OHToUInt(select)
  io.alloc.valid := bitmap.orR()

  val clr = Wire(init = UInt(0, width = numIds))
  when (io.alloc.fire()) { clr := UIntToOH(io.alloc.bits) }

  val set = Wire(init = UInt(0, width = numIds))
  when (io.free.fire()) { set := UIntToOH(io.free.bits) }

  bitmap := (bitmap & ~clr) | set
}

object LFSR64
{ 
  private var counter = 0 
  private def next: Int = {
    counter += 1
    counter
  }
  
  def apply(increment: Bool = Bool(true), seed: Int = next): UInt =
  { 
    val wide = 64
    val lfsr = RegInit(UInt((seed * 0xDEADBEEFCAFEBAB1L) >>> 1, width = wide))
    val xor = lfsr(0) ^ lfsr(1) ^ lfsr(3) ^ lfsr(4)
    when (increment) { lfsr := Cat(xor, lfsr(wide-1,1)) }
    lfsr
  }
}

trait HasNoiseMakerIO
{
  val io = new Bundle {
    val inc = Bool(INPUT)
    val random = UInt(OUTPUT)
  }
}

class LFSRNoiseMaker(wide: Int) extends Module with HasNoiseMakerIO
{
  val lfsrs = Seq.fill((wide+63)/64) { LFSR64(io.inc) }
  io.random := Cat(lfsrs)(wide-1,0)
}

object LFSRNoiseMaker {
  def apply(wide: Int, increment: Bool = Bool(true)): UInt = {
    val nm = Module(new LFSRNoiseMaker(wide))
    nm.io.inc := increment
    nm.io.random
  }
}

/** TLFuzzer drives test traffic over TL2 links. It generates a sequence of randomized
  * requests, and issues legal ones into the DUT. TODO: Currently the fuzzer only generates
  * memory operations, not permissions transfers.
  * @param nOperations is the total number of operations that the fuzzer must complete for the test to pass
  * @param inFlight is the number of operations that can be in-flight to the DUT concurrently
  * @param noiseMaker is a function that supplies a random UInt of a given width every time inc is true
  */
class TLFuzzer(
    nOperations: Int,
    inFlight: Int = 32,
    noiseMaker: (Int, Bool, Int) => UInt = {
                  (wide: Int, increment: Bool, abs_values: Int) =>
                   LFSRNoiseMaker(wide=wide, increment=increment)
                  }
              ) extends LazyModule
{
  val node = TLClientNode(TLClientParameters(sourceId = IdRange(0,inFlight)))

  lazy val module = new LazyModuleImp(this) {
    val io = new Bundle {
      val out = node.bundleOut
      val finished = Bool()
    }

    val out = io.out(0)
    val edge = node.edgesOut(0)

    // Extract useful parameters from the TL edge
    val endAddress   = edge.manager.maxAddress + 1
    val maxTransfer  = edge.manager.maxTransfer
    val beatBytes    = edge.manager.beatBytes
    val maxLgBeats   = log2Up(maxTransfer/beatBytes)
    val addressBits  = log2Up(endAddress)
    val sizeBits     = edge.bundle.sizeBits
    val dataBits     = edge.bundle.dataBits

    // Progress through operations
    val num_reqs = Reg(init = UInt(nOperations-1, log2Up(nOperations)))
    val num_resps = Reg(init = UInt(nOperations-1, log2Up(nOperations)))
    io.finished  := num_resps === UInt(0)

    // Progress within each operation
    val a = out.a.bits
    val a_beats1 = edge.numBeats1(a)
    val a_counter = RegInit(UInt(0, width = maxLgBeats))
    val a_counter1 = a_counter - UInt(1)
    val a_first = a_counter === UInt(0)
    val a_last  = a_counter === UInt(1) || a_beats1 === UInt(0)
    val req_done = out.a.fire() && a_last

    val d = out.d.bits
    val d_beats1 = edge.numBeats1(d)
    val d_counter = RegInit(UInt(0, width = maxLgBeats))
    val d_counter1 = d_counter - UInt(1)
    val d_first = d_counter === UInt(0)
    val d_last  = d_counter === UInt(1) || d_beats1 === UInt(0)
    val resp_done = out.d.fire() && d_last

    // Source ID generation
    val idMap = Module(new IDMapGenerator(inFlight))
    val alloc = Queue.irrevocable(idMap.io.alloc, 1, pipe = true)
    val src = alloc.bits
    alloc.ready := req_done
    idMap.io.free.valid := resp_done
    idMap.io.free.bits := out.d.bits.source

    // Increment random number generation for the following subfields
    val inc = Wire(Bool())
    val inc_beat = Wire(Bool())
    val arth_op_3 = noiseMaker(3, inc, 0)
    val arth_op   = Mux(arth_op_3 > UInt(4), UInt(4), arth_op_3)
    val log_op    = noiseMaker(2, inc, 0)
    val amo_size  = UInt(2) + noiseMaker(1, inc, 0) // word or dword
    val size      = noiseMaker(sizeBits, inc, 0)
    val addr      = noiseMaker(addressBits, inc, 2) & ~UIntToOH1(size, addressBits)
    val mask      = noiseMaker(beatBytes, inc_beat, 2) & edge.mask(addr, size)
    val data      = noiseMaker(dataBits, inc_beat, 2)

    // Actually generate specific TL messages when it is legal to do so
    val (glegal,  gbits)  = edge.Get(src, addr, size)
    val (pflegal, pfbits) = if(edge.manager.anySupportPutFull) { 
                              edge.Put(src, addr, size, data)
                            } else { (glegal, gbits) }
    val (pplegal, ppbits) = if(edge.manager.anySupportPutPartial) {
                              edge.Put(src, addr, size, data, mask)
                            } else { (glegal, gbits) }
    val (alegal,  abits)  = if(edge.manager.anySupportArithmetic) {
                              edge.Arithmetic(src, addr, size, data, arth_op)
                            } else { (glegal, gbits) }
    val (llegal,  lbits)  = if(edge.manager.anySupportLogical) {
                             edge.Logical(src, addr, size, data, log_op)
                            } else { (glegal, gbits) }
    val (hlegal,  hbits)  = if(edge.manager.anySupportHint) {
                              edge.Hint(src, addr, size, UInt(0))
                            } else { (glegal, gbits) }

    val legal_dest = edge.manager.containsSafe(addr)

    // Pick a specific message to try to send
    val a_type_sel  = noiseMaker(3, inc, 0)

    val legal = legal_dest && MuxLookup(a_type_sel, glegal, Seq(
      UInt("b000") -> glegal,
      UInt("b001") -> pflegal,
      UInt("b010") -> pplegal,
      UInt("b011") -> alegal,
      UInt("b100") -> llegal,
      UInt("b101") -> hlegal))

    val bits = MuxLookup(a_type_sel, gbits, Seq(
      UInt("b000") -> gbits,
      UInt("b001") -> pfbits,
      UInt("b010") -> ppbits,
      UInt("b011") -> abits,
      UInt("b100") -> lbits,
      UInt("b101") -> hbits))

    // Wire both the used and un-used channel signals
    out.a.valid := legal && alloc.valid && num_reqs =/= UInt(0)
    out.a.bits  := bits
    out.b.ready := Bool(true)
    out.c.valid := Bool(false)
    out.d.ready := Bool(true)
    out.e.valid := Bool(false)

    // Increment the various progress-tracking states
    inc := !legal || req_done
    inc_beat := !legal || out.a.fire()

    when (out.a.fire()) {
      a_counter := Mux(a_first, a_beats1, a_counter1)
      when(a_last) { num_reqs := num_reqs - UInt(1) }
    }

    when (out.d.fire()) {
      d_counter := Mux(d_first, d_beats1, d_counter1)
      when(d_last) { num_resps := num_resps - UInt(1) }
    }
  }
}

/** Synthesizeable integration test */
import unittest._

class TLFuzzRAM extends LazyModule
{
  val model = LazyModule(new TLRAMModel)
  val ram  = LazyModule(new TLRAM(AddressSet(0x800, 0x7ff)))
  val ram2 = LazyModule(new TLRAM(AddressSet(0, 0x3ff), beatBytes = 16))
  val gpio = LazyModule(new RRTest1(0x400))
  val xbar = LazyModule(new TLXbar)
  val xbar2= LazyModule(new TLXbar)
  val fuzz = LazyModule(new TLFuzzer(5000))
  val cross = LazyModule(new TLAsyncCrossing)

  model.node := fuzz.node
  xbar2.node := TLAtomicAutomata()(model.node)
  ram2.node := TLFragmenter(16, 256)(xbar2.node)
  xbar.node := TLWidthWidget(16)(TLHintHandler()(xbar2.node))
  cross.nodeIn := TLFragmenter(4, 256)(TLBuffer()(xbar.node))
  val monitor = (ram.node := cross.nodeOut)
  gpio.node := TLFragmenter(4, 32)(TLBuffer()(xbar.node))

  lazy val module = new LazyModuleImp(this) with HasUnitTestIO {
    io.finished := fuzz.module.io.finished

    // Shove the RAM into another clock domain
    val clocks = Module(new util.Pow2ClockDivider(2))
    ram.module.clock := clocks.io.clock_out

    // ... and safely cross TL2 into it
    cross.module.io.in_clock := clock
    cross.module.io.in_reset := reset
    cross.module.io.out_clock := clocks.io.clock_out
    cross.module.io.out_reset := reset

    // Push the Monitor into the right clock domain
    monitor.foreach { m =>
      m.module.clock := clocks.io.clock_out
      m.module.reset := reset
    }
  }
}

class TLFuzzRAMTest extends UnitTest(500000) {
  val dut = Module(LazyModule(new TLFuzzRAM).module)
  io.finished := dut.io.finished
}
