module L2_tlb_lookup(
	input [6:0] io_ptw_ptbr_asid,
	input io_ptw_status_pum,
	input [27:0] io_req_bits_vpn,
	input io_req_bits_store,
	input [27:0] tags_way0_idx,
	input [27:0] tags_way1_idx,
	input [27:0] tags_way2_idx,
	input [27:0] tags_way3_idx,
	input valid_way0_idx,
	input valid_way1_idx,
	input valid_way2_idx,
	input valid_way3_idx,
	input u_array_way0_idx,
	input u_array_way1_idx,
	input u_array_way2_idx,
	input u_array_way3_idx,
	input sw_array_way0_idx,
	input sw_array_way1_idx,
	input sw_array_way2_idx,
	input sw_array_way3_idx,
	input d_array_way0_idx,
	input d_array_way1_idx,
	input d_array_way2_idx,
	input d_array_way3_idx,
	input vm_enabled,
	input bad_va,
	input priv_s,
	input prot_w,

	output [4:0] hitsVec,
	output L2_tlb_miss
    );
	// for dirty_hit_check
	wire [3:0] tmp_d_array;
	wire [3:0] tmp_u_array;
	wire [3:0] tmp_sw_array;
	wire [3:0] priv_ok;
	wire [3:0] T_463;
	wire [3:0] T_465;
	wire [4:0] w_array;
	wire [4:0] GEN_59;
	wire [4:0] T_475;
	wire [4:0] dirty_hit_check;

	assign tmp_d_array 	= {  d_array_way3_idx,  d_array_way2_idx,  d_array_way1_idx,  d_array_way0_idx };
	assign tmp_u_array 	= {  u_array_way3_idx,  u_array_way2_idx,  u_array_way1_idx,  u_array_way0_idx };
	assign tmp_sw_array = { sw_array_way3_idx, sw_array_way2_idx, sw_array_way1_idx, sw_array_way0_idx };
	assign T_463 = io_ptw_status_pum ? tmp_u_array : 4'h0;
	assign priv_ok = priv_s ? ~T_463 : tmp_u_array;
	assign T_465 = priv_ok & tmp_sw_array;
	assign w_array = { prot_w, T_465 };
	assign GEN_59 = { 1'h0, tmp_d_array };
	assign T_475 = io_req_bits_store ? w_array : 5'h0;
	assign dirty_hit_check = (~T_475) | GEN_59;

	// for tlb_lookup, check
	wire [27:0] lookup_tag;
	wire [4:0] tlb_hits;
	wire tlb_hit;

	assign lookup_tag = { io_ptw_ptbr_asid, io_req_bits_vpn[26:6] };
	assign hitsVec[0] = ( valid_way0_idx & vm_enabled ) & ( tags_way0_idx == lookup_tag );
	assign hitsVec[1] = ( valid_way1_idx & vm_enabled ) & ( tags_way1_idx == lookup_tag );
	assign hitsVec[2] = ( valid_way2_idx & vm_enabled ) & ( tags_way2_idx == lookup_tag );
	assign hitsVec[3] = ( valid_way3_idx & vm_enabled ) & ( tags_way3_idx == lookup_tag );
	assign hitsVec[4] = vm_enabled == 1'h0;

	assign tlb_hits = { 1'h0, hitsVec[3:0] } & dirty_hit_check;
	assign tlb_hit = tlb_hits != 5'h0;
	assign L2_tlb_miss = ( !tlb_hit ) & ( vm_enabled & !bad_va);
endmodule
