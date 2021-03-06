package thebeast.pml.corpora;

import thebeast.pml.*;
import thebeast.pml.formula.FactorFormula;
import thebeast.pml.formula.QueryGenerator;
import thebeast.util.HashMultiMapList;
import thebeast.nod.expression.RelationExpression;
import thebeast.nod.statement.Interpreter;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

/**
 * An augmented corpus augments each set of ground atoms with a set of globally defined
 * atoms and all auxilary predicate atoms (using their corresponding generator formulas).
 */
public class AugmentedCorpus extends AbstractCollection<GroundAtoms> implements Corpus {

  private Model model;
  private Corpus delegate;
  private GroundAtoms localAtoms;
  private HashMultiMapList<UserPredicate, RelationExpression>
          generators = new HashMultiMapList<UserPredicate, RelationExpression>();
  private Interpreter interpreter = TheBeast.getInstance().getNodServer().interpreter();

  public AugmentedCorpus(Model model, Corpus delegate) {
    this.model = model;
    this.delegate = delegate;
    localAtoms = model.getSignature().createGroundAtoms();
    QueryGenerator generator = new QueryGenerator(model);
    for (FactorFormula formula : model.getAuxiliaryGenerators()) {
      generators.add(formula.getGeneratorTarget(), generator.generateAuxiliaryQuery(formula, localAtoms, null));
    }
  }

  public Iterator<GroundAtoms> iterator() {

    return new Iterator<GroundAtoms>() {
      Iterator<GroundAtoms> iterator = delegate.iterator();

      public boolean hasNext() {
        return iterator.hasNext();
      }

      public GroundAtoms next() {
        GroundAtoms atoms = iterator.next();
//        if (model.getGlobalPredicates().size() > 0) {
//          atoms.load(model.getGlobalAtoms(), model.getGlobalPredicates());
//        }
        if (generators.isEmpty()) return atoms;
        localAtoms.load(atoms);
        for (Map.Entry<UserPredicate, List<RelationExpression>> entry : generators.entrySet()) {
          for (RelationExpression expr : entry.getValue()) {
            interpreter.assign(atoms.getGroundAtomsOf(entry.getKey()).getRelationVariable(), expr);
          }
        }
        return atoms;
      }

      public void remove() {
        iterator.remove();
      }
    };
  }

  public int size() {
    return delegate.size();
  }

  public Signature getSignature() {
    return model.getSignature();
  }

  public int getUsedMemory() {
    return delegate.getUsedMemory();
  }

  public void append(GroundAtoms atoms) {
    delegate.append(atoms);
  }
}
