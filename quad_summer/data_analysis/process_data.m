function process_data(prefix,config_file_name)
    config_fid=fopen(config_file_name);
    if config_fid~=-1
        while ~feof(config_fid)
            config_line=strtrim(fgets(config_fid));
            if numel(config_line)~=0 && not(strncmp(config_line,'#',1)) %checks for first character is it a comment
                series = regexp(config_line, ',', 'split');
                figure('Visible','off');
                color_map=lines(numel(series)); % creating a colormap
                hold on
                for k=1:numel(series)
                    file_name=fullfile('temp',series{k});
                    fid=fopen(file_name);
                    if fid~=-1
                        data=textscan(fid,'%f,%f');
                        plot(data{1},data{2},'color',color_map(k,:));
                        fclose(fid);
                    else
                        fprintf('cannot open file %s\n',series{k});
                    end
                end
                legend(series,'interpreter','none'); %otherwise it interprets string as latex and '_' gives subscript
                hold off
                title(config_line,'interpreter','none');
                print('-dpdf','-r300',fullfile('temp',strcat(prefix,config_line,'.pdf')));
            end
        end
        fclose(config_fid);
    else
        fprintf('cannot open file config file "%s"',config_file_name);
    end
end
