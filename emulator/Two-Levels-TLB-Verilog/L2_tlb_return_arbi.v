module L2_tlb_return_arbi(
	input L2_tlb_miss,
	input L2_resp_v,
	input L2_resp_u,
	input L2_resp_w,
	input L2_resp_x,
	input L2_resp_r,
	input L2_resp_d,
	input [19:0] L2_resp_ppn,

	input io_ptw_req_ready,
	input io_ptw_resp_valid,
	input io_ptw_resp_bits_pte_v,
	input io_ptw_resp_bits_pte_u,
	input io_ptw_resp_bits_pte_w,
	input io_ptw_resp_bits_pte_x,
	input io_ptw_resp_bits_pte_r,
	input io_ptw_resp_bits_pte_d,
	input [19:0] io_ptw_resp_bits_pte_ppn,
	input io_ptw_resp_bits_pte_a,
	input io_ptw_resp_bits_pte_g,
	input [15:0] io_ptw_resp_bits_pte_reserved_for_hardware,
	input [1:0] io_ptw_resp_bits_pte_reserved_for_software,

	output io_l2tlb_req_ready,
	output io_l2tlb_resp_valid,
	output io_l2tlb_resp_bits_pte_v,
	output io_l2tlb_resp_bits_pte_u,
	output io_l2tlb_resp_bits_pte_w,
	output io_l2tlb_resp_bits_pte_x,
	output io_l2tlb_resp_bits_pte_r,
	output io_l2tlb_resp_bits_pte_d,
	output [19:0] io_l2tlb_resp_bits_pte_ppn,
	output io_l2tlb_resp_bits_pte_a,
	output io_l2tlb_resp_bits_pte_g,
	output [15:0] io_l2tlb_resp_bits_pte_reserved_for_hardware,
	output [1:0] io_l2tlb_resp_bits_pte_reserved_for_software
    );

	wire L2_tlb_hit;

	assign L2_tlb_hit = !L2_tlb_miss;
	assign io_l2tlb_req_ready 		= L2_tlb_hit ? 1'h1 : io_ptw_req_ready;
	assign io_l2tlb_resp_valid 		= L2_tlb_hit ? 1'h1 : io_ptw_resp_valid;
	assign io_l2tlb_resp_bits_pte_v = L2_tlb_hit ? L2_resp_v : io_ptw_resp_bits_pte_v;
	assign io_l2tlb_resp_bits_pte_u = L2_tlb_hit ? L2_resp_u : io_ptw_resp_bits_pte_u;
	assign io_l2tlb_resp_bits_pte_w = L2_tlb_hit ? L2_resp_w : io_ptw_resp_bits_pte_w;
	assign io_l2tlb_resp_bits_pte_x = L2_tlb_hit ? L2_resp_x : io_ptw_resp_bits_pte_x;
	assign io_l2tlb_resp_bits_pte_r = L2_tlb_hit ? L2_resp_r : io_ptw_resp_bits_pte_r;
	assign io_l2tlb_resp_bits_pte_d = L2_tlb_hit ? L2_resp_d : io_ptw_resp_bits_pte_d;
	assign io_l2tlb_resp_bits_pte_ppn = L2_tlb_hit ? L2_resp_ppn : io_ptw_resp_bits_pte_ppn;
	assign io_l2tlb_resp_bits_pte_a = L2_tlb_hit ? 1'h0 : io_ptw_resp_bits_pte_a;
	assign io_l2tlb_resp_bits_pte_g = L2_tlb_hit ? 1'h0 : io_ptw_resp_bits_pte_g;
	assign io_l2tlb_resp_bits_pte_reserved_for_hardware = L2_tlb_hit ? 15'h0 : io_ptw_resp_bits_pte_reserved_for_hardware;
	assign io_l2tlb_resp_bits_pte_reserved_for_software = L2_tlb_hit ? 2'h0  : io_ptw_resp_bits_pte_reserved_for_software;
endmodule
