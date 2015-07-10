#!/bin/bash

if [ $# -ne 2 ]
then
    echo "usage ./analyze_data.sh log_file_name config_file_name"
    exit 2
fi

echo "Splitting data"
rm -rf temp
mkdir temp
./split_data.awk $1
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi #check return code of the above script

echo "Processing data"
mkdir -p output
#using basename to get the filename from path
prefix=$(basename $1)"_"
matlab -nodesktop -nosplash -nodisplay -r "process_data('"$prefix"','"$2"');exit;"

for file_name in temp/*; do
    #file_name will be of forrmat temp/something.something
    base_file_name=$(basename $file_name) #will get something.something
    if [[ $base_file_name =~ ^$prefix.* ]]
    then
        # %q quotes the string i.e escapes it
        merge_cmd=$merge_cmd" "$(printf '%q' "$file_name")
    fi
done

if [ "$merge_cmd" != "" ]
then
    echo "Merging graphs"
    merge_cmd="pdfunite $merge_cmd output/$prefix""merged.pdf"
    eval $merge_cmd
fi

echo "output graphs are stored in output dir you can use pdf viewer to open"
echo "file prefix is "$prefix
