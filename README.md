# Bayesian Hidden Markov Model

## Usage

```
mvn clean install
java -jar target/BHMM-1.0-SNAPSHOT.jar <LOG_LEVEL>
```

## Arguments

```
[FINE | FINER | FINEST] detail of logging.
FINE = Basic information
FINER = Information about variables
FINEST = Information about variables in each sampling step
```

## Configuration

Via `configuration.properties`:

* corpus: Corpus file, see example corpus.txt
* lexicon: Lexicon file, see example lexicon.txt
* gold: Gold standard file, see example gold.txt
* out: Output file
* alpha: Hyperparameter for transitions
* beta: Hyperparameter for emissions
* iterations: Number of sampling iterations
* max: Maximum temperature
* min: Minimum temperature
* decrease: Iteration steps at which the temperature is decreased by the rate
* rate: Rate of decrease
* dbg: Iteration steps at which the FINEST information is written in the log file

## License

MIT
