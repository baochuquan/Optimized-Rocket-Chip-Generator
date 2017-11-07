#!/bin/bash
if [ -f "miss-statistic-lista" ]; then
    rm miss-statistic-lista
fi

touch miss-statistic-lista

for file in ./miss-output/*
do
    if test -f $file
    then
        filename=$file
        echo ${filename##*/} >> miss-statistic-lista
        miss="miss "
        reqs="reqs "
        missnum=`grep "bcq_debug_reg_tlbmiss" $file | wc -l`
        reqsnum=`grep "bcq_debug_reg_tlbreq" $file | wc -l`
        missinfo=${miss}${missnum}
        reqsinfo=${reqs}${reqsnum}
        echo $reqsinfo >> miss-statistic-lista
        echo $missinfo >> miss-statistic-lista
        missratio=`echo "scale=4;$missnum/$reqsnum" | bc`
        echo $missratio >> miss-statistic-lista
    fi
done
