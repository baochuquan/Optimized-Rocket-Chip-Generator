module L2_tlb_plru(
	input [4:0] hitsVec,
	input [3:0] L2_plru_val,

	output [3:0] L2_new_plru_val
    );

	wire T_2442;
	wire [1:0] T_2443;
	wire [1:0] T_2445;

	wire [1:0] T_2452;
	wire [3:0] GEN_122;
	wire [3:0] T_2457;

	wire [1:0] T_2463;
	wire [3:0] GEN_124;

	wire [3:0] T_2464;
	wire [3:0] T_2466;

	wire [3:0] T_2468;
	wire [3:0] T_2469;
	wire [3:0] T_2474;

	wire [3:0] T_2477;
	wire [3:0] T_2475;

	assign T_2442 = hitsVec[3:2] != 2'h0;
	assign T_2443 = hitsVec[3:2] | hitsVec[1:0];
	assign T_2445 = { T_2442, T_2443[1] };

	assign T_2452 = 2'h1 << 1'h1;
	assign GEN_122 = { 2'd0, T_2452 };
	assign T_2457 = GEN_122 | L2_plru_val;

	assign T_2463 = 2'h1 << 1'h1;
	assign GEN_124 = { 2'd0, T_2463 };
	assign T_2464 = GEN_124 | T_2457;
	assign T_2466 = GEN_124 | ~T_2457;

	assign T_2468 = (!T_2445[1]) ? T_2464 : ~T_2466;

	assign T_2469 = { 1'h1, T_2445[1] };
	assign T_2474 = 4'h1 << T_2469;

	assign T_2477 = T_2474 | ~T_2468;
	assign T_2475 = T_2474 | T_2468;
	assign L2_new_plru_val = (!T_2445[0]) ? ~T_2477 : T_2475;

endmodule
