function generateLatencyPlot()

request = input('Quale lacency vuoi plottare? (flink-kafka=1, flink-pulsar=2, 3=throughput, 4=kafkaS): ');
if(request == 1)
    mainDir = 'results-kafka';
elseif(request == 2)
    mainDir = 'results-pulsar';
elseif(request == 3)
    mainDir = 'throughput';
elseif(request == 4)
    mainDir = 'query-kafkaS';
end

%apro la cartella principale
dinfo = dir(fullfile(mainDir));
subdir = dinfo([dinfo.isdir]);
subdir(1:2) = [];%get rid of all directories including . and ..

for a=1:length(subdir)
    newDir = strcat(mainDir ,'/', subdir(a).name);
    estimateDir = dir(fullfile(newDir));
    estimateDir(1:2) = [];
    
    for b=1:length(estimateDir)
        newDir2 = strcat(newDir, '/', estimateDir(b).name);
        localDir = dir(newDir2);
        localDir([localDir.isdir]) = [];
               
        if(request == 1) || (request == 2)
            %draw plot
            directory = join(split(erase(newDir2, ".txt"), "/"), "-");
            data = importResultLatencyFile(newDir2);
            plotLatency(directory{1,1}, data, mainDir);
       
        elseif(request == 4)
            directory = join(split(erase(newDir2, ".txt"), "/"), "-");
            data = importResultLatencyFile(newDir2);
            plotLatencyKafka(directory{1,1}, data, mainDir);
            
        elseif(request == 3)
            data = importThroughputFile(newDir2);
            directory = strcat('Thoughput-',erase(estimateDir(b).name, ".csv"));
            plotThroughput(directory, data,mainDir);
        end
      
    end

end
end

function plotThroughput(name, data, output_path)
    
    BOLD = 1.1;
    MEAN_COLOR = 'blue';
    record_in = data.('num_records_in_per_second');
    record_out = data.('num_records_out_per_second');
    times = data.('time');
    
    media_record_in = mean(record_in); % secondi
    media_record_out = mean(record_out); % secondi
    
    dev_std_in = std(record_in);
    dev_std_out = std(record_out);
    
    %-------------------ingresso-------------------
    stamp1 = figure('Name', 'Thoughput in uscita', 'Renderer', 'painters', 'Position', [10 10 1200 700]);
    plot(times, record_in, '-o', 'Color', 'black', 'DisplayName', 'Thoughput istantaneo in ingresso', 'LineWidth', BOLD);
    
    str_dv_in = sprintf("%s: %f%s\n%s: %f%s", 'Thoughput medio ingresso', media_record_in,' records/s', ...
        'DevStd in ingresso',dev_std_in,' records/s');
    yline(media_record_in, 'Color', MEAN_COLOR, 'LineStyle','-', 'DisplayName', str_dv_in ); %media
    legend show
    lgd=legend('Location','southoutside');
    lgd.Title.String = name;
    
    ylabel('Thoughput (records/s)');
    xlabel('Istanti di cattura (hours:min:sec)');
    
    ax = gca;
    ax.YGrid ='on';
    ax.XGrid ='off';
	title(strcat('Grafico del ',' ', name, 'in ingresso'));

    path1 = strcat('figure/', output_path, '/', name,'-in.png');
    saveas(stamp1,path1);
    
    %-------------------uscita-------------------
    stamp2 = figure('Name', 'Thoughput in uscita', 'Renderer', 'painters', 'Position', [10 10 1200 700]);
    plot(times, record_out,'-o', 'Color', 'black', 'DisplayName', 'Thoughput istantaneo in uscita', 'LineWidth', BOLD);
    str_dv_out = sprintf("%s: %f%s\n%s: %f%s",'Thoughput medio uscita',media_record_out,' records/s', ...
       'DevStd in uscita',dev_std_out,' records/s');
    yline(media_record_out, 'Color', MEAN_COLOR, 'LineStyle','-','DisplayName', str_dv_out); %media
    legend show
    lgd=legend('Location','southoutside');
    lgd.Title.String = name;
    
    ylabel('Thoughput (records/s)');
    xlabel('Istanti di cattura (hours:min:sec)');
    
    ax = gca;
    ax.YGrid ='on';
    ax.XGrid ='off';
	title(strcat('Grafico del ',' ', name, 'in uscita'));

    path2 = strcat('figure/', output_path, '/', name,'-out.png');
    saveas(stamp2,path2);


end

function plotLatency(name, data, output_path)
    
	BOLD = 1.25;
    BOLD_M = 1.1;
    MEAN_COLOR ='[0, 0.4470, 0.7410]';
    n_sampes = height(data);
    y = data.('latency')/1e+9;
    media = mean(y); %da nanosecondi -> secondi
    varianza = var(y);
    dev_std = std(y);
    %fprintf('media: %f, varianza %f\n', media, varianza);
    
    stamp = figure('Name', 'Thoughput in uscita', 'Renderer', 'painters', 'Position', [10 10 1200 700]);
    plot(data.('date'), y, 'Color', 'black','LineWidth', BOLD);
  
    yline(media, 'Color', MEAN_COLOR, 'LineStyle','-','LineWidth', BOLD_M); %media
    
    str_media = sprintf("%s: %f%c\n%s: %f%c", ...
    'Latenza media',media,'s', ...
    'DevStandard',dev_std,'s') ;
    lgd = legend('Latenza empirica', str_media,'Location','northwest'); %best
    lgd.Title.String = name;
    
    ylabel('Latenza (s)');
    str_samples = sprintf('%s %s %d %s', 'Istanza di computazione', '(sample:', n_sampes, ')'); 
    xlabel(str_samples);
    
    ax = gca;
    ax.YGrid ='on';
    ax.XGrid ='off';
    %set(ax,'GridLineStyle','--')
    
    path = strcat('figure/',output_path, '/', name,'.png');
    title(strcat('Grafico della latenza',' ', name));
    saveas(stamp,path);
end

function plotLatencyKafka(name, data, output_path)
    
	BOLD = 1.25;
    BOLD_M = 1.1;
    MEAN_COLOR ='[0, 0.4470, 0.7410]';
    n_sampes = height(data);
    y = data.('latency')/1e+9;
    media = mean(y); %da nanosecondi -> secondi
    varianza = var(y);
    dev_std = std(y);
    %fprintf('media: %f, varianza %f\n', media, varianza);
    
    stamp = figure('Name', 'Thoughput in uscita', 'Renderer', 'painters', 'Position', [10 10 1200 700]);
    plot(y, 'Color', 'black','LineWidth', BOLD);
  
    yline(media, 'Color', MEAN_COLOR, 'LineStyle','-','LineWidth', BOLD_M); %media
    
    str_media = sprintf("%s: %f%c\n%s: %f%c", ...
    'Latenza media',media,'s', ...
    'DevStandard',dev_std,'s') ;
    lgd = legend('Latenza empirica', str_media,'Location','northwest'); %best
    lgd.Title.String = name;
    
    ylabel('Latenza (s)');
    str_samples = sprintf('%s %s %d %s', 'Istanza di computazione', '(sample:', n_sampes, ')'); 
    xlabel(str_samples);
    
    ax = gca;
    ax.YGrid ='on';
    ax.XGrid ='off';
    %set(ax,'GridLineStyle','--')
    
    path = strcat('figure/',output_path, '/', name,'.png');
    title(strcat('Grafico della latenza',' ', name));
    saveas(stamp,path);
end
