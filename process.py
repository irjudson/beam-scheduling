#!/usr/bin/env python
#
#
import csv
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
        
data = rdefaultdict()
data_reader = csv.reader(open('output.clean.csv', 'r'))

for row in data_reader:
    seed, width, height, theta, relays, subs, slot_length, mean_q, channels, ilp, r1, r2 = row

    if seed == "Seed":
        continue

    width = float(width)
    height = float(height)
    theta = int(theta)
    relays = int(relays)
    subs = int(subs)
    mq = float(mean_q)
    ch = int(channels)
    ilp = float(ilp)
    r1 = float(r1)
    r2 = float(r2)

    if data[width][theta][relays][subs][mq][ch].has_key('ilp'):
        data[width][theta][relays][subs][mq][ch]['ilp'].append(ilp)
    else:
        data[width][theta][relays][subs][mq][ch]['ilp'] = [ilp]

    if data[width][theta][relays][subs][mq][ch].has_key('r1'):
        data[width][theta][relays][subs][mq][ch]['r1'].append(r1)
    else:
        data[width][theta][relays][subs][mq][ch]['r1'] = [r1]

    if data[width][theta][relays][subs][mq][ch].has_key('r2'):
        data[width][theta][relays][subs][mq][ch]['r2'].append(r2)
    else:
        data[width][theta][relays][subs][mq][ch]['r2'] = [r2]

for width in sorted(data.keys()):
    for theta in sorted(data[width].keys()):
        for relay in sorted(data[width][theta].keys()):
            for subs in sorted(data[width][theta][relay].keys()):
                for mq in sorted(data[width][theta][relay][subs].keys()):
                    for ch in sorted(data[width][theta][relay][subs][mq].keys()):
                        rd = data[width][theta][relay][subs][mq][ch]
                        lilp = len(rd['ilp'])
                        lr1 = len(rd['r1'])
                        lr2 = len(rd['r2'])
                        rd['ilp'] = simplestats.mean(rd['ilp'])
                        rd['r1'] = simplestats.mean(rd['r1'])
                        rd['r2'] = simplestats.mean(rd['r2'])

# Scenario 1: vary # of subscribers in {20, 30, 40, 50, 60}
d = data[40000.0][40][4]
ilp = []
r1 = []
r2 = []
for subs in sorted(d.keys()):
    ilp.append(str(d[subs][40000.0][4]['ilp'] / 1000000))
    r1.append(str(d[subs][40000.0][4]['r1'] / 1000000))
    r2.append(str(d[subs][40000.0][4]['r2'] / 1000000))
    
print("""
figure(1);
set(1, \"defaulttextfontname\", \"Times-Roman\");
set(1, \"defaultaxesfontname\", \"Times-Roman\");
set(1, \"defaulttextfontsize\", 19);
set(1, \"defaultaxesfontsize\", 19);

X = [ %(idx)s ];
OPT = [ %(ilp)s ];
R1 = [ %(r1)s ];
R2 = [ %(r2)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,R1,':*b','LineWidth',3);
plot(X,R2,'-.sc','LineWidth',3);

xlabel('Number of Subscribers');
ylabel('kb^2');
legend('Optimal','Greedy #1','Greedy #2', \"Location\", \"SouthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
fixAxes;
hold off;
""" % {'idx' : sorted(d.keys()), 'ilp' : ", ".join(ilp), 'r1' : ", ".join(r1), 'r2' : ", ".join(r2)})

# Scenario 2: vary # of relays in {6, 8, 10, 12, 14}
d = data[40000.0][40]
ilp = []
r1 = []
r2 = []
for relays in sorted(d.keys()):
    ilp.append(str(d[relays][40][40000.0][4]['ilp'] / 1000000))
    r1.append(str(d[relays][40][40000.0][4]['r1'] / 1000000))
    r2.append(str(d[relays][40][40000.0][4]['r2'] / 1000000))

print("""
figure(2);
set(2, \"defaulttextfontname\", \"Times-Roman\");
set(2, \"defaultaxesfontname\", \"Times-Roman\");
set(2, \"defaulttextfontsize\", 19);
set(2, \"defaultaxesfontsize\", 19);

X = [ %(idx)s ];
OPT = [ %(ilp)s ];
R1 = [ %(r1)s ];
R2 = [ %(r2)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,R1,':*b','LineWidth',3);
plot(X,R2,'-.sc','LineWidth',3);

xlabel('Number of Relays');
ylabel('kb^2');
legend('Optimal','Greedy #1','Greedy #2', \"Location\", \"SouthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
fixAxes;
hold off;
""" % {'idx' : sorted(d.keys()), 'ilp' : ", ".join(ilp), 'r1' : ", ".join(r1), 'r2' : ", ".join(r2)})

# Scenario 3: vary theta in {20, 30, 40, 50, 60}
d = data[40000.0]
ilp = []
r1 = []
r2 = []
for theta in sorted(d.keys()):
    ilp.append(str(d[theta][4][40][40000.0][4]['ilp'] / 1000000))
    r1.append(str(d[theta][4][40][40000.0][4]['r1'] / 1000000))
    r2.append(str(d[theta][4][40][40000.0][4]['r2'] / 1000000))

print("""
figure(3);
set(3, \"defaulttextfontname\", \"Times-Roman\");
set(3, \"defaultaxesfontname\", \"Times-Roman\");
set(3, \"defaulttextfontsize\", 19);
set(3, \"defaultaxesfontsize\", 19);

X = [ %(idx)s ];
OPT = [ %(ilp)s ];
R1 = [ %(r1)s ];
R2 = [ %(r2)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,R1,':*b','LineWidth',3);
plot(X,R2,'-.sc','LineWidth',3);

xlabel('Theta');
ylabel('kb^2');
legend('Optimal','Greedy #1','Greedy #2', \"Location\", \"NorthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
hold off;
""" % {'idx' : sorted(d.keys()), 'ilp' : ", ".join(ilp), 'r1' : ", ".join(r1), 'r2' : ", ".join(r2)})

# Scenario 4: vary meanq in {20000, 30000, 40000, 50000, 60000}
d = data[40000.0][40][4][40]
ilp = []
r1 = []
r2 = []
for mq in sorted(d.keys()):
    ilp.append(str(d[mq][4]['ilp'] / 1000000))
    r1.append(str(d[mq][4]['r1'] / 1000000))
    r2.append(str(d[mq][4]['r2'] / 1000000))

print("""
figure(4);
set(4, \"defaulttextfontname\", \"Times-Roman\");
set(4, \"defaultaxesfontname\", \"Times-Roman\");
set(4, \"defaulttextfontsize\", 19);
set(4, \"defaultaxesfontsize\", 19);

X = [ %(idx)s ];
OPT = [ %(ilp)s ];
R1 = [ %(r1)s ];
R2 = [ %(r2)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,R1,':*b','LineWidth',3);
plot(X,R2,'-.sc','LineWidth',3);

xlabel('Mean Q (kb)');
ylabel('kb^2');
legend('Optimal','Greedy #1','Greedy #2', \"Location\", \"SouthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
hold off;
""" % {'idx' : sorted(map(lambda x: x / 1000, (d.keys()))), 'ilp' : ", ".join(ilp), 'r1' : ", ".join(r1), 'r2' : ", ".join(r2)})

# Scenario 5: vary region size {20, 30, 40, 50, 60} km per side
d = data
ilp = []
r1 = []
r2 = []
for size in sorted(d.keys()):
    ilp.append(str(d[size][40][4][40][40000][4]['ilp'] / 1000000))
    r1.append(str(d[size][40][4][40][40000][4]['r1'] / 1000000))
    r2.append(str(d[size][40][4][40][40000][4]['r2'] / 1000000))

print("""
figure(5);
set(5, \"defaulttextfontname\", \"Times-Roman\");
set(5, \"defaultaxesfontname\", \"Times-Roman\");
set(5, \"defaulttextfontsize\", 19);
set(5, \"defaultaxesfontsize\", 19);
X = [ %(idx)s ];
OPT = [ %(ilp)s ];
R1 = [ %(r1)s ];
R2 = [ %(r2)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,R1,':*b','LineWidth',3);
plot(X,R2,'-.sc','LineWidth',3);

xlabel('Side Length (km)');
ylabel('kb^2');
legend('Optimal','Greedy #1','Greedy #2', \"Location\", \"NorthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X, 'YTickMode', 'manual');
hold off;

fixAxes;
""" % {'idx' : sorted(map(lambda x: x / 1000, d.keys())), 'ilp' : ", ".join(ilp), 'r1' : ", ".join(r1), 'r2' : ", ".join(r2)})

# Scenario 6: vary the number of channels {2, 3, 4, 5, 6} 
d = data[40000.0][40][4][40][40000.0]
ilp = []
r1 = []
r2 = []
for size in sorted(d.keys()):
    ilp.append(str(d[size]['ilp'] / 1000000))
    r1.append(str(d[size]['r1'] / 1000000))
    r2.append(str(d[size]['r2'] / 1000000))

print("""
figure(6);
set(6, \"defaulttextfontname\", \"Times-Roman\");
set(6, \"defaultaxesfontname\", \"Times-Roman\");
set(6, \"defaulttextfontsize\", 19);
set(6, \"defaultaxesfontsize\", 19);
X = [ %(idx)s ];
OPT = [ %(ilp)s ];
R1 = [ %(r1)s ];
R2 = [ %(r2)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,R1,':*b','LineWidth',3);
plot(X,R2,'-.sc','LineWidth',3);

xlabel('Number of Channels');
ylabel('kb^2');
legend('Optimal','Greedy #1','Greedy #2', \"Location\", \"SouthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X, 'YTickMode', 'manual');
hold off;

fixAxes;
""" % {'idx' : sorted(d.keys()), 'ilp' : ", ".join(ilp), 'r1' : ", ".join(r1), 'r2' : ", ".join(r2)})
