figure(1);
X = [ 40, 50, 60, 70, 80, 90 ];
ILP = [ 1.5732459450261642E11, 1.6015483312328397E11, 1.7100638923616553E11, 1.518239722791343E11, 1.6359845057517264E11, 1.5571348319983078E11 ];
LPR = [ 1.4722919178530508E11, 1.481470636862612E11, 1.5588126701905328E11, 1.4023308636397385E11, 1.5112727541587714E11, 1.4001120328803687E11 ];
GDY = [ 1.4803978607134985E11, 1.4659355676420728E11, 1.5263155568269977E11, 1.4014345998878763E11, 1.5204425011385156E11, 1.3896512958379767E11 ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,LPR,':*b','LineWidth',3);
plot(X,GDY,'-.sc','LineWidth',3);

xlabel('Theta (degrees)');
ylabel('Total Throughput (Mbps)');

legend('ILP','LPR','GDY', "Location", "NorthWest")

set(gca, 'XTickMode', 'manual', 'XTick', X);
hold off;
fixAxes;
print(1, "scenario1.eps", "-deps");
print(1, "scenario1.pdf", "-dpdf");


