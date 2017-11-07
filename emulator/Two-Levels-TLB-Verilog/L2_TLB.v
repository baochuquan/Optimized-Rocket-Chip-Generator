module L2_TLB(
	input   clock,
  	input   reset,

  	input  io_req_valid,
  	input  [27:0] io_req_bits_vpn,//?????27???????????????28?
  	input  io_req_bits_store,
	input  io_req_bits_instruction,

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
	input   io_ptw_status_uie,

	output   io_l2tlb_req_ready,//ptw??????
	input  io_l2tlb_req_valid,//TLB??????
	input [1:0] io_l2tlb_req_bits_prv,
	input  io_l2tlb_req_bits_pum,
	input  io_l2tlb_req_bits_mxr,
	input [26:0] io_l2tlb_req_bits_addr,//??????27+7=34??????27?
	input  io_l2tlb_req_bits_store,
	input  io_l2tlb_req_bits_fetch,

	output   io_l2tlb_resp_valid,//ptw???????
	output  [15:0] io_l2tlb_resp_bits_pte_reserved_for_hardware,
	output  [37:0] io_l2tlb_resp_bits_pte_ppn,//?????20??????????TLB???20?
	output  [1:0] io_l2tlb_resp_bits_pte_reserved_for_software,
	output   io_l2tlb_resp_bits_pte_d,
	output   io_l2tlb_resp_bits_pte_a,
	output   io_l2tlb_resp_bits_pte_g,
	output   io_l2tlb_resp_bits_pte_u,
	output   io_l2tlb_resp_bits_pte_x,
	output   io_l2tlb_resp_bits_pte_w,
	output   io_l2tlb_resp_bits_pte_r,
	output   io_l2tlb_resp_bits_pte_v,
	input tlb_miss,
	input bad_va,
	input vm_enabled,
	input priv_s
    );
	parameter
    	S_ready           = 2'h0,
    	S_request         = 2'h1,
    	S_wait            = 2'h2,
    	S_wait_invalidate = 2'h3;


	reg [63:0] valid_way0;
	reg [63:0] valid_way1;
	reg [63:0] valid_way2;
	reg [63:0] valid_way3;
	reg [33:0] asid_vpn_way0[63:0];
	reg [33:0] asid_vpn_way1[63:0];
	reg [33:0] asid_vpn_way2[63:0];
	reg [33:0] asid_vpn_way3[63:0];
	reg [19:0] ppns_way0[63:0];
	reg [19:0] ppns_way1[63:0];
	reg [19:0] ppns_way2[63:0];
	reg [19:0] ppns_way3[63:0];
	reg [63:0] u_array_way0;
	reg [63:0] u_array_way1;
	reg [63:0] u_array_way2;
	reg [63:0] u_array_way3;
	reg [63:0] sw_array_way0;
	reg [63:0] sw_array_way1;
	reg [63:0] sw_array_way2;
	reg [63:0] sw_array_way3;
	reg [63:0] d_array_way0;
	reg [63:0] d_array_way1;
	reg [63:0] d_array_way2;
	reg [63:0] d_array_way3;
	reg [63:0] r_array_way0;
	reg [63:0] r_array_way1;
	reg [63:0] r_array_way2;
	reg [63:0] r_array_way3;
	reg [63:0] x_array_way0;
	reg [63:0] x_array_way1;
	reg [63:0] x_array_way2;
	reg [63:0] x_array_way3;
	reg [63:0] w_array_way0;
	reg [63:0] w_array_way1;
	reg [63:0] w_array_way2;
	reg [63:0] w_array_way3;

	reg [3:0] L2_plru_val[63:0];

	reg [1:0] state;
	reg [33:0] r_refill_asid_vpn;
    reg [1:0]  r_refill_waddr;
    reg r_req_instruction;
    reg r_req_store; 

	wire [5:0] index;

	// for L2_tlb_lookup output
	wire [4:0] hitsVec;
	wire L2_tlb_miss;
	// for L2_tlb_item_generate
	wire [19:0] L2_resp_ppn;
	wire L2_resp_v;
	wire L2_resp_u;
	wire L2_resp_w;
	wire L2_resp_x;
	wire L2_resp_r;
	wire L2_resp_d;
	// for L2_tlb_plru
	wire [3:0] L2_new_plru_val;
	// for L2_tlb_req_trans
	// for L2_tlb_addr_prot
	wire [19:0] passthrough_ppn;
	wire prot_w;
	// for L2_tlb_attri_change
	wire [3:0] new_valid;
	wire [3:0] new_u_array;
	wire [3:0] new_sw_array;
	wire [3:0] new_d_array;
	wire [3:0] new_w_array;
	wire [3:0] new_r_array;
	wire [3:0] new_x_array;
	// for L2_tlb_replace
	wire [1:0] repl_waddr;
	// for L2_tlb_return_arbi

	integer i;

	assign index = io_req_bits_vpn[5:0];

	L2_tlb_lookup i_tlb_lookup(
		.io_ptw_ptbr_asid( io_ptw_ptbr_asid ),
		.io_ptw_status_pum( io_ptw_status_pum ),
		.io_req_bits_vpn( io_req_bits_vpn ),
		.io_req_bits_store( io_req_bits_store ),
		.tags_way0_idx( asid_vpn_way0[index][33:6] ),
		.tags_way1_idx( asid_vpn_way1[index][33:6] ),
		.tags_way2_idx( asid_vpn_way2[index][33:6] ),
		.tags_way3_idx( asid_vpn_way3[index][33:6] ),
		.valid_way0_idx( valid_way0[index] ),
		.valid_way1_idx( valid_way1[index] ),
		.valid_way2_idx( valid_way2[index] ),
		.valid_way3_idx( valid_way3[index] ),
		.u_array_way0_idx( u_array_way0[index] ),
		.u_array_way1_idx( u_array_way1[index] ),
		.u_array_way2_idx( u_array_way2[index] ),
		.u_array_way3_idx( u_array_way3[index] ),
		.sw_array_way0_idx( sw_array_way0[index] ),
		.sw_array_way1_idx( sw_array_way1[index] ),
		.sw_array_way2_idx( sw_array_way2[index] ),
		.sw_array_way3_idx( sw_array_way3[index] ),
		.d_array_way0_idx( d_array_way0[index] ),
		.d_array_way1_idx( d_array_way1[index] ),
		.d_array_way2_idx( d_array_way2[index]),
		.d_array_way3_idx( d_array_way3[index] ),
		.vm_enabled( vm_enabled ),
		.bad_va( bad_va ),
		.priv_s( priv_s ),
		.prot_w( prot_w ),

		.hitsVec( hitsVec ),
		.L2_tlb_miss( L2_tlb_miss )
	    );
	L2_tlb_item_generate i_tlb_item_generate(
		.hitsVec( hitsVec ),
		.ppns_way0_idx( ppns_way0[index] ),
		.ppns_way1_idx( ppns_way1[index] ),
		.ppns_way2_idx( ppns_way2[index] ),
		.ppns_way3_idx( ppns_way3[index] ),
		.passthrough_ppn( passthrough_ppn ),
		.valid_way0_idx( valid_way0[index] ),
		.valid_way1_idx( valid_way1[index] ),
		.valid_way2_idx( valid_way2[index] ),
		.valid_way3_idx( valid_way3[index] ),
		.u_array_way0_idx( u_array_way0[index] ),
		.u_array_way1_idx( u_array_way1[index] ),
		.u_array_way2_idx( u_array_way2[index] ),
		.u_array_way3_idx( u_array_way3[index] ),
		.w_array_way0_idx( w_array_way0[index] ),
		.w_array_way1_idx( w_array_way1[index] ),
		.w_array_way2_idx( w_array_way2[index] ),
		.w_array_way3_idx( w_array_way3[index] ),
		.x_array_way0_idx( x_array_way0[index] ),
		.x_array_way1_idx( x_array_way1[index] ),
		.x_array_way2_idx( x_array_way2[index] ),
		.x_array_way3_idx( x_array_way3[index] ),
		.r_array_way0_idx( r_array_way0[index] ),
		.r_array_way1_idx( r_array_way1[index] ),
		.r_array_way2_idx( r_array_way2[index] ),
		.r_array_way3_idx( r_array_way3[index] ),
		.d_array_way0_idx( d_array_way0[index] ),
		.d_array_way1_idx( d_array_way1[index] ),
		.d_array_way2_idx( d_array_way2[index] ),
		.d_array_way3_idx( d_array_way3[index] ),

		.L2_resp_ppn( L2_resp_ppn ),
		.L2_resp_v( L2_resp_v ),
		.L2_resp_u( L2_resp_u ),
		.L2_resp_w( L2_resp_w ),
		.L2_resp_x( L2_resp_x ),
		.L2_resp_r( L2_resp_r ),
		.L2_resp_d( L2_resp_d )
	    );
	L2_tlb_plru i_tlb_plru(
		.hitsVec( hitsVec ),
		.L2_plru_val( L2_plru_val[index] ),

		.L2_new_plru_val( L2_new_plru_val )
	    );
	L2_tlb_req_trans i_tlb_req_trans(
		.state( state ),
		.r_refill_asid_vpn( r_refill_asid_vpn ),
		.r_req_instruction( r_req_instruction ),
		.r_req_store( r_req_store ),
		.io_ptw_status_pum( io_ptw_status_pum ),
		.io_ptw_status_mxr( io_ptw_status_mxr ),
		.io_ptw_status_prv( io_ptw_status_prv ),

		.io_req_ready( io_req_ready ),
		.io_ptw_req_valid( io_ptw_req_valid ),
		.io_ptw_req_bits_addr( io_ptw_req_bits_addr ),
		.io_ptw_req_bits_fetch( io_ptw_req_bits_fetch ),
		.io_ptw_req_bits_store( io_ptw_req_bits_store ),
		.io_ptw_req_bits_pum( io_ptw_req_bits_pum ),
		.io_ptw_req_bits_mxr( io_ptw_req_bits_mxr ),
		.io_ptw_req_bits_prv( io_ptw_req_bits_prv )
	    );
	L2_tlb_addr_prot i_tlb_addr_prot(
		.io_ptw_resp_valid( io_ptw_resp_valid ),
		.io_req_bits_vpn( io_req_bits_vpn ),
		.io_ptw_resp_bits_pte_ppn( io_ptw_resp_bits_pte_ppn ),

		.passthrough_ppn( passthrough_ppn ),
		.prot_w( prot_w )
	    );
	L2_tlb_attri_change i_tlb_attri_change(
		.r_refill_waddr( r_refill_waddr ),
		.io_ptw_invalidate( io_ptw_invalidate ),
		.io_ptw_resp_valid( io_ptw_resp_valid ),
		.io_ptw_resp_bits_pte_v( io_ptw_resp_bits_pte_v ),
		.io_ptw_resp_bits_pte_u( io_ptw_resp_bits_pte_u ),
		.io_ptw_resp_bits_pte_w( io_ptw_resp_bits_pte_w ),
		.io_ptw_resp_bits_pte_x( io_ptw_resp_bits_pte_x ),
		.io_ptw_resp_bits_pte_r( io_ptw_resp_bits_pte_r ),
		.io_ptw_resp_bits_pte_d( io_ptw_resp_bits_pte_d ),
		.valid_way0_idx( valid_way0[index] ),
		.valid_way1_idx( valid_way1[index] ),
		.valid_way2_idx( valid_way2[index] ),
		.valid_way3_idx( valid_way3[index] ),
		.u_array_way0_idx( u_array_way0[index] ),
		.u_array_way1_idx( u_array_way1[index] ),
		.u_array_way2_idx( u_array_way2[index] ),
		.u_array_way3_idx( u_array_way3[index] ),
		.sw_array_way0_idx( sw_array_way0[index] ),
		.sw_array_way1_idx( sw_array_way1[index] ),
		.sw_array_way2_idx( sw_array_way2[index] ),
		.sw_array_way3_idx( sw_array_way3[index] ),
		.d_array_way0_idx( d_array_way0[index] ),
		.d_array_way1_idx( d_array_way1[index] ),
		.d_array_way2_idx( d_array_way2[index] ),
		.d_array_way3_idx( d_array_way3[index] ),
		.w_array_way0_idx( w_array_way0[index] ),
		.w_array_way1_idx( w_array_way1[index] ),
		.w_array_way2_idx( w_array_way2[index] ),
		.w_array_way3_idx( w_array_way3[index] ),
		.r_array_way0_idx( r_array_way0[index] ),
		.r_array_way1_idx( r_array_way1[index] ),
		.r_array_way2_idx( r_array_way2[index] ),
		.r_array_way3_idx( r_array_way3[index] ),
		.x_array_way0_idx( x_array_way0[index] ),
		.x_array_way1_idx( x_array_way1[index] ),
		.x_array_way2_idx( x_array_way2[index] ),
		.x_array_way3_idx( x_array_way3[index] ),
		.prot_w( prot_w ),

		.new_valid( new_valid ),
		.new_u_array( new_u_array ),
		.new_sw_array( new_sw_array ),
		.new_d_array( new_d_array ),
		.new_w_array(new_w_array ),
		.new_r_array( new_r_array ),
		.new_x_array( new_x_array )
	    );
	L2_tlb_replace i_tlb_replace(
		.valid_way0_idx( valid_way0[index] ),
		.valid_way1_idx( valid_way1[index] ),
		.valid_way2_idx( valid_way2[index] ),
		.valid_way3_idx( valid_way3[index] ),
		.L2_plru_val( L2_plru_val[index] ),

		.repl_waddr( repl_waddr )
	    );
	L2_tlb_return_arbi i_tlb_return_arbi(
		.L2_tlb_miss( L2_tlb_miss ),
		.L2_resp_v( L2_resp_v ),
		.L2_resp_u( L2_resp_u ),
		.L2_resp_w( L2_resp_w ),
		.L2_resp_x( L2_resp_x ),
		.L2_resp_r( L2_resp_r ),
		.L2_resp_d( L2_resp_d ),
		.L2_resp_ppn( L2_resp_ppn ),

		.io_ptw_req_ready( io_ptw_req_ready ),
		.io_ptw_resp_valid( io_ptw_resp_valid ),
		.io_ptw_resp_bits_pte_v( io_ptw_resp_bits_pte_v ),
		.io_ptw_resp_bits_pte_u( io_ptw_resp_bits_pte_u ),
		.io_ptw_resp_bits_pte_w( io_ptw_resp_bits_pte_w ),
		.io_ptw_resp_bits_pte_x( io_ptw_resp_bits_pte_x ),
		.io_ptw_resp_bits_pte_r( io_ptw_resp_bits_pte_r ),
		.io_ptw_resp_bits_pte_d( io_ptw_resp_bits_pte_d ),
		.io_ptw_resp_bits_pte_ppn( io_ptw_resp_bits_pte_ppn ),
		.io_ptw_resp_bits_pte_a( io_ptw_resp_bits_pte_a ),
		.io_ptw_resp_bits_pte_g( io_ptw_resp_bits_pte_g ),
		.io_ptw_resp_bits_pte_reserved_for_hardware( io_ptw_resp_bits_pte_reserved_for_hardware ),
		.io_ptw_resp_bits_pte_reserved_for_software( io_ptw_resp_bits_pte_reserved_for_software ),

		.io_l2tlb_req_ready( io_l2tlb_req_ready ),
		.io_l2tlb_resp_valid( io_l2tlb_resp_valid ),
		.io_l2tlb_resp_bits_pte_v( io_l2tlb_resp_bits_pte_v ),
		.io_l2tlb_resp_bits_pte_u( io_l2tlb_resp_bits_pte_u ),
		.io_l2tlb_resp_bits_pte_w( io_l2tlb_resp_bits_pte_w ),
		.io_l2tlb_resp_bits_pte_x( io_l2tlb_resp_bits_pte_x ),
		.io_l2tlb_resp_bits_pte_r( io_l2tlb_resp_bits_pte_r ),
		.io_l2tlb_resp_bits_pte_d( io_l2tlb_resp_bits_pte_d ),
		.io_l2tlb_resp_bits_pte_ppn( io_l2tlb_resp_bits_pte_ppn ),
		.io_l2tlb_resp_bits_pte_a( io_l2tlb_resp_bits_pte_a ),
		.io_l2tlb_resp_bits_pte_g( io_l2tlb_resp_bits_pte_g ),
		.io_l2tlb_resp_bits_pte_reserved_for_hardware( io_l2tlb_resp_bits_pte_reserved_for_hardware ),
		.io_l2tlb_resp_bits_pte_reserved_for_software( io_l2tlb_resp_bits_pte_reserved_for_software )
	    );
	always @(posedge clock) begin
    // 状态机    
    if( reset ) begin // 状态机控制，控制状态转移
      state <= S_ready; 
    end else begin
      if( io_ptw_resp_valid ) begin
        state <= S_ready; 
      end else begin
        if( state == S_wait && io_ptw_invalidate ) begin // wait && io_ptw_invalidate
          state <= S_wait_invalidate; 
        end else begin
          if( state == S_request ) begin // state == request 1
            if( io_ptw_req_ready ) begin 
              if( io_ptw_invalidate ) begin //
                state <= S_wait_invalidate; // state <= wait_invalidate
              end else begin
                state <= S_wait;
              end
            end else begin
              if( io_ptw_invalidate ) begin // 
                state <= S_ready; 
              end/* else begin
                if( state == S_ready && io_req_valid && L2_tlb_miss ) begin // io_req_ready && io_req_valid && tlb_miss
                  state <= S_request; 
                end
              end*/
            end
          end else begin
            if( state == S_ready && io_req_valid && L2_tlb_miss ) begin // to deal state != request 
              state <= S_request; 
            end
          end
        end
      end
    end

    if( state == S_ready && io_req_valid && L2_tlb_miss ) begin // io_req_ready && io_req_valid && tlb_miss
      	r_refill_asid_vpn <= { io_ptw_ptbr_asid, io_req_bits_vpn[26:0] }; 
      	r_refill_waddr    <= repl_waddr; // 与plru_val寄存器有关
      	r_req_instruction <= io_req_bits_instruction;
      	r_req_store       <= io_req_bits_store;
    end

    // 更新伪LRU寄存器
    if( io_req_valid && !tlb_miss )
      L2_plru_val[index] <= L2_new_plru_val;

    if(reset) begin
      	valid_way0 <= 64'h0;
		valid_way1 <= 64'h0;
		valid_way2 <= 64'h0;
		valid_way3 <= 64'h0;
		u_array_way0 <= 64'h0;
		u_array_way1 <= 64'h0;
		u_array_way2 <= 64'h0;
		u_array_way3 <= 64'h0;
		sw_array_way0 <= 64'h0;
		sw_array_way1 <= 64'h0;
		sw_array_way2 <= 64'h0;
		sw_array_way3 <= 64'h0;
		x_array_way0 <= 64'h0;
		x_array_way1 <= 64'h0;
		x_array_way2 <= 64'h0;
		x_array_way3 <= 64'h0;
		r_array_way0 <= 64'h0;
		r_array_way1 <= 64'h0;
		r_array_way2 <= 64'h0;
		r_array_way3 <= 64'h0;
		w_array_way0 <= 64'h0;
		w_array_way1 <= 64'h0;
		w_array_way2 <= 64'h0;
		w_array_way3 <= 64'h0;
		d_array_way0 <= 64'h0;
		d_array_way1 <= 64'h0;
		d_array_way2 <= 64'h0;
		d_array_way3 <= 64'h0;
		for(i = 0; i < 64; i = i + 1) begin
			ppns_way0[i] <= 20'h0;
			ppns_way1[i] <= 20'h0;
			ppns_way2[i] <= 20'h0;
			ppns_way3[i] <= 20'h0;
		end
		for(i = 0; i < 64; i = i + 1) begin
			asid_vpn_way0[i] <= 20'h0;
			asid_vpn_way1[i] <= 20'h0;
			asid_vpn_way2[i] <= 20'h0;
			asid_vpn_way3[i] <= 20'h0;
		end
    end else begin
      	valid_way0[index] <= new_valid[0];
		valid_way1[index] <= new_valid[1];
		valid_way2[index] <= new_valid[2];
		valid_way3[index] <= new_valid[3];
		// 对应表项的属性为设置，说明：更新的值已经考虑了io_l2tlb_resp_valid = 0的情况，所以不用写入条件判断之中
	    u_array_way0[index]  <= new_u_array[0];
	    u_array_way1[index]  <= new_u_array[1];
	    u_array_way2[index]  <= new_u_array[2];
	    u_array_way3[index]  <= new_u_array[3];
	    sw_array_way0[index] <= new_sw_array[0];
	    sw_array_way1[index] <= new_sw_array[1];
	    sw_array_way2[index] <= new_sw_array[2];
	    sw_array_way3[index] <= new_sw_array[3];
	    x_array_way0[index]  <= new_x_array[0];
	    x_array_way1[index]  <= new_x_array[1];
	    x_array_way2[index]  <= new_x_array[2];
	    x_array_way3[index]  <= new_x_array[3];
	    r_array_way0[index]  <= new_r_array[0];
	    r_array_way1[index]  <= new_r_array[1];
	    r_array_way2[index]  <= new_r_array[2];
	    r_array_way3[index]  <= new_r_array[3];
	    w_array_way0[index]  <= new_w_array[0];
	    w_array_way1[index]  <= new_w_array[1];
	    w_array_way2[index]  <= new_w_array[2];
	    w_array_way3[index]  <= new_w_array[3];
	    d_array_way0[index]  <= new_d_array[0];
	    d_array_way1[index]  <= new_d_array[1];
	    d_array_way2[index]  <= new_d_array[2];
	    d_array_way3[index]  <= new_d_array[3];
    end 


    if(io_l2tlb_resp_valid) begin 
      // 如果PTW响应有效，则根据r_refill_waddr提供的表项位置，将GEN_0数据写入对应表项的PPN中
      if(2'h0 == r_refill_waddr) begin
        ppns_way0[index] <= io_l2tlb_resp_bits_pte_ppn; // 即io_l2tlb_resp_bits_pte_ppn[19:0]
      end
      if(2'h1 == r_refill_waddr) begin
        ppns_way1[index] <= io_l2tlb_resp_bits_pte_ppn;
      end
      if(2'h2 == r_refill_waddr) begin
        ppns_way2[index] <= io_l2tlb_resp_bits_pte_ppn;
      end
      if(2'h3 == r_refill_waddr) begin
        ppns_way3[index] <= io_l2tlb_resp_bits_pte_ppn;
      end

      // 如果PTW响应有效，则根据r_refill_waddr提供的表项位置，将GEN_1数据写入对应表项的TAGS中
      if(2'h0 == r_refill_waddr) begin
        asid_vpn_way0[index] <= r_refill_asid_vpn; // 即r_refill_tag[33:0]
      end
      if(2'h1 == r_refill_waddr) begin
        asid_vpn_way1[index] <= r_refill_asid_vpn;
      end
      if(2'h2 == r_refill_waddr) begin
        asid_vpn_way2[index] <= r_refill_asid_vpn;
      end
      if(2'h3 == r_refill_waddr) begin
        asid_vpn_way3[index] <= r_refill_asid_vpn;
      end
    end    
  end
endmodule