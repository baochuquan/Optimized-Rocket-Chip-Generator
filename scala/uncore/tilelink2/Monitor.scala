// See LICENSE for license details.

package uncore.tilelink2

import Chisel._
import chisel3.internal.sourceinfo.{SourceInfo, SourceLine}
import diplomacy._

class TLMonitor(gen: () => TLBundleSnoop, edge: () => TLEdge, sourceInfo: SourceInfo) extends LazyModule
{
  def extra(implicit sourceInfo: SourceInfo) = {
    sourceInfo match {
      case SourceLine(filename, line, col) => s" (connected at $filename:$line:$col)"
      case _ => ""
    }
  }

  def legalizeFormatA(bundle: TLBundleA, edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    assert (TLMessages.isA(bundle.opcode), "'A' channel has invalid opcode" + extra)

    // Reuse these subexpressions to save some firrtl lines
    val source_ok = edge.client.contains(bundle.source)
    val is_aligned = edge.isHiAligned(bundle.addr_hi, bundle.size)
    val mask = edge.full_mask(bundle)

    when (bundle.opcode === TLMessages.Acquire) {
      assert (edge.manager.supportsAcquireSafe(edge.address(bundle), bundle.size), "'A' channel carries Acquire type unsupported by manager" + extra)
      assert (source_ok, "'A' channel Acquire carries invalid source ID" + extra)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'A' channel Acquire smaller than a beat" + extra)
      assert (is_aligned, "'A' channel Acquire address not aligned to size" + extra)
      assert (TLPermissions.isGrow(bundle.param), "'A' channel Acquire carries invalid grow param" + extra)
      assert (~bundle.mask === UInt(0), "'A' channel Acquire contains invalid mask" + extra)
    }

    when (bundle.opcode === TLMessages.Get) {
      assert (edge.manager.supportsGetSafe(edge.address(bundle), bundle.size), "'A' channel carries Get type unsupported by manager" + extra)
      assert (source_ok, "'A' channel Get carries invalid source ID" + extra)
      assert (is_aligned, "'A' channel Get address not aligned to size" + extra)
      assert (bundle.param === UInt(0), "'A' channel Get carries invalid param" + extra)
      assert (bundle.mask === mask, "'A' channel Get contains invalid mask" + extra)
    }

    when (bundle.opcode === TLMessages.PutFullData) {
      assert (edge.manager.supportsPutFullSafe(edge.address(bundle), bundle.size), "'A' channel carries PutFull type unsupported by manager" + extra)
      assert (source_ok, "'A' channel PutFull carries invalid source ID" + extra)
      assert (is_aligned, "'A' channel PutFull address not aligned to size" + extra)
      assert (bundle.param === UInt(0), "'A' channel PutFull carries invalid param" + extra)
      assert (bundle.mask === mask, "'A' channel PutFull contains invalid mask" + extra)
    }

    when (bundle.opcode === TLMessages.PutPartialData) {
      assert (edge.manager.supportsPutPartialSafe(edge.address(bundle), bundle.size), "'A' channel carries PutPartial type unsupported by manager" + extra)
      assert (source_ok, "'A' channel PutPartial carries invalid source ID" + extra)
      assert (is_aligned, "'A' channel PutPartial address not aligned to size" + extra)
      assert (bundle.param === UInt(0), "'A' channel PutPartial carries invalid param" + extra)
      assert ((bundle.mask & ~mask) === UInt(0), "'A' channel PutPartial contains invalid mask" + extra)
    }

    when (bundle.opcode === TLMessages.ArithmeticData) {
      assert (edge.manager.supportsArithmeticSafe(edge.address(bundle), bundle.size), "'A' channel carries Arithmetic type unsupported by manager" + extra)
      assert (source_ok, "'A' channel Arithmetic carries invalid source ID" + extra)
      assert (is_aligned, "'A' channel Arithmetic address not aligned to size" + extra)
      assert (TLAtomics.isArithmetic(bundle.param), "'A' channel Arithmetic carries invalid opcode param" + extra)
      assert (bundle.mask === mask, "'A' channel Arithmetic contains invalid mask" + extra)
    }

    when (bundle.opcode === TLMessages.LogicalData) {
      assert (edge.manager.supportsLogicalSafe(edge.address(bundle), bundle.size), "'A' channel carries Logical type unsupported by manager" + extra)
      assert (source_ok, "'A' channel Logical carries invalid source ID" + extra)
      assert (is_aligned, "'A' channel Logical address not aligned to size" + extra)
      assert (TLAtomics.isLogical(bundle.param), "'A' channel Logical carries invalid opcode param" + extra)
      assert (bundle.mask === mask, "'A' channel Logical contains invalid mask" + extra)
    }

    when (bundle.opcode === TLMessages.Hint) {
      assert (edge.manager.supportsHintSafe(edge.address(bundle), bundle.size), "'A' channel carries Hint type unsupported by manager" + extra)
      assert (source_ok, "'A' channel Hint carries invalid source ID" + extra)
      assert (is_aligned, "'A' channel Hint address not aligned to size" + extra)
      assert (bundle.mask === mask, "'A' channel Hint contains invalid mask" + extra)
    }
  }

  def legalizeFormatB(bundle: TLBundleB, edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    assert (TLMessages.isB(bundle.opcode), "'B' channel has invalid opcode" + extra)

    // Reuse these subexpressions to save some firrtl lines
    val address_ok = edge.manager.containsSafe(edge.address(bundle))
    val is_aligned = edge.isHiAligned(bundle.addr_hi, bundle.size)
    val mask = edge.full_mask(bundle)

    when (bundle.opcode === TLMessages.Probe) {
      assert (edge.client.supportsProbe(bundle.source, bundle.size), "'B' channel carries Probe type unsupported by client" + extra)
      assert (address_ok, "'B' channel Probe carries unmanaged address" + extra)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'B' channel Probe smaller than a beat" + extra)
      assert (is_aligned, "'B' channel Probe address not aligned to size" + extra)
      assert (TLPermissions.isCap(bundle.param), "'B' channel Probe carries invalid cap param" + extra)
      assert (~bundle.mask === UInt(0).asUInt, "'B' channel Probe contains invalid mask" + extra)
    }

    when (bundle.opcode === TLMessages.Get) {
      assert (edge.client.supportsGet(bundle.source, bundle.size), "'B' channel carries Get type unsupported by client" + extra)
      assert (address_ok, "'B' channel Get carries unmanaged address" + extra)
      assert (is_aligned, "'B' channel Get address not aligned to size" + extra)
      assert (bundle.param === UInt(0), "'B' channel Get carries invalid param" + extra)
      assert (bundle.mask === mask, "'A' channel Get contains invalid mask" + extra)
    }

    when (bundle.opcode === TLMessages.PutFullData) {
      assert (edge.client.supportsPutFull(bundle.source, bundle.size), "'B' channel carries PutFull type unsupported by client" + extra)
      assert (address_ok, "'B' channel PutFull carries unmanaged address" + extra)
      assert (is_aligned, "'B' channel PutFull address not aligned to size" + extra)
      assert (bundle.param === UInt(0), "'B' channel PutFull carries invalid param" + extra)
      assert (bundle.mask === mask, "'B' channel PutFull contains invalid mask" + extra)
    }

    when (bundle.opcode === TLMessages.PutPartialData) {
      assert (edge.client.supportsPutPartial(bundle.source, bundle.size), "'B' channel carries PutPartial type unsupported by client" + extra)
      assert (address_ok, "'B' channel PutPartial carries unmanaged address" + extra)
      assert (is_aligned, "'B' channel PutPartial address not aligned to size" + extra)
      assert (bundle.param === UInt(0), "'B' channel PutPartial carries invalid param" + extra)
      assert ((bundle.mask & ~mask) === UInt(0), "'B' channel PutPartial contains invalid mask" + extra)
    }

    when (bundle.opcode === TLMessages.ArithmeticData) {
      assert (edge.client.supportsArithmetic(bundle.source, bundle.size), "'B' channel carries Arithmetic type unsupported by client" + extra)
      assert (address_ok, "'B' channel Arithmetic carries unmanaged address" + extra)
      assert (is_aligned, "'B' channel Arithmetic address not aligned to size" + extra)
      assert (TLAtomics.isArithmetic(bundle.param), "'B' channel Arithmetic carries invalid opcode param" + extra)
      assert (bundle.mask === mask, "'B' channel Arithmetic contains invalid mask" + extra)
    }

    when (bundle.opcode === TLMessages.LogicalData) {
      assert (edge.client.supportsLogical(bundle.source, bundle.size), "'B' channel carries Logical type unsupported by client" + extra)
      assert (address_ok, "'B' channel Logical carries unmanaged address" + extra)
      assert (is_aligned, "'B' channel Logical address not aligned to size" + extra)
      assert (TLAtomics.isLogical(bundle.param), "'B' channel Logical carries invalid opcode param" + extra)
      assert (bundle.mask === mask, "'B' channel Logical contains invalid mask" + extra)
    }

    when (bundle.opcode === TLMessages.Hint) {
      assert (edge.client.supportsHint(bundle.source, bundle.size), "'B' channel carries Hint type unsupported by client" + extra)
      assert (address_ok, "'B' channel Hint carries unmanaged address" + extra)
      assert (is_aligned, "'B' channel Hint address not aligned to size" + extra)
      assert (bundle.mask === mask, "'B' channel Hint contains invalid mask" + extra)
    }
  }

  def legalizeFormatC(bundle: TLBundleC, edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    assert (TLMessages.isC(bundle.opcode), "'C' channel has invalid opcode" + extra)

    val source_ok = edge.client.contains(bundle.source)
    val is_aligned = edge.isHiAligned(bundle.addr_hi, bundle.size) && edge.isLoAligned(bundle.addr_lo, bundle.size)
    val address_ok = edge.manager.containsSafe(edge.address(bundle))

    when (bundle.opcode === TLMessages.ProbeAck) {
      assert (address_ok, "'C' channel ProbeAck carries unmanaged address" + extra)
      assert (source_ok, "'C' channel ProbeAck carries invalid source ID" + extra)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'C' channel ProbeAck smaller than a beat" + extra)
      assert (is_aligned, "'C' channel ProbeAck address not aligned to size" + extra)
      assert (TLPermissions.isReport(bundle.param), "'C' channel ProbeAck carries invalid report param" + extra)
      assert (!bundle.error, "'C' channel Probe carries an error" + extra)
    }

    when (bundle.opcode === TLMessages.ProbeAckData) {
      assert (address_ok, "'C' channel ProbeAckData carries unmanaged address" + extra)
      assert (source_ok, "'C' channel ProbeAckData carries invalid source ID" + extra)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'C' channel ProbeAckData smaller than a beat" + extra)
      assert (is_aligned, "'C' channel ProbeAckData address not aligned to size" + extra)
      assert (TLPermissions.isReport(bundle.param), "'C' channel ProbeAckData carries invalid report param" + extra)
      assert (!bundle.error, "'C' channel ProbeData carries an error" + extra)
    }

    when (bundle.opcode === TLMessages.Release) {
      assert (edge.manager.supportsAcquireSafe(edge.address(bundle), bundle.size), "'C' channel carries Release type unsupported by manager" + extra)
      assert (source_ok, "'C' channel Release carries invalid source ID" + extra)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'C' channel Release smaller than a beat" + extra)
      assert (is_aligned, "'C' channel Release address not aligned to size" + extra)
      assert (TLPermissions.isShrink(bundle.param), "'C' channel Release carries invalid shrink param" + extra)
      assert (!bundle.error, "'C' channel Release carries an error" + extra)
    }

    when (bundle.opcode === TLMessages.ReleaseData) {
      assert (edge.manager.supportsAcquireSafe(edge.address(bundle), bundle.size), "'C' channel carries ReleaseData type unsupported by manager" + extra)
      assert (source_ok, "'C' channel ReleaseData carries invalid source ID" + extra)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'C' channel ReleaseData smaller than a beat" + extra)
      assert (is_aligned, "'C' channel ReleaseData address not aligned to size" + extra)
      assert (TLPermissions.isShrink(bundle.param), "'C' channel ReleaseData carries invalid shrink param" + extra)
      assert (!bundle.error, "'C' channel ReleaseData carries an error" + extra)
    }

    when (bundle.opcode === TLMessages.AccessAck) {
      assert (address_ok, "'C' channel AccessAck carries unmanaged address" + extra)
      assert (source_ok, "'C' channel AccessAck carries invalid source ID" + extra)
      assert (is_aligned, "'C' channel AccessAck address not aligned to size" + extra)
      assert (bundle.param === UInt(0), "'C' channel AccessAck carries invalid param" + extra)
    }

    when (bundle.opcode === TLMessages.AccessAckData) {
      assert (address_ok, "'C' channel AccessAckData carries unmanaged address" + extra)
      assert (source_ok, "'C' channel AccessAckData carries invalid source ID" + extra)
      assert (is_aligned, "'C' channel AccessAckData address not aligned to size" + extra)
      assert (bundle.param === UInt(0), "'C' channel AccessAckData carries invalid param" + extra)
    }

    when (bundle.opcode === TLMessages.HintAck) {
      assert (address_ok, "'C' channel HintAck carries unmanaged address" + extra)
      assert (source_ok, "'C' channel HintAck carries invalid source ID" + extra)
      assert (is_aligned, "'C' channel HintAck address not aligned to size" + extra)
      assert (bundle.param === UInt(0), "'C' channel HintAck carries invalid param" + extra)
      assert (!bundle.error, "'C' channel HintAck carries an error" + extra)
    }
  }

  def legalizeFormatD(bundle: TLBundleD, edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    assert (TLMessages.isD(bundle.opcode), "'D' channel has invalid opcode" + extra)

    val source_ok = edge.client.contains(bundle.source)
    val is_aligned = edge.isLoAligned(bundle.addr_lo, bundle.size)
    val sink_ok = edge.manager.containsById(bundle.sink)

    when (bundle.opcode === TLMessages.ReleaseAck) {
      assert (source_ok, "'D' channel ReleaseAck carries invalid source ID" + extra)
      assert (is_aligned, "'D' channel ReleaseAck address not aligned to size" + extra)
      assert (sink_ok, "'D' channel ReleaseAck carries invalid sink ID" + extra)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'D' channel ReleaseAck smaller than a beat" + extra)
      assert (bundle.param === UInt(0), "'D' channel ReleaseeAck carries invalid param" + extra)
      assert (!bundle.error, "'D' channel ReleaseAck carries an error" + extra)
    }

    when (bundle.opcode === TLMessages.Grant) {
      assert (source_ok, "'D' channel Grant carries invalid source ID" + extra)
      assert (is_aligned, "'D' channel Grant address not aligned to size" + extra)
      assert (sink_ok, "'D' channel Grant carries invalid sink ID" + extra)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'D' channel Grant smaller than a beat" + extra)
      assert (TLPermissions.isCap(bundle.param), "'D' channel Grant carries invalid cap param" + extra)
    }

    when (bundle.opcode === TLMessages.GrantData) {
      assert (source_ok, "'D' channel GrantData carries invalid source ID" + extra)
      assert (is_aligned, "'D' channel GrantData address not aligned to size" + extra)
      assert (sink_ok, "'D' channel GrantData carries invalid sink ID" + extra)
      assert (bundle.size >= UInt(log2Ceil(edge.manager.beatBytes)), "'D' channel GrantData smaller than a beat" + extra)
      assert (TLPermissions.isCap(bundle.param), "'D' channel GrantData carries invalid cap param" + extra)
    }

    when (bundle.opcode === TLMessages.AccessAck) {
      assert (source_ok, "'D' channel AccessAck carries invalid source ID" + extra)
      assert (is_aligned, "'D' channel AccessAck address not aligned to size" + extra)
      assert (sink_ok, "'D' channel AccessAck carries invalid sink ID" + extra)
      // size is ignored
      assert (bundle.param === UInt(0), "'D' channel AccessAck carries invalid param" + extra)
    }

    when (bundle.opcode === TLMessages.AccessAckData) {
      assert (source_ok, "'D' channel AccessAckData carries invalid source ID" + extra)
      assert (is_aligned, "'D' channel AccessAckData address not aligned to size" + extra)
      assert (sink_ok, "'D' channel AccessAckData carries invalid sink ID" + extra)
      // size is ignored
      assert (bundle.param === UInt(0), "'D' channel AccessAckData carries invalid param" + extra)
    }

    when (bundle.opcode === TLMessages.HintAck) {
      assert (source_ok, "'D' channel HintAck carries invalid source ID" + extra)
      assert (is_aligned, "'D' channel HintAck address not aligned to size" + extra)
      assert (sink_ok, "'D' channel HintAck carries invalid sink ID" + extra)
      // size is ignored
      assert (bundle.param === UInt(0), "'D' channel HintAck carries invalid param" + extra)
      assert (!bundle.error, "'D' channel HintAck carries an error" + extra)
    }
  }

  def legalizeFormatE(bundle: TLBundleE, edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    assert (edge.manager.containsById(bundle.sink), "'E' channels carries invalid sink ID" + extra)
  }

  def legalizeFormat(bundle: TLBundleSnoop, edge: TLEdge)(implicit sourceInfo: SourceInfo) = {
    when (bundle.a.valid) { legalizeFormatA(bundle.a.bits, edge) }
    when (bundle.b.valid) { legalizeFormatB(bundle.b.bits, edge) }
    when (bundle.c.valid) { legalizeFormatC(bundle.c.bits, edge) }
    when (bundle.d.valid) { legalizeFormatD(bundle.d.bits, edge) }
    when (bundle.e.valid) { legalizeFormatE(bundle.e.bits, edge) }
  }

  def legalizeMultibeatA(a: IrrevocableSnoop[TLBundleA], edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    val counter = RegInit(UInt(0, width = log2Up(edge.maxTransfer)))
    val opcode  = Reg(UInt())
    val param   = Reg(UInt())
    val size    = Reg(UInt())
    val source  = Reg(UInt())
    val addr_hi = Reg(UInt())
    when (a.valid && counter =/= UInt(0)) {
      assert (a.bits.opcode === opcode, "'A' channel opcode changed within multibeat operation" + extra)
      assert (a.bits.param  === param,  "'A' channel param changed within multibeat operation" + extra)
      assert (a.bits.size   === size,   "'A' channel size changed within multibeat operation" + extra)
      assert (a.bits.source === source, "'A' channel source changed within multibeat operation" + extra)
      assert (a.bits.addr_hi=== addr_hi,"'A' channel addr_hi changed with multibeat operation" + extra)
    }
    when (a.fire()) {
      counter := counter - UInt(1)
      when (counter === UInt(0)) {
        counter := edge.numBeats(a.bits) - UInt(1)
        opcode  := a.bits.opcode
        param   := a.bits.param
        size    := a.bits.size
        source  := a.bits.source
        addr_hi := a.bits.addr_hi
      }
    }
  }

  def legalizeMultibeatB(b: IrrevocableSnoop[TLBundleB], edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    val counter = RegInit(UInt(0, width = log2Up(edge.maxTransfer)))
    val opcode  = Reg(UInt())
    val param   = Reg(UInt())
    val size    = Reg(UInt())
    val source  = Reg(UInt())
    val addr_hi = Reg(UInt())
    when (b.valid && counter =/= UInt(0)) {
      assert (b.bits.opcode === opcode, "'B' channel opcode changed within multibeat operation" + extra)
      assert (b.bits.param  === param,  "'B' channel param changed within multibeat operation" + extra)
      assert (b.bits.size   === size,   "'B' channel size changed within multibeat operation" + extra)
      assert (b.bits.source === source, "'B' channel source changed within multibeat operation" + extra)
      assert (b.bits.addr_hi=== addr_hi,"'B' channel addr_hi changed with multibeat operation" + extra)
    }
    when (b.fire()) {
      counter := counter - UInt(1)
      when (counter === UInt(0)) {
        counter := edge.numBeats(b.bits) - UInt(1)
        opcode  := b.bits.opcode
        param   := b.bits.param
        size    := b.bits.size
        source  := b.bits.source
        addr_hi := b.bits.addr_hi
      }
    }
  }

  def legalizeMultibeatC(c: IrrevocableSnoop[TLBundleC], edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    val counter = RegInit(UInt(0, width = log2Up(edge.maxTransfer)))
    val opcode  = Reg(UInt())
    val param   = Reg(UInt())
    val size    = Reg(UInt())
    val source  = Reg(UInt())
    val addr_hi = Reg(UInt())
    val addr_lo = Reg(UInt())
    when (c.valid && counter =/= UInt(0)) {
      assert (c.bits.opcode === opcode, "'C' channel opcode changed within multibeat operation" + extra)
      assert (c.bits.param  === param,  "'C' channel param changed within multibeat operation" + extra)
      assert (c.bits.size   === size,   "'C' channel size changed within multibeat operation" + extra)
      assert (c.bits.source === source, "'C' channel source changed within multibeat operation" + extra)
      assert (c.bits.addr_hi=== addr_hi,"'C' channel addr_hi changed with multibeat operation" + extra)
      assert (c.bits.addr_lo=== addr_lo,"'C' channel addr_lo changed with multibeat operation" + extra)
    }
    when (c.fire()) {
      counter := counter - UInt(1)
      when (counter === UInt(0)) {
        counter := edge.numBeats(c.bits) - UInt(1)
        opcode  := c.bits.opcode
        param   := c.bits.param
        size    := c.bits.size
        source  := c.bits.source
        addr_hi := c.bits.addr_hi
        addr_lo := c.bits.addr_lo
      }
    }
  }

  def legalizeMultibeatD(d: IrrevocableSnoop[TLBundleD], edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    val counter = RegInit(UInt(0, width = log2Up(edge.maxTransfer)))
    val opcode  = Reg(UInt())
    val param   = Reg(UInt())
    val size    = Reg(UInt())
    val source  = Reg(UInt())
    val sink    = Reg(UInt())
    val addr_lo = Reg(UInt())
    when (d.valid && counter =/= UInt(0)) {
      assert (d.bits.opcode === opcode, "'D' channel opcode changed within multibeat operation" + extra)
      assert (d.bits.param  === param,  "'D' channel param changed within multibeat operation" + extra)
      assert (d.bits.size   === size,   "'D' channel size changed within multibeat operation" + extra)
      assert (d.bits.source === source, "'D' channel source changed within multibeat operation" + extra)
      assert (d.bits.sink   === sink,   "'D' channel sink changed with multibeat operation" + extra)
      assert (d.bits.addr_lo=== addr_lo,"'D' channel addr_lo changed with multibeat operation" + extra)
    }
    when (d.fire()) {
      counter := counter - UInt(1)
      when (counter === UInt(0)) {
        counter := edge.numBeats(d.bits) - UInt(1)
        opcode  := d.bits.opcode
        param   := d.bits.param
        size    := d.bits.size
        source  := d.bits.source
        sink    := d.bits.sink
        addr_lo := d.bits.addr_lo
      }
    }
  }

  def legalizeMultibeat(bundle: TLBundleSnoop, edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    legalizeMultibeatA(bundle.a, edge)
    legalizeMultibeatB(bundle.b, edge)
    legalizeMultibeatC(bundle.c, edge)
    legalizeMultibeatD(bundle.d, edge)
  }

  def legalizeIrrevocable(irr: IrrevocableSnoop[TLChannel], edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    val last_v = RegNext(irr.valid, Bool(false))
    val last_r = RegNext(irr.ready, Bool(false))
    val last_b = RegNext(irr.bits)
    val bits_changed = irr.bits.asUInt === last_b.asUInt

    when (last_v && !last_r) {
      assert(irr.valid,    s"${irr.bits.channelName} had contents that were revoked by the supplier (valid lowered)" + extra)
      assert(bits_changed, s"${irr.bits.channelName} had contents that were revoked by the supplier (contents changed)" + extra)
    }
  }

  def legalizeIrrevocable(bundle: TLBundleSnoop, edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    legalizeIrrevocable(bundle.a, edge)
    legalizeIrrevocable(bundle.b, edge)
    legalizeIrrevocable(bundle.c, edge)
    legalizeIrrevocable(bundle.d, edge)
    legalizeIrrevocable(bundle.e, edge)
  }

  def legalizeSourceUnique(bundle: TLBundleSnoop, edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    val inflight = RegInit(UInt(0, width = edge.client.endSourceId))

    val a_counter = RegInit(UInt(0, width = log2Up(edge.maxTransfer)))
    val a_beats1 = edge.numBeats1(bundle.a.bits)
    val a_first = a_counter === UInt(0)
    val a_last = a_counter === UInt(1) || a_beats1 === UInt(0)

    val d_counter = RegInit(UInt(0, width = log2Up(edge.maxTransfer)))
    val d_beats1 = edge.numBeats1(bundle.d.bits)
    val d_first = d_counter === UInt(0)
    val d_last = d_counter === UInt(1) || d_beats1 === UInt(0)

    val bypass = bundle.a.bits.source === bundle.d.bits.source
    val a_bypass = bypass && bundle.d.valid && d_last
    val d_bypass = bypass && bundle.a.valid && a_last

    if (edge.manager.minLatency > 0) {
      assert(!bypass || !bundle.a.valid || !bundle.d.valid, s"'A' and 'D' concurrent, despite minlatency ${edge.manager.minLatency}" + extra)
    }

    val a_set = Wire(init = UInt(0, width = edge.client.endSourceId))
    when (bundle.a.fire()) {
      a_counter := Mux(a_first, a_beats1, a_counter - UInt(1))
      when (a_last) { a_set := UIntToOH(bundle.a.bits.source) }
      assert(a_bypass || !inflight(bundle.a.bits.source), "'A' channel re-used a source ID" + extra)
    }

    val d_clr = Wire(init = UInt(0, width = edge.client.endSourceId))
    when (bundle.d.fire() && bundle.d.bits.opcode =/= TLMessages.ReleaseAck) {
      d_counter := Mux(d_first, d_beats1, d_counter - UInt(1))
      when (d_last) { d_clr := UIntToOH(bundle.d.bits.source) }
      assert(d_bypass || inflight(bundle.d.bits.source), "'D' channel acknowledged for nothing inflight" + extra)
    }

    inflight := (inflight | a_set) & ~d_clr
  }

  def legalize(bundle: TLBundleSnoop, edge: TLEdge)(implicit sourceInfo: SourceInfo) {
    legalizeFormat     (bundle, edge)
    legalizeMultibeat  (bundle, edge)
    legalizeIrrevocable(bundle, edge)
    legalizeSourceUnique(bundle, edge)
  }

  lazy val module = new LazyModuleImp(this) {
    val io = new Bundle {
      val in = gen().asInput
    }

    legalize(io.in, edge())(sourceInfo)
  }
}
