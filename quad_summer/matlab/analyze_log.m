function analyze_log(log_file_name)
    x_prefix='cm';
    x_val='GARBAGE';
    prefixes={'ry'; 'rp'; 'rr'; 'ay'; 'ap'; 'ar'; 'chp'; 'chr'; 'chy'; 'bs'; 'm1'; 'm2'; 'gy'; 'gp'; 'gr'; 'y'; 'p'; 'r'};
    fid=fopen(log_file_name);
    while ~feof(fid)
        data=fgets(fid);
        cell_array=strsplit(data)
    end
end
