# ZipTrack

## Description
ZipTrack analyses traces of concurrent programs, compressed as SLPs (straight line programs)
and checks if there is a race.
ZipTrack performs two analyses :

	1. HB race detection
	2. LockSet violation detection

ZipTrack is written in Java. 
The following classes let you perform different analyses:

	1. `ZipHB.java` - for HB race detection on compressed traces
	2. `ZipLockSet.java` - for detecting violations of lockset discipline on compressed traces
	3. `ZipMetaInfo.java` - for printing trace characteristics.
	4. `TransformGrammar.java` - for transforming an SLP S into another SLP S' with more production rules that have terminal symbols only.

## Usage

In order to use ZipTrack, you need traces.
We use [RVPredict](https://runtimeverification.com/predict/)'s logger functionality for this.
We then compress these traces as SLPs, using [Sequitur](https://github.com/craignm/sequitur).
These compressed traces can then be analyzed by ZipTrack.

### Generating traces:

1. Download and install [RVPredict](https://runtimeverification.com/predict/).
2. Run the logger :

```
java -jar /path/to/rv-predict.jar  --log  --base-log-dir /path/to/base_folder --log-dirname sub_folder <java_class_to_be_analyzed>

```
This command creates binary log files in `/path/to/base_folder/sub_folder`.

### Compressing traces:

1. First convert the bin files into a readable format. 
For this, run the following command:
```
java -classpath /path/to/ziptrack/bin:/path/to/rapid/lib/* PrintTrace -p=/path/to/base_folder/sub_folder -f=rv > /path/to/base_folder/sub_folder/trace.txt 
```
This command creates two files: `/path/to/base_folder/sub_folder/trace.txt` and `/path/to/base_folder/sub_folder/map.shared.txt`


3. Use Sequitur to compress `trace.txt`. 
For this, first compile Sequitur.
ZipTrack comes with a copy of Sequitur (forked from [here](https://github.com/craignm/sequitur/)).
```
cd /path/to/ziptrack/sequitur/c++
make
/path/to/ziptrack/sequitur/c++/sequitur -d -p -m 2000 < /path/to/base_folder/sub_folder/trace.txt > /path/to/base_folder/sub_folder/slp.txt
```

This command creates the SLP in the file `/path/to/base_folder/sub_folder/slp.txt`.

### Compiling ZipTrack

Use the build file :

```
cd /path/to/ziptrack
ant build.xml
```

### Run ZipTrack

1. [Optional] First transform the grammar:
```
java -Xmx10000m -Xms10000m -classpath /path/to/ziptrack/bin:/path/to/ziptrack/lib/* TransformGrammar -m /path/to/base_folder/sub_folder/map.shared.txt -t /path/to/base_folder/sub_folder/slp.txt  -s > /path/to/base_folder/sub_folder/slp_new.txt
## Replace the old SLP :
cp /path/to/base_folder/sub_folder/slp.txt /path/to/base_folder/sub_folder/slp_old.txt
cp /path/to/base_folder/sub_folder/slp_new.txt /path/to/base_folder/sub_folder/slp.txt 
```

2. Run ZipHB :

java -Xmx10000m -Xms10000m -classpath /path/to/ziptrack/bin:/path/to/ziptrack/lib/* ZipHB -m /path/to/base_folder/sub_folder/map.shared.txt -t /path/to/base_folder/sub_folder/slp.txt  -s 

3. Run ZipLockSet :

java -Xmx10000m -Xms10000m -classpath /path/to/ziptrack/bin:/path/to/ziptrack/lib/* ZipLockSet -m /path/to/base_folder/sub_folder/map.shared.txt -t /path/to/base_folder/sub_folder/slp.txt  -s 

4. Run ZipMetaInfo :

java -Xmx10000m -Xms10000m -classpath /path/to/ziptrack/bin:/path/to/ziptrack/lib/* ZipMetaInfo -m /path/to/base_folder/sub_folder/map.shared.txt -t /path/to/base_folder/sub_folder/slp.txt

### Run analyses on Uncompressed traces

We will use RAPID for this.

1. Run Djit+ VC algorithm :

```
java -Xmx10000m -Xms10000m -classpath /path/to/rapid/bin:/path/to/rapid/lib/* HB -p=/path/to/base_folder/sub_folder -f=rv -s
```

2. Run FastTrack VC algorithm :

```
java -Xmx10000m -Xms10000m -classpath /path/to/rapid/bin:/path/to/rapid/lib/* HBEpoch -p=/path/to/base_folder/sub_folder -f=rv -s
```

3. Run Goldilocks algorithm :

```
java -Xmx10000m -Xms10000m -classpath /path/to/rapid/bin:/path/to/rapid/lib/* Goldilocks -p=/path/to/base_folder/sub_folder -f=rv -s
```

4. Run Goldilocks algorithm :

```
java -Xmx10000m -Xms10000m -classpath /path/to/rapid/bin:/path/to/rapid/lib/* LockSet -p=/path/to/base_folder/sub_folder -f=rv -s
```