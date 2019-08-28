Read-only repository, future improvements will be at [personal repo](https://github.com/davips/mysql2csv)!
===========

# mysql2csv
This code extracts results from a mysql database:

https://drive.google.com/file/d/1tWQ380hHnPPsFPTRZfPH0uvYK8QV1lDg/view

The dump.sql.gz file is known to work with MySQL Server version: 5.5.5-10.1.37-MariaDB-0+deb9u1 HDD, but not with MySQL Server version: 5.7.25-0ubuntu0.18.04.2 SSD due to row size issues.

The results of this script are here:

Complete results kappa

https://drive.google.com/open?id=1gd6AX8COwf2ipiFB8DWgk-NXAXbNUWmi

Complete results accbal

https://drive.google.com/open?id=1GMqK5iomr-hhP5_URHoNJvYgPuSp5hIF

As an example, it currently generates the following output in a file:
	
	dataset, method, classifier, fold, run, query number, kappa

	abalone-3class, SG, RF, 0, 0, 0, 0.328383
	abalone-3class, SG, RF, 0, 0, 1, 0.344992
	abalone-3class, SG, RF, 0, 0, 2, 0.316946
	abalone-3class, SG, RF, 0, 0, 3, 0.317996
	abalone-3class, SG, RF, 0, 0, 4, 0.328734
	abalone-3class, SG, RF, 0, 0, 5, 0.322177
	abalone-3class, SG, RF, 0, 0, 6, 0.327025
	abalone-3class, SG, RF, 0, 0, 7, 0.334405
	abalone-3class, SG, RF, 0, 0, 8, 0.336869
	abalone-3class, SG, RF, 0, 0, 9, 0.365523

# Installation
Clone this repository:

	git clone https://github.com/active-learning/mysql2csv
	
Install sbt: https://www.scala-sbt.org/download.html

Run:

	cd mysql2csv
	sbt run
	
# Database description

table r (results)

	m: measure id (which accuracy measure to use,  (see (1) bellow for details)
	p: pool id (each run, fold, learner and strategy combination from 5x5-fold CV are represented by a pool)
	v: the value of the measure


table p (pools)

	id: pool id
	s: strategy id (see ** bellow for details)
	l: learner id (see *** bellow for details)
	r: run number (0-4)
	f: [test] fold number (0-4)


table q (queries)

	p: pool id
	t: query number
	i: instance id


table i (instances in Weka database format)

	id: instance id
	V1: first attribute
	V2: second attribute
	...
	Vn: last attribute
	c: class attribute


table h ("hits and misses" of predictions, aka confusion matrices)

	p: pool id
	t: query number
	mat: compressed confusion matrix, see CM.scala file in active-learning-scala repository for how to read it


tables f, l, mea, run and t can be ignored.



(1) -> A measure after t queries is a diferent measure than after t+1 queries)

	Two types of measures are precalculated (to avoid messing around with confusion matrices):
		balanced accuracy, with id = 100000000 + 10000*t where t is the query number)
		kappa, with id = 200000000 + 10000*t where t is the query number)


** -> Strategies can use internally a different classifier than the one (learner) that will be used in actual prediction.

	Therefore, a pair strategy-classifier is seen as a unique strategy.
	To acommodate this into the database, strategy ids are dependent on the classifier.
	The id (clid) of the classifier is given by a conversion map from the learner id.
	That said, usually the internal classifier and the learner are the same.
	Classifier-less strategies have clid=0.
	Map of strategy ids:
		"Rnd" -> ((clid: Int) => 0),
		"Mar" -> ((clid: Int) => 3000000 + clid),
		"SG" -> ((clid: Int) => 14000000 + clid),
		"ERE" -> ((clid: Int) => 11000000 + clid),
		"OER" -> ((clid: Int) => 74000000 + clid),
		"HS" -> ((clid: Int) => 1),
		"TUman" -> ((clid: Int) => 127177 + clid),
		"TUmah" -> ((clid: Int) => 127179 + clid),
		"TUeuc" -> ((clid: Int) => 127176 + clid),
		"HTUman" -> ((clid: Int) => 94172007 + clid),
		"HTUmah" -> ((clid: Int) => 94172009 + clid),
		"HTUeuc" -> ((clid: Int) => 94172006 + clid),
		"ATUman" -> ((clid: Int) => 791),
		"ATUmah" -> ((clid: Int) => 991),
		"ATUeuc" -> ((clid: Int) => 691)
	Map of clids
		"5NN" -> 20,
		"C4.5" -> 50,
		"NB" -> 30,
		"SVM" -> 10,
		"RF" -> 0

*** -> Learner ids

	"5NN" -> 2, 
	"C4.5" -> 666003, 
	"NB" -> 12, 
	"SVM" -> 2651110, 
	"RF" -> 773

# Example queries
A typical sequence of SQL queries to get 200 (or less, depending on the dataset) queries is the following:

	pool <- "select id from p where s=$sid and l=$lid and r=$run and f=$fold"
	results <- "select v from r where m>=200000000 and m<200000000+200*10000 and p=$pool order by m"
