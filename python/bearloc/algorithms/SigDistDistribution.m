clear all;
close all;

load('./data/cooked/ABS_SigDB.mat');

DBSize = size(ABS_SigDB, 1);
sigDistDist = zeros(DBSize*DBSize, 2); % sigDistDistribution: [Physical Dist, Sig Dist]

for i = 1:DBSize
    for j = 1:DBSize
        sigDistDist((i-1)*DBSize+j, 1) = pdist2([ABS_SigDB{i,6:7}], [ABS_SigDB{j,6:7}]);
        sigDistDist((i-1)*DBSize+j, 2) = pdist2(ABS_SigDB{i,2}, ABS_SigDB{j,2});
    end
end

ds = mat2dataset(sigDistDist,'VarNames',{'PDist','SDist'});
stat = grpstats(ds,'PDist', {'mean', 'std', 'min', 'max'})

%errorbar(stat.PDist, stat.mean_SDist, stat.min_SDist, stat.max_SDist);
plot(stat.PDist, stat.mean_SDist);