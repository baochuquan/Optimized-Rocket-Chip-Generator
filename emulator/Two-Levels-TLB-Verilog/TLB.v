module TLB(
  input   clock,
  input   reset,
  output  io_req_ready,//????????
  input   io_req_valid,//??????
  input  [27:0] io_req_bits_vpn,//?????27???????????????28?
  input   io_req_bits_passthrough,
  input   io_req_bits_instruction,//??????????
  input   io_req_bits_store,//????store???????
  output  io_resp_miss,//TLB??
  output [19:0] io_resp_ppn,
  output  io_resp_xcpt_ld,//load??
  output  io_resp_xcpt_st,//store??
  output  io_resp_xcpt_if,//????
  output  io_resp_cacheable,//?????
  input   io_ptw_req_ready,//ptw??????
  output  io_ptw_req_valid,//TLB??????
  output [1:0] io_ptw_req_bits_prv,
  output  io_ptw_req_bits_pum,
  output  io_ptw_req_bits_mxr,
  output [26:0] io_ptw_req_bits_addr,//??????27+7=34??????27?
  output  io_ptw_req_bits_store,
  output  io_ptw_req_bits_fetch,
  input   io_ptw_resp_valid,//ptw???????
  input  [15:0] io_ptw_resp_bits_pte_reserved_for_hardware,
  input  [37:0] io_ptw_resp_bits_pte_ppn,//?????20??????????TLB???20?
  input  [1:0] io_ptw_resp_bits_pte_reserved_for_software,
  input   io_ptw_resp_bits_pte_d,
  input   io_ptw_resp_bits_pte_a,
  input   io_ptw_resp_bits_pte_g,
  input   io_ptw_resp_bits_pte_u,
  input   io_ptw_resp_bits_pte_x,
  input   io_ptw_resp_bits_pte_w,
  input   io_ptw_resp_bits_pte_r,
  input   io_ptw_resp_bits_pte_v,
  input  [6:0] io_ptw_ptbr_asid,
  input  [37:0] io_ptw_ptbr_ppn,
  input   io_ptw_invalidate,
  input   io_ptw_status_debug,
  input  [31:0] io_ptw_status_isa,
  input  [1:0] io_ptw_status_prv,
  input   io_ptw_status_sd,
  input  [30:0] io_ptw_status_zero3,
  input   io_ptw_status_sd_rv32,
  input  [1:0] io_ptw_status_zero2,
  input  [4:0] io_ptw_status_vm,
  input  [3:0] io_ptw_status_zero1,
  input   io_ptw_status_mxr,
  input   io_ptw_status_pum,
  input   io_ptw_status_mprv,
  input  [1:0] io_ptw_status_xs,
  input  [1:0] io_ptw_status_fs,
  input  [1:0] io_ptw_status_mpp,
  input  [1:0] io_ptw_status_hpp,
  input   io_ptw_status_spp,
  input   io_ptw_status_mpie,
  input   io_ptw_status_hpie,
  input   io_ptw_status_spie,
  input   io_ptw_status_upie,
  input   io_ptw_status_mie,
  input   io_ptw_status_hie,
  input   io_ptw_status_sie,
  input   io_ptw_status_uie
  );
  wire io_l2tlb_req_ready;//ptw??????
  wire io_l2tlb_req_valid;//TLB??????
  wire [1:0] io_l2tlb_req_bits_prv;
  wire io_l2tlb_req_bits_pum;
  wire io_l2tlb_req_bits_mxr;
  wire [26:0] io_l2tlb_req_bits_addr;//??????27+7=34??????27?
  wire io_l2tlb_req_bits_store;
  wire io_l2tlb_req_bits_fetch;
  wire io_l2tlb_resp_valid;//ptw???????
  wire [15:0] io_l2tlb_resp_bits_pte_reserved_for_hardware;
  wire [37:0] io_l2tlb_resp_bits_pte_ppn;//?????20??????????TLB???20?
  wire [1:0] io_l2tlb_resp_bits_pte_reserved_for_software;
  wire io_l2tlb_resp_bits_pte_d;
  wire io_l2tlb_resp_bits_pte_a;
  wire io_l2tlb_resp_bits_pte_g;
  wire io_l2tlb_resp_bits_pte_u;
  wire io_l2tlb_resp_bits_pte_x;
  wire io_l2tlb_resp_bits_pte_w;
  wire io_l2tlb_resp_bits_pte_r;
  wire io_l2tlb_resp_bits_pte_v;
  wire tlb_miss;
  wire bad_va;
  wire vm_enabled;

L1_TLB i_l1_tlb(
  .clock( clock ),
  .reset( reset ),
  .io_req_ready( io_req_ready ),//????????
  .io_req_valid( io_req_valid ),//??????
  .io_req_bits_vpn( io_req_bits_vpn ),//?????27???????????????28?
  .io_req_bits_passthrough( io_req_bits_passthrough ),
  .io_req_bits_instruction( io_req_bits_instruction ),//??????????
  .io_req_bits_store( io_req_bits_store ),//????store???????
  .io_resp_miss( io_resp_miss ),//TLB??
  .io_resp_ppn( io_resp_ppn ),
  .io_resp_xcpt_ld( io_resp_xcpt_ld ),//load??
  .io_resp_xcpt_st( io_resp_xcpt_st ),//store??
  .io_resp_xcpt_if( io_resp_xcpt_if ),//????
  .io_resp_cacheable( io_resp_cacheable ),//?????
  .io_l2tlb_req_ready( io_l2tlb_req_ready ),//ptw??????
  .io_l2tlb_req_valid( io_l2tlb_req_valid ),//TLB??????
  .io_l2tlb_req_bits_prv( io_l2tlb_req_bits_prv ),
  .io_l2tlb_req_bits_pum( io_l2tlb_req_bits_pum ),
  .io_l2tlb_req_bits_mxr( io_l2tlb_req_bits_mxr ),
  .io_l2tlb_req_bits_addr( io_l2tlb_req_bits_addr ),//??????27+7=34??????27?
  .io_l2tlb_req_bits_store( io_l2tlb_req_bits_store ),
  .io_l2tlb_req_bits_fetch( io_l2tlb_req_bits_fetch ),
  .io_l2tlb_resp_valid( io_l2tlb_resp_valid ),//ptw???????
  .io_l2tlb_resp_bits_pte_reserved_for_hardware( io_l2tlb_resp_bits_pte_reserved_for_hardware ),
  .io_l2tlb_resp_bits_pte_ppn( io_l2tlb_resp_bits_pte_ppn ),//?????20??????????TLB???20?
  .io_l2tlb_resp_bits_pte_reserved_for_software( io_l2tlb_resp_bits_pte_reserved_for_software ),
  .io_l2tlb_resp_bits_pte_d( io_l2tlb_resp_bits_pte_d ),
  .io_l2tlb_resp_bits_pte_a( io_l2tlb_resp_bits_pte_a ),
  .io_l2tlb_resp_bits_pte_g( io_l2tlb_resp_bits_pte_g ),
  .io_l2tlb_resp_bits_pte_u( io_l2tlb_resp_bits_pte_u ),
  .io_l2tlb_resp_bits_pte_x( io_l2tlb_resp_bits_pte_x ),
  .io_l2tlb_resp_bits_pte_w( io_l2tlb_resp_bits_pte_w ),
  .io_l2tlb_resp_bits_pte_r( io_l2tlb_resp_bits_pte_r ),
  .io_l2tlb_resp_bits_pte_v( io_l2tlb_resp_bits_pte_v ),
  .io_ptw_ptbr_asid( io_ptw_ptbr_asid ),
  .io_ptw_ptbr_ppn( io_ptw_ptbr_ppn ),
  .io_ptw_invalidate( io_ptw_invalidate ),
  .io_ptw_status_debug( io_ptw_status_debug ),
  .io_ptw_status_isa( io_ptw_status_isa ),
  .io_ptw_status_prv( io_ptw_status_prv ),
  .io_ptw_status_sd( io_ptw_status_sd ),
  .io_ptw_status_zero3( io_ptw_status_zero3 ),
  .io_ptw_status_sd_rv32( io_ptw_status_sd_rv32 ),
  .io_ptw_status_zero2( io_ptw_status_zero2 ),
  .io_ptw_status_vm( io_ptw_status_vm ),
  .io_ptw_status_zero1( io_ptw_status_zero1 ),
  .io_ptw_status_mxr( io_ptw_status_mxr ),
  .io_ptw_status_pum( io_ptw_status_pum ),
  .io_ptw_status_mprv( io_ptw_status_mprv ),
  .io_ptw_status_xs( io_ptw_status_xs ),
  .io_ptw_status_fs( io_ptw_status_fs ),
  .io_ptw_status_mpp( io_ptw_status_mpp ),
  .io_ptw_status_hpp( io_ptw_status_hpp ),
  .io_ptw_status_spp( io_ptw_status_spp ),
  .io_ptw_status_mpie( io_ptw_status_mpie ),
  .io_ptw_status_hpie( io_ptw_status_hpie ),
  .io_ptw_status_spie( io_ptw_status_spie ),
  .io_ptw_status_upie( io_ptw_status_upie ),
  .io_ptw_status_mie( io_ptw_status_mie ),
  .io_ptw_status_hie( io_ptw_status_hie ),
  .io_ptw_status_sie( io_ptw_status_sie ),
  .io_ptw_status_uie( io_ptw_status_uie ),

  .tlb_miss( tlb_miss ),
  .bad_va( bad_va ),
  .vm_enabled( vm_enabled )
  );

L2_TLB i_l2_tlb(
  .clock( clock ),
  .reset( reset ),

  .io_req_valid( io_req_valid ),
  .io_req_bits_vpn( io_req_bits_vpn ),//?????27???????????????28?
  .io_req_bits_store( io_req_bits_store ),
  .io_req_bits_instruction( io_req_bits_instruction ),

  .io_ptw_req_ready( io_ptw_req_ready ),//ptw??????
  .io_ptw_req_valid( io_ptw_req_valid ),//TLB??????
  .io_ptw_req_bits_prv( io_ptw_req_bits_prv ),
  .io_ptw_req_bits_pum( io_ptw_req_bits_pum ),
  .io_ptw_req_bits_mxr( io_ptw_req_bits_mxr ),
  .io_ptw_req_bits_addr( io_ptw_req_bits_addr ),//??????27+7=34??????27?
  .io_ptw_req_bits_store( io_ptw_req_bits_store ),
  .io_ptw_req_bits_fetch( io_ptw_req_bits_fetch ),
  .io_ptw_resp_valid( io_ptw_resp_valid ),//ptw???????
  .io_ptw_resp_bits_pte_reserved_for_hardware( io_ptw_resp_bits_pte_reserved_for_hardware ),
  .io_ptw_resp_bits_pte_ppn( io_ptw_resp_bits_pte_ppn ),//?????20??????????TLB???20?
  .io_ptw_resp_bits_pte_reserved_for_software( io_ptw_resp_bits_pte_reserved_for_software ),
  .io_ptw_resp_bits_pte_d( io_ptw_resp_bits_pte_d ),
  .io_ptw_resp_bits_pte_a( io_ptw_resp_bits_pte_a ),
  .io_ptw_resp_bits_pte_g( io_ptw_resp_bits_pte_g ),
  .io_ptw_resp_bits_pte_u( io_ptw_resp_bits_pte_u ),
  .io_ptw_resp_bits_pte_x( io_ptw_resp_bits_pte_x ),
  .io_ptw_resp_bits_pte_w( io_ptw_resp_bits_pte_w ),
  .io_ptw_resp_bits_pte_r( io_ptw_resp_bits_pte_r ),
  .io_ptw_resp_bits_pte_v( io_ptw_resp_bits_pte_v ),
  .io_ptw_ptbr_asid( io_ptw_ptbr_asid ),
  .io_ptw_ptbr_ppn( io_ptw_ptbr_ppn ),
  .io_ptw_invalidate( io_ptw_invalidate ),
  .io_ptw_status_debug( io_ptw_status_debug),
  .io_ptw_status_isa( io_ptw_status_isa ),
  .io_ptw_status_prv( io_ptw_status_prv ),
  .io_ptw_status_sd( io_ptw_status_sd ),
  .io_ptw_status_zero3( io_ptw_status_zero3 ),
  .io_ptw_status_sd_rv32( io_ptw_status_sd_rv32 ),
  .io_ptw_status_zero2( io_ptw_status_zero2 ),
  .io_ptw_status_vm( io_ptw_status_vm ),
  .io_ptw_status_zero1( io_ptw_status_zero1 ),
  .io_ptw_status_mxr( io_ptw_status_mxr ),
  .io_ptw_status_pum( io_ptw_status_pum ),
  .io_ptw_status_mprv( io_ptw_status_mprv ),
  .io_ptw_status_xs( io_ptw_status_xs ),
  .io_ptw_status_fs( io_ptw_status_fs ),
  .io_ptw_status_mpp( io_ptw_status_mpp ),
  .io_ptw_status_hpp( io_ptw_status_hpp ),
  .io_ptw_status_spp( io_ptw_status_spp ),
  .io_ptw_status_mpie( io_ptw_status_mpie ),
  .io_ptw_status_hpie( io_ptw_status_hpie ),
  .io_ptw_status_spie( io_ptw_status_spie ),
  .io_ptw_status_upie( io_ptw_status_upie ),
  .io_ptw_status_mie( io_ptw_status_mie ),
  .io_ptw_status_hie( io_ptw_status_hie ),
  .io_ptw_status_sie( io_ptw_status_sie ),
  .io_ptw_status_uie( io_ptw_status_uie ),

  .io_l2tlb_req_ready( io_l2tlb_req_ready ),//ptw??????
  .io_l2tlb_req_valid( io_l2tlb_req_valid ),//TLB??????
  .io_l2tlb_req_bits_prv( io_l2tlb_req_bits_prv ),
  .io_l2tlb_req_bits_pum( io_l2tlb_req_bits_pum ),
  .io_l2tlb_req_bits_mxr( io_l2tlb_req_bits_mxr ),
  .io_l2tlb_req_bits_addr( io_l2tlb_req_bits_addr ),//??????27+7=34??????27?
  .io_l2tlb_req_bits_store( io_l2tlb_req_bits_store ),
  .io_l2tlb_req_bits_fetch( io_l2tlb_req_bits_fetch ),

  .io_l2tlb_resp_valid( io_l2tlb_resp_valid ),//ptw???????
  .io_l2tlb_resp_bits_pte_reserved_for_hardware( io_l2tlb_resp_bits_pte_reserved_for_hardware ),
  .io_l2tlb_resp_bits_pte_ppn( io_l2tlb_resp_bits_pte_ppn ),//?????20??????????TLB???20?
  .io_l2tlb_resp_bits_pte_reserved_for_software( io_l2tlb_resp_bits_pte_reserved_for_software ),
  .io_l2tlb_resp_bits_pte_d( io_l2tlb_resp_bits_pte_d ),
  .io_l2tlb_resp_bits_pte_a( io_l2tlb_resp_bits_pte_a ),
  .io_l2tlb_resp_bits_pte_g( io_l2tlb_resp_bits_pte_g ),
  .io_l2tlb_resp_bits_pte_u( io_l2tlb_resp_bits_pte_u ),
  .io_l2tlb_resp_bits_pte_x( io_l2tlb_resp_bits_pte_x ),
  .io_l2tlb_resp_bits_pte_w( io_l2tlb_resp_bits_pte_x ),
  .io_l2tlb_resp_bits_pte_r( io_l2tlb_resp_bits_pte_r ),
  .io_l2tlb_resp_bits_pte_v( io_l2tlb_resp_bits_pte_v ),
  .tlb_miss( tlb_miss ),
  .bad_va( bad_va ),
  .vm_enabled( vm_enabled ),
  .priv_s( priv_s )
  );

endmodule