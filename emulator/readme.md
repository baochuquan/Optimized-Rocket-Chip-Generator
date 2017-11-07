# emulator文档目录
1. TLB-modern: 使用Verilog编写设计的一款两级TLB结构，存在bug，未通过测试
2. baochuquan-tests: 里面带编号的文件夹为独立的测试程序，各自拥有makefile，可以make直接在结构级模拟器上运行。
3. generated-src: make命令自动生成的目录，用于存放.v等文件  
4. miss-output: 使用结构级模拟器运行汇编测试程序，生成相应的执行信息。
5. miss-output-v: 使用结构级模拟器运行汇编测试程序，生成相应的执行信息。某个脚本单独测试一个汇编测试所存放的执行信息文件的位置。
6. output
7. output-v
8. tlb-modules-for-syn: 不同配置生成的不同TLB结构的.v
9. tmp-tests
10. tmp-Verilog
11. verilator: 仓库自带的目录

注意看一下该目录下的.sh脚本。
