#!/bin/bash
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64uf-v-ldst 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64uf-v-ldst.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64uf-v-move 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64uf-v-move.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64uf-v-fsgnj 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64uf-v-fsgnj.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64uf-v-fcmp 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64uf-v-fcmp.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64uf-v-fcvt 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64uf-v-fcvt.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64uf-v-fcvt_w 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64uf-v-fcvt_w.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64uf-v-fclass 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64uf-v-fclass.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64uf-v-fadd 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64uf-v-fadd.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64uf-v-fdiv 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64uf-v-fdiv.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64uf-v-fmin 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64uf-v-fmin.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64uf-v-fmadd 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64uf-v-fmadd.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ud-v-ldst 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ud-v-ldst.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ud-v-move 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ud-v-move.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ud-v-fsgnj 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ud-v-fsgnj.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ud-v-fcmp 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ud-v-fcmp.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ud-v-fcvt 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ud-v-fcvt.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ud-v-fcvt_w 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ud-v-fcvt_w.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ud-v-fclass 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ud-v-fclass.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ud-v-fadd 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ud-v-fadd.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ud-v-fdiv 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ud-v-fdiv.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ud-v-fmin 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ud-v-fmin.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ud-v-fmadd 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ud-v-fmadd.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ud-v-structural 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ud-v-structural.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-lrsc 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-lrsc.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amoadd_w 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amoadd_w.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amoand_w 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amoand_w.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amoor_w 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amoor_w.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amoxor_w 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amoxor_w.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amoswap_w 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amoswap_w.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amomax_w 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amomax_w.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amomaxu_w 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amomaxu_w.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amomin_w 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amomin_w.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amominu_w 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amominu_w.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amoadd_d 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amoadd_d.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amoand_d 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amoand_d.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amoor_d 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amoor_d.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amoxor_d 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amoxor_d.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amoswap_d 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amoswap_d.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amomax_d 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amomax_d.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amomaxu_d 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amomaxu_d.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amomin_d 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amomin_d.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ua-v-amominu_d 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ua-v-amominu_d.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64uc-v-rvc 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64uc-v-rvc.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-simple 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-simple.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-add 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-add.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-addi 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-addi.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-and 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-and.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-andi 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-andi.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-auipc 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-auipc.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-beq 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-beq.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-bge 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-bge.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-bgeu 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-bgeu.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-blt 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-blt.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-bltu 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-bltu.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-bne 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-bne.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-fence_i 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-fence_i.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-jal 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-jal.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-jalr 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-jalr.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-lb 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-lb.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-lbu 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-lbu.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-lh 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-lh.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-lhu 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-lhu.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-lui 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-lui.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-lw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-lw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-or 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-or.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-ori 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-ori.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-sb 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-sb.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-sh 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-sh.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-sw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-sw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-sll 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-sll.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-slli 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-slli.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-slt 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-slt.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-slti 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-slti.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-sra 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-sra.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-srai 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-srai.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-srl 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-srl.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-srli 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-srli.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-sub 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-sub.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-xor 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-xor.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-xori 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-xori.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-addw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-addw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-addiw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-addiw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-ld 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-ld.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-lwu 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-lwu.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-sd 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-sd.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-slliw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-slliw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-sllw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-sllw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-sltiu 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-sltiu.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-sltu 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-sltu.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-sraiw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-sraiw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-sraw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-sraw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-srliw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-srliw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-srlw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-srlw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64ui-v-subw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64ui-v-subw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64um-v-mul 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64um-v-mul.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64um-v-mulh 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64um-v-mulh.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64um-v-mulhsu 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64um-v-mulhsu.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64um-v-mulhu 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64um-v-mulhu.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64um-v-div 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64um-v-div.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64um-v-divu 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64um-v-divu.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64um-v-rem 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64um-v-rem.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64um-v-remu 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64um-v-remu.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64um-v-divuw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64um-v-divuw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64um-v-divw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64um-v-divw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64um-v-mulw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64um-v-mulw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64um-v-remuw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64um-v-remuw.out && [ $PIPESTATUS -eq 0 ]
./emulator-rocketchip-DefaultConfig +max-cycles=1000000 +verbose output/rv64um-v-remw 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64um-v-remw.out && [ $PIPESTATUS -eq 0 ]



if [ -f "miss-statistic-list" ]; then
    rm miss-statistic-list
fi

touch miss-statistic-list

for file in ./miss-output-v/*
do
    if test -f $file
    then
        filename=$file
        echo ${filename##*/} >> miss-statistic-list
        itlb_l1_mis="ITLB L1 mis "
        itlb_l1_req="ITLB L1 req "
        itlb_l1_valid_mis="ITLB L1 valid mis "
        itlb_l1_valid_req="ITLB L1 valid req "
        itlb_l2_valid_mis="ITLB L2 valid mis "
        itlb_l2_valid_req="ITLB L2 valid req "
        itlb_l1_misnum=`grep "D/I+111+ bcq_dbg_l1_req" $file | wc -l`
        itlb_l1_reqnum=`grep "D/I+111+ bcq_dbg_l1_mis" $file | wc -l`
        itlb_l1_valid_misnum=`grep "D/I+111+ bcq_dbg_l1_valid_req" $file | wc -l`
        itlb_l1_valid_reqnum=`grep "D/I+111+ bcq_dbg_l1_valid_mis" $file | wc -l`
        itlb_l2_valid_misnum=`grep "D/I+111+ bcq_dbg_l2_valid_req" $file | wc -l`
        itlb_l2_valid_reqnum=`grep "D/I+111+ bcq_dbg_l2_valid_mis" $file | wc -l`

		dtlb_l1_mis="DTLB L1 mis "
        dtlb_l1_req="DTLB L1 req "
        dtlb_l1_valid_mis="DTLB L1 valid mis "
        dtlb_l1_valid_req="DTLB L1 valid req "
        dtlb_l2_valid_mis="DTLB L2 valid mis "
        dtlb_l2_valid_req="DTLB L2 valid req "
        dtlb_l1_misnum=`grep "D/I+111+ bcq_dbg_l1_req" $file | wc -l`
        dtlb_l1_reqnum=`grep "D/I+111+ bcq_dbg_l1_mis" $file | wc -l`
        dtlb_l1_valid_misnum=`grep "D/I+  0+ bcq_dbg_l1_valid_req" $file | wc -l`
        dtlb_l1_valid_reqnum=`grep "D/I+  0+ bcq_dbg_l1_valid_mis" $file | wc -l`
        dtlb_l2_valid_misnum=`grep "D/I+  0+ bcq_dbg_l2_valid_req" $file | wc -l`
        dtlb_l2_valid_reqnum=`grep "D/I+  0+ bcq_dbg_l2_valid_mis" $file | wc -l`

        itlb_l1_misinfo=${itlb_l1_mis}${itlb_l1_misnum}
        itlb_l1_reqinfo=${itlb_l1_req}${itlb_l1_reqnum}
        itlb_l1_valid_misinfo=${itlb_l1_valid_mis}${itlb_l1_valid_misnum}
        itlb_l1_valid_reqinfo=${itlb_l1_valid_req}${itlb_l1_valid_reqnum}
        itlb_l2_valid_reqinfo=${itlb_l2_valid_req}${itlb_l2_valid_reqnum}
        itlb_l2_valid_reqinfo=${itlb_l2_valid_req}${itlb_l2_valid_reqnum}

        dtlb_l1_misinfo=${dtlb_l1_mis}${dtlb_l1_misnum}
        dtlb_l1_reqinfo=${dtlb_l1_req}${dtlb_l1_reqnum}
        dtlb_l1_valid_misinfo=${dtlb_l1_valid_mis}${dtlb_l1_valid_misnum}
        dtlb_l1_valid_reqinfo=${dtlb_l1_valid_req}${dtlb_l1_valid_reqnum}
        dtlb_l2_valid_reqinfo=${dtlb_l2_valid_req}${dtlb_l2_valid_reqnum}
        dtlb_l2_valid_reqinfo=${dtlb_l2_valid_req}${dtlb_l2_valid_reqnum}


        echo $itlb_l1_misinfo >> miss-statistic-list
        echo $itlb_l1_reqinfo >> miss-statistic-list
        echo $itlb_l1_valid_misinfo >> miss-statistic-list
        echo $itlb_l1_valid_reqinfo >> miss-statistic-list
        echo $itlb_l2_valid_misinfo >> miss-statistic-list
        echo $itlb_l2_valid_reqinfo >> miss-statistic-list
        echo $dtlb_l1_misinfo >> miss-statistic-list
        echo $dtlb_l1_reqinfo >> miss-statistic-list
        echo $dtlb_l1_valid_misinfo >> miss-statistic-list
        echo $dtlb_l1_valid_reqinfo >> miss-statistic-list
        echo $dtlb_l2_valid_misinfo >> miss-statistic-list
        echo $dtlb_l2_valid_reqinfo >> miss-statistic-list
#        missratio=`echo "scale=6;$missnum/$reqsnum" | bc`
#        echo $missratio >> miss-statistic-list
    fi
done
