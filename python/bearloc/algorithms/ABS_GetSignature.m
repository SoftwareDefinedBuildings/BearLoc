function [sig, freq] = ABS_GetSignature(filename, varargin)
%TODO: add timestamp as return value

% Parameters
sigPcnt = 0.05; % Percentile in sorted spectrum for signature
freqLoBnd = 0; % Lower bound of sampling frequency (Hz)
freqUpBnd = 7000; % Upper bound of sampling frequency (Hz)
freqItvl = 1; % Frequency interval of final results
frameTime = 0.1; % Frame Time (second)
winFunc = @hamming; % Window function
interpFunc = 'linear'; % Interpolation method
startTime = 0;
stopTime = inf;

if nargin > 1
    sigPcnt = varargin{1};
end
if nargin > 2
    freqLoBnd = varargin{2};
end
if nargin > 3
    freqUpBnd = varargin{3};
end
if nargin > 4
    freqItvl = varargin{4};
end
if nargin > 5
    frameTime = varargin{5};
end
if nargin > 6
    winFunc = varargin{6};
end
if nargin > 7
    interpFunc = varargin{7};
end
if nargin > 8
    startTime = varargin{8};
end
if nargin > 9
    stopTime = varargin{9};
end


% Read audio samples
[wave, rate] = audioread(filename);
wave = wave(rate*startTime+1 : min(rate*stopTime, length(wave)));
frameLen = frameTime * rate;

% parameter check -
% There are numbers of checks, here I just list two of them
if freqUpBnd <= freqLoBnd
    error(strcat('Frequency Upper Bound (now %d) has to be larger', ...
        ' than lower bound (now %d)!'), freqUpBnd, freqLoBnd);
end

if freqUpBnd >= rate*(frameLen-1)/frameLen
    error(strcat('Frequency Upper Bound (now %d) has to be smaller', ...
        ' than sampling rate*(sample#-1)/sample# (now %d)!'), ...
        freqUpBnd, rate*(frameLen-1)/frameLen);
end

% Divide samples into frames (each frame per column)
waveLen = length(wave);
frameNum = floor(waveLen/frameLen);
frames = zeros(frameLen, frameNum);
cursor = 1;
for i = 1:frameNum
    frames(:, i) = wave(cursor:cursor + frameLen - 1, 1);
    cursor = cursor + frameLen;
end

% Multiply frames by a window function
win = window(winFunc, frameLen);
frames = repmat(win, 1, frameNum) .* frames;

% Compute power spectrum of each time
frameSpecs = fft(frames); % fft will be executed for each column separately
frameSpecsPower = frameSpecs .* conj(frameSpecs)/frameLen;

% Discard rows out of freqLoBnd-freqUpBnd Hz
freq = (0:frameLen-1) * (rate/frameLen);

tmp = freq - freqLoBnd;
tmp(tmp > 0) = -inf;
[~, loIdx] = max(tmp);

tmp = freq - freqUpBnd;
tmp(tmp < 0) = inf;
[~, upIdx] = min(tmp);

freq = freq(loIdx):(rate/frameLen):freq(upIdx);
frameLen = upIdx - loIdx + 1;
frameSpecsPower = frameSpecsPower(loIdx:upIdx, :);

% Sort each remaining column
frameSpecsPower = [frameSpecsPower; mean(frameSpecsPower)];
frameSpecsPower = sortrows(frameSpecsPower', frameLen + 1)';
frameSpecsPower = frameSpecsPower(1:frameLen, :);

% Extract sigPcnt percentile column and take logarithm
freqRaw = freq;
sigRaw = frameSpecsPower(:, floor(sigPcnt * frameNum));
freq = freqLoBnd:freqItvl:freqUpBnd;
sig = interp1(freqRaw, sigRaw, freq, interpFunc);
