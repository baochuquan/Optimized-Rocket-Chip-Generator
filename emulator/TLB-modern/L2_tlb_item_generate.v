module L2_tlb_item_generate(
	input [4:0] hitsVec,
	input [19:0] ppns_way0_idx,
	input [19:0] ppns_way1_idx,
	input [19:0] ppns_way2_idx,
	input [19:0] ppns_way3_idx,
	input [19:0] passthrough_ppn,
	input valid_way0_idx,
	input valid_way1_idx,
	input valid_way2_idx,
	input valid_way3_idx,
	input u_array_way0_idx,
	input u_array_way1_idx,
	input u_array_way2_idx,
	input u_array_way3_idx,
	input w_array_way0_idx,
	input w_array_way1_idx,
	input w_array_way2_idx,
	input w_array_way3_idx,
	input x_array_way0_idx,
	input x_array_way1_idx,
	input x_array_way2_idx,
	input x_array_way3_idx,
	input r_array_way0_idx,
	input r_array_way1_idx,
	input r_array_way2_idx,
	input r_array_way3_idx,
	input d_array_way0_idx,
	input d_array_way1_idx,
	input d_array_way2_idx,
	input d_array_way3_idx,

	output [19:0] L2_resp_ppn,
	output L2_resp_v,
	output L2_resp_u,
	output L2_resp_w,
	output L2_resp_x,
	output L2_resp_r,
	output L2_resp_d
    );

	wire [19:0] tmp_ppns_way0;
	wire [19:0] tmp_ppns_way1;
	wire [19:0] tmp_ppns_way2;
	wire [19:0] tmp_ppns_way3;
	wire [19:0] tmp_passthrough_ppn;
	wire tmp_v_way0;
	wire tmp_v_way1;
	wire tmp_v_way2;
	wire tmp_v_way3;
	wire tmp_u_way0;
	wire tmp_u_way1;
	wire tmp_u_way2;
	wire tmp_u_way3;
	wire tmp_w_way0;
	wire tmp_w_way1;
	wire tmp_w_way2;
	wire tmp_w_way3;
	wire tmp_x_way0;
	wire tmp_x_way1;
	wire tmp_x_way2;
	wire tmp_x_way3;
	wire tmp_r_way0;
	wire tmp_r_way1;
	wire tmp_r_way2;
	wire tmp_r_way3;
	wire tmp_d_way0;
	wire tmp_d_way1;
	wire tmp_d_way2;
	wire tmp_d_way3;

	assign tmp_ppns_way0 = hitsVec[0] ? ppns_way0_idx : 20'h0;
	assign tmp_ppns_way1 = hitsVec[1] ? ppns_way1_idx : 20'h0;
	assign tmp_ppns_way2 = hitsVec[2] ? ppns_way2_idx : 20'h0;
	assign tmp_ppns_way3 = hitsVec[3] ? ppns_way3_idx : 20'h0;
	assign tmp_passthrough_ppn = hitsVec[4] ? passthrough_ppn : 20'h0;
	assign L2_resp_ppn = tmp_ppns_way0 | tmp_ppns_way1 | tmp_ppns_way2 | tmp_ppns_way3 | tmp_passthrough_ppn;

	assign tmp_v_way0 = hitsVec[0] ? valid_way0_idx : 1'h0;
	assign tmp_v_way1 = hitsVec[1] ? valid_way1_idx : 1'h0;
	assign tmp_v_way2 = hitsVec[2] ? valid_way2_idx : 1'h0;
	assign tmp_v_way3 = hitsVec[3] ? valid_way3_idx : 1'h0;
	assign L2_resp_v = tmp_v_way0 | tmp_v_way1 | tmp_v_way2 | tmp_v_way3;

	assign tmp_u_way0 = hitsVec[0] ? u_array_way0_idx : 1'h0;
	assign tmp_u_way1 = hitsVec[1] ? u_array_way1_idx : 1'h0;
	assign tmp_u_way2 = hitsVec[2] ? u_array_way2_idx : 1'h0;
	assign tmp_u_way3 = hitsVec[3] ? u_array_way3_idx : 1'h0;
	assign L2_resp_u = tmp_u_way0 | tmp_u_way1 | tmp_u_way2 | tmp_u_way3;

	assign tmp_w_way0 = hitsVec[0] ? w_array_way0_idx : 1'h0;
	assign tmp_w_way1 = hitsVec[1] ? w_array_way1_idx : 1'h0;
	assign tmp_w_way2 = hitsVec[2] ? w_array_way2_idx : 1'h0;
	assign tmp_w_way3 = hitsVec[3] ? w_array_way3_idx : 1'h0;
	assign L2_resp_w = tmp_w_way0 | tmp_w_way1 | tmp_w_way2 | tmp_w_way3;

	assign tmp_r_way0 = hitsVec[0] ? r_array_way0_idx : 1'h0;
	assign tmp_r_way1 = hitsVec[1] ? r_array_way1_idx : 1'h0;
	assign tmp_r_way2 = hitsVec[2] ? r_array_way2_idx : 1'h0;
	assign tmp_r_way3 = hitsVec[3] ? r_array_way3_idx : 1'h0;
	assign L2_resp_r = tmp_r_way0 | tmp_r_way1 | tmp_r_way2 | tmp_r_way3;

	assign tmp_d_way0 = hitsVec[0] ? d_array_way0_idx : 1'h0;
	assign tmp_d_way1 = hitsVec[1] ? d_array_way1_idx : 1'h0;
	assign tmp_d_way2 = hitsVec[2] ? d_array_way2_idx : 1'h0;
	assign tmp_d_way3 = hitsVec[3] ? d_array_way3_idx : 1'h0;
	assign L2_resp_d = tmp_d_way0 | tmp_d_way1 | tmp_d_way2 | tmp_d_way3;
endmodule
