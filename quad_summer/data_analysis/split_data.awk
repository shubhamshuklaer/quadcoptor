#!/usr/bin/awk -f
#usage ./split_data.awk log_file_name
BEGIN {
    x_val=-1;
    x_prefix="cm";
}

/^\S+\s+[0-9]+(.[0-9]+)?$/ {
    if ($1 == "cm"){
        x_val=$2;
    }else if(x_val!=-1){
        printf "%s,%s\r\n",x_val,$2 >> "temp/"$1;
    }
}
