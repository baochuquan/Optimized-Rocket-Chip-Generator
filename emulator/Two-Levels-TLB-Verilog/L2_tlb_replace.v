module L2_tlb_replace(
	input valid_way0_idx,
	input valid_way1_idx,
	input valid_way2_idx,
	input valid_way3_idx,
	input [3:0] L2_plru_val,

	output [1:0] repl_waddr
    );
	
	wire [3:0] tmp_valid_n;
	wire [3:0] tmp_valid;
	wire [1:0] valid_repl_waddr;

	wire [3:0] T_452;
	wire [1:0] T_454;
	wire [3:0] T_455;
	wire [2:0] T_457;

	assign tmp_valid = { valid_way3_idx, valid_way2_idx, valid_way1_idx, valid_way0_idx };
	assign tmp_valid_n = ~tmp_valid;
	assign valid_repl_waddr = tmp_valid_n[0] ? 2'h0 : (tmp_valid_n[1] ? 2'h1 : (tmp_valid_n[2] ? 2'h2 : 2'h3));

	assign T_452 = L2_plru_val >> 1'h1;
	assign T_454 = { 1'h1, T_452[0] };
	assign T_455 = L2_plru_val >> T_454;
	assign T_457 = { T_454, T_455[0] };

	assign repl_waddr = tmp_valid_n ? valid_repl_waddr : T_457[1:0];
endmodule
