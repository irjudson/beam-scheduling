figure(1);
X = [ 40, 50, 60, 70, 80, 90 ];
ILP = [ 1.744077898653958E11, 1.8525695188615323E11, 1.6479379959662814E11, 1.8451677712306924E11, 1.745817841835061E11, 1.545910745668289E11 ];
LPR = [ 1.6917295928978537E11, 1.7796362449027496E11, 1.5679469229189456E11, 1.780775591273806E11, 1.6531757970551733E11, 1.3914900710272247E11 ];
GDY = [ 1.7203681328163528E11, 1.7661181195908386E11, 1.5731029969932584E11, 1.792270267604487E11, 1.6531757970551733E11, 1.4108430119628653E11 ];
 
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


