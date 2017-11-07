// See LICENSE for license details.

package rocket

import Chisel._
import util._
import Chisel.ImplicitConversions._
import junctions._
import scala.math._
import cde.{Parameters, Field}
import uncore.agents._
//import uncore.agents.PseudoLRU
import uncore.coherence._

import uncore.tilelink._
import uncore.util._
import uncore.constants._


case object PgLevels extends Field[Int]
case object ASIdBits extends Field[Int]

case object TLBLevel extends Field[Int]
case object L1TLBEntries extends Field[Int]
case object L1TLBReplaceAlgorithm extends Field[String]
case object L2TLBWays extends Field[Int]
case object L2TLBSets extends Field[Int]
case object L2TLBReplaceAlgorithm extends Field[String]

trait HasTLBParameters extends HasCoreParameters {
  val level = p(TLBLevel)
  val entries = p(L1TLBEntries)
  val replacealgorithm = p(L1TLBReplaceAlgorithm)
  val ways = p(L2TLBWays)
  val sets = p(L2TLBSets)
  val l2replacealgorithm = p(L2TLBReplaceAlgorithm)
  val camAddrBits = log2Ceil(entries)
  val camTagBits = asIdBits + vpnBits

}

class L2Req(implicit p: Parameters) extends CoreBundle()(p) {
  val prv = Bits(width = 2)
  val pum = Bool()
  val mxr = Bool()
  val addr = UInt(width = vpnBits)
  val store = Bool()
  val fetch = Bool()
}

class L2Resp(implicit p: Parameters) extends CoreBundle()(p) {
  val pte = new PTE
}

class L1L2TLBIO(implicit p: Parameters) extends CoreBundle()(p) {
  val req = Decoupled(new L2Req)  // req
  val resp = Valid(new L2Resp).flip
  val ptbr = new PTBR().asInput
  val invalidate = Bool(INPUT)
  val status = new MStatus().asInput
}

class TLBReq(implicit p: Parameters) extends CoreBundle()(p) {
  val vpn = UInt(width = vpnBitsExtended)
  val passthrough = Bool()
  val instruction = Bool()
  val store = Bool()
}

class TLBResp(implicit p: Parameters) extends CoreBundle()(p) {
  // lookup responses
  val miss = Bool(OUTPUT)
  val ppn = UInt(OUTPUT, ppnBits)
  val xcpt_ld = Bool(OUTPUT)
  val xcpt_st = Bool(OUTPUT)
  val xcpt_if = Bool(OUTPUT)
  val cacheable = Bool(OUTPUT)
}

class TLB(implicit val p: Parameters) extends Module with HasTLBParameters {
  val io = new Bundle {
    val req = Decoupled(new TLBReq).flip
    val resp = new TLBResp
    val ptw = new TLBPTWIO
  }
  if(level == 1){
    val tlb = Module(new OneLevelTLB())
    tlb.io.req <> io.req
    io.resp <> tlb.io.resp
    io.ptw <> tlb.io.ptw
  } else {
    val tlb = Module(new TwoLevelTLB())
    tlb.io.req <> io.req
    io.resp <> tlb.io.resp
    io.ptw <> tlb.io.ptw
  }
}

class OneLevelTLB(implicit val p: Parameters) extends Module with HasTLBParameters {
  val io = new Bundle {
    val req = Decoupled(new TLBReq).flip
    val resp = new TLBResp
    val ptw = new TLBPTWIO
  }

  val valid = Reg(init = UInt(0, entries))
  val ppns = Reg(Vec(entries, UInt(width = ppnBits)))
  val tags = Reg(Vec(entries, UInt(width = asIdBits + vpnBits)))

  val s_ready :: s_request :: s_wait :: s_wait_invalidate :: Nil = Enum(UInt(), 4)
  val state = Reg(init=s_ready)
  val r_refill_tag = Reg(UInt(width = asIdBits + vpnBits)) // 回填的tag
  val r_refill_waddr = Reg(UInt(width = log2Ceil(entries))) // 回填的地址，8表项索引
  val r_req = Reg(new TLBReq) // 请求

  // 暂存
  val do_mprv = io.ptw.status.mprv && !io.req.bits.instruction // 如果MPRV=1且不是取指操作，此时要特殊处理，因为此时特权模式相当于被设置为MPP的值（即低级特权级的值，和中断嵌套有关）
  val priv = Mux(do_mprv, io.ptw.status.mpp, io.ptw.status.prv) // 当前特权级的选择，为MPP的值或者prv的值
  val priv_s = priv === PRV.S // 是否为S模式
  val priv_uses_vm = priv <= PRV.S && !io.ptw.status.debug // 根据当前模式以及debug位来判断是否使用虚拟内存。Debug模式不进行地址转换

  // share a single physical memory attribute checker (unshare if critical path)
  val passthrough_ppn = io.req.bits.vpn(ppnBits-1, 0) // 直接回传VPN
  val refill_ppn = io.ptw.resp.bits.pte.ppn(ppnBits-1, 0) // 从PTW获取ppn响应作为重填的ppn值
  val do_refill = Bool(usingVM) && io.ptw.resp.valid // 重填标识：根据usingVM 以及 PTW是否有效响应 来决定是否重填
  val mpu_ppn = Mux(do_refill, refill_ppn, passthrough_ppn) // 根据重填标识 来选择返回的PPN值，在重填的PPN值和VPN之间选择
  val prot = addrMap.getProt(mpu_ppn << pgIdxBits) // 猜测: 地址保护，左移12位
  val cacheable = addrMap.isCacheable(mpu_ppn << pgIdxBits) // 猜测: 
  def pgaligned(r: MemRegion) = { // 判断页是否对齐
    val pgsize = 1 << pgIdxBits
    (r.start % pgsize) == 0 && (r.size % pgsize) == 0
  }
  require(addrMap.flatten.forall(e => pgaligned(e.region)),
    "MemoryMap regions must be page-aligned")
  
  val lookup_tag = Cat(io.ptw.ptbr.asid, io.req.bits.vpn(vpnBits-1,0)) // 拼接ASID和VPN得到TAG，用于查找TLB
  val vm_enabled = Bool(usingVM) && io.ptw.status.vm(3) && priv_uses_vm && !io.req.bits.passthrough // 根据usingVM, vm(3), 以及前面的priv_uses_vm, 不使用passthrough来得出最终的VM是否使能标识
  val hitsVec = (0 until entries).map(i => valid(i) && vm_enabled && tags(i) === lookup_tag) :+ !vm_enabled // 查找TLB
  val hits = hitsVec.asUInt
  
  // permission bit arrays
  val pte_array = Reg(new PTE)
  val u_array = Reg(UInt(width = entries)) // user permission
  val sw_array = Reg(UInt(width = entries)) // write permission
  val sx_array = Reg(UInt(width = entries)) // execute permission
  val sr_array = Reg(UInt(width = entries)) // read permission
  val xr_array = Reg(UInt(width = entries)) // read permission to executable page
  val cash_array = Reg(UInt(width = entries)) // cacheable
  val dirty_array = Reg(UInt(width = entries)) // PTE dirty bit

  when (do_refill) { // 进行重填操作
    val pte = io.ptw.resp.bits.pte // 获取PTW的响应
    ppns(r_refill_waddr) := pte.ppn // 填写TLB中对应entry的PPN
    tags(r_refill_waddr) := r_refill_tag  // 填写TLB中对应entry的tag

    // 设置相应的属性域
    val mask = UIntToOH(r_refill_waddr) 
    valid := valid | mask
    u_array := Mux(pte.u, u_array | mask, u_array & ~mask)//pte.u = 1, u_array = 1; otherwise, u_array = 0
    sw_array := Mux(pte.sw() && prot.w, sw_array | mask, sw_array & ~mask)//
    sx_array := Mux(pte.sx() && prot.x, sx_array | mask, sx_array & ~mask)
    sr_array := Mux(pte.sr() && prot.r, sr_array | mask, sr_array & ~mask)
    xr_array := Mux(pte.sx() && prot.r, xr_array | mask, xr_array & ~mask)
    cash_array := Mux(cacheable, cash_array | mask, cash_array & ~mask)
    dirty_array := Mux(pte.d, dirty_array | mask, dirty_array & ~mask)
  }
 
  val priv_ok = Mux(priv_s, ~Mux(io.ptw.status.pum, u_array, UInt(0)), u_array) // 如果不是是S模式，会忽略PUM；否则，如果S模式下且PUM=1，此时让问U模式可访问的页会出错。
  val w_array = Cat(prot.w, priv_ok & sw_array) // 
  val x_array = Cat(prot.x, priv_ok & sx_array)
  val r_array = Cat(prot.r, priv_ok & (sr_array | Mux(io.ptw.status.mxr, xr_array, UInt(0))))
  val c_array = Cat(cacheable, cash_array)

  val bad_va =
    if (vpnBits == vpnBitsExtended) Bool(false) // 当vpnBits == vpnBitsExtended时，虚拟地址正确；
    else io.req.bits.vpn(vpnBits) =/= io.req.bits.vpn(vpnBits-1) // 如果vpnBits != vpnBitsExtended，但是请求的vpn的低vpnBits位等于请求的低vpnBits-1位，则虚拟地址正确
  // it's only a store hit if the dirty bit is set
  val tlb_hits = hits(entries-1, 0) & (dirty_array | ~Mux(io.req.bits.store, w_array, UInt(0))) // 如果store操作，且对应的页表项的D位没有修改，则产生miss
  val tlb_hit = tlb_hits.orR
  val tlb_miss = vm_enabled && !bad_va && !tlb_hit

  val repl_waddr = Wire(UInt(0, width = log2Ceil(entries)))
  if(replacealgorithm == "RANDOM") {
    val rpl_alg = new MyRandom(entries)
    repl_waddr := Mux(!valid.andR, PriorityEncoder(~valid), rpl_alg.replace) // 选择替换地址，根据有效位以及rpl_alg算法进行权衡
    when (io.req.valid && !tlb_miss) { // 当请求有效，且TLB命中，则进行一次PRLU更新
      rpl_alg.access(OHToUInt(hits(entries-1, 0)))
    }
  } else {
    val rpl_alg = new MyPLRU(entries)
    repl_waddr := Mux(!valid.andR, PriorityEncoder(~valid), rpl_alg.replace) // 选择替换地址，根据有效位以及rpl_alg算法进行权衡
    when (io.req.valid && !tlb_miss) { // 当请求有效，且TLB命中，则进行一次PRLU更新
      rpl_alg.access(OHToUInt(hits(entries-1, 0)))
    }
  }

  io.req.ready := state === s_ready 
  io.resp.xcpt_ld := bad_va || (~r_array & hits).orR // 向上一级响应异常
  io.resp.xcpt_st := bad_va || (~w_array & hits).orR 
  io.resp.xcpt_if := bad_va || (~x_array & hits).orR
  io.resp.cacheable := (c_array & hits).orR
  io.resp.miss := do_refill || tlb_miss // 如果发生重填（肯定是从PTW获取）或者TLB缺失，都将产生MISS信号
  io.resp.ppn := Mux1H(hitsVec, ppns :+ passthrough_ppn) // ppns为一个数组，后面再append一个值，当命中则从ppns中选对应的值，否则选择passthrough_ppn

  io.ptw.req.valid := state === s_request
  io.ptw.req.bits <> io.ptw.status
  io.ptw.req.bits.addr := r_refill_tag // ASID+tag域，用于向PTW请求。应该是去低位的tag，不带ASID
  io.ptw.req.bits.store := r_req.store // 转发store信号
  io.ptw.req.bits.fetch := r_req.instruction // 转发instructions信号

  if (usingVM) {
    when (io.req.fire() && tlb_miss) { // 请求信号ready，且TLB缺失
      state := s_request 
      r_refill_tag := lookup_tag
      r_refill_waddr := repl_waddr
      r_req := io.req.bits // 获取请求
    }
    when (state === s_request) {
      when (io.ptw.invalidate) { // 
        state := s_ready
      }
      when (io.ptw.req.ready) { // PTW接收请求ready
        state := s_wait
        when (io.ptw.invalidate) { state := s_wait_invalidate }
      }
    }
    when (state === s_wait) {
      when (io.ptw.invalidate) {
        state := s_wait_invalidate
      }
    }
    when (io.ptw.resp.valid) { // PTW响应有效
      state := s_ready
    }
    when (io.ptw.invalidate) {  // PTW发来flush操作信号
      valid := 0
    }
  }
}

class TwoLevelTLB(implicit val p: Parameters) extends Module with HasTLBParameters {
  val io = new Bundle {
    val req = Decoupled(new TLBReq).flip
    val resp = new TLBResp
    val ptw = new TLBPTWIO
  }
  val l1_tlb = Module(new L1TLB())
  val l2_tlb = Module(new L2TLB())

  l1_tlb.io.req <> io.req
  l2_tlb.io.req <> io.req

  io.resp <> l1_tlb.io.resp

  l2_tlb.io.l1tlb <> l1_tlb.io.l2tlb

  io.ptw <> l2_tlb.io.ptw
}
//*************************************************
// L1 TLB: Version 1.1 
// Description: with miss/hit statistic
//*************************************************
class L1TLB(implicit val p: Parameters) extends Module with HasTLBParameters {
  val io = new Bundle {
    val req = Decoupled(new TLBReq).flip
    val resp = new TLBResp
    val l2tlb = new L1L2TLBIO
  }

  val valid = Reg(init = UInt(0, entries))
  val ppns = Reg(Vec(entries, UInt(width = ppnBits)))
  val tags = Reg(Vec(entries, UInt(width = asIdBits + vpnBits)))

  val s_ready :: s_request :: s_wait :: s_wait_invalidate :: Nil = Enum(UInt(), 4)
  val state = Reg(init=s_ready)
  val r_refill_tag = Reg(UInt(width = asIdBits + vpnBits)) // 回填的tag
  val r_refill_waddr = Reg(UInt(width = log2Ceil(entries))) // 回填的地址，8表项索引
  val r_req = Reg(new TLBReq) // 请求

  // 暂存
  val do_mprv = io.l2tlb.status.mprv && !io.req.bits.instruction // 如果MPRV=1且不是取指操作，此时要特殊处理，因为此时特权模式相当于被设置为MPP的值（即低级特权级的值，和中断嵌套有关）
  val priv = Mux(do_mprv, io.l2tlb.status.mpp, io.l2tlb.status.prv) // 当前特权级的选择，为MPP的值或者prv的值
  val priv_s = priv === PRV.S // 是否为S模式
  val priv_uses_vm = priv <= PRV.S && !io.l2tlb.status.debug // 根据当前模式以及debug位来判断是否使用虚拟内存。Debug模式不进行地址转换

  // share a single physical memory attribute checker (unshare if critical path)
  val passthrough_ppn = io.req.bits.vpn(ppnBits-1, 0) // 直接回传VPN
  val refill_ppn = io.l2tlb.resp.bits.pte.ppn(ppnBits-1, 0) // 从l2tlb获取ppn响应作为重填的ppn值
  val do_refill = Bool(usingVM) && io.l2tlb.resp.valid // 重填标识：根据usingVM 以及 l2tlb是否有效响应 来决定是否重填
  val mpu_ppn = Mux(do_refill, refill_ppn, passthrough_ppn) // 根据重填标识 来选择返回的PPN值，在重填的PPN值和VPN之间选择
  val prot = addrMap.getProt(mpu_ppn << pgIdxBits) // 猜测: 地址保护，左移12位
  val cacheable = addrMap.isCacheable(mpu_ppn << pgIdxBits) // 猜测: 
  def pgaligned(r: MemRegion) = { // 判断页是否对齐
    val pgsize = 1 << pgIdxBits
    (r.start % pgsize) == 0 && (r.size % pgsize) == 0
  }
  require(addrMap.flatten.forall(e => pgaligned(e.region)),
    "MemoryMap regions must be page-aligned")
  
  val lookup_tag = Cat(io.l2tlb.ptbr.asid, io.req.bits.vpn(vpnBits-1,0)) // 拼接ASID和VPN得到TAG，用于查找TLB
  val vm_enabled = Bool(usingVM) && io.l2tlb.status.vm(3) && priv_uses_vm && !io.req.bits.passthrough // 根据usingVM, vm(3), 以及前面的priv_uses_vm, 不使用passthrough来得出最终的VM是否使能标识
  val hitsVec = (0 until entries).map(i => valid(i) && vm_enabled && tags(i) === lookup_tag) :+ !vm_enabled // 查找TLB
  val hits = hitsVec.asUInt
  
  // permission bit arrays
  val pte_array = Reg(new PTE)
  val u_array = Reg(UInt(width = entries)) // user permission
  val sw_array = Reg(UInt(width = entries)) // write permission
  val sx_array = Reg(UInt(width = entries)) // execute permission
  val sr_array = Reg(UInt(width = entries)) // read permission
  val xr_array = Reg(UInt(width = entries)) // read permission to executable page
  val cash_array = Reg(UInt(width = entries)) // cacheable
  val dirty_array = Reg(UInt(width = entries)) // PTE dirty bit

  when (do_refill) { // 进行重填操作
    val pte = io.l2tlb.resp.bits.pte // 获取l2tlb的响应
    ppns(r_refill_waddr) := pte.ppn // 填写TLB中对应entry的PPN
    tags(r_refill_waddr) := r_refill_tag  // 填写TLB中对应entry的tag

    // 设置相应的属性域
    val mask = UIntToOH(r_refill_waddr) 
    valid := valid | mask
    u_array := Mux(pte.u, u_array | mask, u_array & ~mask)//pte.u = 1, u_array = 1; otherwise, u_array = 0
    sw_array := Mux(pte.sw() && prot.w, sw_array | mask, sw_array & ~mask)//
    sx_array := Mux(pte.sx() && prot.x, sx_array | mask, sx_array & ~mask)
    sr_array := Mux(pte.sr() && prot.r, sr_array | mask, sr_array & ~mask)
    xr_array := Mux(pte.sx() && prot.r, xr_array | mask, xr_array & ~mask)
    cash_array := Mux(cacheable, cash_array | mask, cash_array & ~mask)
    dirty_array := Mux(pte.d, dirty_array | mask, dirty_array & ~mask)
  }

  val priv_ok = Mux(priv_s, ~Mux(io.l2tlb.status.pum, u_array, UInt(0)), u_array) // 如果不是是S模式，会忽略PUM；否则，如果S模式下且PUM=1，此时让问U模式可访问的页会出错。
  val w_array = Cat(prot.w, priv_ok & sw_array) // 
  val x_array = Cat(prot.x, priv_ok & sx_array)
  val r_array = Cat(prot.r, priv_ok & (sr_array | Mux(io.l2tlb.status.mxr, xr_array, UInt(0))))
  val c_array = Cat(cacheable, cash_array)

  val bad_va =
    if (vpnBits == vpnBitsExtended) Bool(false) // 当vpnBits == vpnBitsExtended时，虚拟地址正确；
    else io.req.bits.vpn(vpnBits) =/= io.req.bits.vpn(vpnBits-1) // 如果vpnBits != vpnBitsExtended，但是请求的vpn的低vpnBits位等于请求的低vpnBits-1位，则虚拟地址正确
  // it's only a store hit if the dirty bit is set
  val tlb_hits = hits(entries-1, 0) & (dirty_array | ~Mux(io.req.bits.store, w_array, UInt(0))) // 如果store操作，且对应的页表项的D位没有修改，则产生miss
  val tlb_hit = tlb_hits.orR
  val tlb_miss = vm_enabled && !bad_va && !tlb_hit

  // replace algorithm select
  val repl_waddr = Wire(UInt(0, width = log2Ceil(entries)))
  if(replacealgorithm == "RANDOM") {
    val rpl_alg = new MyRandom(entries)
    repl_waddr := Mux(!valid.andR, PriorityEncoder(~valid), rpl_alg.replace) // 选择替换地址，根据有效位以及rpl_alg算法进行权衡
    when (io.req.valid && !tlb_miss) { // 当请求有效，且TLB命中，则进行一次PRLU更新
      rpl_alg.access(OHToUInt(hits(entries-1, 0)))
    }
  } else {
    val rpl_alg = new MyPLRU(entries)
    repl_waddr := Mux(!valid.andR, PriorityEncoder(~valid), rpl_alg.replace) // 选择替换地址，根据有效位以及rpl_alg算法进行权衡
    when (io.req.valid && !tlb_miss) { // 当请求有效，且TLB命中，则进行一次PRLU更新
      rpl_alg.access(OHToUInt(hits(entries-1, 0)))
    }
  }

  io.req.ready := state === s_ready 
  io.resp.xcpt_ld := bad_va || (~r_array & hits).orR // 向上一级响应异常
  io.resp.xcpt_st := bad_va || (~w_array & hits).orR 
  io.resp.xcpt_if := bad_va || (~x_array & hits).orR
  io.resp.cacheable := (c_array & hits).orR
  io.resp.miss := do_refill || tlb_miss // 如果发生重填（肯定是从l2tlb获取）或者TLB缺失，都将产生MISS信号
  io.resp.ppn := Mux1H(hitsVec, ppns :+ passthrough_ppn) // ppns为一个数组，后面再append一个值，当命中则从ppns中选对应的值，否则选择passthrough_ppn

  io.l2tlb.req.valid := state === s_request || state === s_wait
  io.l2tlb.req.bits <> io.l2tlb.status
  io.l2tlb.req.bits.addr := r_refill_tag // ASID+tag域，用于向l2tlb请求。应该是去低位的tag，不带ASID
  io.l2tlb.req.bits.store := r_req.store // 转发store信号
  io.l2tlb.req.bits.fetch := r_req.instruction // 转发instructions信号

/*
  val bcq_dbg_itlb = Reg(init = UInt(0, 8))
  when(io.req.bits.instruction) {
    bcq_dbg_itlb := 111
  }
// statistics
  val bcq_dbg_l1_req        = Reg(init = UInt(0, 64))
  val bcq_dbg_l1_valid_req  = Reg(init = UInt(0, 64))
  val bcq_dbg_l1_mis        = Reg(init = UInt(0, 64))
  val bcq_dbg_l1_valid_mis  = Reg(init = UInt(0, 64))

  val bcq_dbg_l1_req_flg        = Reg(Bits(width = 2))
  val bcq_dbg_l1_req_tmp        = Cat(bcq_dbg_l1_req_flg(0), io.req.valid)
  val bcq_dbg_l1_valid_req_flg  = Reg(Bits(width = 2))
  val bcq_dbg_l1_valid_req_tmp  = Cat(bcq_dbg_l1_valid_req_flg(0), io.req.fire())
  val bcq_dbg_l1_mis_flg        = Reg(Bits(width = 2))
  val bcq_dbg_l1_mis_tmp        = Cat(bcq_dbg_l1_mis_flg(0), io.resp.miss)
  val bcq_dbg_l1_valid_mis_flg  = Reg(Bits(width = 2))
  val bcq_dbg_l1_valid_mis_tmp  = Cat(bcq_dbg_l1_valid_mis_flg(0), io.req.fire() && tlb_miss)
  when (true) {
    bcq_dbg_l1_req_flg        := bcq_dbg_l1_req_tmp
    bcq_dbg_l1_valid_req_flg  := bcq_dbg_l1_valid_req_tmp
    bcq_dbg_l1_mis_flg        := bcq_dbg_l1_mis_tmp
    bcq_dbg_l1_valid_mis_flg  := bcq_dbg_l1_valid_mis_tmp
  }
  when (bcq_dbg_l1_req_flg === 1){
    bcq_dbg_l1_req := bcq_dbg_l1_req + 1
    printf("D|I+%d+ bcq_dbg_l1_req : %d\n", bcq_dbg_itlb, bcq_dbg_l1_req)
  }
  when (bcq_dbg_l1_valid_req_flg === 1){
    bcq_dbg_l1_valid_req := bcq_dbg_l1_valid_req + 1
    printf("D|I+%d+ bcq_dbg_l1_valid_req : %d\n", bcq_dbg_itlb, bcq_dbg_l1_valid_req)
  }
  when (bcq_dbg_l1_mis_flg === 1){
    bcq_dbg_l1_mis := bcq_dbg_l1_mis + 1
    printf("D|I+%d+ bcq_dbg_l1_mis : %d\n", bcq_dbg_itlb, bcq_dbg_l1_mis)
  }
  when (bcq_dbg_l1_valid_mis_flg === 1){
    bcq_dbg_l1_valid_mis := bcq_dbg_l1_valid_mis + 1
    printf("D|I+%d+ bcq_dbg_l1_valid_mis : %d\n", bcq_dbg_itlb, bcq_dbg_l1_valid_mis)
  }
*/

  if (usingVM) { 
    when (io.req.fire() && tlb_miss) { // 请求信号ready，且TLB缺失
      state := s_request 
      r_refill_tag := lookup_tag
      r_refill_waddr := repl_waddr
      r_req := io.req.bits // 获取请求
    }
    when (state === s_request) {
      when (io.l2tlb.invalidate) { // 
        state := s_ready
      }
      when (io.l2tlb.req.ready) { // l2tlb接收请求ready
        state := s_wait
        when (io.l2tlb.invalidate) { state := s_wait_invalidate }
      }
    }
    when (state === s_wait && io.l2tlb.invalidate) {
      state := s_wait_invalidate
    }
    when (io.l2tlb.resp.valid) { // l2tlb响应有效
      state := s_ready
    }

    when (io.l2tlb.invalidate) {  // l2tlb发来flush操作信号
      valid := 0
    }
  }
}
//*************************************************
// L2 TLB: Version 1.1 
// Description: without debug information, with shorten tag
//*************************************************
class L2TLB(implicit val p: Parameters) extends Module with HasTLBParameters {
  val io = new Bundle {
    val req = Decoupled(new TLBReq).flip
    val l1tlb = new L1L2TLBIO().flip
    val ptw = new TLBPTWIO
  }
  // L2 TLB
  val l2ways = ways
  val l2sets = sets
  val l2all  = l2ways * l2sets
  val way_width = log2Ceil(l2ways)
  val set_width = log2Ceil(l2sets)

  // TLB entry storageinit = UInt(0, entries)
  val L2_valid = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_tags                   = Reg(Vec(l2all, UInt(width = asIdBits + vpnBits - set_width)))
  val L2_ppn                    = Reg(Vec(l2all, UInt(width = ppnBits)))
  val L2_d = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_a = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_g = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_u = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_x = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_w = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_r = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_v = Reg(Vec(l2sets, UInt(0, l2ways)))

  val s_ready :: s_request :: s_wait :: s_wait_invalidate :: Nil = Enum(UInt(), 4)
  val state = Reg(init=s_ready)
  val r_req = Reg(new L2Req) // 请求
  val r_refill_tag = Reg(UInt(width = asIdBits + vpnBits - set_width)) // 回填的tag
  val r_refill_waddr = Reg(UInt(width = way_width)) // 回填的地址，8表项索引
  val r_refill_index = Reg(UInt(width = set_width))
  val r_total_base = Reg(UInt(width = way_width + set_width))

  val l1req_index = io.l1tlb.req.bits.addr(set_width-1, 0)
  val l1req_tag = Cat(io.l1tlb.ptbr.asid, io.l1tlb.req.bits.addr(vpnBits-1,set_width))
  val l1req_base = l1req_index * l2ways
  val hitsVec = (0 until l2ways).map(i => L2_valid(l1req_index)(i) && L2_tags(l1req_base + i) === l1req_tag)// 查找TLB  
  val hits = hitsVec.asUInt
  val tlb_hits = hits(l2ways-1, 0) & (L2_d(l1req_index) | ~Mux(io.l1tlb.req.bits.store, L2_w(l1req_index), UInt(0)))
  val tlb_hit = tlb_hits.orR
  val tlb_miss = !tlb_hit
  val l1req_idx_way = OHToUInt(hits)

  // replace algorithm select
  val repl_waddr = Wire(UInt(0, way_width))
  if(l2replacealgorithm == "RANDOM") {
    val rpl_alg = new MySeqRandom(l2sets, l2ways)
    rpl_alg.access(l1req_index)
    rpl_alg.update(io.l1tlb.req.valid, !tlb_miss, l1req_index, OHToUInt(hits))
    repl_waddr := Mux(!L2_valid(l1req_index).andR, PriorityEncoder(~L2_valid(l1req_index)), rpl_alg.any_way(l1req_index))
  } else {
    val rpl_alg = new MySeqPLRU(l2sets, l2ways)
    rpl_alg.access(l1req_index)
    rpl_alg.update(io.l1tlb.req.valid, !tlb_miss, l1req_index, OHToUInt(hits))
    repl_waddr := Mux(!L2_valid(l1req_index).andR, PriorityEncoder(~L2_valid(l1req_index)), rpl_alg.any_way(l1req_index))
  }
  
  val do_refill = io.ptw.resp.valid // 重填标识：根据usingVM 以及 l2tlb是否有效响应 来决定是否重填
  when (do_refill) { // 进行重填操作
    val pte = io.ptw.resp.bits.pte // 获取PTW的响应
    
    // 设置相应的属性域
    val mask = UIntToOH(r_refill_waddr) 
    val tmp_valid = L2_valid(r_refill_index) | mask
    val tmp_d = Mux(pte.d, L2_d(r_refill_index) | mask, L2_d(r_refill_index) & ~mask)
    val tmp_a = Mux(pte.a, L2_a(r_refill_index) | mask, L2_a(r_refill_index) & ~mask)
    val tmp_g = Mux(pte.g, L2_g(r_refill_index) | mask, L2_g(r_refill_index) & ~mask)
    val tmp_u = Mux(pte.u, L2_u(r_refill_index) | mask, L2_u(r_refill_index) & ~mask)
    val tmp_x = Mux(pte.x, L2_x(r_refill_index) | mask, L2_x(r_refill_index) & ~mask)
    val tmp_w = Mux(pte.w, L2_w(r_refill_index) | mask, L2_w(r_refill_index) & ~mask)
    val tmp_r = Mux(pte.r, L2_r(r_refill_index) | mask, L2_r(r_refill_index) & ~mask)
    val tmp_v = Mux(pte.v, L2_v(r_refill_index) | mask, L2_v(r_refill_index) & ~mask)

    L2_valid(r_refill_index)  := tmp_valid
    L2_tags(r_total_base + r_refill_waddr)                  := r_refill_tag
    L2_ppn(r_total_base + r_refill_waddr)                   := pte.ppn
    L2_d(r_refill_index)      := tmp_d
    L2_a(r_refill_index)      := tmp_a
    L2_g(r_refill_index)      := tmp_g
    L2_u(r_refill_index)      := tmp_u
    L2_x(r_refill_index)      := tmp_x
    L2_w(r_refill_index)      := tmp_w
    L2_r(r_refill_index)      := tmp_r
    L2_v(r_refill_index)      := tmp_v
  }

  io.ptw.req.valid := state === s_request
  io.ptw.req.bits <> io.ptw.status
  io.ptw.req.bits.addr := r_req.addr // ASID+tag域，用于向PTW请求。应该是去低位的tag，不带ASID
  io.ptw.req.bits.store := r_req.store // 转发store信号
  io.ptw.req.bits.fetch := r_req.fetch // 转发instructions信号


  io.l1tlb.req.ready := state === s_ready
  io.l1tlb.resp.valid := state === s_ready && tlb_hit && io.l1tlb.req.valid

  io.l1tlb.resp.bits.pte.reserved_for_hardware := 0
  io.l1tlb.resp.bits.pte.ppn                   := L2_ppn(l1req_base + l1req_idx_way)
  io.l1tlb.resp.bits.pte.reserved_for_software := 0
  io.l1tlb.resp.bits.pte.d := L2_d(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.a := L2_a(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.g := L2_g(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.u := L2_u(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.x := L2_x(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.w := L2_w(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.r := L2_r(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.v := L2_v(l1req_index)(l1req_idx_way)

  io.l1tlb.ptbr <> io.ptw.ptbr
  io.l1tlb.invalidate <> io.ptw.invalidate
  io.l1tlb.status <> io.ptw.status

/*
  val bcq_dbg_itlb = Reg(init = UInt(0, 8))
  when(io.l1tlb.req.bits.fetch) {
    bcq_dbg_itlb := 111
  }
// statistics
  val bcq_dbg_l2_valid_req  = Reg(init = UInt(0, 64))
  val bcq_dbg_l2_valid_mis  = Reg(init = UInt(0, 64))

  val bcq_dbg_l2_valid_req_flg  = Reg(Bits(width = 2))
  val bcq_dbg_l2_valid_req_tmp  = Cat(bcq_dbg_l2_valid_req_flg(0), io.l1tlb.req.valid)
  val bcq_dbg_l2_valid_mis_flg  = Reg(Bits(width = 2))
  val bcq_dbg_l2_valid_mis_tmp  = Cat(bcq_dbg_l2_valid_mis_flg(0), io.l1tlb.req.fire() && tlb_miss)
  when (true) {
    bcq_dbg_l2_valid_req_flg  := bcq_dbg_l2_valid_req_tmp
    bcq_dbg_l2_valid_mis_flg  := bcq_dbg_l2_valid_mis_tmp
  }
  when (bcq_dbg_l2_valid_req_flg === 1){
    bcq_dbg_l2_valid_req := bcq_dbg_l2_valid_req + 1
    printf("D|I+%d+ bcq_dbg_l2_valid_req : %d\n", bcq_dbg_itlb, bcq_dbg_l2_valid_req)
  }
  when (bcq_dbg_l2_valid_mis_flg === 1){
    bcq_dbg_l2_valid_mis := bcq_dbg_l2_valid_mis + 1
    printf("D|I+%d+ bcq_dbg_l2_valid_mis : %d\n", bcq_dbg_itlb, bcq_dbg_l2_valid_mis)
  }
*/
  when (io.l1tlb.req.fire() && !tlb_hit) {
    state := s_request
    r_req := io.l1tlb.req.bits
    r_refill_index  := l1req_index
    r_refill_tag    := l1req_tag
    r_refill_waddr  := repl_waddr 
    r_total_base   := l1req_index * l2ways
  }
  when (state === s_request) {
    when (io.ptw.invalidate) {
      state := s_ready
    }
    when (io.ptw.req.ready) {
      state := s_wait
      when (io.ptw.invalidate) { state := s_wait_invalidate }
    }
  }
  when (state === s_wait && io.ptw.invalidate) {
    state := s_wait_invalidate
  }
  when (io.ptw.resp.valid) {
    state := s_ready
  }
  when (io.ptw.invalidate) {
    for( i <- 0 until l2sets) {
      L2_valid(i) := 0
    }
  }
}


//*************************************************
// L1 TLB: Version 1.0 
// Description: with debug information
//*************************************************
/*
class L1TLB(implicit val p: Parameters) extends Module with HasTLBParameters {
  val io = new Bundle {
    val req = Decoupled(new TLBReq).flip
    val resp = new TLBResp
    val l2tlb = new L1L2TLBIO
  }

  val valid = Reg(init = UInt(0, entries))
  val ppns = Reg(Vec(entries, UInt(width = ppnBits)))
  val tags = Reg(Vec(entries, UInt(width = asIdBits + vpnBits)))

  val s_ready :: s_request :: s_wait :: s_wait_invalidate :: Nil = Enum(UInt(), 4)
  val state = Reg(init=s_ready)
  val r_refill_tag = Reg(UInt(width = asIdBits + vpnBits)) // 回填的tag
  val r_refill_waddr = Reg(UInt(width = log2Ceil(entries))) // 回填的地址，8表项索引
  val r_req = Reg(new TLBReq) // 请求

  // 暂存
  val do_mprv = io.l2tlb.status.mprv && !io.req.bits.instruction // 如果MPRV=1且不是取指操作，此时要特殊处理，因为此时特权模式相当于被设置为MPP的值（即低级特权级的值，和中断嵌套有关）
  val priv = Mux(do_mprv, io.l2tlb.status.mpp, io.l2tlb.status.prv) // 当前特权级的选择，为MPP的值或者prv的值
  val priv_s = priv === PRV.S // 是否为S模式
  val priv_uses_vm = priv <= PRV.S && !io.l2tlb.status.debug // 根据当前模式以及debug位来判断是否使用虚拟内存。Debug模式不进行地址转换

  // share a single physical memory attribute checker (unshare if critical path)
  val passthrough_ppn = io.req.bits.vpn(ppnBits-1, 0) // 直接回传VPN
  val refill_ppn = io.l2tlb.resp.bits.pte.ppn(ppnBits-1, 0) // 从l2tlb获取ppn响应作为重填的ppn值
  val do_refill = Bool(usingVM) && io.l2tlb.resp.valid // 重填标识：根据usingVM 以及 l2tlb是否有效响应 来决定是否重填
  val mpu_ppn = Mux(do_refill, refill_ppn, passthrough_ppn) // 根据重填标识 来选择返回的PPN值，在重填的PPN值和VPN之间选择
  val prot = addrMap.getProt(mpu_ppn << pgIdxBits) // 猜测: 地址保护，左移12位
  val cacheable = addrMap.isCacheable(mpu_ppn << pgIdxBits) // 猜测: 
  def pgaligned(r: MemRegion) = { // 判断页是否对齐
    val pgsize = 1 << pgIdxBits
    (r.start % pgsize) == 0 && (r.size % pgsize) == 0
  }
  require(addrMap.flatten.forall(e => pgaligned(e.region)),
    "MemoryMap regions must be page-aligned")
  
  val lookup_tag = Cat(io.l2tlb.ptbr.asid, io.req.bits.vpn(vpnBits-1,0)) // 拼接ASID和VPN得到TAG，用于查找TLB
  val vm_enabled = Bool(usingVM) && io.l2tlb.status.vm(3) && priv_uses_vm && !io.req.bits.passthrough // 根据usingVM, vm(3), 以及前面的priv_uses_vm, 不使用passthrough来得出最终的VM是否使能标识
  val hitsVec = (0 until entries).map(i => valid(i) && vm_enabled && tags(i) === lookup_tag) :+ !vm_enabled // 查找TLB
  val hits = hitsVec.asUInt
  
  // permission bit arrays
  val pte_array = Reg(new PTE)
  val u_array = Reg(UInt(width = entries)) // user permission
  val sw_array = Reg(UInt(width = entries)) // write permission
  val sx_array = Reg(UInt(width = entries)) // execute permission
  val sr_array = Reg(UInt(width = entries)) // read permission
  val xr_array = Reg(UInt(width = entries)) // read permission to executable page
  val cash_array = Reg(UInt(width = entries)) // cacheable
  val dirty_array = Reg(UInt(width = entries)) // PTE dirty bit

  when (do_refill) { // 进行重填操作
    val pte = io.l2tlb.resp.bits.pte // 获取l2tlb的响应
    ppns(r_refill_waddr) := pte.ppn // 填写TLB中对应entry的PPN
    tags(r_refill_waddr) := r_refill_tag  // 填写TLB中对应entry的tag

    // 设置相应的属性域
    val mask = UIntToOH(r_refill_waddr) 
    valid := valid | mask
    u_array := Mux(pte.u, u_array | mask, u_array & ~mask)//pte.u = 1, u_array = 1; otherwise, u_array = 0
    sw_array := Mux(pte.sw() && prot.w, sw_array | mask, sw_array & ~mask)//
    sx_array := Mux(pte.sx() && prot.x, sx_array | mask, sx_array & ~mask)
    sr_array := Mux(pte.sr() && prot.r, sr_array | mask, sr_array & ~mask)
    xr_array := Mux(pte.sx() && prot.r, xr_array | mask, xr_array & ~mask)
    cash_array := Mux(cacheable, cash_array | mask, cash_array & ~mask)
    dirty_array := Mux(pte.d, dirty_array | mask, dirty_array & ~mask)
  }
 
  val rpl_alg = new PseudoLRU(entries)
  val repl_waddr = Mux(!valid.andR, PriorityEncoder(~valid), rpl_alg.replace) // 选择替换地址，根据有效位以及rpl_alg算法进行权衡

  val priv_ok = Mux(priv_s, ~Mux(io.l2tlb.status.pum, u_array, UInt(0)), u_array) // 如果不是是S模式，会忽略PUM；否则，如果S模式下且PUM=1，此时让问U模式可访问的页会出错。
  val w_array = Cat(prot.w, priv_ok & sw_array) // 
  val x_array = Cat(prot.x, priv_ok & sx_array)
  val r_array = Cat(prot.r, priv_ok & (sr_array | Mux(io.l2tlb.status.mxr, xr_array, UInt(0))))
  val c_array = Cat(cacheable, cash_array)

  val bad_va =
    if (vpnBits == vpnBitsExtended) Bool(false) // 当vpnBits == vpnBitsExtended时，虚拟地址正确；
    else io.req.bits.vpn(vpnBits) =/= io.req.bits.vpn(vpnBits-1) // 如果vpnBits != vpnBitsExtended，但是请求的vpn的低vpnBits位等于请求的低vpnBits-1位，则虚拟地址正确
  // it's only a store hit if the dirty bit is set
  val tlb_hits = hits(entries-1, 0) & (dirty_array | ~Mux(io.req.bits.store, w_array, UInt(0))) // 如果store操作，且对应的页表项的D位没有修改，则产生miss
  val tlb_hit = tlb_hits.orR
  val tlb_miss = vm_enabled && !bad_va && !tlb_hit

  when (io.req.valid && !tlb_miss) { // 当请求有效，且TLB命中，则进行一次PRLU更新
    rpl_alg.access(OHToUInt(hits(entries-1, 0)))
  }

  io.req.ready := state === s_ready 
  io.resp.xcpt_ld := bad_va || (~r_array & hits).orR // 向上一级响应异常
  io.resp.xcpt_st := bad_va || (~w_array & hits).orR 
  io.resp.xcpt_if := bad_va || (~x_array & hits).orR
  io.resp.cacheable := (c_array & hits).orR
  io.resp.miss := do_refill || tlb_miss // 如果发生重填（肯定是从l2tlb获取）或者TLB缺失，都将产生MISS信号
  io.resp.ppn := Mux1H(hitsVec, ppns :+ passthrough_ppn) // ppns为一个数组，后面再append一个值，当命中则从ppns中选对应的值，否则选择passthrough_ppn

  io.l2tlb.req.valid := state === s_request || state === s_wait
  io.l2tlb.req.bits <> io.l2tlb.status
  io.l2tlb.req.bits.addr := r_refill_tag // ASID+tag域，用于向l2tlb请求。应该是去低位的tag，不带ASID
  io.l2tlb.req.bits.store := r_req.store // 转发store信号
  io.l2tlb.req.bits.fetch := r_req.instruction // 转发instructions信号

  when(true) {
    printf("L1: state: %x\n",state)
  }

  if (usingVM) { 
    when (io.req.fire() && tlb_miss) { // 请求信号ready，且TLB缺失
      printf("L1: TLBreq(io.req.bits.vpn): %x | (io.req.bits.passthrough/instruction/store: %x %x %x\n", io.req.bits.vpn, io.req.bits.passthrough, io.req.bits.instruction, io.req.bits.store)
      state := s_request 
      r_refill_tag := lookup_tag
      r_refill_waddr := repl_waddr
      r_req := io.req.bits // 获取请求
    }
    when (state === s_request) {
      when (io.l2tlb.invalidate) { // 
        state := s_ready
      }
      when (io.l2tlb.req.ready) { // l2tlb接收请求ready
        state := s_wait
        when (io.l2tlb.invalidate) { state := s_wait_invalidate }
      }
    }
    when (state === s_wait && io.l2tlb.invalidate) {
      state := s_wait_invalidate
    }
    when (io.l2tlb.resp.valid) { // l2tlb响应有效
      printf("L1: l2tlb.resp.valid || pte.ppn/d/a/g/u/x/w/r/v: %x %x %x %x %x %x %x %x %x\n", io.l2tlb.resp.bits.pte.ppn, io.l2tlb.resp.bits.pte.d, io.l2tlb.resp.bits.pte.a, io.l2tlb.resp.bits.pte.g, io.l2tlb.resp.bits.pte.u, io.l2tlb.resp.bits.pte.x, io.l2tlb.resp.bits.pte.w, io.l2tlb.resp.bits.pte.r, io.l2tlb.resp.bits.pte.v)
      state := s_ready
    }

    when (io.l2tlb.invalidate) {  // l2tlb发来flush操作信号
      valid := 0
    }
  }
}
*/

//*************************************************
// L2 TLB: Version 1.0 
// Description: with debug information
//*************************************************
/*
class L2TLB(implicit val p: Parameters) extends Module with HasTLBParameters {
  val io = new Bundle {
    val req = Decoupled(new TLBReq).flip
    val l1tlb = new L1L2TLBIO().flip
    val ptw = new TLBPTWIO
  }
  // L2 TLB
  val l2ways = entries
  val l2sets = entries * 4
  val l2all  = l2ways * l2sets
  val way_width = log2Ceil(l2ways)
  val set_width = log2Ceil(l2sets)

  // TLB entry storageinit = UInt(0, entries)
  val L2_valid = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_tags                   = Reg(Vec(l2all, UInt(width = asIdBits + vpnBits)))
  val L2_reserved_for_hardware  = Reg(Vec(l2all, UInt(width = 16)))
  val L2_ppn                    = Reg(Vec(l2all, UInt(width = ppnBits)))
  val L2_reserved_for_software  = Reg(Vec(l2all, UInt(width = 2)))
  val L2_d = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_a = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_g = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_u = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_x = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_w = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_r = Reg(Vec(l2sets, UInt(0, l2ways)))
  val L2_v = Reg(Vec(l2sets, UInt(0, l2ways)))

  val s_ready :: s_request :: s_wait :: s_wait_invalidate :: Nil = Enum(UInt(), 4)
  val state = Reg(init=s_ready)
  val r_req = Reg(new L2Req) // 请求
  val r_refill_tag = Reg(UInt(width = asIdBits + vpnBits)) // 回填的tag
  val r_refill_waddr = Reg(UInt(width = way_width)) // 回填的地址，8表项索引
  val r_refill_index = Reg(UInt(width = set_width))
  val r_total_base = Reg(UInt(width = way_width + set_width))

  val l1req_index = io.l1tlb.req.bits.addr(set_width-1, 0)
  val l1req_tag = Cat(io.l1tlb.ptbr.asid, io.l1tlb.req.bits.addr(vpnBits-1,0))
  val l1req_base = l1req_index * l2ways
  val hitsVec = (0 until l2ways).map(i => L2_valid(l1req_index)(i) && L2_tags(l1req_base + i) === l1req_tag)// 查找TLB  
  val hits = hitsVec.asUInt
  val tlb_hits = hits(l2ways-1, 0) & (L2_d(l1req_index) | ~Mux(io.l1tlb.req.bits.store, L2_w(l1req_index), UInt(0)))
  val tlb_hit = tlb_hits.orR
  val tlb_miss = !tlb_hit
  val l1req_idx_way = OHToUInt(hits)

  val rpl_alg = new Myrpl_alg(l2sets, l2ways)
  rpl_alg.access(l1req_index)
  rpl_alg.update(io.l1tlb.req.valid, !tlb_miss, l1req_index, OHToUInt(hits))

  val repl_waddr = Mux(!L2_valid(l1req_index).andR, PriorityEncoder(~L2_valid(l1req_index)), rpl_alg.any_way(l1req_index))

  val do_refill = io.ptw.resp.valid // 重填标识：根据usingVM 以及 l2tlb是否有效响应 来决定是否重填
  when (do_refill) { // 进行重填操作
    val pte = io.ptw.resp.bits.pte // 获取PTW的响应
    
    // 设置相应的属性域
    val mask = UIntToOH(r_refill_waddr) 
    val tmp_valid = L2_valid(r_refill_index) | mask
    val tmp_d = Mux(pte.d, L2_d(r_refill_index) | mask, L2_d(r_refill_index) & ~mask)
    val tmp_a = Mux(pte.a, L2_a(r_refill_index) | mask, L2_a(r_refill_index) & ~mask)
    val tmp_g = Mux(pte.g, L2_g(r_refill_index) | mask, L2_g(r_refill_index) & ~mask)
    val tmp_u = Mux(pte.u, L2_u(r_refill_index) | mask, L2_u(r_refill_index) & ~mask)
    val tmp_x = Mux(pte.x, L2_x(r_refill_index) | mask, L2_x(r_refill_index) & ~mask)
    val tmp_w = Mux(pte.w, L2_w(r_refill_index) | mask, L2_w(r_refill_index) & ~mask)
    val tmp_r = Mux(pte.r, L2_r(r_refill_index) | mask, L2_r(r_refill_index) & ~mask)
    val tmp_v = Mux(pte.v, L2_v(r_refill_index) | mask, L2_v(r_refill_index) & ~mask)

    L2_valid(r_refill_index) := tmp_valid
    L2_tags(r_total_base + r_refill_waddr) := r_refill_tag
    L2_reserved_for_hardware(r_total_base + r_refill_waddr) := pte.reserved_for_hardware
    L2_ppn(r_total_base + r_refill_waddr) := pte.ppn
    L2_reserved_for_software(r_total_base + r_refill_waddr) := pte.reserved_for_software
    L2_d(r_refill_index) := tmp_d
    L2_a(r_refill_index) := tmp_a
    L2_g(r_refill_index) := tmp_g
    L2_u(r_refill_index) := tmp_u
    L2_x(r_refill_index) := tmp_x
    L2_w(r_refill_index) := tmp_w
    L2_r(r_refill_index) := tmp_r
    L2_v(r_refill_index) := tmp_v
    printf("L2: do_refill | set: %x, way: %x | mask: %x tmp_d/a/g/u/x/w/r/v: %x %x %x %x %x %x %x %x | valid/tags/ppn: %x %x %x || origin_valid/tags/ppn: %x %x %x\n", r_refill_index, r_refill_waddr, mask, tmp_d, tmp_a, tmp_g, tmp_u, tmp_x, tmp_w, tmp_r, tmp_v, tmp_valid, r_refill_tag, pte.ppn, L2_valid(r_refill_index), L2_tags(r_refill_index*l2ways+r_refill_waddr), L2_ppn(r_refill_index*l2ways+r_refill_waddr))
  }

  io.ptw.req.valid := state === s_request
  io.ptw.req.bits <> io.ptw.status
  io.ptw.req.bits.addr := r_req.addr // ASID+tag域，用于向PTW请求。应该是去低位的tag，不带ASID
  io.ptw.req.bits.store := r_req.store // 转发store信号
  io.ptw.req.bits.fetch := r_req.fetch // 转发instructions信号


  io.l1tlb.req.ready := state === s_ready
  io.l1tlb.resp.valid := state === s_ready && tlb_hit && io.l1tlb.req.valid

  io.l1tlb.resp.bits.pte.reserved_for_hardware := L2_reserved_for_hardware(l1req_base + l1req_idx_way)
  io.l1tlb.resp.bits.pte.ppn                   := L2_ppn(l1req_base + l1req_idx_way)
  io.l1tlb.resp.bits.pte.reserved_for_software := L2_reserved_for_software(l1req_base + l1req_idx_way)
  io.l1tlb.resp.bits.pte.d := L2_d(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.a := L2_a(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.g := L2_g(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.u := L2_u(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.x := L2_x(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.w := L2_w(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.r := L2_r(l1req_index)(l1req_idx_way)
  io.l1tlb.resp.bits.pte.v := L2_v(l1req_index)(l1req_idx_way)

  io.l1tlb.ptbr <> io.ptw.ptbr
  io.l1tlb.invalidate <> io.ptw.invalidate
  io.l1tlb.status <> io.ptw.status

  when(io.l1tlb.req.valid && state === s_ready) {
    printf("L2: hits: %x || L2_d/a/g/u/x/w/r/v: %x %x %x %x %x %x %x %x | L2_valid/L2_tags/L2_ppn: %x %x %x\n", hits, L2_d(r_refill_index), L2_a(r_refill_index), L2_g(r_refill_index), L2_u(r_refill_index), L2_x(r_refill_index), L2_w(r_refill_index), L2_r(r_refill_index), L2_v(r_refill_index), L2_valid(r_refill_index), L2_tags(r_refill_index*l2ways+r_refill_waddr), L2_ppn(r_refill_index*l2ways+r_refill_waddr))
  }

  when (true) {
    printf("L2: l1req_index: %x | l1req_tag: %x\n", l1req_index, l1req_tag)
    printf("L2: state: %x, io.l1tlb.resp.valid: %x, tlb_hit: %x, io.l1tlb.req.valid: %x\n", state, io.l1tlb.resp.valid, tlb_hit, io.l1tlb.req.valid)
  }

  when (io.l1tlb.req.fire() && tlb_hit) {
    printf("L2 hit: %x\n", io.l1tlb.req.bits.addr)
  }

  when (io.l1tlb.req.fire() && !tlb_hit) {
    printf("L2: L2req(io.l1tlb.req.bits.addr): %x\n", io.l1tlb.req.bits.addr)
    state := s_request
    r_req := io.l1tlb.req.bits
    r_refill_index  := l1req_index
    r_refill_tag    := l1req_tag
    r_refill_waddr  := repl_waddr 
    r_total_base   := l1req_index * l2ways
  }
  when (state === s_request) {
    printf("L2: PTWreq(io.ptw.req.bits.addr/store/fetch/pum/mxr/prv: %x %x %x %x %x %x\n", io.ptw.req.bits.addr, io.ptw.req.bits.store, io.ptw.req.bits.fetch, io.ptw.req.bits.pum, io.ptw.req.bits.mxr, io.ptw.req.bits.prv)

    when (io.ptw.invalidate) {
      state := s_ready
    }
    when (io.ptw.req.ready) {
      state := s_wait
      when (io.ptw.invalidate) { state := s_wait_invalidate }
    }
  }
  when (state === s_wait && io.ptw.invalidate) {
    state := s_wait_invalidate
  }
  when (io.ptw.resp.valid) {
    printf("L2: io.ptw.resp.valid || pte.ppn/d/a/g/u/x/w/r/v: %x %x %x %x %x %x %x %x %x\n", io.ptw.resp.bits.pte.ppn, io.ptw.resp.bits.pte.d, io.ptw.resp.bits.pte.a, io.ptw.resp.bits.pte.g, io.ptw.resp.bits.pte.u, io.ptw.resp.bits.pte.x, io.ptw.resp.bits.pte.w, io.ptw.resp.bits.pte.r, io.ptw.resp.bits.pte.v)
    state := s_ready
  }
  when (io.ptw.invalidate) {
    printf("L2: ptw.invalidate\n")
    for( i <- 0 until l2sets) {
      L2_valid(i) := 0
    }
  }
}
*/




class DecoupledTLB(implicit p: Parameters) extends Module {
  val io = new Bundle {
    val req = Decoupled(new TLBReq).flip
    val resp = Decoupled(new TLBResp)
    val ptw = new TLBPTWIO
  }

  val req = Reg(new TLBReq)
  val resp = Reg(new TLBResp)
  val tlb = Module(new TLB)

  val s_idle :: s_tlb_req :: s_tlb_resp :: s_done :: Nil = Enum(Bits(), 4)
  val state = Reg(init = s_idle)

  when (io.req.fire()) {
    req := io.req.bits
    state := s_tlb_req
  }

  when (tlb.io.req.fire()) {
    state := s_tlb_resp
  }

  when (state === s_tlb_resp) {
    when (tlb.io.resp.miss) {
      state := s_tlb_req
    } .otherwise {
      resp := tlb.io.resp
      state := s_done
    }
  }

  when (io.resp.fire()) { state := s_idle }

  io.req.ready := state === s_idle

  tlb.io.req.valid := state === s_tlb_req
  tlb.io.req.bits := req

  io.resp.valid := state === s_done
  io.resp.bits := resp

  io.ptw <> tlb.io.ptw
}
