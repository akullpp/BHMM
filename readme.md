Tested on: Windows 7 (x64) with JDK 1.7.0_15 (x64)
Custom style: line length 121

Usage: java -jar BHMM.jar [LOG LEVEL] or set up the *.java files accordingly

Commandline arguments:
[FINE | FINER | FINEST] detail of logging.
FINE = Basic information
FINER = Information about variables
FINEST = Information about variables in each sampling step

Configuration via config.properties:
corpus = Corpus file, see example corpus.txt
lexicon = Lexicon file, see example lexicon.txt
gold = Gold standard file, see example gold.txt
out = Output file
alpha = Hyperparameter for transitions
beta = Hyperparameter for emissions
iterations = Number of sampling iterations
max = Maximum temperature
min = Minimum temperature
decrease = Iteration steps at which the temperature is decreased by the rate
rate = Rate of decrease
dbg = Iteration steps at which the FINEST information is written in the log file

Folder content:
./src/
    META-INF/
        MANIFEST.MF - Manifest for the JAR
    hd/nld/bayes/
        BHMM.java - Sampling, evaluation, annealing
        HMM.java - Datastructures
        ID.java - String mapping to unique IDs
        IO.java - Input/Output
        Main.java - Entry point, logging, properties
BHMM.jar - JAR file
BHMM.uml - UML class diagram
config.properties - Configuration
corpus.txt - toy corpus
diagram.png - Picture of UML class diagram
gold.txt - toy gold standard
lexicon.txt - toy lexicon
log.txt - example log
out.txt - example output
readme.txt - this file
