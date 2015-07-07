d=dir('temp');

for n=1:numel(d)
    file_name=fullfile('temp',d(n).name);
    fid=fopen(file_name);
    if fid~=-1
        data=textscan(fid,'%f,%f');
        figure('Visible','off');
        plot(data{1},data{2});
        title(d(n).name);
        print('-depsc2','-r300',fullfile('output',d(n).name));
        fclose(fid);
    else
        if ~isdir(file_name)
            fprintf('cannot open file %s\n',d(n).name);
        end
    end

end
