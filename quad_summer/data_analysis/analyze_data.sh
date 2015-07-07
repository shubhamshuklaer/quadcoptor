#!/bin/bash

if [ $# -ne 1 ]
then
    echo "usage ./analyze_data.sh log_file_name"
fi

echo "Splitting data"
rm -rf temp
mkdir temp
./split_data.awk $1

echo "Processing data"
mkdir -p output
matlab -nodesktop -nosplash -nodisplay -r "run('process_data.m');exit;"

echo "output graphs are stored in output dir you can use pdf viewer to open"
rm -rf temp
