#!/usr/bin/env python
#
#
import csv, math, sys
import simplestats
import pprint
from collections import defaultdict

class rdefaultdict(object): 
    def __init__(self, ):
        self.__dd = defaultdict(rdefaultdict) 
    def __getattr__(self,attr):
        return self.__dd.__getattribute__(attr) 
    def __getitem__(self,*args):
        return self.__dd.__getitem__(*args) 
    def __setitem__(self,*args):
        return self.__dd.__setitem__(*args)

filename = sys.argv[1]
data = rdefaultdict()
data_reader = csv.reader(open(filename, 'r'))

for row in data_reader:
#    seed, iter, width, height, nodes, users, channels, source, dest, dijkstra, prim, rcs = row
    seed, iter, width, height, nodes, users, channels, source, dest, spdpcs, spgdy, btldpcs, btlgdy, rcsdpcs, rcs = row

    if seed == "Seed":
        continue

    iter = int(iter)
    width = float(width)
    height = float(height)
    nodes = int(nodes)
    users = int(users)
    ch = int(channels)
    source = int(source)
    dest = int(dest)
    spdpcs = float(spdpcs)
    spgdy = float(spgdy)
    btldpcs = float(btldpcs)
    btlgdy = float(btlgdy)
    rcsdpcs = float(rcsdpcs)
    rcs = float(rcs)

    if data[width][nodes][ch].has_key('spdpcs'):
        data[width][nodes][ch]['spdpcs'].append(spdpcs)
    else:
        data[width][nodes][ch]['spdpcs'] = [spdpcs]

    if data[width][nodes][ch].has_key('spgdy'):
        data[width][nodes][ch]['spgdy'].append(spgdy)
    else:
        data[width][nodes][ch]['spgdy'] = [spgdy]

    if data[width][nodes][ch].has_key('btldpcs'):
        data[width][nodes][ch]['btldpcs'].append(btldpcs)
    else:
        data[width][nodes][ch]['btldpcs'] = [btldpcs]

    if data[width][nodes][ch].has_key('btlgdy'):
        data[width][nodes][ch]['btlgdy'].append(btlgdy)
    else:
        data[width][nodes][ch]['btlgdy'] = [btlgdy]

    if data[width][nodes][ch].has_key('rcsdpcs'):
        data[width][nodes][ch]['rcsdpcs'].append(rcsdpcs)
    else:
        data[width][nodes][ch]['rcsdpcs'] = [rcsdpcs]

    if data[width][nodes][ch].has_key('rcs'):
        data[width][nodes][ch]['rcs'].append(rcs)
    else:
        data[width][nodes][ch]['rcs'] = [rcs]

for width in sorted(data.keys()):
    for nodes in sorted(data[width].keys()):
        for channels in sorted(data[width][nodes].keys()):
            rd = data[width][nodes][channels]
            rd['spdpcs'] = simplestats.mean(rd['spdpcs'])
            rd['spgdy'] = simplestats.mean(rd['spgdy'])
            rd['btldpcs'] = simplestats.mean(rd['btldpcs'])
            rd['btlgdy'] = simplestats.mean(rd['btlgdy'])
            rd['rcsdpcs'] = simplestats.mean(rd['rcsdpcs'])
            rd['rcs'] = simplestats.mean(rd['rcs'])
            
# Scenario 1: vary # of channels in {1, 2, 3, 4, 5}
d = data[50000.0][25]
spdpcs = []
spgdy = []
btldpcs = []
btlgdy = []
rcsdpcs = []
rcs = []
idx = [9, 16, 25, 36, 49];
idx = [ 3, 6, 9, 12, 15 ];

for nodes in d.keys():
    print data[50000.0][25].keys()
    spdpcs.append(str(d[nodes]['spdpcs'] / 1000000))
    spgdy.append(str(d[nodes]['spgdy'] / 1000000))
    btldpcs.append(str(d[nodes]['btldpcs'] / 1000000))
    btlgdy.append(str(d[nodes]['btlgdy'] / 1000000))
    rcsdpcs.append(str(d[nodes]['rcsdpcs'] / 1000000))
    rcs.append(str(d[nodes]['rcs'] / 1000000))
    
print("""
figure(1);
set(1, \"defaulttextfontname\", \"Times-Roman\");
set(1, \"defaultaxesfontname\", \"Times-Roman\");
set(1, \"defaulttextfontsize\", 11);
set(1, \"defaultaxesfontsize\", 11);

X = %(idx)s;
OPT = [ %(spdpcs)s ];
R1 = [ %(spgdy)s ];
R2 = [ %(btldpcs)s ];
R3 = [ %(btlgdy)s ];
R4 = [ %(rcsdpcs)s ];
R5 = [ %(rcs)s ];

plot(X, R4, '-xb',  'LineWidth', 5);
hold on;
plot(X, R5, '--xb', 'LineWidth', 5);
plot(X, R2, '-or',  'LineWidth', 5);
plot(X, R3, '-:or', 'LineWidth', 5);
plot(X,OPT,'-+k',   'LineWidth', 5);
plot(X, R1, '-.+k', 'LineWidth', 5);

xlabel('Number of Channels');
ylabel('Average Throughput (Mbps)');
legend('RCS-DPCS', 'RCS', 'Btl-DPCS', 'Btl-Gdy', 'SP-DPCS','SP-Gdy', \"Location\", \"SouthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
axis([X(1), X(length(X))]);
refresh();
fixAxes;
hold off;
print -deps -color figure1.eps
""" % {'idx' : sorted(idx), 'spdpcs' : ", ".join(spdpcs), 
       'spgdy' : ", ".join(spgdy), 'rcs' : ", ".join(rcs), 'btldpcs' : ", ".join(btldpcs), 
       'btlgdy' : ", ".join(btlgdy), 'rcsdpcs' : ", ".join(rcsdpcs)})

# Scenario 2: vary size keeping density constant
d = data
spdpcs = []
spgdy = []
btldpcs = []
btlgdy = []
rcsdpcs = []
rcs = []
idx = [(30000.0, 9), (40000.0, 16), (50000.0, 25), (60000.0, 36), (70000.0, 49)]

for (size,nodes) in idx:
    spdpcs.append(str(d[size][nodes][3]['spdpcs'] / 1000000))
    spgdy.append(str(d[size][nodes][3]['spgdy'] / 1000000))
    btldpcs.append(str(d[size][nodes][3]['btldpcs'] / 1000000))
    btlgdy.append(str(d[size][nodes][3]['btlgdy'] / 1000000))
    rcsdpcs.append(str(d[size][nodes][3]['rcsdpcs'] / 1000000))
    rcs.append(str(d[size][nodes][3]['rcs'] / 1000000))

idx = [30, 40, 50, 60, 70];

print("""
figure(2);
set(2, \"defaulttextfontname\", \"Times-Roman\");
set(2, \"defaultaxesfontname\", \"Times-Roman\");
set(2, \"defaulttextfontsize\", 11);
set(2, \"defaultaxesfontsize\", 11);

X = %(idx)s;
OPT = [ %(spdpcs)s ];
R1 = [ %(spgdy)s ];
R2 = [ %(btldpcs)s ];
R3 = [ %(btlgdy)s ];
R4 = [ %(rcsdpcs)s ];
R5 = [ %(rcs)s ];

plot(X, R4, '-xb',  'LineWidth', 5);
hold on;
plot(X, R5, '--xb', 'LineWidth', 5);
plot(X, R2, '-or',  'LineWidth', 5);
plot(X, R3, '-:or', 'LineWidth', 5);
plot(X,OPT,'-+k',   'LineWidth', 5);
plot(X, R1, '-.+k', 'LineWidth', 5);

xlabel('Region sidelength (km)');
ylabel('Average Throughput (Mbps)');
legend('RCS-DPCS', 'RCS', 'Btl-DPCS', 'Btl-Gdy', 'SP-DPCS','SP-Gdy', \"Location\", \"NorthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
axis([X(1), X(length(X))]);
refresh();
fixAxes;
hold off;
print -deps -color figure2.eps
""" % {'idx' : sorted(idx), 'spdpcs' : ", ".join(spdpcs), 
       'spgdy' : ", ".join(spgdy), 'rcs' : ", ".join(rcs), 'btldpcs' : ", ".join(btldpcs), 
       'btlgdy' : ", ".join(btlgdy), 'rcsdpcs' : ", ".join(rcsdpcs)})

# Scenario 3: vary node density
d = data[50000.0]
spdpcs = []
spgdy = []
btldpcs = []
btlgdy = []
rcsdpcs = []
rcs = []
idx = [9, 16, 25, 36, 49]

for nodes in idx:
    spdpcs.append(str(d[nodes][3]['spdpcs'] / 1000000))
    spgdy.append(str(d[nodes][3]['spgdy'] / 1000000))
    btldpcs.append(str(d[nodes][3]['btldpcs'] / 1000000))
    btlgdy.append(str(d[nodes][3]['btlgdy'] / 1000000))
    rcsdpcs.append(str(d[nodes][3]['rcsdpcs'] / 1000000))
    rcs.append(str(d[nodes][3]['rcs'] / 1000000))

print("""
figure(3);
set(3, \"defaulttextfontname\", \"Times-Roman\");
set(3, \"defaultaxesfontname\", \"Times-Roman\");
set(3, \"defaulttextfontsize\", 11);
set(3, \"defaultaxesfontsize\", 11);

X = %(idx)s;
OPT = [ %(spdpcs)s ];
R1 = [ %(spgdy)s ];
R2 = [ %(btldpcs)s ];
R3 = [ %(btlgdy)s ];
R4 = [ %(rcsdpcs)s ];
R5 = [ %(rcs)s ];

plot(X, R4, '-xb',  'LineWidth', 5);
hold on;
plot(X, R5, '--xb', 'LineWidth', 5);
plot(X, R2, '-or',  'LineWidth', 5);
plot(X, R3, '-:or', 'LineWidth', 5);
plot(X,OPT,'-+k',   'LineWidth', 5);
plot(X, R1, '-.+k', 'LineWidth', 5);

xlabel('Node Density (Nodes / 50km x 50km region)');
ylabel('Average Throughput (Mbps)');
legend('RCS-DPCS', 'RCS', 'Btl-DPCS', 'Btl-Gdy', 'SP-DPCS','SP-Gdy', \"Location\", \"NorthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
axis([X(1), X(length(X))]);
refresh();
fixAxes;
hold off;

print -deps -color figure3.eps
""" % {'idx' : sorted(d.keys()), 'spdpcs' : ", ".join(spdpcs), 
       'spgdy' : ", ".join(spgdy), 'rcs' : ", ".join(rcs), 'btldpcs' : ", ".join(btldpcs), 
       'btlgdy' : ", ".join(btlgdy), 'rcsdpcs' : ", ".join(rcsdpcs)})
