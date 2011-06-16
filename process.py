#!/usr/bin/env python
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
    seed, width, height, theta, relays, subs, slot_length, mean_q, ilp, lpr, gdy = row
    if seed == "Seed":
        continue

    width = float(width)
    height = float(height)
    theta = int(theta)
    relays = int(relays)
    subs = int(subs)
    mq = float(mean_q)
    ilp = float(ilp)
    lpr = float(lpr)
    gdy = float(gdy)

    if data[width][theta][relays][subs][mq].has_key('ilp'):
        data[width][theta][relays][subs][mq]['ilp'].append(ilp)
    else:
        data[width][theta][relays][subs][mq]['ilp'] = [ilp]

    if data[width][theta][relays][subs][mq].has_key('lpr'):
        data[width][theta][relays][subs][mq]['lpr'].append(lpr)
    else:
        data[width][theta][relays][subs][mq]['lpr'] = [lpr]

    if data[width][theta][relays][subs][mq].has_key('gdy'):
        data[width][theta][relays][subs][mq]['gdy'].append(gdy)
    else:
        data[width][theta][relays][subs][mq]['gdy'] = [gdy]

for width in sorted(data.keys()):
    for theta in sorted(data[width].keys()):
        for relay in sorted(data[width][theta].keys()):
            for subs in sorted(data[width][theta][relay].keys()):
                for mq in sorted(data[width][theta][relay][subs].keys()):
                    rd = data[width][theta][relay][subs][mq]
                    lilp = len(rd['ilp'])
                    llpr = len(rd['lpr'])
                    lgdy = len(rd['gdy'])
#                    if lilp != 10 or llpr != 10 or lgdy != 10:
#                        print("Missing/Extra (" + ",".join([str(lilp), str(llpr), str(lgdy)]) +
#                              ") data for " + ", ".join(map(lambda x: str(x), [width, theta, relay, subs, mq])))
                            
#                    print(", ".join(map(lambda x: str(x),
#                                        [width, theta, relay, subs, mq, simplestats.mean(rd['ilp']),
#                                         simplestats.mean(rd['lpr']), simplestats.mean(rd['gdy']) ])))
                    rd['ilp'] = simplestats.mean(rd['ilp']) / 1024*1024
                    rd['lpr'] = simplestats.mean(rd['lpr']) / 1024*1024
                    rd['gdy'] = simplestats.mean(rd['gdy']) / 1024*1024

# Scenario 1: vary # of subscribers in {20, 30, 40, 50, 60}
d = data[40000.0][40][10]
#print("Scenario 1: vary # of subscribers in {20, 30, 40, 50, 60}")
ilp = []
lpr = []
gdy = []
for subs in sorted(d.keys()):
    ilp.append(str(d[subs][40000.0]['ilp']))
    lpr.append(str(d[subs][40000.0]['lpr']))
    gdy.append(str(d[subs][40000.0]['gdy']))
    
#    print("%d, %f, %f, %f" % (subs, d[subs][40000.0]['ilp'], d[subs][40000.0]['lpr'], d[subs][40000.0]['gdy']))

print("""
figure(1);
X = [ %(idx)s ];
OPT = [ %(ilp)s ];
LPR = [ %(lpr)s ];
GDY = [ %(gdy)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,LPR,':*b','LineWidth',3);
plot(X,GDY,'-.sc','LineWidth',3);

xlabel('Number of Subscribers');
ylabel('Kb^2');
legend('Optimal','LP Rounding','Greedy', \"Location\", \"NorthWest\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
hold off;
fixAxes;
print(1, \"scenario1.eps\", \"-deps\");
print(1, \"scenario1.pdf\", \"-dpdf\");
""" % {'idx' : sorted(d.keys()), 'ilp' : ", ".join(ilp), 'lpr' : ", ".join(lpr), 'gdy' : ", ".join(gdy)})

# Scenario 2: vary # of relays in {6, 8, 10, 12, 14}
d = data[40000.0][40]
#print("Scenario 2: vary # of relays in {6, 8, 10, 12, 14}")
ilp = []
lpr = []
gdy = []
for relays in sorted(d.keys()):
    ilp.append(str(d[relays][40][40000.0]['ilp']))
    lpr.append(str(d[relays][40][40000.0]['lpr']))
    gdy.append(str(d[relays][40][40000.0]['gdy']))
#    print("%d, %f, %f, %f" % (relays, d[relays][40][40000.0]['ilp'],
#                              d[relays][40][40000.0]['lpr'], d[relays][40][40000.0]['gdy']))

print("""
figure(2);
X = [ %(idx)s ];
OPT = [ %(ilp)s ];
LPR = [ %(lpr)s ];
GDY = [ %(gdy)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,LPR,':*b','LineWidth',3);
plot(X,GDY,'-.sc','LineWidth',3);

xlabel('Number of Relays');
ylabel('Kb^2');
legend('Optimal','LP Rounding','Greedy', \"Location\", \"SouthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
hold off;
fixAxes;
print(2, \"scenario2.eps\", \"-deps\");
print(2, \"scenario2.pdf\", \"-dpdf\");
""" % {'idx' : sorted(d.keys()), 'ilp' : ", ".join(ilp), 'lpr' : ", ".join(lpr), 'gdy' : ", ".join(gdy)})

# Scenario 3: vary theta in {20, 30, 40, 50, 60}
#print("Scenario 3: vary theta in {20, 30, 40, 50, 60}")
d = data[40000.0]
ilp = []
lpr = []
gdy = []
for theta in sorted(d.keys()):
    ilp.append(str(d[theta][10][40][40000.0]['ilp']))
    lpr.append(str(d[theta][10][40][40000.0]['lpr']))
    gdy.append(str(d[theta][10][40][40000.0]['gdy']))
#    print("%d, %f, %f, %f" % (theta, d[theta][10][40][40000.0]['ilp'],
#                              d[theta][10][40][40000.0]['lpr'], d[theta][10][40][40000.0]['gdy']))

print("""
figure(3);
X = [ %(idx)s ];
OPT = [ %(ilp)s ];
LPR = [ %(lpr)s ];
GDY = [ %(gdy)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,LPR,':*b','LineWidth',3);
plot(X,GDY,'-.sc','LineWidth',3);

xlabel('Theta');
ylabel('Kb^2');
legend('Optimal','LP Rounding','Greedy', \"Location\", \"NorthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
hold off;
fixAxes;
print(3, \"scenario3.eps\", \"-deps\");
print(3, \"scenario3.pdf\", \"-dpdf\");
""" % {'idx' : sorted(d.keys()), 'ilp' : ", ".join(ilp), 'lpr' : ", ".join(lpr), 'gdy' : ", ".join(gdy)})

# Scenario 4: vary meanq in {20000, 30000, 40000, 50000, 60000}
#print("Scenario 4: vary meanq in {20000, 30000, 40000, 50000, 60000}")
d = data[40000.0][40][10][40]
ilp = []
lpr = []
gdy = []
for mq in sorted(d.keys()):
    ilp.append(str(d[mq]['ilp']))
    lpr.append(str(d[mq]['lpr']))
    gdy.append(str(d[mq]['gdy']))
#    print("%d, %f, %f, %f" % (mq, d[mq]['ilp'], d[mq]['lpr'], d[mq]['gdy']))

print("""
figure(4);
X = [ %(idx)s ];
OPT = [ %(ilp)s ];
LPR = [ %(lpr)s ];
GDY = [ %(gdy)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,LPR,':*b','LineWidth',3);
plot(X,GDY,'-.sc','LineWidth',3);

xlabel('Mean Q');
ylabel('Kb^2');
legend('Optimal','LP Rounding','Greedy', \"Location\", \"NorthWest\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
hold off;
fixAxes;
print(4, \"scenario4.eps\", \"-deps\");
print(4, \"scenario4.pdf\", \"-dpdf\");
""" % {'idx' : sorted(d.keys()), 'ilp' : ", ".join(ilp), 'lpr' : ", ".join(lpr), 'gdy' : ", ".join(gdy)})

# Scenario 5: vary region size {20, 30, 40, 50, 60} km per side
#print("Scenario 5: vary region size {20, 30, 40, 50, 60} km per side")
d = data
ilp = []
lpr = []
gdy = []
for size in sorted(d.keys()):
    ilp.append(str(d[size][40][10][40][40000]['ilp']))
    lpr.append(str(d[size][40][10][40][40000]['lpr']))
    gdy.append(str(d[size][40][10][40][40000]['gdy']))

#    print("%d, %f, %f, %f" % (size, d[size][40][10][40][40000]['ilp'],
#                              d[size][40][10][40][40000]['lpr'], d[size][40][10][40][40000]['gdy']))
print("""
figure(5);
X = [ %(idx)s ];
OPT = [ %(ilp)s ];
LPR = [ %(lpr)s ];
GDY = [ %(gdy)s ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,LPR,':*b','LineWidth',3);
plot(X,GDY,'-.sc','LineWidth',3);

xlabel('km^2');
ylabel('Kb^2');
legend('Optimal','LP Rounding','Greedy', \"Location\", \"NorthEast\")
set(gca, 'XTickMode', 'manual', 'XTick', X);
hold off;
fixAxes;
print(5, \"scenario5.eps\", \"-deps\");
print(5, \"scenario5.pdf\", \"-dpdf\");
""" % {'idx' : sorted(d.keys()), 'ilp' : ", ".join(ilp), 'lpr' : ", ".join(lpr), 'gdy' : ", ".join(gdy)})
