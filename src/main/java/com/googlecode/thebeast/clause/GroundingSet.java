package com.googlecode.thebeast.clause;

/**
 * A GroundingSet represents a set of groundings for a generalized clause.
 *
 * @author Sebastian Riedel
 * @see com.googlecode.thebeast.clause.GeneralizedClause
 */
public interface GroundingSet extends Iterable<Grounding> {

  /**
   * Gets the clause groundings of this set can ground.
   *
   * @return a generalized clause groundings of this set are meant to ground.
   */
  GeneralizedClause getClause();

}
