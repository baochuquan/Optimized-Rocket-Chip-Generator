#!/bin/bash
 ./emulator-rocketchip-DefaultConfig +max-cycles=900000 +verbose output/$1 3>&1 1>&2 2>&3 | /home/baochuquan/Desktop/MPRC-RISCV/riscv/bin/spike-dasm  > miss-output-v/$1.out && [ $PIPESTATUS -eq 0 ]
