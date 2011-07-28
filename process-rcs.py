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
    seed, iter, width, height, nodes, users, channels, source, dest, dijkstra, prim, rcs = row

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
    dijkstra = float(dijkstra)
    prim = float(prim)
    rcs = float(rcs)

    if data[width][nodes][ch].has_key('dijkstra'):
        data[width][nodes][ch]['dijkstra'].append(dijkstra)
    else:
        data[width][nodes][ch]['dijkstra'] = [dijkstra]

    if data[width][nodes][ch].has_key('prim'):
        data[width][nodes][ch]['prim'].append(prim)
    else:
        data[width][nodes][ch]['prim'] = [prim]

    if data[width][nodes][ch].has_key('rcs'):
        data[width][nodes][ch]['rcs'].append(rcs)
    else:
        data[width][nodes][ch]['rcs'] = [rcs]

for width in sorted(data.keys()):
    for nodes in sorted(data[width].keys()):
        for channels in sorted(data[width][nodes].keys()):
            rd = data[width][nodes][channels]
            rd['dijkstra'] = simplestats.mean(rd['dijkstra'])
            rd['prim'] = simplestats.mean(rd['prim'])
            rd['rcs'] = simplestats.mean(rd['rcs'])
            
# Scenario 1: vary # of channels in {1, 2, 3, 4, 5}
d = data[50000.0][25]
dij = []
pri = []
rcs = []
for channels in sorted(d.keys()):
    dij.append(str(d[channels]['dijkstra'] / 1000000))
    pri.append(str(d[channels]['prim'] / 1000000))
    rcs.append(str(d[channels]['rcs'] / 1000000))
    
print("""
figure(1);
set(1, \"defaulttextfontname\", \"Times-Roman\");
set(1, \"defaultaxesfontname\", \"Times-Roman\");
set(1, \"defaulttextfontsize\", 19);
set(1, \"defaultaxesfontsize\", 19);

X = %(idx)s;
OPT = [ %(dijkstra)s ];
R1 = [ %(prim)s ];
R2 = [ %(rcs)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,R1,':*b','LineWidth',3);
plot(X,R2,'-.sc','LineWidth',3);

xlabel('Number of Channels per Band');
ylabel('Average Throughput (kb^2)');
legend('CS-ShortestPath','RCS-Bottleneck','RCS-PathExtend', \"Location\", \"SouthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
axis([X(1), X(length(X))]);
replot;
fixAxes;
hold off;
""" % {'idx' : sorted(d.keys()), 'dijkstra' : ", ".join(dij), 'prim' : ", ".join(pri), 'rcs' : ", ".join(rcs)})

# Scenario 2: vary size keeping density constant
d = data
dij = []
pri = []
rcs = []
idx = [3, 4, 5, 6, 7]
for width in idx:
    nodes = math.pow(width, 2)
    width = width * 10000.0
    dij.append(str(d[width][nodes][3]['dijkstra'] / 1000000))
    pri.append(str(d[width][nodes][3]['prim'] / 1000000))
    rcs.append(str(d[width][nodes][3]['rcs'] / 1000000))

idx = [int(math.pow(x,2)) for x in idx]

print("""
figure(2);
set(2, \"defaulttextfontname\", \"Times-Roman\");
set(2, \"defaultaxesfontname\", \"Times-Roman\");
set(2, \"defaulttextfontsize\", 19);
set(2, \"defaultaxesfontsize\", 19);

X = %(idx)s;
OPT = [ %(dij)s ];
R1 = [ %(pri)s ];
R2 = [ %(rcs)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,R1,':*b','LineWidth',3);
plot(X,R2,'-.sc','LineWidth',3);

xlabel('Network Area (km^2)');
ylabel('Average Throughput (kb^2)');
legend('CS-ShortestPath','RCS-Bottleneck','RCS-PathExtend', \"Location\", \"NorthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
axis([X(1), X(length(X))]);
replot;
fixAxes;
hold off;
""" % {'idx' : idx, 'dij' : ", ".join(dij), 'pri' : ", ".join(pri), 'rcs' : ", ".join(rcs)})

# Scenario 3: vary node density
d = data[50000.0]
dij = []
pri = []
rcs = []
idx = [9, 16, 25, 36, 49]

for nodes in idx:
    dij.append(str(d[nodes][3]['dijkstra'] / 1000000))
    pri.append(str(d[nodes][3]['prim'] / 1000000))
    rcs.append(str(d[nodes][3]['rcs'] / 1000000))

print("""
figure(3);
set(3, \"defaulttextfontname\", \"Times-Roman\");
set(3, \"defaultaxesfontname\", \"Times-Roman\");
set(3, \"defaulttextfontsize\", 19);
set(3, \"defaultaxesfontsize\", 19);

X = %(idx)s;
OPT = [ %(dijkstra)s ];
R1 = [ %(prim)s ];
R2 = [ %(rcs)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,R1,':*b','LineWidth',3);
plot(X,R2,'-.sc','LineWidth',3);

xlabel('Node Density (Nodes/50 km^2)');
ylabel('Average Throughput (kb^2)');
legend('CS-ShortestPath','RCS-Bottleneck','RCS-PathExtend', \"Location\", \"NorthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
axis([X(1), X(length(X))]);
replot;
fixAxes;
hold off;
""" % {'idx' : idx, 'dijkstra' : ", ".join(dij), 'prim' : ", ".join(pri), 'rcs' : ", ".join(rcs)})

