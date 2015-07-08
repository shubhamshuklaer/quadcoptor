function process_data(prefix)
    d=dir('temp');

    for n=1:numel(d)
        file_name=fullfile('temp',d(n).name);
        fid=fopen(file_name);
        if fid~=-1
            data=textscan(fid,'%f,%f');
            figure('Visible','off');
            plot(data{1},data{2});
            title(d(n).name);
            print('-dpdf','-r300',fullfile('temp',strcat(prefix,d(n).name,'.pdf')));
            fclose(fid);
        else
            if ~isdir(file_name)
                fprintf('cannot open file %s\n',d(n).name);
            end
        end

    end
end
