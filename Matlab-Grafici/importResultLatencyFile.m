function data = importResultLatencyFile(filename, startRow, endRow)
% Initialize variables.
delimiter = ';';
if nargin<=2
startRow = 1;
endRow = inf;
end

% Format for each line of text:
%   column1: datetimes (%{yyyy-MM-dd}D)
%	column2: text (%s)
%   column3: double (%f)
% For more information, see the TEXTSCAN documentation.
formatSpec = '%{yyyy-MM-dd}D%s%f%[^\n\r]';

% Open the text file.
fileID = fopen(filename,'r');

% Read columns of data according to the format.
% This call is based on the structure of the file used to generate this
% code. If an error occurs for a different file, try regenerating the code
% from the Import Tool.
dataArray = textscan(fileID, formatSpec, endRow(1)-startRow(1)+1, 'Delimiter', delimiter, 'TextType', 'string', 'HeaderLines', startRow(1)-1, 'ReturnOnError', false, 'EndOfLine', '\r\n');
for block=2:length(startRow)
frewind(fileID);
dataArrayBlock = textscan(fileID, formatSpec, endRow(block)-startRow(block)+1, 'Delimiter', delimiter, 'TextType', 'string', 'HeaderLines', startRow(block)-1, 'ReturnOnError', false, 'EndOfLine', '\r\n');
for col=1:length(dataArray)
dataArray{col} = [dataArray{col};dataArrayBlock{col}];
end
end
% Close the text file.
fclose(fileID);
% Create output variable
data = table(dataArray{1:end-1}, 'VariableNames', {'date','results','latency'});

