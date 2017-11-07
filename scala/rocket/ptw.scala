// See LICENSE for license details.

package rocket

import Chisel._
import uncore.agents._
import uncore.constants._
import util._
import Chisel.ImplicitConversions._
import cde.{Parameters, Field}

class PTWReq(implicit p: Parameters) extends CoreBundle()(p) {
  val prv = Bits(width = 2)
  val pum = Bool()
  val mxr = Bool()
  val addr = UInt(width = vpnBits)
  val store = Bool()
  val fetch = Bool()
}

class PTWResp(implicit p: Parameters) extends CoreBundle()(p) {
  val pte = new PTE
}

class TLBPTWIO(implicit p: Parameters) extends CoreBundle()(p) {
  val req = Decoupled(new PTWReq)
  val resp = Valid(new PTWResp).flip
  val ptbr = new PTBR().asInput
  val invalidate = Bool(INPUT)
  val status = new MStatus().asInput
}

class DatapathPTWIO(implicit p: Parameters) extends CoreBundle()(p) {
  val ptbr = new PTBR().asInput
  val invalidate = Bool(INPUT)
  val status = new MStatus().asInput
}

class PTE(implicit p: Parameters) extends CoreBundle()(p) {
  val reserved_for_hardware = Bits(width = 16)
  val ppn = UInt(width = 38)
  val reserved_for_software = Bits(width = 2)
  val d = Bool()
  val a = Bool()
  val g = Bool()
  val u = Bool()
  val x = Bool()
  val w = Bool()
  val r = Bool()
  val v = Bool()

  def table(dummy: Int = 0) = v && !r && !w && !x  // v=1, r=0, w=0, x=0
  def leaf(dummy: Int = 0) = v && (r || (x && !w)) // v=1, r=1(r=0, x=1, w=0)
  def ur(dummy: Int = 0) = sr() && u 
  def uw(dummy: Int = 0) = sw() && u
  def ux(dummy: Int = 0) = sx() && u
  def sr(dummy: Int = 0) = leaf() && r // v=1, r=1
  def sw(dummy: Int = 0) = leaf() && w // v=1, r=1, w=1
  def sx(dummy: Int = 0) = leaf() && x // v=1, (r=1, x=1)(r=0, x=1, w=0)

  def access_ok(req: PTWReq) = {
    // 权限检查
    val perm_ok = Mux(req.fetch, x, Mux(req.store, w, r || (x && req.mxr)))// 当mxr=0时，只能load readable page；当mxr=1时，可以load Readable或executable page
    // 特权级检查
    val priv_ok = Mux(u, !req.pum, req.prv(0))//
    leaf() && priv_ok && perm_ok
  }
}

class PTW(n: Int)(implicit p: Parameters) extends CoreModule()(p) {
  val io = new Bundle {
    val requestor = Vec(n, new TLBPTWIO).flip
    val mem = new HellaCacheIO
    val dpath = new DatapathPTWIO
  }

  require(usingAtomics, "PTW requires atomic memory operations")

  val s_ready :: s_req :: s_wait1 :: s_wait2 :: s_set_dirty :: s_wait1_dirty :: s_wait2_dirty :: s_done :: Nil = Enum(UInt(), 8)
  val state = Reg(init=s_ready)
  val count = Reg(UInt(width = log2Up(pgLevels)))
  val s1_kill = Reg(next = Bool(false))

  val r_req = Reg(new PTWReq)
  val r_req_dest = Reg(Bits())
  val r_pte = Reg(new PTE)
  
  val vpn_idxs = (0 until pgLevels).map(i => (r_req.addr >> (pgLevels-i-1)*pgLevelBits)(pgLevelBits-1,0))
  val vpn_idx = vpn_idxs(count)

  val arb = Module(new RRArbiter(new PTWReq, n))
  arb.io.in <> io.requestor.map(_.req)
  arb.io.out.ready := state === s_ready

  val pte = {
    val tmp = new PTE().fromBits(io.mem.resp.bits.data)
    val res = Wire(init = new PTE().fromBits(io.mem.resp.bits.data))
    res.ppn := tmp.ppn(ppnBits-1, 0)
    when ((tmp.ppn >> ppnBits) =/= 0) { res.v := false }
    res
  }
  val pte_addr = Cat(r_pte.ppn, vpn_idx) << log2Ceil(xLen/8)

  when (arb.io.out.fire()) {
    r_req := arb.io.out.bits
    r_req_dest := arb.io.chosen
    r_pte.ppn := io.dpath.ptbr.ppn
  }

  val (pte_cache_hit, pte_cache_data) = {//命中状态, 命中数据
    val size = 1 << log2Up(pgLevels * 2)
    val plru = new PseudoLRU(size)
    val valid = Reg(init = UInt(0, size))
    val tags = Reg(Vec(size, UInt(width = paddrBits)))
    val data = Reg(Vec(size, UInt(width = ppnBits)))

    val hits = tags.map(_ === pte_addr).asUInt & valid
    val hit = hits.orR
    when (io.mem.resp.valid && pte.table() && !hit) {//缓存未命中且mem返回有效数据，且返回为非叶子页表
      val r = Mux(valid.andR, plru.replace, PriorityEncoder(~valid))// 如果缓存全部有效，则由plru指定，否则由优先队列指定
      valid := valid | UIntToOH(r)// 设定替换项为有效
      tags(r) := pte_addr// 填充该项
      data(r) := pte.ppn// 填充该项
    }
    when (hit && state === s_req) { plru.access(OHToUInt(hits)) }//当缓存命中且state = s_req，刷新plru
    when (io.dpath.invalidate) { valid := 0 }// 当io.dpath.invalidate失效，清除所有页表缓存

    (hit && count < pgLevels-1, Mux1H(hits, data))//返回是否命中非叶子页表，选择缓存中命中数据
  }

  val pte_wdata = Wire(init=new PTE().fromBits(0))
  pte_wdata.a := true
  pte_wdata.d := r_req.store// 如果是存储指令，设置为dirty
  
  io.mem.req.valid     := state.isOneOf(s_req, s_set_dirty)//
  io.mem.req.bits.phys := Bool(true)
  io.mem.req.bits.cmd  := Mux(state === s_set_dirty, M_XA_OR, M_XRD)
  io.mem.req.bits.typ  := log2Ceil(xLen/8)
  io.mem.req.bits.addr := pte_addr// 一级页表的
  io.mem.s1_data := pte_wdata.asUInt
  io.mem.s1_kill := s1_kill
  io.mem.invalidate_lr := Bool(false)
  
  val resp_ppns = (0 until pgLevels-1).map(i => Cat(pte_addr >> (pgIdxBits + pgLevelBits*(pgLevels-i-1)), r_req.addr(pgLevelBits*(pgLevels-i-1)-1,0))) :+ (pte_addr >> pgIdxBits)
  for (i <- 0 until io.requestor.size) {
    io.requestor(i).resp.valid := state === s_done && (r_req_dest === i)
    io.requestor(i).resp.bits.pte := r_pte // 返回
    io.requestor(i).resp.bits.pte.ppn := resp_ppns(count)// 感觉是多余的？
    io.requestor(i).ptbr := io.dpath.ptbr
    io.requestor(i).invalidate := io.dpath.invalidate
    io.requestor(i).status := io.dpath.status
  }

  // control state machine
  switch (state) {
    is (s_ready) {
      when (arb.io.out.valid) {
        state := s_req
      }
      count := UInt(0)
    }
    is (s_req) {
      when (pte_cache_hit) {
        s1_kill := true // 如果缓存中存在，则取消前一周期的请求（即取消本次请求）
        state := s_req // 并查找下一级页表项
        count := count + 1
        r_pte.ppn := pte_cache_data
      }.elsewhen (io.mem.req.ready) { // 内存准备好接收请求
        state := s_wait1
      }
    }
    is (s_wait1) {
      state := s_wait2
      when (io.mem.xcpt.pf.ld) { // 内存响应Load异常，返回TLB，并将有效位V置为0
        r_pte.v := false
        state := s_done 
      }
    }
    is (s_wait2) {
      when (io.mem.s2_nack) { // 内存两个周期前的请求没有响应（即对于本次请求，内存响应s2_nack————s_req时的那次请求）
        state := s_req // 于是重新请求
      }
      when (io.mem.resp.valid) { // 内存响应请求
        state := s_done
        when (pte.access_ok(r_req) && (!pte.a || (r_req.store && !pte.d))) { // 叶子页表项，但是还未设置A，D位
          state := s_set_dirty
        }.otherwise {
          r_pte := pte // 叶子页表项，且已经设置A，D位，可以返回给TLB了
        }
        when (pte.table() && count < pgLevels-1) { // 非叶子页表项
          state := s_req // 继续下一级页表项的请求
          count := count + 1
        }
      }
    }
    is (s_set_dirty) {
      when (io.mem.req.ready) { // 内存准备好接收请求
        state := s_wait1_dirty
      }
    }
    is (s_wait1_dirty) {
      state := s_wait2_dirty
      when (io.mem.xcpt.pf.st) { // 内存响应store异常，返回TLB，并将有效位置为0
        r_pte.v := false
        state := s_done
      }
    }
    is (s_wait2_dirty) {
      when (io.mem.s2_nack) { // 内存两个周期前的请求没有响应（即对于本次请求，内存响应s2_nack————s_set_dirty时的那次请求）
        state := s_set_dirty
      }
      when (io.mem.resp.valid) { //内存响应有效
        state := s_req // 重写A, D之后，进行最后一轮的叶子页表项读取
      }
    }
    is (s_done) {
      state := s_ready
    }
  }
}
