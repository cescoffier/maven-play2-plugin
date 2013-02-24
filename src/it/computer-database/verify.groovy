import java.io.*;

def artifactNameWithoutExtension = "target/it-computer-database-1.0-SNAPSHOT"

// check the main jar artifact
def jar = new File (basedir, artifactNameWithoutExtension + ".jar");
assert jar.exists();
assert jar.canRead();

// check the zip distribution
def dist = new File (basedir, artifactNameWithoutExtension + ".zip");
assert dist.exists();
assert dist.canRead();

// check the war file
def war = new File (basedir, artifactNameWithoutExtension + ".war");
assert war.exists();
assert war.canRead();

