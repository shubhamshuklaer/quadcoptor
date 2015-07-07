function process_data(prefix)
    d=dir('temp');

    for n=1:numel(d)
        file_name=fullfile('temp',d(n).name);
        fid=fopen(file_name);
        if fid~=-1
            header=textscan(fid,'%s,%s',1); %read 1 line will be the header ex cm,ap
            data=textscan(fid,'%f,%f');
            figure('Visible','off');
            plot(data{1},data{2});
            title(header{2});
            xlabel(header{1}); % x-axis label
            ylabel(header{2}); % y-axis label
            print('-dpdf','-r300',fullfile('temp',strcat(prefix,num2str(n),'.pdf')));
            fclose(fid);
        else
            if ~isdir(file_name)
                fprintf('cannot open file %s\n',d(n).name);
            end
        end

    end
end
