function db = ABS_GetDB(dbPath, dbName, rawPath, rawConfig)

if exist([dbPath, dbName], 'file')
    load([dbPath, dbName]);
    
else
    
    splitNum = 1;  % split each record file to multiple fignerprints
    frameLen = 60/splitNum; % frameLength of each slpit 
   
    dbSize = size(rawConfig, 1) * splitNum;
    % ABS_SigDB columns: Freqs, Sig, Loc (5 columns)
    ABS_SigDB = cell(dbSize, 4);
    for i = 1:dbSize
        [ABS_SigDB{i, 2}, ABS_SigDB{i, 1}] = ...
            ABS_GetSignature([rawPath, rawConfig{ceil(i/splitNum), 1}], 0.05, 0, ...
            7000, 1, 0.1, @hamming, 'linear', ...
            floor(mod(i-1,splitNum)*frameLen), floor(mod(i-1,splitNum)*frameLen) + frameLen);
        ABS_SigDB(i, 3:7) = rawConfig(ceil(i/splitNum), 3:7); % the index is hardcoded 
    end

    save([dbPath, dbName], 'ABS_SigDB');
    disp('New Database Generated.');
    
end

db = ABS_SigDB;
