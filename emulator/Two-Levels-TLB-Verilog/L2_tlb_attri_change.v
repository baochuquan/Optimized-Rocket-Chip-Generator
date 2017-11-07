module L2_tlb_attri_change(
	input [1:0] r_refill_waddr,
	input io_ptw_invalidate,
	input io_ptw_resp_valid,
	input io_ptw_resp_bits_pte_v,
	input io_ptw_resp_bits_pte_u,
	input io_ptw_resp_bits_pte_w,
	input io_ptw_resp_bits_pte_x,
	input io_ptw_resp_bits_pte_r,
	input io_ptw_resp_bits_pte_d,
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
	input w_array_way0_idx,
	input w_array_way1_idx,
	input w_array_way2_idx,
	input w_array_way3_idx,
	input r_array_way0_idx,
	input r_array_way1_idx,
	input r_array_way2_idx,
	input r_array_way3_idx,
	input x_array_way0_idx,
	input x_array_way1_idx,
	input x_array_way2_idx,
	input x_array_way3_idx,
	input prot_w,

	output [3:0] new_valid,
	output [3:0] new_u_array,
	output [3:0] new_sw_array,
	output [3:0] new_d_array,
	output [3:0] new_w_array,
	output [3:0] new_r_array,
	output [3:0] new_x_array
    );

	wire [3:0] tmp_v_array;
	wire [3:0] tmp_u_array;
	wire [3:0] tmp_sw_array;
	wire [3:0] tmp_d_array;
	wire [3:0] tmp_w_array;
	wire [3:0] tmp_r_array;
	wire [3:0] tmp_x_array;

	wire [3:0] T_362;
	wire [3:0] T_363;
	wire [3:0] GEN_34;
	wire [3:0] T_364;
	wire [3:0] T_366;
	wire [3:0] T_367;
	wire T_374;
	wire [3:0] T_375;
	wire [3:0] T_377;
	wire [3:0] T_378;
	wire [3:0] T_416;
	wire [3:0] T_418;
	wire [3:0] T_425;
	wire [3:0] T_419;
	wire [3:0] T_420;
	wire [3:0] T_426;
	wire [3:0] T_421;
	wire [3:0] T_422;
	wire [3:0] T_427;
	wire [3:0] T_423;
	wire [3:0] T_424;
	wire [3:0] T_428;

	assign tmp_v_array = {    valid_way3_idx,    valid_way2_idx,    valid_way1_idx,    valid_way0_idx };
	assign tmp_u_array = {  u_array_way3_idx,  u_array_way2_idx,  u_array_way1_idx,  u_array_way0_idx };
	assign tmp_sw_array = {sw_array_way3_idx, sw_array_way2_idx, sw_array_way1_idx, sw_array_way0_idx };
	assign tmp_d_array = {  d_array_way3_idx,  d_array_way2_idx,  d_array_way1_idx,  d_array_way0_idx };
	assign tmp_w_array = {  w_array_way3_idx,  w_array_way2_idx,  w_array_way1_idx,  w_array_way0_idx };
	assign tmp_r_array = {  r_array_way3_idx,  r_array_way2_idx,  r_array_way1_idx,  r_array_way0_idx };
	assign tmp_x_array = {  x_array_way3_idx,  x_array_way2_idx,  x_array_way1_idx,  x_array_way0_idx };

	assign T_362 = 4'h1 << r_refill_waddr;
	assign T_363 = T_362 | tmp_v_array;
	assign GEN_34 = io_ptw_resp_valid ? T_363 : tmp_v_array;
	assign new_valid = io_ptw_invalidate ? 4'h0 : GEN_34;

	assign T_364 = T_362 | tmp_u_array;
	assign T_366 = (~T_362) & tmp_u_array;
	assign T_367 = io_ptw_resp_bits_pte_u ? T_364 : T_366;
	assign new_u_array = io_ptw_resp_valid ? T_367 : tmp_u_array;

	assign T_374 = ((io_ptw_resp_bits_pte_x & !io_ptw_resp_bits_pte_w) | io_ptw_resp_bits_pte_r)  & io_ptw_resp_bits_pte_v & io_ptw_resp_bits_pte_w & prot_w;
	assign T_375 = T_362 | tmp_sw_array;
	assign T_377 = (~T_362) & tmp_sw_array;
	assign T_378 = T_374 ? T_375 : T_377;
	assign new_sw_array = io_ptw_resp_valid ? T_378 : tmp_sw_array;

	assign T_416 = T_362 | tmp_d_array;
	assign T_418 = (~T_362) & tmp_d_array;
	assign T_425 = io_ptw_resp_bits_pte_d ? T_416 : T_418;
	assign new_d_array = io_ptw_resp_valid ? T_425 : tmp_d_array;

	assign T_419 = T_362 | tmp_w_array;
	assign T_420 = (~T_362) & tmp_w_array;
	assign T_426 = io_ptw_resp_bits_pte_w ? T_419 : T_420;
	assign new_w_array = io_ptw_resp_valid ? T_426 : tmp_w_array;

	assign T_421 = T_362 | tmp_r_array;
	assign T_422 = (~T_362) & tmp_r_array;
	assign T_427 = io_ptw_resp_bits_pte_w ? T_421 : T_422;
	assign new_r_array = io_ptw_resp_valid ? T_427 : tmp_r_array;

	assign T_423 = T_362 | tmp_x_array;
	assign T_424 = (~T_362) & tmp_x_array;
	assign T_428 = io_ptw_resp_bits_pte_x ? T_423 : T_424;
	assign new_w_array = io_ptw_resp_valid ? T_428 : tmp_w_array;
endmodule
