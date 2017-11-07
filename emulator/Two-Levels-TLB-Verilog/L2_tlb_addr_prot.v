module L2_tlb_addr_prot(
	input io_ptw_resp_valid,
	input [19:0] io_req_bits_vpn,
	input [19:0] io_ptw_resp_bits_pte_ppn,

	output [19:0] passthrough_ppn,
	output prot_w
    );
	wire [19:0] mpu_ppn;
	wire [31:0] GEN_57;
	wire [31:0] T_226;
	wire T_230;
	wire [2:0] T_234;
	wire T_239;
	wire [2:0] T_242;
	wire T_247;
	wire [2:0] T_250;
	wire T_255;
	wire [2:0] T_258;
	wire cacheable_buf;
	wire [2:0] T_266;
	wire [2:0] T_274;

	assign passthrough_ppn = io_req_bits_vpn;
	assign mpu_ppn = io_ptw_resp_valid ? io_req_bits_vpn : io_ptw_resp_bits_pte_ppn;
	assign GEN_57 = { 12'd0, mpu_ppn };
	assign T_226 = GEN_57 << 12;

	assign T_230 = T_226 < 32'h1000;
	assign T_239 = (32'h1000 <= T_226) & (T_226 < 32'h2000);
	assign T_247 = (32'h2000000 <= T_226) & (T_226 < 32'h2010000);
	assign T_255 = (32'hC000000 <= T_226) & (T_226 < 32'h10000000);
	assign cacheable_buf = (32'h8000000 <= T_226) & (T_226 < 32'h90000000);

	assign T_234 = T_230 ? 3'h7 : 3'h0;
	assign T_242 = T_239 ? 3'h5 : 3'h0;
	assign T_250 = T_247 ? 3'h3 : 3'h0;
	assign T_258 = T_255 ? 3'h3 : 3'h0;
	assign T_266 = cacheable_buf ? 3'h7 : 3'h0;

	assign T_274 = T_234 | T_242 | T_250 | T_258 | T_266;
	assign prot_w = T_274[1];
endmodule
