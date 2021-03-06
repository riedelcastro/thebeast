package thebeast.nodmem.value;

import thebeast.nod.type.RelationType;
import thebeast.nod.value.RelationValue;
import thebeast.nod.value.TupleValue;
import thebeast.nod.value.Value;
import thebeast.nod.value.ValueVisitor;
import thebeast.nod.util.TabularValuePrinter;
import thebeast.nod.statement.Insert;
import thebeast.nodmem.mem.MemChunk;
import thebeast.nodmem.mem.MemVector;
import thebeast.nodmem.type.MemHeading;
import thebeast.nodmem.type.MemRelationType;
import thebeast.nodmem.type.MemTupleType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;

/**
 * @author Sebastian Riedel
 */
public class MemRelation extends AbstractMemValue<RelationType> implements RelationValue {

  private MemChunk chunk;
  private MemVector pointer;
  private MemHeading heading;
  private MemTupleType tupleType;

  private static LinkedList<WeakReference<RelationValue>> references = new LinkedList<WeakReference<RelationValue>>();
  private static ReferenceQueue<RelationValue> queue = new ReferenceQueue<RelationValue>();


  public MemRelation(MemChunk chunk, MemVector pointer, MemRelationType type) {
    super(type);
    this.chunk = chunk;
    this.pointer = pointer;
    heading = type.heading();
    tupleType = new MemTupleType(type.heading());
    references.add(new WeakReference<RelationValue>(this, queue));
  }


  public static List<WeakReference<RelationValue>> references() {
    return references;
  }


  public MemRelation(MemRelationType type, int capacity) {
    super(type);
    heading = type.heading();
    tupleType = new MemTupleType(type.heading());
    chunk = new MemChunk(0, capacity, heading.getDim());
    pointer = new MemVector();
    references.add(new WeakReference<RelationValue>(this, queue));        
  }

  public void acceptValueVisitor(ValueVisitor visitor) {
    visitor.visitRelation(this);
  }

  public Iterator<? extends Value> values() {
    return iterator();
  }

  public int size() {
    return chunk.getSize();
  }

  public Iterator<TupleValue> tuples() {
    return iterator();
  }

  public void writeTo(OutputStream os, boolean header) {
    TabularValuePrinter printer = new TabularValuePrinter(new PrintStream(os), header);
    this.acceptValueVisitor(printer);
  }

  public Iterator<TupleValue> iterator() {
    final int size = chunk.getSize();
    return new Iterator<TupleValue>() {
      private MemVector current = new MemVector(pointer);
      private int row = 0;

      public boolean hasNext() {
        return row < size;
      }

      public TupleValue next() {
        MemVector p = new MemVector(current);
        ++row;
        current.add(heading.getNumIntCols(), heading.getNumDoubleCols(), heading.getNumChunkCols());
        return tupleType.tupleFromChunk(chunk, p);
      }

      public void remove() {

      }
    };
  }


  public MemChunk getChunk() {
    return chunk;
  }

  public MemVector getPointer() {
    return pointer;
  }

  public void copyFrom(AbstractMemValue v) {

  }

  public MemChunk chunk() {
    return chunk;
  }

  public void ensure(int size) {
    if (chunk.getCapacity() < size) chunk.increaseCapacity(size - chunk.getCapacity());
  }


  public void clear() {
    chunk.setSize(0);
  }
}
