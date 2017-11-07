#!/bin/bash
 ./emulator-rocketchip-DefaultConfig +max-cycles=500000 +verbose output/rv64uf-v-fcmp 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output/rv64uf-v-fcmp.out && [ $PIPESTATUS -eq 0 ]
