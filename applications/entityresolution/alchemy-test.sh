#!/bin/bash

experiment=cora-mws-1m-best

for i in 0 1 2 3 4 5 6 7 8 9; do \
#for i in 1 2 3 4 5 6 7 8 9; do \
#for i in 0; do \
weights=weights/multiple-$i.weights
#weights=weights/multiple-$i.nonneg.weights
#weights=bibserv.weights
mln=weights/hard-multiple-$i.mln
#mln=weights/hard-multiple-$i.nonneg.mln
#mln=corpora/cora/cora.mln
db=corpora/cora/folds/stripped.corafold-$i.db
gold=corpora/cora/folds/corafold-$i.db.atoms
out=/tmp/$experiment-$i.out
det=/tmp/$experiment-$i.det
processed=/tmp/$experiment-$i.processed

echo Fold $i
echo "Inference"

#~/opt/alchemy/bin/infer -seed 1 -i $mln -e $db -r $out \
# -q SameBib,SameTitle,SameAuthor,SameVenue -ms > acl/$experiment-$i.alchemy.output
#~/opt/alchemy/bin/infer -seed 1 -i $mln -e $db -r $out \
# -q SameBib,SameTitle,SameAuthor,SameVenue -simtp > results/$experiment-$i.alchemy.output
~/opt/alchemy/bin/infer -seed 1 -mwsMaxSteps 1000000 -heuristic 1 -breakHardClauses true -tries 1 -i $mln -e $db -r $out \
 -q SameBib,SameTitle,SameAuthor,SameVenue -m > acl/$experiment-$i.alchemy.output
cp $out $det
#echo "Converting to deterministic db..."
#java -Xmx500m -cp ../../classes/production thebeast.util.alchemy.AlchemyProb2Det \
#  < $out \
#  > $det
echo "Postprocessing..."
cp $det $processed
#java -Xmx500m -cp ../../classes/production thebeast.util.alchemy.AlchemyTransitivityEnforcer \
#  < $det \
#  > $processed
echo "converting to atoms..."
java -Xmx500m -cp ../../classes/production thebeast.util.alchemy.AlchemyConverter \
  $mln \
  $processed \
  $out.types.pml \
  $out.predicates.pml \
  $out.atoms
echo "evaluating atoms"
java -Xmx1000m -cp ../../classes/production/:../../lib/jline-0.9.9.jar:../../lib/java-cup-11a.jar:../../lib/lpsolve55j.jar:../../lib/ \
  thebeast.pml.CorpusEvaluation  \
  text model.pml $weights $gold $out.atoms > acl/$experiment-$i.results
done;

