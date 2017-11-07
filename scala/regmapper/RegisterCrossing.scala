// See LICENSE for license details.

package regmapper

import Chisel._
import chisel3.util.{Irrevocable}
import util.{AsyncQueue,AsyncScope,AsyncResetRegVec}

// A very simple flow control state machine, run in the specified clock domain
class BusyRegisterCrossing(clock: Clock, reset: Bool)
    extends Module(_clock = clock, _reset = reset) {
  val io = new Bundle {
    val progress       = Bool(INPUT)
    val request_valid  = Bool(INPUT)
    val response_ready = Bool(INPUT)
    val busy           = Bool(OUTPUT)
  }

  val busy = RegInit(Bool(false))
  when (io.progress) {
    busy := Mux(busy, !io.response_ready, io.request_valid)
  }
  io.busy := busy
}

// RegField should support connecting to one of these
class RegisterWriteIO[T <: Data](gen: T) extends Bundle {
  val request  = Decoupled(gen).flip()
  val response = Irrevocable(Bool()) // ignore .bits
}

// To turn on/off a domain:
//  1. lower allow on the other side
//  2. wait for inflight traffic to resolve
//  3. assert reset in the domain
//  4. turn off the domain
//  5. turn on the domain
//  6. deassert reset in the domain
//  7. raise allow on the other side

class RegisterWriteCrossingIO[T <: Data](gen: T) extends Bundle {
  // Master clock domain
  val master_clock   = Clock(INPUT)
  val master_reset   = Bool(INPUT)
  val master_allow   = Bool(INPUT) // actually wait for the slave
  val master_port    = new RegisterWriteIO(gen)
  // Slave clock domain
  val slave_clock    = Clock(INPUT)
  val slave_reset    = Bool(INPUT)
  val slave_allow    = Bool(INPUT) // honour requests from the master
  val slave_register = gen.asOutput
  val slave_valid    = Bool(OUTPUT) // is high on 1st cycle slave_register has a new value
}

class RegisterWriteCrossing[T <: Data](gen: T, sync: Int = 3) extends Module {
  val io = new RegisterWriteCrossingIO(gen)
  // The crossing must only allow one item inflight at a time
  val crossing = Module(new AsyncQueue(gen, 1, sync))

  // We can just randomly reset one-side of a single entry AsyncQueue.
  // If the enq side is reset, at worst deq.bits is reassigned from mem(0), which stays fixed.
  // If the deq side is reset, at worst the master rewrites mem(0) once, deq.bits stays fixed.
  crossing.io.enq_clock := io.master_clock
  crossing.io.enq_reset := io.master_reset
  crossing.io.deq_clock := io.slave_clock
  crossing.io.deq_reset := io.slave_reset

  crossing.io.enq.bits := io.master_port.request.bits
  io.slave_register := crossing.io.deq.bits
  io.slave_valid    := crossing.io.deq.valid

  // If the slave is not operational, just drop the write.
  val progress = crossing.io.enq.ready || !io.master_allow

  val reg = Module(new BusyRegisterCrossing(io.master_clock, io.master_reset))
  reg.io.progress := progress
  reg.io.request_valid  := io.master_port.request.valid
  reg.io.response_ready := io.master_port.response.ready

  crossing.io.deq.ready := Bool(true)
  crossing.io.enq.valid := io.master_port.request.valid && !reg.io.busy
  io.master_port.request.ready  := progress && !reg.io.busy
  io.master_port.response.valid := progress && reg.io.busy
}

// RegField should support connecting to one of these
class RegisterReadIO[T <: Data](gen: T) extends Bundle {
  val request  = Decoupled(Bool()).flip() // ignore .bits
  val response = Irrevocable(gen)
}

class RegisterReadCrossingIO[T <: Data](gen: T) extends Bundle {
  // Master clock domain
  val master_clock   = Clock(INPUT)
  val master_reset   = Bool(INPUT)
  val master_allow   = Bool(INPUT) // actually wait for the slave
  val master_port    = new RegisterReadIO(gen)
  // Slave clock domain
  val slave_clock    = Clock(INPUT)
  val slave_reset    = Bool(INPUT)
  val slave_allow    = Bool(INPUT) // honour requests from the master
  val slave_register = gen.asInput
}

class RegisterReadCrossing[T <: Data](gen: T, sync: Int = 3) extends Module {
  val io = new RegisterReadCrossingIO(gen)
  // The crossing must only allow one item inflight at a time
  val crossing = Module(new AsyncQueue(gen, 1, sync))

  // We can just randomly reset one-side of a single entry AsyncQueue.
  // If the enq side is reset, at worst deq.bits is reassigned from mem(0), which stays fixed.
  // If the deq side is reset, at worst the slave rewrites mem(0) once, deq.bits stays fixed.
  crossing.io.enq_clock := io.slave_clock
  crossing.io.enq_reset := io.slave_reset
  crossing.io.deq_clock := io.master_clock
  crossing.io.deq_reset := io.master_reset

  crossing.io.enq.bits := io.slave_register
  io.master_port.response.bits := crossing.io.deq.bits

  // If the slave is not operational, just repeat the last value we saw.
  val progress = crossing.io.deq.valid || !io.master_allow

  val reg = Module(new BusyRegisterCrossing(io.master_clock, io.master_reset))
  reg.io.progress := progress
  reg.io.request_valid  := io.master_port.request.valid
  reg.io.response_ready := io.master_port.response.ready

  io.master_port.response.valid := progress && reg.io.busy
  io.master_port.request.ready  := progress && !reg.io.busy
  crossing.io.deq.ready := io.master_port.request.valid && !reg.io.busy
  crossing.io.enq.valid := Bool(true)
}

/** Wrapper to create an
  *  asynchronously reset slave register which can be 
  *  both read and written 
  *  using crossing FIFOs.
  *  The reset and allow assertion & de-assertion 
  *  should be synchronous to their respective 
  *  domains. 
  */

object AsyncRWSlaveRegField {

  def apply(slave_clock: Clock,
    slave_reset: Bool,
    width:  Int,
    init: Int,
    name: Option[String] = None,
    master_allow: Bool = Bool(true),
    slave_allow: Bool = Bool(true)
  ): (UInt, RegField) = {

    val async_slave_reg = Module(new AsyncResetRegVec(width, init))
    name.foreach(async_slave_reg.suggestName(_))
    async_slave_reg.reset := slave_reset
    async_slave_reg.clock := slave_clock

    val wr_crossing = Module (new RegisterWriteCrossing(UInt(width = width)))
    name.foreach(n => wr_crossing.suggestName(s"${n}_wcrossing"))

    val scope = Module (new AsyncScope())

    wr_crossing.io.master_clock := scope.clock
    wr_crossing.io.master_reset := scope.reset
    wr_crossing.io.master_allow := master_allow
    wr_crossing.io.slave_clock  := slave_clock
    wr_crossing.io.slave_reset  := slave_reset
    wr_crossing.io.slave_allow  := slave_allow

    async_slave_reg.io.en := wr_crossing.io.slave_valid
    async_slave_reg.io.d  := wr_crossing.io.slave_register

    val rd_crossing = Module (new RegisterReadCrossing(UInt(width = width )))
    name.foreach(n => rd_crossing.suggestName(s"${n}_rcrossing"))

    rd_crossing.io.master_clock := scope.clock
    rd_crossing.io.master_reset := scope.reset
    rd_crossing.io.master_allow := master_allow
    rd_crossing.io.slave_clock  := slave_clock
    rd_crossing.io.slave_reset  := slave_reset
    rd_crossing.io.slave_allow  := slave_allow

    rd_crossing.io.slave_register := async_slave_reg.io.q

    (async_slave_reg.io.q, RegField(width, rd_crossing.io.master_port, wr_crossing.io.master_port))
  }
}
