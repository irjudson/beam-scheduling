figure(1);
X = [ 20, 30, 40, 50, 60, 70 ];
ILP = [ 1.5228258336532748E11, 1.5688368362320834E11, 1.5792807969909076E11, 1.763513128149822E11, 1.594361425781629E11, 1.6711473415336606E11 ];
LPR = [ 1.382701043680521E11, 1.4551998256928888E11, 1.5046137308638797E11, 1.7119030374213388E11, 1.5352815540795828E11, 1.637011791741076E11 ];
GDY = [ 1.376307518240298E11, 1.4499559939499368E11, 1.5164228992211642E11, 1.7180265675914737E11, 1.5302419696442474E11, 1.6494607197871533E11 ];

plot(X,OPT,'--og ','LineWidth',3);
hold on;
plot(X,LPR,':*b','LineWidth',3);
plot(X,GDY,'-.sc','LineWidth',3);

xlabel('Number of Subscribers');
ylabel('Total Throughput (Mbps)');

legend('ILP','LPR','GDY', "Location", "NorthWest")

set(gca, 'XTickMode', 'manual', 'XTick', X);
hold off;
fixAxes;
print(1, "scenario1.eps", "-deps");
print(1, "scenario1.pdf", "-dpdf");


