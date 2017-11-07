#!/bin/bash
if [ -f "miss-statistic-list" ]; then
    rm miss-statistic-list
fi

touch miss-statistic-list

for file in ./miss-output-v/*
do
    if test -f $file
    then
        echo "=============================" >> miss-statistic-list
        filename=$file
        echo ${filename##*/} >> miss-statistic-list
        itlb_l1_mis="ITLB L1 mis "
        itlb_l1_req="ITLB L1 req "
        itlb_l1_valid_mis="ITLB L1 valid mis "
        itlb_l1_valid_req="ITLB L1 valid req "
        itlb_l2_valid_mis="ITLB L2 valid mis "
        itlb_l2_valid_req="ITLB L2 valid req "
        itlb_l1_misnum=`grep "D|I+111+ bcq_dbg_l1_mis" $file | wc -l`
        itlb_l1_reqnum=`grep "D|I+111+ bcq_dbg_l1_req" $file | wc -l`
        itlb_l1_valid_misnum=`grep "D|I+111+ bcq_dbg_l1_valid_mis" $file | wc -l`
        itlb_l1_valid_reqnum=`grep "D|I+111+ bcq_dbg_l1_valid_req" $file | wc -l`
        itlb_l2_valid_misnum=`grep "D|I+111+ bcq_dbg_l2_valid_mis" $file | wc -l`
        itlb_l2_valid_reqnum=`grep "D|I+111+ bcq_dbg_l2_valid_req" $file | wc -l`

		dtlb_l1_mis="DTLB L1 mis "
        dtlb_l1_req="DTLB L1 req "
        dtlb_l1_valid_mis="DTLB L1 valid mis "
        dtlb_l1_valid_req="DTLB L1 valid req "
        dtlb_l2_valid_mis="DTLB L2 valid mis "
        dtlb_l2_valid_req="DTLB L2 valid req "
        dtlb_l1_misnum=`grep "D|I+  0+ bcq_dbg_l1_mis" $file | wc -l`
        dtlb_l1_reqnum=`grep "D|I+  0+ bcq_dbg_l1_req" $file | wc -l`
        dtlb_l1_valid_misnum=`grep "D|I+  0+ bcq_dbg_l1_valid_mis" $file | wc -l`
        dtlb_l1_valid_reqnum=`grep "D|I+  0+ bcq_dbg_l1_valid_req" $file | wc -l`
        dtlb_l2_valid_misnum=`grep "D|I+  0+ bcq_dbg_l2_valid_mis" $file | wc -l`
        dtlb_l2_valid_reqnum=`grep "D|I+  0+ bcq_dbg_l2_valid_req" $file | wc -l`

        itlb_l1_misinfo=${itlb_l1_mis}${itlb_l1_misnum}
        itlb_l1_reqinfo=${itlb_l1_req}${itlb_l1_reqnum}
        itlb_l1_valid_misinfo=${itlb_l1_valid_mis}${itlb_l1_valid_misnum}
        itlb_l1_valid_reqinfo=${itlb_l1_valid_req}${itlb_l1_valid_reqnum}
        itlb_l2_valid_misinfo=${itlb_l2_valid_mis}${itlb_l2_valid_misnum}
        itlb_l2_valid_reqinfo=${itlb_l2_valid_req}${itlb_l2_valid_reqnum}

        dtlb_l1_misinfo=${dtlb_l1_mis}${dtlb_l1_misnum}
        dtlb_l1_reqinfo=${dtlb_l1_req}${dtlb_l1_reqnum}
        dtlb_l1_valid_misinfo=${dtlb_l1_valid_mis}${dtlb_l1_valid_misnum}
        dtlb_l1_valid_reqinfo=${dtlb_l1_valid_req}${dtlb_l1_valid_reqnum}
        dtlb_l2_valid_misinfo=${dtlb_l2_valid_mis}${dtlb_l2_valid_misnum}
        dtlb_l2_valid_reqinfo=${dtlb_l2_valid_req}${dtlb_l2_valid_reqnum}

        echo $itlb_l1_misinfo >> miss-statistic-list
        echo $itlb_l1_reqinfo >> miss-statistic-list
        echo $itlb_l1_valid_misinfo >> miss-statistic-list
        echo $itlb_l1_valid_reqinfo >> miss-statistic-list
        echo $itlb_l2_valid_misinfo >> miss-statistic-list
        echo $itlb_l2_valid_reqinfo >> miss-statistic-list
        echo "-----------------------------" >> miss-statistic-list
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
