package beamscheduling;

import java.util.*;
import ilog.concert.*;
import ilog.cplex.*;

class LPRound {

    Network network;
    double threshold = 0.0001;

    public LPRound(Network<Vertex, Edge> network) {
        this.network = network;
    }

    public double solve() {
        // create an ILP
        Vertex[] relay = network.relayList;
        int numRelays = network.relayList.length;
        int numSubscribers = network.subList.length;
        Vertex[] subL = network.subList;
        int numThetas = Network.thetaSet.length;
        double rate[][][] = new double[numRelays][numSubscribers][numThetas];


        for (int i = 0; i < numRelays; i++) {
            for (int j = 0; j < numSubscribers; j++) {
                for (int k = 0; k < numThetas; k++) {
                    rate[i][j][k] = relay[i].calculateThroughput(network.thetaSet[k], subL[j]) * network.timeslotLength;
                }
            }
        }

        try {
            IloCplex cplex = new IloCplex();

            // x[i][j][k]: relay i transmits to subscriber j using beam theta_k
            IloNumVar[][][] x = new IloNumVar[numRelays][numSubscribers][numThetas];
            for (int i = 0; i < numRelays; i++) {
                for (int j = 0; j < numSubscribers; j++) {
                    for (int k = 0; k < numThetas; k++) {
                        // I. 0 <= x[u][v][j] <= 1
                        x[i][j][k] = cplex.numVar(0, 1, "x(" + i + ")(" + j + ")(" + k + ")");


                        // if c[i][j][k] = 0, set x[i][j][k] to zero
                        if (rate[i][j][k] < threshold) {
                            cplex.addEq(0, x[i][j][k]);
                        }
                    }
                }
            }

            // s[i][k][l]: relay i uses beam theta_k and picks beam set l
            IloNumVar[][][] s = new IloNumVar[numRelays][numThetas][];
            for (int i = 0; i < numRelays; i++) {
                for (int k = 0; k < numThetas; k++) {
                    int numSets = network.beamSet[i][k].length;
                    s[i][k] = new IloNumVar[numSets];
                    for (int l = 0; l < numSets; l++) {
                        s[i][k][l] = cplex.numVar(0, 1, "s(" + i + ")(" + k + ")(" + l + ")");
                    }
                }
            }


            for (int j = 0; j < numSubscribers; j++) {
                IloLinearNumExpr expr = cplex.linearNumExpr();
                for (int i = 0; i < numRelays; i++) {
                    for (int k = 0; k < numThetas; k++) {
                        expr.addTerm(1, x[i][j][k]);
                    }
                }
                cplex.addLe(expr, 1);
            }

            for (int i = 0; i < numRelays; i++) {
                IloLinearNumExpr expr = cplex.linearNumExpr();
                for (int k = 0; k < numThetas; k++) {
                    int numSets = network.beamSet[i][k].length;
                    for (int l = 0; l < numSets; l++) {
                        expr.addTerm(1, s[i][k][l]);
                    }
                }
                cplex.addLe(expr, 1);
            }

            for (int i = 0; i < numRelays; i++) {
                for (int j = 0; j < numSubscribers; j++) {
                    for (int k = 0; k < numThetas; k++) {
                        IloLinearNumExpr lhs = cplex.linearNumExpr();
                        int numSets = network.beamSet[i][k].length;
                        for (int l = 0; l < numSets; l++) {
                            if (network.beamSet[i][k][l].contains(subL[j])) {
                                lhs.addTerm(1, s[i][k][l]);
                            }
                        }
                        IloLinearNumExpr rhs = cplex.linearNumExpr();
                        rhs.addTerm(1, x[i][j][k]);
                        cplex.addGe(lhs, rhs);
                    }
                }
            }


            IloNumVar[] y = new IloNumVar[numSubscribers];
            for (int j = 0; j < numSubscribers; j++) {
                y[j] = cplex.numVar(0, subL[j].queueLength, "y(" + j + ")");
                cplex.addLe(y[j], subL[j].queueLength);
                IloLinearNumExpr expr = cplex.linearNumExpr();
                for (int i = 0; i < numRelays; i++) {
                    for (int k = 0; k < numThetas; k++) {
                        expr.addTerm(rate[i][j][k], x[i][j][k]);
                    }
                }
                cplex.addLe(y[j], expr);
            }

            // maximize the following 
            IloLinearNumExpr maximizeExpr = cplex.linearNumExpr();
            for (int j = 0; j < numSubscribers; j++) {
                maximizeExpr.addTerm(subL[j].queueLength, y[j]);
            }


            // solve the problem
            IloObjective obj = cplex.maximize(maximizeExpr);
            cplex.add(obj);
            cplex.setOut(null);
//            cplex.exportModel("MaxTotalWeight.lp");
            if (cplex.solve()) {
                double cplexTotal = cplex.getObjValue();
                //System.out.println("MILP objective value = " + cplexTotal);
                //System.out.println("Solution status = " + cplex.getStatus());


                double xVal[][][] = new double[numRelays][numSubscribers][numThetas];
                double xSum[][] = new double[numRelays][numSubscribers];
                for (int i = 0; i < numRelays; i++) {
                    for (int j = 0; j < numSubscribers; j++) {
                        xSum[i][j] = 0.0;
                        for (int k = 0; k < numThetas; k++) {
                            xVal[i][j][k] = cplex.getValue(x[i][j][k]);
                            xSum[i][j] += xVal[i][j][k];
                        }
                    }
                }

                // Loop 2: Choose the beam sets for each relay
                for (int i = 0; i < network.relayList.length; i++) {
                    Vertex r = network.relayList[i];
                    for (int k = 0; k < network.thetaSet.length; k++) {
                        double bestReward = 0.0;
                        for (int l = 0; l < network.beamSet[i][k].length; l++) {
                            double reward = 0.0;
                            HashSet<Vertex> beamSet = network.beamSet[i][k][l];
                            for (int j = 0; j < numSubscribers; j++) {
                                if (beamSet.contains(subL[j])) {
                                    reward += subL[j].queueLength * Math.min(subL[j].queueLength, rate[i][j][k] * xSum[i][j]);
                                }
                            }
                            //System.out.println("r: " + r + " k: " + k + " l: " + l + "  reward = " + reward);
                            if (reward >= bestReward) {
                                bestReward = reward;
                                r.bestK = k;
                                r.bestL = l;
                            }
                        }
                    }
                    //System.out.println("relay " + r + ": best theta " + network.thetaSet[r.bestK] + " best l " + r.bestL);

                }


                // Loop 3: Find a better Relay if there is one
                // and it's beam is covering this node
                double objectiveVal = 0.0;

                for (int i = 0; i < network.subList.length; i++) {
                    Vertex sub = network.subList[i];
                    double maxThroughput = 0.0;
                    sub.preferredRelay = null;
                    for (int j = 0; j < network.relayList.length; j++) {
                        Vertex r = network.relayList[j];
                        HashSet<Vertex> bestBeamSet = network.beamSet[j][r.bestK][r.bestL];
                        double throughput = r.calculateThroughput(network.thetaSet[r.bestK], sub) * network.timeslotLength;
                        if (bestBeamSet.contains(sub) && throughput > maxThroughput) {
                            maxThroughput = throughput;
                            sub.preferredRelay = r;
                        }
                    }
                    objectiveVal += sub.queueLength * Math.min(sub.queueLength, maxThroughput);

                }
                //System.out.println("LPRound throughput = " + overallThroughput);

                return objectiveVal;
            }


        } catch (IloException ex) {
            ex.printStackTrace();
        }
        return -1.0; // error
    }
}
