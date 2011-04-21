package beamscheduling;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class CmdLineOptions {

    // Execution stuff
  @Option(name = "-w", aliases = {"--width"}, metaVar = "WIDTH",
          usage = "Width of the simulation bounding box.")
    public double width = 30000.0;

  @Option(name = "-h", aliases = {"--height"}, metaVar = "HEIGHT",
          usage = "Height of the simulation bounding box.")
    public double height = 30000.0;

  @Option(name = "-n", aliases = {"--nodes"}, metaVar = "NODES",
          usage = "Number of nodes in the network.")
    public int nodes = 10;

  @Option(name = "-m", aliases = {"--sectors"}, metaVar = "SECTORS",
          usage = "Number of sectors per antenna / node.")
    public int sectors = 8;

  @Option(name = "-s", aliases = {"--seed"}, metaVar = "SEED",
          usage = "Specify the random seed, defaults to a random seed.")
    public long seed = System.nanoTime();
}
