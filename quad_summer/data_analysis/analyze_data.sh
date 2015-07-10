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

# IFS='' (or IFS=) prevents leading/trailing whitespace from being trimmed.
# -r prevents backslash escapes from being interpreted.
# || [[ -n $line ]] prevents the last line from being ignored if it doesn't
# end with a \n (since read returns a non-zero exit code when it encounters EOF).
while read -r line || [[ -n $line ]]; do
    if [[ ! -z "$line" ]] && [[ ${line:0:1} != "#" ]]
    then
        file_name="temp/"$prefix$line".pdf"
        merge_cmd=$merge_cmd" "$(printf '%q' "$file_name")
    fi
done < "$2"

if [ "$merge_cmd" != "" ]
then
    echo "Merging graphs"
    merge_cmd="pdfunite $merge_cmd output/$prefix""merged.pdf"
    eval $merge_cmd
fi

echo "output graphs are stored in output dir you can use pdf viewer to open"
echo "file prefix is "$prefix
