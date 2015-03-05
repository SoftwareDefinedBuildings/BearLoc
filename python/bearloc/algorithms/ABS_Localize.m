function rv = ABS_Localize(testFile, varargin)

testFile

% If next point have more than cutIncGap distances from
% nearst sigature in DB, cut the result list from next one
cutIncGap = 0.3;
varThrld = 100;

dbPath = './data/cooked/';
dbName = 'ABS_SigDB.mat';
rawPath = './data/raw/';
%rawPath = '';
rawConfig = ABS_Config([rawPath, 'Config.csv']);
% take only training data
trainConfig = rawConfig(strcmp('Train', rawConfig(:,8)), :);

db = ABS_GetDB(dbPath, dbName, rawPath, trainConfig);
%Add this to test DB point number vs accracy, remove this line soon after
%that, sorry for bad coding style
%db = removerows(db,'ind',varargin{2});

if nargin == 1
    sig = ABS_GetSignature(testFile);
else
    sig = varargin{1};
end


distList = zeros(1, size(db, 1));
for i = 1:size(db, 1)
    distList(i) = pdist2(db{i,2}, sig);
end

[distList, distIdx] = sort(distList);
distList = distList';

cutIdx = 1;
for cutIdx = 1:(size(distList, 1) - 1)
    if distList(cutIdx+1)/distList(1) > 1 + cutIncGap
        break;
    end
end

locList = db(distIdx, 3:7);
distList = distList(1:cutIdx);
locList = locList(1:cutIdx, :);

e = 2.7183;
confidence = 1/e^(std([locList{:, 4}])/varThrld)/2 ...
    + 1/e^(std([locList{:, 5}])/varThrld)/2;

if nargin == 1
    if sum(strcmp(testFile, trainConfig(:,1))) ~= 0
        warning('Test file is in training set.');
    end
end

% rv = [locList, distList, confidence];
rv = locList(1,:);
rv
% rv = {locList;};
% rv